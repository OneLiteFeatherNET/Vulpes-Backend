rootProject.name = "vulpes-backend"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://eldonexus.de/repository/maven-public/")
    }
}

plugins {
    id("io.micronaut.platform.catalog") version "4.4.4"
}

dependencyResolutionManagement {
    repositories {
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        mavenCentral()
        maven("https://jitpack.io")
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
            version("micronaut", "4.4.4")
            version("vulpes.model", "1.0.0")

            library("vulpes.api", "net.theevilreaper.vulpes.api", "vulpes-model").versionRef("vulpes.model")
            library("jetbrains.annotation", "org.jetbrains", "annotations").version("26.0.2")

            plugin("micronaut.application", "io.micronaut.application").versionRef("micronaut")
            plugin("micronaut.aot", "io.micronaut.aot").versionRef("micronaut")
            plugin("shadowJar", "com.gradleup.shadow").version("9.0.0-beta8")
        }
    }
}

