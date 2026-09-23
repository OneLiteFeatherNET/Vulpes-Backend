# Proposal

## Why

Every endpoint this backend serves is open. All 14 controllers answer any caller that can reach the
port -- create, update and delete included -- and nothing in the request carries the identity of who
made it. A `ProjectEntity` has no owner, and `AbstractCrudService` checks only that a resource
belongs to the *addressed* project, never that the caller may address it.

The deployed Stelaris UI does not sign in at all: its release carries no `auth` configuration and
sends no bearer token. Its OIDC support exists on a feature branch, and a local instance running
that branch with hand-maintained configuration does send a token -- which the backend ignores
entirely. Closing that gap is the first of three steps: the two that follow -- stamping
`createdBy`/`modifiedBy` onto the model, then keeping a revision history -- both consume something
that does not exist yet, a stable identity for the caller. This change produces exactly that, and
stops there.

## What Changes

- Add `micronaut-security-jwt`. The backend becomes a **resource server**: it validates bearer
  tokens the UI obtained itself and never talks to the identity provider except to fetch signing
  keys. No login endpoint, no client secret, no redirect handling, no session.
- Validate tokens against a **JWKS endpoint**, with issuer, audience and the identity claim all
  taken from configuration. Entra ID is what runs today; Keycloak has to remain a configuration
  change rather than a code change, so nothing provider-specific may reach the code.
- Validate `iss` and `aud` in addition to signature and expiry. Micronaut checks the latter two on
  its own, but issuer and audience **only when configured** -- and for this deployment the audience
  check is the entire point (see Preconditions).
- Expose the caller as a principal carrying **both the subject and the issuer it came from**. The
  identity claim is configurable, because the standard `sub` is not the right choice everywhere:
  Entra ID issues a pairwise `sub` that is unique per app registration, so a new app registration
  silently renumbers every user, while `oid` is stable across apps within the tenant. Keycloak has
  no `oid` and uses `sub`. Steps two and three write this value into the database; recording which
  issuer it came from keeps a later provider migration readable instead of merging two unrelated
  number spaces.
- Secure all 14 controllers by requiring an authenticated caller. No roles, no scopes: a valid token
  grants the same access an anonymous caller has today.
- Keep `/health`, `/prometheus`, `/swagger` and `/swagger-ui` reachable without a token. The first
  two are non-negotiable: the Kubernetes probes and the `ServiceMonitor` in `charts/vulpes-backend`
  call them, and a pod whose probes receive 401 never becomes ready.
- Keep failures in the project's single error shape. Micronaut Security answers rejections through
  its own `AuthorizationExceptionHandler`, which bypasses the `ErrorResponseProcessor` that gives
  every other failure its RFC 9457 body. Two new `ErrorCode` entries and a replacement handler keep
  401 and 403 branchable on `code` like everything else.
- Declare `bearerAuth` as an OpenAPI `SecurityScheme`. Without it, no operation in the generated
  Dart client declares a security requirement, so the client's bearer interceptor never fires even
  when a token has been set. It also gives Swagger UI an authorize control.
- **Keep the API locally testable both with and without a token.** Two things have to be true at
  the same time on a developer's machine:
  - *Without a token*, the existing workflow keeps working -- `docker compose -f docker/compose.yml
    up -d` plus `./gradlew run`, Swagger UI on `localhost:8085`, and UI development against it, all
    with no identity provider in the loop.
  - *With a token*, validation behaves exactly as it does in a deployed instance, so that a wrong
    audience, an expired token or a broken signature fails locally instead of in staging.

  This rules out disabling security in the `local` environment: with the filter switched off there
  is no validation left to exercise. What it leaves is a development-only fallback principal, which
  also keeps the code path intact for step two. The fallback must trigger on **the absence of an
  `Authorization` header**, never on a failed validation -- otherwise a rejected token quietly
  succeeds locally and the whole second half of the requirement is theatre.
- **The fallback is gated on Micronaut environments, not on a property.** It requires the `local`
  environment, which this project already uses (`application-local.yml`, `MICRONAUT_ENVIRONMENTS=local`)
  and which is only ever set explicitly. A self-deducible name such as `dev` is unsuitable as an
  anchor for exactly that reason. On its own this is not a hard barrier: the Helm chart derives
  `MICRONAUT_ENVIRONMENTS` from `.Values.profiles`, so a deployment could name `local` by mistake.
  The fallback therefore additionally requires that no `kubernetes` or `cloud` environment is active
  -- Micronaut activates those in-cluster, and this project's Kubernetes discovery client already
  relies on that. A stray `local` in an overlay is then inert.
  Whether AOT's `deduceEnvironment`/`cacheEnvironment` settings (both enabled in `build.gradle.kts`)
  affect in-cluster environment detection in the optimized jar has not been verified, and the
  hardening above depends on it.
- Let the existing tests mint their own tokens rather than disabling security for the test
  environment, so the eight rest-assured controller tests keep exercising the real filter chain.
- **Ship a local setup that covers all three modes.** A `docker/.env.example` and a Compose setup that
  runs with or without an identity provider, so that a developer can reach each of:
  1. *no token* -- the fallback principal, for fast iteration;
  2. *Keycloak locally* -- fully offline, and the standing proof that the provider really is
     interchangeable rather than Entra wired in through the back door;
  3. *a real Entra token* -- pasted from the UI, the only mode that exercises the actual deployed
     configuration.
  Mode 2 validates the mechanism, not the configuration: Keycloak has no `oid`, its issuer is a
  local realm, and its audience is whatever we choose. The failures that will actually happen in
  production -- wrong audience, wrong issuer variant, missing claim -- are invisible in that mode.
  The documentation has to say so, or local green will be mistaken for deployment-ready.
