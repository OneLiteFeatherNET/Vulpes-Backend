# Tasks

Order matters across groups: 1 and 2 are invisible to the running system, 10 is the moment
authentication becomes real. See `design.md` — Migration Plan.

## 1. Identity provider (no code, blocks everything else)

- [x] 1.1 Register the backend in Entra ID with an Application ID URI and an exposed scope
      (`access_as_user`); verify the scope is listed under the registration's exposed API.
      Created as "Vulpes Backend" (single-tenant), with `api://<backend-app-id>` as the
      identifier URI, the `access_as_user` delegated scope, and a service principal so the
      tenant will issue tokens for it.
- [x] 1.2 Decide and record `accessTokenAcceptedVersion` on that registration; verify by noting
      which issuer form it implies. Set to **2**, so the issuer is the
      `login.microsoftonline.com/<tenant>/v2.0` form. Note for 1.5: a v2 token's `aud` is the
      resource's **application id**, not its `api://` URI -- configuring the URI would refuse
      every request.
- [x] 1.3 Grant the Stelaris registration access to the backend's scope; verify the permission
      appears on the Stelaris registration. Done from both sides: the backend pre-authorizes
      Stelaris (so no consent prompt appears), and Stelaris' `requiredResourceAccess` now lists
      the backend's scope alongside its existing Microsoft Graph permissions, which were left
      untouched.
- [ ] 1.4 Change Stelaris to request the backend scope instead of the plain sign-in scopes; verify
      by decoding a token the UI obtains and confirming `aud` names the backend, not
      `00000003-0000-0000-c000-000000000000`. This is configuration, not code: add
      `api://<backend-app-id>/access_as_user` to `config.auth.scopes`. Locally that is the
      unversioned `config.json` the `stelaris-local` container mounts. In production it needs the
      OIDC feature released and an `auth` block in Stelaris' Flux release -- the deployed UI
      currently does not sign in at all.
- [ ] 1.5 Record the observed `iss`, `aud` and identity claim from that decoded token as the values
      groups 2 and 10 will configure; verify they are read off a real token, not derived.
      **Blocked on 1.4** -- it needs a token that Stelaris actually requested for this scope.
      (Attempted via the Azure CLI as a stand-in; the tenant refused it, and the temporary
      pre-authorization added for the attempt was removed again.)

## 2. Dependency and configuration surface

- [x] 2.1 Add `micronaut-security-jwt` to the version catalog in `settings.gradle.kts` and to
      `build.gradle.kts`; verify `./gradlew build` succeeds
- [x] 2.2 Add issuer, audience, JWKS URI and identity-claim properties to `application.yml`, all
      as environment-variable placeholders with no defaults that would silently accept anything;
      verify the application fails to start when a required value is absent

## 3. Error shape (before anything can reject a request)

- [x] 3.1 Add `UNAUTHENTICATED` (401) and `ACCESS_DENIED` (403) to `ErrorCode`; verify
      `ErrorCodeTest` covers their status and title mapping
- [x] 3.2 Replace the `AuthorizationExceptionHandler` so rejections render through `ProblemDetail`;
      verify a test asserts `application/problem+json`, a `code`, a `status` of 401 and a `traceId`
- [x] 3.3 Use a fixed `detail` constant per code that names no failed criterion and echoes no part
      of the token; verify a test asserts identical bodies for an expired token and one with a
      wrong audience

## 4. Caller identity

- [x] 4.1 Add a value type carrying issuer and subject; verify unit coverage of its construction
- [x] 4.2 Derive it from the configured claim on every accepted token; verify tests cover the claim
      being read from `sub` and from `oid` under different configuration
- [x] 4.3 Reject with 401 when the configured claim is absent from an otherwise valid token; verify
      a test asserts no substituted or empty identity reaches request processing
- [x] 4.4 Make the identity reachable from the service layer where `AbstractCrudService` performs
      writes, without wiring it into any entity yet; verify a test reads it from that layer

## 5. Securing the endpoints

- [x] 5.1 Enable security and annotate all 14 controllers with `@Secured(IS_AUTHENTICATED)`; verify
      a test asserts 401 on a representative endpoint of each controller without a token
- [x] 5.2 Open `/health` and `/prometheus` via `micronaut.security.endpoints.open`; verify tests
      assert 200 without a token on both
- [x] 5.3 Keep the Swagger specification and Swagger UI paths reachable; verify a test requests one
      of each without a token
- [x] 5.4 Confirm CORS preflight is unaffected; verify a test issues an `OPTIONS` request from a
      permitted origin without a token and asserts a non-401 CORS response

## 6. Development fallback

