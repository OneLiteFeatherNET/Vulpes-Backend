# Design

## Context

See `proposal.md` for motivation and `specs/api-authentication/spec.md` for the behaviour this has
to produce. What shapes the approach:

- The project has no security layer at all today: no `micronaut-security` dependency, no `@Secured`
  anywhere, no notion of a principal. Nothing has to be unpicked first.
- Every write path funnels through `AbstractCrudService.create/update/delete`. Steps two and three
  will read the caller identity there, so this change has to leave it reachable from that layer.
- All failures are already rendered as RFC 9457 by `ProblemErrorResponseProcessor`, including
  framework errors. Authentication failures must not become the exception.
- `charts/vulpes-backend` derives `MICRONAUT_ENVIRONMENTS` from `.Values.profiles`, maintained in
  the cluster repository, and the AOT build has both `deduceEnvironment` and `cacheEnvironment`
  enabled.
- The identity provider in use is Entra ID. Its access tokens carry a pairwise `sub` that is unique
  per app registration -- and this change creates a new app registration.

Verified while designing, so it need not be rediscovered:

- `CorsFilter` runs at `ServerFilterPhase.METRICS.after()` (9250), the security filter at
  `ServerFilterPhase.SECURITY` (39000). CORS preflight is answered before security is consulted;
  the spec's preflight requirement needs no extra work.
- Micronaut Security offers `micronaut.security.endpoints.open` for built-in endpoints, so health
  and metrics do not need annotation-level exceptions.

## Goals / Non-Goals

**Goals**

- One filter chain for every environment, so that what runs locally is what runs deployed, with the
  single documented exception of the missing-token fallback.
- Every provider-specific value in configuration, so that swapping Entra ID for Keycloak touches
  only YAML and Helm values.
- A caller identity that step two can consume without redesign.

**Non-Goals**

- No abstraction layer over Micronaut Security. Provider independence comes from configuration, not
  from an in-house indirection.
- No authorization model. No roles, no scopes, no per-project checks, and no groundwork for them
  beyond making the identity available.
- No runtime switch that turns authentication off in a deployed instance.

## Decisions

### D1: Signing keys from a configured JWKS URI, not from OpenID discovery

The JWKS endpoint is named directly in configuration rather than derived from the issuer's
`/.well-known/openid-configuration`.

*Why:* discovery makes the identity provider a startup dependency for every pod. A rollout during a
provider outage would leave pods unable to become ready — a failure mode with no upside, since the
JWKS URL is effectively static per tenant. Key *rotation* still works either way: the key set is
fetched from that URL at runtime and re-fetched when an unknown key id appears, which the spec
requires.

*Alternative considered:* issuer-based discovery. Rejected for the startup coupling. If a provider
ever moves its JWKS path, that is a configuration change with a deploy, which is acceptable for an
event that happens approximately never.

### D2: Caller identity is `{issuer, subject}`, with the subject claim configurable

A small value type carries both. The claim supplying the subject is configuration, defaulting to
`sub`, set to `oid` for the Entra deployment.

*Why `oid` here:* Entra's `sub` is a pairwise identifier, unique per (user, app registration). This
change introduces a new app registration for the backend, so every `sub` value would differ from
anything observed today — and would change again if the registration were ever replaced. `oid` is
the user's object id in the tenant and is stable across registrations. Keycloak has no `oid`, hence
the claim name being configuration rather than a constant.

*Why the issuer travels with it:* steps two and three persist this value. If the provider is ever
replaced, rows written under the old issuer and rows written under the new one would otherwise sit
in the same column meaning different things, with nothing to tell them apart after the fact.

*Alternative considered:* storing only the subject and treating the issuer as implicit deployment
knowledge. Rejected: implicit knowledge does not survive in a database that outlives the decision.

### D3: The missing-token fallback is a low-priority authentication provider, gated twice

Registered so that the JWT validation path always takes precedence. It contributes an identity
**only when the request carries no `Authorization` header at all** — not when validation failed.

Two independent gates, both of which must hold:

1. the `local` environment is active, and
2. the process is not running in a cluster.

For (2) the check is the presence of `KUBERNETES_SERVICE_HOST` in the environment, not Micronaut's
deduced `kubernetes`/`cloud` environments. Kubernetes injects that variable into every pod
unconditionally, whereas AOT's `deduceEnvironment` with `cacheEnvironment` raises the question of
what was baked in at build time versus detected at runtime. The direct check sidesteps that
question entirely; it cannot be cached into the image because it reads the live environment.

