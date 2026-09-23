# Spec Delta

## Purpose

Establishes who is calling the HTTP API. It defines which requests need a bearer token, what makes
a token acceptable, how the caller's identity is derived from its claims, which endpoints stay
reachable without one, how a rejection is reported, and how the API is exercised locally with and
without a token.

## ADDED Requirements

### Requirement: Resource endpoints require an authenticated caller

Every endpoint that reads or modifies project data SHALL require a valid bearer token. A request
without one SHALL be rejected, and no resource data SHALL appear in the rejection.

Authentication establishes *who* is calling, not *what they may do*: any accepted token grants the
same access. Roles, scopes and per-project permissions are out of scope for this capability.

#### Scenario: Request without a token

- **WHEN** a request is made to a project data endpoint with no `Authorization` header
- **THEN** the response status SHALL be 401
- **AND** the body SHALL NOT contain any project data

#### Scenario: Request with an acceptable token

- **WHEN** a request is made to a project data endpoint with a bearer token that meets every
  acceptance criterion
- **THEN** the request SHALL be processed exactly as it was before this capability existed

#### Scenario: Request with an expired token

- **WHEN** a request carries a bearer token whose expiry lies in the past
- **THEN** the response status SHALL be 401

#### Scenario: Authorization header that is not a bearer token

- **WHEN** a request carries an `Authorization` header that is not a bearer token
- **THEN** the response status SHALL be 401

### Requirement: Token acceptance criteria

A bearer token SHALL be accepted only when all of the following hold: its signature verifies
against the configured issuer's published signing keys, it is neither expired nor used before its
validity begins, its issuer claim matches the configured issuer exactly, and its audience claim
names this backend.

The audience check is not optional hardening. Without it, every token the configured issuer has
minted for any application would open this API, which would reduce the access rule to "has signed
in with that identity provider somewhere".

#### Scenario: Token issued for a different application

- **WHEN** a request carries a token that is correctly signed by the configured issuer, is
  unexpired, but whose audience names a different application
- **THEN** the response status SHALL be 401

#### Scenario: Token from an unconfigured issuer

- **WHEN** a request carries a token whose issuer claim does not match the configured issuer
- **THEN** the response status SHALL be 401
- **AND** this SHALL hold even when the token is otherwise well-formed and unexpired

#### Scenario: Token with a broken signature

- **WHEN** a request carries a token whose signature does not verify against the issuer's published
  signing keys
- **THEN** the response status SHALL be 401

#### Scenario: Signing keys are fetched from the issuer

- **WHEN** the issuer rotates its signing keys and publishes the new key set
- **THEN** tokens signed with the new key SHALL be accepted without redeploying the backend

### Requirement: Caller identity is derived from configured claims

For every accepted token the system SHALL derive a caller identity consisting of a subject value
and the issuer that vouched for it. The claim that supplies the subject value SHALL be
configurable, because the portable `sub` claim is not the stable choice on every provider: some
issue a subject that is unique per registered application and therefore changes when that
registration is replaced, while offering a separate claim that is stable across applications.

Recording the issuer alongside the subject keeps later consumers of this identity able to tell
values from different providers apart, rather than merging two unrelated identifier spaces.

#### Scenario: Identity is available to request processing

- **WHEN** a request is accepted
- **THEN** the caller's subject value and the issuer it came from SHALL be available to the code
  handling that request

#### Scenario: Configured identity claim is absent from the token

- **WHEN** a request carries an otherwise acceptable token that does not contain the configured
  identity claim
- **THEN** the response status SHALL be 401
- **AND** the request SHALL NOT be processed with an empty or substituted identity

#### Scenario: Identity claim is changed by configuration

- **WHEN** the configured identity claim is changed to a different claim name
- **THEN** the caller identity SHALL be taken from that claim without any change to the code

### Requirement: Operational endpoints stay reachable without a token

The health, metrics and API documentation endpoints SHALL remain reachable without a bearer token.

Liveness and readiness probes and the metrics scrape call these endpoints without credentials; if
they were to receive 401, a deployed instance would never become ready and its metrics would stop
being collected.

#### Scenario: Health endpoint without a token

- **WHEN** `/health` is requested with no `Authorization` header
- **THEN** the response status SHALL be 200

#### Scenario: Metrics endpoint without a token

- **WHEN** `/prometheus` is requested with no `Authorization` header
- **THEN** the response status SHALL be 200

#### Scenario: API documentation without a token

- **WHEN** the Swagger specification or Swagger UI paths are requested with no `Authorization`
  header
- **THEN** they SHALL be served

### Requirement: Rejections use the project's problem format

Authentication and authorization failures SHALL be reported in the same RFC 9457 problem format as
every other failure this API produces, served as `application/problem+json` and carrying a stable
`code` that clients can branch on.

A client must not have to special-case the shape of an error simply because it concerns
authentication.

#### Scenario: Unauthenticated request body

- **WHEN** a request is rejected because it carries no acceptable token
- **THEN** the response content type SHALL be `application/problem+json`
- **AND** the body SHALL carry a `code`, a `status` of 401, and a `traceId`

#### Scenario: Forbidden request body

- **WHEN** a request is rejected because the caller is authenticated but not permitted
- **THEN** the response content type SHALL be `application/problem+json`
- **AND** the body SHALL carry a `code` and a `status` of 403

#### Scenario: Rejection reveals nothing about the token

- **WHEN** a request is rejected for any token-related reason
- **THEN** the `detail` SHALL NOT disclose which criterion failed, nor echo any part of the
  presented token

### Requirement: Cross-origin preflight is not authenticated

