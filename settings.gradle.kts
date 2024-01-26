rootProject.name = "vulpes-spring-backend"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotlin", "1.9.22")

            library("jackson", "com.fasterxml.jackson.module", "jackson-module-kotlin").version("2.16.1")
            library("annotation", "org.jetbrains", "annotations").version("24.1.0")

            library(
                "vulpes.api",
                "net.theevilreaper.vulpes.api",
                "vulpes-spring-api"
            ).version("0.0.3-SNAPSHOT+b6928369")

            library("spring.starter.web", "org.springframework.boot", "spring-boot-starter-web").withoutVersion()
            library(
                "spring.starter.data.mongodb",
                "org.springframework.boot",
                "spring-boot-starter-data-mongodb"
            ).withoutVersion()
            library("spring.starter.test", "org.springframework.boot", "spring-boot-starter-test").withoutVersion()

            library("embed.mongo", "de.flapdoodle.embed", "de.flapdoodle.embed.mongo.spring30x").version("4.11.0")

            plugin("spring", "org.springframework.boot").version("3.2.1")
            plugin("spring.dependency", "io.spring.dependency-management").version("1.1.4")
            plugin("kotlin.jvm", "org.jetbrains.kotlin.jvm").versionRef("kotlin")
            plugin("kotlin.spring", "org.jetbrains.kotlin.plugin.spring").versionRef("kotlin")
        }
    }
}