On startup with the fallback active, the application logs a warning naming the synthetic identity.

*Why not `micronaut.security.enabled: false` for local:* the spec requires that an unacceptable
token is rejected locally exactly as it would be deployed. With the filter disabled there is
nothing to reject with, and the "with a token" mode would verify nothing.

*Why gate on the absence of the header rather than on a failed authentication:* a fallback that
catches failed validation would make every rejected token appear to work locally. That is the
precise failure this design exists to prevent.

*Why an environment rather than a property:* `local` in a production values file is conspicuous;
`dev-bypass: true` is not. The second gate then makes even the conspicuous mistake inert.

### D4: Deny by default, with an explicit open list

Security is enabled globally with no blanket rule that admits unannotated endpoints; controllers
carry `@Secured(SecurityRule.IS_AUTHENTICATED)`. Health, metrics and the Swagger paths are opened
explicitly through `micronaut.security.endpoints.open` and the static-resource configuration.

*Why:* a controller added later is secured because nobody did anything, rather than insecure
because somebody forgot an annotation. The open list is short, reviewable, and its entries are
exactly the ones an operator would expect to see.

### D5: Rejections are routed into `ProblemDetail`

`ErrorCode` gains `UNAUTHENTICATED` (401) and `ACCESS_DENIED` (403), and `ErrorCode.fromStatus`
maps 401 and 403 onto them.

**Corrected during implementation:** this section previously claimed that Micronaut Security
bypasses the `ErrorResponseProcessor` and that a replacement `AuthorizationExceptionHandler` would
be needed. That is wrong. `DefaultAuthorizationExceptionHandler` takes an `ErrorResponseProcessor`
as a constructor dependency and renders its body through it, so the project's own processor already
serves rejections — verified against a running instance, which answers an unauthenticated request
with `application/problem+json` and `"code":"UNAUTHENTICATED"`. No replacement handler is written.

The `detail` text is a fixed constant per code. It never names which criterion failed and never
echoes any part of the presented token: a differentiated message would tell an attacker whether a
token was merely expired, aimed at the wrong audience, or signed by the wrong issuer. This mirrors
the reasoning already documented in `AbstractCrudService` for cross-project lookups.

### D6: Tests and local development sign their own tokens

A second signature source — a symmetric secret — is configured alongside the remote JWKS in the
`test` and `local` environments only. A test helper mints tokens against it.

*Why:* it lets the suite assert the real matrix (valid, expired, wrong audience, wrong issuer,
missing claim) against the real filter chain, with no network and no provider. Micronaut accepts
multiple signature configurations concurrently, so this needs no conditional wiring in the code.

The fallback from D3 is inactive in `test`. Otherwise every test that asserts a 401 would observe a
200 and pass for the wrong reason.

### D7: One Compose file with a profile; backend settings in `application-local.yml`

Keycloak joins `docker/compose.yml` behind a Compose profile: `docker compose up` starts Postgres
alone, `docker compose --profile keycloak up` adds an identity provider with an imported realm.

Issuer, audience, JWKS URI and identity claim for local runs live in `application-local.yml`, which
is tracked and already carries local credentials in the clear. `docker/.env.example` documents only what
the containers consume -- next to `compose.yml`, because that is where Compose looks for `.env`.

*Why not `.env` for the backend values:* the backend runs via `./gradlew run`, outside Compose, and
Gradle does not read `.env`. Putting backend settings there would require every developer to
remember `set -a; source .env` before starting, and the day someone forgets, the application starts
with different settings than the containers it talks to. One source of truth per consumer.

*Note on `.gitignore`:* `docker/` is ignored wholesale and only `docker/compose.yml` is tracked. A
realm import file placed beside it would be invisible to git. The ignore rule needs a negation for
whatever is added, and the spec has a scenario for exactly this.

*Trade-off accepted:* "profile" now means three things in this repository — a Compose profile, a
Micronaut environment, and `.Values.profiles` in the chart (which means the second). The
documentation has to be explicit about which is meant where.

### D8: `bearerAuth` declared once at application level

A single `@SecurityScheme` declaration on the application class, applied globally, rather than
per-controller annotations.

## Risks / Trade-offs

