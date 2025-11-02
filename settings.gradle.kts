rootProject.name = "vulpes-backend"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("io.micronaut.platform.catalog") version "4.6.2"
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
            version("micronaut", "4.6.2")
            version("vulpes.model", "1.6.0-beta.11")
            version("uuid.creator", "6.1.1")

            library("uuid.creator", "com.github.f4b6a3", "uuid-creator").versionRef("uuid.creator")
            library("vulpes.api", "net.onelitefeather", "vulpes-model").versionRef("vulpes.model")
            library("jetbrains.annotation", "org.jetbrains", "annotations").version("26.1.0")

            plugin("micronaut.application", "io.micronaut.application").versionRef("micronaut")
            plugin("micronaut.aot", "io.micronaut.aot").versionRef("micronaut")
            plugin("micronaut.test-resources", "io.micronaut.test-resources").versionRef("micronaut")
        }
    }
}

