# Vulpes Backend

A backend server for OneLiteFeather's Vulpes project, providing a REST API and database access.

## Features

- REST API for managing custom attributes, fonts, items, and notifications
- OpenAPI documentation
- Automatic Dart Dio client generation

## OpenAPI and Dart Client Generation

This project automatically generates a Dart Dio client from the OpenAPI specification during the build process. The client is then pushed to a separate Git repository with the project version as a tag.

### How it works

1. The OpenAPI specification is generated during the build process using Micronaut's OpenAPI support.
2. The OpenAPI Generator Gradle plugin is used to generate a Dart Dio client from the specification.
3. The generated client is pushed to the [vulpes-client](https://github.com/OneLiteFeatherNET/vulpes-client) repository with the project version as a tag.

### Configuration

The OpenAPI Generator is configured in the `build.gradle.kts` file:

```kotlin
openApiGenerate {
    generatorName.set("dart-dio")
    inputSpec.set("$buildDir/tmp/kapt3/classes/main/META-INF/swagger/vulpes-backend-1.0.yml")
    outputDir.set("$buildDir/generated/dart-client")
    apiPackage.set("net.onelitefeather.vulpes.client.api")
    invokerPackage.set("net.onelitefeather.vulpes.client.invoker")
    modelPackage.set("net.onelitefeather.vulpes.client.model")
    configOptions.set(mapOf(
        "pubName" to "vulpes_client",
        "pubVersion" to (project.version as String),
        "pubDescription" to "Vulpes API Client",
        "pubAuthor" to "OneLiteFeather",
        "pubAuthorEmail" to "p.glanz@madfix.me",
        "pubHomepage" to "https://github.com/OneLiteFeatherNET/vulpes-client",
        "pubRepository" to "https://github.com/OneLiteFeatherNET/vulpes-client",
        "dateLibrary" to "core",
        "enumUnknownDefaultCase" to "true"
    ))
}
```

### GitHub Actions

The GitHub Actions workflow is configured to run the client generation and repository pushing during the release process. The workflow uses a custom secret called `CLIENT_REPO_TOKEN` for authenticating with GitHub when pushing to the client repository.

To set up the `CLIENT_REPO_TOKEN`:

1. Create a personal access token with the `repo` scope.
2. Add the token as a secret in the repository settings with the name `CLIENT_REPO_TOKEN`.

## Development

### Prerequisites

- Java 21
- Gradle
- Node.js (for semantic-release)

### Building

```bash
./gradlew build
```

### Running

```bash
docker compose -f docker/compose.yml up -d     # PostgreSQL
MICRONAUT_ENVIRONMENTS=local ./gradlew run
```

The API then answers on `http://localhost:8085`, with Swagger UI at `/swagger-ui/index.html`.

### Testing

```bash
./gradlew test
```

## Authentication

Every endpoint except `/health`, `/prometheus` and the Swagger paths requires a bearer token
issued by the configured OpenID Connect provider. The backend is a resource server: it validates
tokens the UI obtained itself and never issues, refreshes or stores one. A token is accepted only
when its signature verifies against the issuer's published keys, it is unexpired, and its `iss` and
`aud` match what is configured — the audience check being the one that stops tokens minted for
some other application from opening this API.

Deployments supply four values, which is the whole of what makes the provider interchangeable:

| Variable | Meaning |
|---|---|
| `OIDC_ISSUER` | the exact `iss` claim of accepted tokens |
| `OIDC_AUDIENCE` | the `aud` claim naming this backend |
| `OIDC_JWKS_URL` | where the issuer publishes its signing keys |
| `OIDC_IDENTITY_CLAIM` | the claim identifying the caller (default `sub`; `oid` on Entra ID) |

The application refuses to start if the first three are missing, rather than accepting everything
or failing later on every route.

> On Entra ID, set `OIDC_IDENTITY_CLAIM=oid`. Entra's `sub` is unique per app registration, so
> replacing that registration silently renumbers every caller; `oid` is stable across
> registrations. Keycloak has no `oid` and uses `sub`.

### Working locally

Three modes, all supported at once:

**1 — Without a token.** Nothing extra to run:

```bash
docker compose -f docker/compose.yml up -d
MICRONAUT_ENVIRONMENTS=local ./gradlew run
curl http://localhost:8085/project            # answers
```

Requests arriving with no `Authorization` header at all are attributed to a synthetic caller, and
the application says so at startup. A request that *does* carry a token is still validated and
still refused when it fails — the fallback is not a way past validation, only a way around
needing a provider.

This applies only to the `local` environment, and never inside a cluster: the bypass additionally
requires `KUBERNETES_SERVICE_HOST` to be unset, so naming `local` in a deployment's profile list
by mistake leaves the API closed.

**2 — With a local identity provider.** Adds Keycloak, pre-configured:

```bash
cp docker/.env.example docker/.env        # optional; every value has a default
docker compose -f docker/compose.yml --profile keycloak up -d

TOKEN=$(curl -s -X POST http://localhost:8081/realms/vulpes/protocol/openid-connect/token \
  -d client_id=vulpes-ui-local -d username=developer -d password=developer \
  -d grant_type=password | python3 -c 'import json,sys; print(json.load(sys.stdin)["access_token"])')

curl -H "Authorization: Bearer $TOKEN" http://localhost:8085/project
```

The realm ships an audience mapper, which is what puts `vulpes-backend` into the token's `aud`.
Without it Keycloak issues tokens whose audience is `account`, and this API refuses those.

**3 — With a real token.** Copy one out of the running UI and send it. This is the only mode that
tests the deployed configuration.

> Mode 2 exercises the *mechanism*, not the *configuration*. Its issuer is a local realm, its
> audience is whatever the realm says, and Keycloak has no `oid` claim. The failures most likely
> to occur in production — wrong audience, wrong issuer variant, a missing identity claim — cannot
> surface there. Green in mode 2 does not mean ready to deploy.

## Error handling

Every endpoint answers a failure with a single body shape, [RFC 9457 Problem Details](https://www.rfc-editor.org/rfc/rfc9457), served as `application/problem+json`:

```json
{
  "type": "https://vulpes.onelitefeather.net/errors/resource-not-found",
  "title": "Resource not found",
  "status": 404,
  "detail": "Attribute not found.",
  "instance": "/project/6f1c.../attribute/update",
  "code": "RESOURCE_NOT_FOUND",
  "traceId": "4bf92f3577b34da6a3ce929d0e0e4736",
  "errors": []
}
```

This holds for framework errors too — unbindable path variables, malformed JSON, 405, 415 — because the shape is produced by an `ErrorResponseProcessor`, which is the hook every built-in Micronaut handler routes its body through.

### For clients

- Branch and localize on **`code`**, never on `detail`. The codes are the `ErrorCode` enum and reach the generated Dart client as an enum; `detail` is English prose and may be reworded at any time.
- On `VALIDATION_FAILED`, **`errors`** lists the rejected fields as `{field, code, message}`, where `field` is the request property path (`displayName`), so a form can mark the matching input.
- Show **`traceId`** in support dialogs. It is the OpenTelemetry trace id when tracing is enabled, and always identifies the matching server log line.

### For contributors

- Raise failures with `ApiException`; the status, title and problem type come from the `ErrorCode` you pass.
- The message you pass **is the response body**. Author it at the throw site from data the caller already sent us. Never forward a message from JDBC, Hibernate or any other lower layer — those carry table names, column names and SQL fragments ([CWE-209](https://cwe.mitre.org/data/definitions/209.html)). Details for 5xx are a fixed constant for the same reason.
- When the honest reason differs from what the caller may learn — a cross-project access, for instance — pass it as `internalDetail`. It is logged and never serialized.

## License

This project is licensed under the AGPL-3.0 License - see the LICENSE file for details.