**Switching this on is a hard cut for the UI** → The deployed UI does not sign in at all, and a
local instance that does requests no resource scope, so it receives a Microsoft Graph token.
Either way, nothing reaching the backend today would pass. Mitigated by ordering: the Entra work,
the Stelaris release with sign-in, and its production configuration all land first, and all are
inert while the backend still ignores tokens. Only then does the backend deploy. See the migration
plan.

**A wrong issuer value rejects everything, and the value depends on a setting made in Entra** →
`accessTokenAcceptedVersion` decides between the `sts.windows.net/<tenant>/` and
`login.microsoftonline.com/<tenant>/v2.0` forms. Mitigated by decoding an actual token from the new
app registration and reading `iss` from it before configuring anything, rather than reasoning about
which form applies.

**The local Keycloak mode can be mistaken for coverage of the deployed configuration** → It
exercises the mechanism, not the configuration: different issuer, different audience, no `oid`.
Mitigated by saying so in the documentation, and by keeping the third mode (a real token pasted
from the UI) as the one that validates the deployment.

**A synthetic identity could reach the database in step two** → A developer running locally will
write rows stamped with the fallback identity. Mitigated by making that identity obviously
synthetic rather than a plausible-looking id, so such rows are recognisable on sight.

**Probes and scraping break if the open list is wrong** → A pod whose probes get 401 never becomes
ready, and the failure appears as a rollout hang rather than as an auth error. Mitigated by a test
asserting unauthenticated 200 on `/health` and `/prometheus`, so a regression fails in CI instead
of during a deploy.

**Removing the datasource from `application.yml` could strand a deployment** → The connection
details were taken out so Test Resources can supply a throwaway database to the suite. Verified
against the cluster repository rather than assumed: the overlay at
`apps/clusters/feathre-core/apps/vulpes-backend/release.yaml` supplies the full datasource under
its `prod` profile -- url, username, password, `schema-generate: NONE` and the pool settings -- so
a deployed instance never relied on the values that were removed. Local runs get theirs from
`application-local.yml`.

**Rollback cannot be a configuration flip** → By design there is no runtime switch to disable
authentication, so recovery is redeploying the previous image. Accepted: a switch that could undo
this in production is the same switch an attacker would look for.

## Migration Plan

The order matters. Steps 1 to 4 are invisible to the running system; step 5 is where
authentication becomes real, and it must not come before step 3 is live.

1. **Entra ID:** register the backend, set an Application ID URI, expose a scope, decide
   `accessTokenAcceptedVersion`. Grant the Stelaris registration access to that scope. No effect on
   anything running.
2. **Read the real values:** add the backend scope to Stelaris' scope list and sign in -- a local
   instance of the OIDC branch is enough. Decode the token and read `iss` and `aud` off it; these
   are the values step 4 is configured with. Stelaris needs no code change for this: its scopes
   are configuration (`config.auth.scopes`), and it calls no Graph API, so one token serves.
3. **Stelaris in production:** release the OIDC feature, and add the `auth` block to its Flux
   release with the scopes `openid profile offline_access api://<backend-app-id>/access_as_user`.
   The UI now signs in and sends a Vulpes token; the backend still ignores it.
4. **Backend overlays -- both of them:** issuer, audience, JWKS URI and identity claim as chart
   values in `vulpes-backend` *and* `vulpes-backend-dev`. Harmless ahead of time, since the chart
   version in use ignores the block.
5. **Backend release:** the moment authentication becomes real. Charts are published on a release
   only, so merging the feature into `main` ships nothing; merging the release-please release PR
   does. The two environments then behave differently:
   - `vulpes-backend-dev` follows `>=2.6.0` on a five-minute interval and rolls the new chart out
     with no PR and no approval. Without step 4 in its overlay the pod refuses to start.
   - `vulpes-backend` (prod) is pinned to an exact version and moves only when that pin is changed
     deliberately -- which must wait for step 3.
   Because release-please bundles everything on `main` into the next release, merging the feature
   before step 4 makes *any* later release, including an unrelated fix, carry this change into dev.
6. **Rollback:** redeploy the previous image. The UI keeps sending tokens and they are ignored
   again, so it keeps working throughout.

## Open Questions

- What the synthetic development identity should be — a fixed value, or one a developer can
  override to imitate a specific user. This changes no spec scenario and can be settled while
  implementing.
- Whether the Keycloak realm import is committed or generated by a documented setup step. Affects
  only how `docker/` and `.gitignore` are arranged.