Cross-origin preflight requests SHALL be answered according to the configured CORS policy without
requiring a bearer token.

Browsers do not attach credentials to preflight requests. A preflight answered with 401 surfaces in
the browser as an opaque CORS failure rather than an authentication error, which hides the real
cause from whoever is debugging it.

#### Scenario: Preflight from a permitted origin

- **WHEN** an `OPTIONS` preflight request arrives from an origin the CORS policy permits, without
  an `Authorization` header
- **THEN** it SHALL receive the configured CORS response
- **AND** the response status SHALL NOT be 401

### Requirement: The identity provider is interchangeable through configuration

The issuer, the location of its signing keys, the expected audience and the identity claim SHALL
all be supplied by configuration. Changing to a different OpenID Connect provider SHALL NOT require
a code change.

Provider-specific claim names, endpoint shapes or token layouts SHALL NOT be hard-coded.

#### Scenario: Pointing the backend at a different provider

- **WHEN** the configured issuer, key location, audience and identity claim are changed to describe
  a different OpenID Connect provider
- **THEN** tokens from that provider SHALL be accepted on the same terms
- **AND** no source change SHALL be required

#### Scenario: One issuer at a time

- **WHEN** an issuer is configured
- **THEN** tokens from any other issuer SHALL be rejected, including during a provider migration

### Requirement: Local development works with and without a token

In a development environment the API SHALL be usable in three modes: with no token at all, with a
token from a locally running identity provider, and with a token from the real deployed provider.

#### Scenario: No token in a development environment

- **WHEN** a request without an `Authorization` header reaches a backend running in a development
  environment
- **THEN** the request SHALL be processed
- **AND** a clearly synthetic caller identity SHALL be supplied to request processing, so that
  consumers of the caller identity behave as they do for a real caller

#### Scenario: Unacceptable token in a development environment

- **WHEN** a request carrying a token that fails any acceptance criterion reaches a backend running
  in a development environment
- **THEN** the response status SHALL be 401
- **AND** the synthetic identity SHALL NOT be substituted

  The fallback is conditioned on the absence of a token, never on a failed validation. Were it
  otherwise, a token rejected in a deployed environment would appear to work locally, and the
  "with a token" mode would verify nothing.

#### Scenario: Acceptable token in a development environment

- **WHEN** a request carrying an acceptable token reaches a backend running in a development
  environment
- **THEN** the caller identity SHALL be derived from the token, not from the fallback

### Requirement: The development fallback cannot reach a deployed instance

The unauthenticated fallback SHALL be enabled only by the active runtime environment, and SHALL NOT
be enablable by any application property or request-supplied value. It SHALL additionally be
inactive whenever the instance detects that it is running in a cluster, so that naming the
development environment in a deployment's configuration by mistake does not open the API.

An instance that has the fallback active SHALL say so at startup, at warning level or above.

#### Scenario: Deployed instance without a token

- **WHEN** a request without an `Authorization` header reaches an instance that is not running in a
  development environment
- **THEN** the response status SHALL be 401

#### Scenario: Development environment named in a cluster deployment

- **WHEN** an instance running in a cluster has the development environment active
- **THEN** the fallback SHALL remain inactive
- **AND** requests without a token SHALL be rejected with 401

#### Scenario: Fallback is announced

- **WHEN** an instance starts with the fallback active
- **THEN** it SHALL log at warning level or above that it is accepting unauthenticated requests

#### Scenario: Automated tests do not inherit the fallback

- **WHEN** the automated test suite runs
- **THEN** the fallback SHALL NOT be active
- **AND** tests asserting a 401 SHALL observe a 401

### Requirement: The local setup covers all three modes

The repository SHALL provide what a developer needs to reach each of the three modes: an example
environment file listing every setting that has to be supplied, and a container setup that runs the
supporting services with or without a local identity provider.

The documentation SHALL state that the local identity provider exercises the token *mechanism*
only. Its issuer, audience and available claims differ from the deployed provider's, so the
failures most likely to occur in production -- a wrong audience, the wrong issuer variant, a
missing identity claim -- cannot surface in that mode. Only a token from the real provider
exercises the deployed configuration.

#### Scenario: Starting the supporting services without an identity provider

- **WHEN** a developer starts the container setup in its default form
- **THEN** the services needed to run the backend SHALL start
- **AND** no identity provider SHALL be required

#### Scenario: Starting the supporting services with a local identity provider

- **WHEN** a developer starts the container setup in the form that includes an identity provider
- **THEN** an identity provider SHALL start, pre-configured so that a token it issues is accepted
  by a backend using the accompanying example settings

#### Scenario: Example environment file is complete

- **WHEN** a developer copies the example environment file and supplies values for its entries
- **THEN** every setting this capability requires SHALL be present in that file
- **AND** each entry SHALL state what it is for

#### Scenario: Files the setup adds are actually committed

- **WHEN** a file is added for the local setup
- **THEN** it SHALL be tracked by version control

  The repository currently ignores the container setup directory wholesale, with a single tracked
  exception, so a new file placed there is silently absent from a fresh clone.

### Requirement: The API description advertises bearer authentication

The generated OpenAPI description SHALL declare a bearer token security scheme, so that generated
clients have a defined place to supply the token and the interactive documentation offers a way to
authorize.

#### Scenario: Security scheme in the generated description

- **WHEN** the OpenAPI description is generated
- **THEN** it SHALL declare a bearer token security scheme covering the authenticated endpoints

#### Scenario: Interactive documentation can authorize

- **WHEN** a developer opens Swagger UI
- **THEN** it SHALL offer a way to supply a bearer token for subsequent requests
