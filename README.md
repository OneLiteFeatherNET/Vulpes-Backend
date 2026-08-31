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
./gradlew run
```

### Testing

```bash
./gradlew test
```

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