- **BREAKING** for API consumers: requests without an acceptable bearer token are rejected. The
  deployed UI sends no token at all, so this cannot ship before Stelaris signs in in production --
  see Preconditions. Switching it on is a hard cut: either the UI is sending acceptable tokens by
  then, or it stops working entirely.

### Non-goals

Deliberately out of scope, to keep this change to one repository and one concern:

- Roles, scopes, or per-project permissions. Who may touch *which* project is a separate question,
  and answering it needs a token claim designed on the identity provider side first.
- `createdBy` / `modifiedBy` on the model. That is step two and requires a `vulpes-model` release.
- Revision history. That is step three.
- User management, login flows, or a token endpoint on this backend. Signing tokens locally for
  tests and for development against a running instance is in scope; issuing them to callers at
  runtime is not.
- Accepting more than one issuer at a time. A provider migration would need an overlap window where
  tokens from both are valid; that is a change of its own, not a requirement here.

## Preconditions

**The tokens the UI sends today cannot be accepted, and no amount of backend code changes that.**

A captured request carries an access token whose `aud` is `00000003-0000-0000-c000-000000000000` --
Microsoft Graph -- with `scp: openid profile email`. It is a Graph token that happens to be sent to
Vulpes; the backend does not appear in it at all. Accepting it would mean accepting the audience
that *every* application receives when a user signs in with Entra ID, so the effective access rule
would become "has signed in with a Microsoft account somewhere". The `iss` check narrows that to the
tenant, but within the tenant any app a user consents to would become a master key for Vulpes.
Microsoft also states that Graph tokens are not intended for validation by anyone but Graph and may
change format without notice.

What has to exist before this change can be switched on, none of it code in this repository:

1. An app registration for the backend, with an Application ID URI (`api://…`) and an exposed scope
   (conventionally `access_as_user`).
2. Stelaris requesting that scope instead of the plain sign-in scopes, so the token it receives
   carries the backend as its `aud`.
3. A decision on the token version. A fresh app registration can set `accessTokenAcceptedVersion: 2`,
   which moves `iss` from `https://sts.windows.net/<tenant>/` to
   `https://login.microsoftonline.com/<tenant>/v2.0`. The configured issuer has to match exactly, or
   everything answers 401.

This is identity-provider configuration, it is part of the change, and it has to be written down --
otherwise it will be rediscovered the next time a tenant or a realm is set up.

## Capabilities

### New Capabilities

- `api-authentication`: how the backend establishes the identity of an HTTP caller -- which requests
  require a token, what makes a token acceptable, how the caller's identity is derived from its
  claims, which endpoints stay open, how a rejection is reported, and how the API is exercised
  without a token during development.

### Modified Capabilities

None. The project has no specs yet (`openspec list --specs` is empty), so nothing existing changes.

## Impact

**Code**
- `build.gradle.kts` -- one new dependency
- `src/main/resources/application.yml` -- security configuration; `application-local.yml` -- the
  development bypass
- `domain/error/ErrorCode.java` -- two entries for 401 and 403
- `exception/` -- a handler that routes rejections through `ProblemDetail`
- `VulpesBackend.java` or a dedicated class -- the OpenAPI `SecurityScheme` declaration
- `src/test/` -- token helper; the eight rest-assured controller tests

**Outside the code**
- Entra ID: a new app registration for the backend, an exposed scope, and a scope change in Stelaris
  (see Preconditions)
- `docker/` and a new `docker/.env.example` -- note that `.gitignore` ignores `docker/` wholesale and only
  `docker/compose.yml` is tracked, so any new file there is invisible to git until the ignore rule
  is amended; and that `.env` reaches Compose but never `./gradlew run`, which runs outside it
- `charts/vulpes-backend` -- issuer, audience, JWKS source and identity claim become deployment
  configuration
- The generated Dart client (`vulpes-backend-client-dart`) gains a security scheme and can be
  regenerated; the UI keeps working as it does today once its token carries the right audience
- CORS already permits the UI's origin (`localhost` with any port, which covers where Stelaris runs
  locally). Whether the CORS filter answers preflight requests ahead of the security filter needs
  confirming, or `OPTIONS` calls will be rejected before CORS ever sees them.

## Open decisions

To be settled in `design.md`:

1. **Discovery or a fixed JWKS URI.** Deriving the endpoint from the issuer's
   `/.well-known/openid-configuration` is one line of configuration and survives key rotation, but
   makes the identity provider a startup dependency for every pod. A configured `jwks-uri` starts
   even when the provider is briefly unreachable.
2. **What identity the development fallback presents.** That it is a fallback principal rather
   than a kill switch, and that it is gated on environments, is settled above. Open: whether it
   presents a fixed synthetic subject or one a developer can override, and how it announces itself
   at startup so a running instance never hides the fact that it is accepting unauthenticated
   calls. Also open: where locally signed tokens come from, so the "with a token" half works before
   the Entra app registration exists -- Micronaut can hold a local secret signature and the remote
   JWKS at the same time, and the tests need the same helper. The specs must pin down that the
   fallback stays out of the `test` environment, or the 401 tests prove nothing.
3. **How the local setup is shaped.** Whether Keycloak is a Compose profile inside the existing
   `docker/compose.yml` (one file, but "profile" then means three different things across Compose,
   Micronaut and the Helm chart) or a second file; and whether the issuer, audience and claim
   settings live in `application-local.yml` or in a `.env` that the documented start command
   sources, since Gradle will not read it on its own. One of the two, not both.
4. **Which claim identifies the caller for this deployment.** `oid` is the stable answer on Entra
   ID, `sub` the portable one. The mechanism is configurable either way; the default and the
   deployed value are the decision.
