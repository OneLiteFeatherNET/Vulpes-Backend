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

## License

This project is licensed under the AGPL-3.0 License - see the LICENSE file for details.