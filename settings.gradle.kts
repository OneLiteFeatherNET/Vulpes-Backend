rootProject.name = "vulpes-backend"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("io.micronaut.platform.catalog") version "5.0.2"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven {
            name = "OneLiteFeatherRepository"
            url = uri("https://repo.onelitefeather.dev/onelitefeather")
            if (System.getenv("CI") != null) {
                credentials {
                    username = System.getenv("ONELITEFEATHER_MAVEN_USERNAME")
                    password = System.getenv("ONELITEFEATHER_MAVEN_PASSWORD")
                }
            } else {
                credentials(PasswordCredentials::class)
                authentication {
                    create<BasicAuthentication>("basic")
                }
            }
        }
    }
    versionCatalogs {
        create("libs") {
            version("micronaut", "5.0.2")
            version("vulpes.model", "3.5.0")
            version("uuid.creator", "6.1.1")
            version("datafaker", "2.7.0")
            version("jetbrains.annotation", "26.1.0")
            version("hibernate.validator", "9.1.5.Final")
            version("jakarta.validation", "3.1.1")
            version("cyclonedx", "3.5.1")
            version("logstash.logback.encoder", "9.0")
            version("janino", "3.1.12")
            version("opentelemetry.instrumentation.alpha", "2.20.1-alpha")
            version("spotless", "8.10.4")
            version("palantir.java.format", "2.102.0")
            version("errorprone.plugin", "5.2.0")
            version("error.prone.core", "2.51.0")
            version("nullaway", "0.14.3")
            version("jspecify", "1.0.1")
            version("archunit", "1.5.1")

            library("uuid.creator", "com.github.f4b6a3", "uuid-creator").versionRef("uuid.creator")
            library("vulpes.api", "net.onelitefeather", "vulpes-model").versionRef("vulpes.model")
            library("jetbrains.annotation", "org.jetbrains", "annotations").versionRef("jetbrains.annotation")
            library("datafaker", "net.datafaker", "datafaker").versionRef("datafaker")
            library("testcontainers.junit", "org.testcontainers", "testcontainers-junit-jupiter").withoutVersion()

            library("hibernate.validator", "org.hibernate.validator", "hibernate-validator").versionRef("hibernate.validator")
            library("jakarta.validation", "jakarta.validation", "jakarta.validation-api").versionRef("jakarta.validation")

            // Observability — JSON logging + OpenTelemetry (see build.gradle.kts).
            // OTLP exporter version is managed by the Micronaut platform BOM.
            library("logstash.logback.encoder", "net.logstash.logback", "logstash-logback-encoder").versionRef("logstash.logback.encoder")
            library("janino", "org.codehaus.janino", "janino").versionRef("janino")
            library("opentelemetry.exporter.otlp", "io.opentelemetry", "opentelemetry-exporter-otlp").withoutVersion()
            library("opentelemetry.logback.mdc", "io.opentelemetry.instrumentation", "opentelemetry-logback-mdc-1.0").versionRef("opentelemetry.instrumentation.alpha")

            // Code standards for the asset packages (see build.gradle.kts).
            library("error.prone.core", "com.google.errorprone", "error_prone_core").versionRef("error.prone.core")
            library("nullaway", "com.uber.nullaway", "nullaway").versionRef("nullaway")
            library("jspecify", "org.jspecify", "jspecify").versionRef("jspecify")
            library("archunit.junit5", "com.tngtech.archunit", "archunit-junit5").versionRef("archunit")

            plugin("micronaut.application", "io.micronaut.application").versionRef("micronaut")
            plugin("micronaut.aot", "io.micronaut.aot").versionRef("micronaut")
            plugin("micronaut.test-resources", "io.micronaut.test-resources").versionRef("micronaut")
            plugin("cyclonedx", "org.cyclonedx.bom").versionRef("cyclonedx")
            plugin("spotless", "com.diffplug.spotless").versionRef("spotless")
            plugin("errorprone", "net.ltgt.errorprone").versionRef("errorprone.plugin")
        }
    }
}