- [x] 6.1 Add an authentication provider that contributes a fixed, obviously synthetic identity
      (subject and issuer both naming local development, never a plausible user id); verify a test
      asserts the value is recognisable as synthetic
- [x] 6.2 Condition it on the absence of an `Authorization` header only; verify a test asserts 401
      for an expired and for a wrong-audience token while the fallback is active
- [x] 6.3 Gate it on the `local` environment AND on `KUBERNETES_SERVICE_HOST` being absent; verify
      a test asserts it stays inactive when that variable is set even with `local` active
- [x] 6.4 Log a warning at startup naming the synthetic identity when the fallback is active;
      verify the message appears when starting with `MICRONAUT_ENVIRONMENTS=local`
- [x] 6.5 Ensure the fallback is inactive in the `test` environment; verify the 401 assertions from
      5.1 still fail the build if the fallback were to leak in

## 7. Test infrastructure

- [x] 7.1 Configure a symmetric signature source alongside the remote JWKS for the `test` and
      `local` environments only; verify a locally minted token is accepted while JWKS stays
      configured
- [x] 7.2 Add a test helper that mints tokens with controllable issuer, audience, claims and
      expiry; verify it produces a token accepted by the running filter chain
- [x] 7.3 Authenticate the existing HTTP tests via that helper; verify the full suite passes
      without disabling security. (There were none to update: every controller test instantiates
      its controller directly, and the one rest-assured test is `@Disabled`. New integration tests
      cover the HTTP paths instead.)
- [x] 7.4 Add the rejection matrix as tests — valid, expired, wrong audience, wrong issuer, broken
      signature, missing identity claim; verify each asserts 401 and the problem body from 3.2

## 8. Local setup

- [x] 8.1 Add Keycloak to `docker/compose.yml` behind a Compose profile with a committed realm
      import; verify `docker compose -f docker/compose.yml up -d` starts Postgres alone and
      `--profile keycloak` additionally starts Keycloak
- [x] 8.2 Add the negation to `.gitignore` for every new file under `docker/`; verify
      `git check-ignore -v` reports no match and `git status` shows them as untracked
- [x] 8.3 Add `docker/.env.example` documenting each setting the containers consume, with a comment
      per entry; verify a copy to `docker/.env` plus `docker compose --profile keycloak up` starts
      cleanly. (Placed under `docker/`, not the repository root: Compose resolves `.env` relative
      to the compose file, so a root `.env` was silently ignored -- verified with an overridden
      port that took effect only from `docker/.env`.)
- [x] 8.4 Put issuer, audience, JWKS URI and identity claim for the Keycloak realm into
      `application-local.yml`; verify a token from the local realm is accepted by
      `MICRONAUT_ENVIRONMENTS=local ./gradlew run`
- [x] 8.5 Document the three local modes in the README, stating explicitly that the Keycloak mode
      exercises the token mechanism and not the deployed configuration — different issuer,
      different audience, no `oid`; verify a reader can reach each mode from the README alone

## 9. API description and client

- [x] 9.1 Declare `bearerAuth` as a `@SecurityScheme` at application level; verify the generated
      OpenAPI document contains the scheme and that Swagger UI shows an authorize control
- [x] 9.2 Regenerate the Dart client from the updated specification; verify it exposes a place to
      supply the bearer token. Verified by generating from the updated spec with
      openapi-generator 7.17.0 (the version `generate.sh` pins) into a throwaway directory:
      `lib/src/auth/bearer_auth.dart` and `setBearerAuth(name, token)` appear, and every operation
      now declares `{'type': 'http', 'scheme': 'bearer', 'name': 'bearerAuth'}` -- where the
      client currently published carries no `bearerAuth` in any API method at all, so its
      interceptor would never have fired. Publishing the regenerated client is the release
      process's job in `vulpes-backend-client-dart`, not this repository's.

## 10. Deployment

- [x] 10.1 Add issuer, audience, JWKS URI and identity claim as chart values in
      `charts/vulpes-backend`, with no default that would accept an unintended issuer; verify a
      `helm template` run renders them into the deployment
- [x] 10.2 Confirm `.Values.profiles` in the cluster overlays does not name `local`; verify by
      reading the rendered `MICRONAUT_ENVIRONMENTS` value. (Checked in Kubernetes-FLUX,
      `apps/clusters/feathre-core/apps/vulpes-backend/release.yaml`: `profiles: [ "prod", "k8s" ]`,
      no occurrence of `local` anywhere in the file.)
- [ ] 10.3 Deploy and verify: the UI works unchanged, probes report ready, metrics are still
      scraped, and a request without a token returns 401 in the problem format
