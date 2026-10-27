import net.ltgt.gradle.errorprone.errorprone

plugins {
    alias(libs.plugins.errorprone)
    alias(libs.plugins.micronaut.application)
    alias(libs.plugins.micronaut.aot)
    alias(libs.plugins.micronaut.test.resources) // TODO: Needs be fixed, ref: https://github.com/micronaut-projects/micronaut-gradle-plugin/issues/1195
    jacoco
    `maven-publish`
    alias(libs.plugins.cyclonedx)
    alias(libs.plugins.spotless)
}

version = "3.2.0" // x-release-please-version

// Code standards are limited to the asset packages for now; the rest of the code base is not formatted or
// null-checked yet and can follow package by package. Asset sources and tests are listed explicitly.
val assetSources = listOf(
    "src/main/java/net/onelitefeather/vulpes/backend/controller/asset/**/*.java",
    "src/main/java/net/onelitefeather/vulpes/backend/domain/asset/**/*.java",
    "src/main/java/net/onelitefeather/vulpes/backend/entity/asset/**/*.java",
    "src/main/java/net/onelitefeather/vulpes/backend/storage/**/*.java",
    "src/main/java/net/onelitefeather/vulpes/backend/service/AssetService.java",
    "src/main/java/net/onelitefeather/vulpes/backend/service/impl/Asset*.java",
    "src/test/java/net/onelitefeather/vulpes/backend/controller/asset/**/*.java",
    "src/test/java/net/onelitefeather/vulpes/backend/domain/asset/**/*.java",
    "src/test/java/net/onelitefeather/vulpes/backend/architecture/**/*.java",
)

spotless {
    java {
        target(assetSources)
        palantirJavaFormat(libs.versions.palantir.java.format.get())
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

// Development-only code (e.g. the data seeder). It is compiled against `main` and put on
// the `developmentOnly` classpath, which `./gradlew run` uses but the runtime jars and the
// Docker image do not.
val dev: SourceSet by sourceSets.creating {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
}
configurations[dev.implementationConfigurationName].extendsFrom(configurations.implementation.get())
configurations[dev.runtimeOnlyConfigurationName].extendsFrom(configurations.runtimeOnly.get())

dependencies {
    //Micronaut
    annotationProcessor(mn.micronaut.serde.processor)
    annotationProcessor(mn.micronaut.http.validation)
    annotationProcessor(mn.micronaut.data.processor)
    annotationProcessor(mn.micronaut.validation.processor)
    annotationProcessor(mn.micronaut.inject.java)
    annotationProcessor(mn.micronaut.openapi)

    compileOnly(mn.micronaut.openapi.annotations)

    implementation(mn.micronaut.serde.jackson)
    implementation(mn.micronaut.data.processor)
    implementation(mn.micronaut.validation)
    implementation(mn.micronaut.data.jpa)
    implementation(mn.micronaut.hibernate.jpa)
    implementation(mn.micronaut.data.hibernate.jpa)
    implementation(mn.micronaut.data.tx.hibernate)
    implementation(mn.micronaut.jdbc.hikari)
    implementation(mn.micronaut.runtime)
    implementation(mn.micronaut.openapi)
    implementation(mn.validation)
    implementation(mn.swagger.core)
    implementation(mn.micronaut.management)
    implementation(mn.micronaut.micrometer.core)
    implementation(mn.micronaut.micrometer.registry.prometheus)
    // Kubernetes service discovery — beans only activate in the k8s environment,
    // so this is inert in local/dev. Needs RBAC (read services/endpoints) in-cluster.
    implementation(mn.micronaut.kubernetes.discovery.client)
    // Object storage for project assets (textures, models). The local implementation is the
    // PoC default; an S3/MinIO implementation can replace it by dependency and config only.
    implementation(mn.micronaut.`object`.storage.core)
    implementation(mn.micronaut.`object`.storage.local)
    // External Dependencies
    implementation(mn.postgresql)
    implementation(mn.snakeyaml)
    implementation(mn.logback.core)
    implementation(mn.logback.classic)
    // Distributed tracing (OpenTelemetry). Spans/export are only active when
    // OTEL_TRACES_EXPORTER=otlp is set (prod/Docker) — see application.yml.
    implementation(mn.micronaut.tracing.opentelemetry.http)
    implementation(mn.micronaut.tracing.opentelemetry.jdbc)
    implementation(libs.opentelemetry.exporter.otlp)
    // Structured JSON logging for Grafana Loki + trace/log correlation.
    // logstash encoder renders JSON; the OTel MDC appender injects trace_id/span_id.
    implementation(libs.logstash.logback.encoder)
    implementation(libs.opentelemetry.logback.mdc)
    // Enables the <if>/<then>/<else> conditional in logback.xml.
    runtimeOnly(libs.janino)
    // Vulpes API
    implementation(libs.vulpes.api)
    // UUID Creator
    implementation(libs.uuid.creator)
    // JSpecify nullability annotations, checked by NullAway in the asset packages.
    implementation(libs.jspecify)
    errorprone(libs.error.prone.core)
    errorprone(libs.nullaway)

    testImplementation(libs.archunit.junit5)
    testImplementation(mn.junit.jupiter.api)
    testImplementation(mn.junit.jupiter.params)
    testImplementation(mn.testcontainers.core)
    testImplementation(mn.testcontainers.postgres)
    testImplementation(mn.micronaut.test.rest.assured)
    testImplementation(mn.micronaut.validation)
    testImplementation(mn.micronaut.test.resources.extensions.core)
    testImplementation(mn.micronaut.test.resources.extensions.junit.platform)
    // Faker library for JUnit tests
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.datafaker)
    testImplementation(libs.hibernate.validator)
    testImplementation(libs.jakarta.validation)

    testRuntimeOnly(mn.junit.jupiter.engine)

    // Development-only: data seeder (see src/dev)
    "devAnnotationProcessor"(mn.micronaut.inject.java)
    "devAnnotationProcessor"(mn.micronaut.data.processor) // maps jakarta.transaction.Transactional
    "devImplementation"(libs.datafaker)
    developmentOnly(dev.output)
    developmentOnly(libs.datafaker)
    testImplementation(dev.output)
}

application {
    mainClass.set("net.onelitefeather.vulpes.backend.VulpesBackend")
}

graalvmNative.toolchainDetection = false

micronaut {
    runtime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("net.onelitefeather.vulpes.*")
    }
    aot {
        optimizeServiceLoading = false
        convertYamlToJava = false
        precomputeOperations = true
        cacheEnvironment = true
        optimizeClassLoading = false
        deduceEnvironment = true
        optimizeNetty = true
        // Keep logback.xml parsed at runtime so the env-driven JSON/plain switch
        // and ${...} substitutions work in the optimized (Docker/prod) jar.
        replaceLogbackXml = false
    }
}
tasks {
    named("internalStartTestResourcesService") { // Workaround for Java 25 Graal: https://github.com/micronaut-projects/micronaut-gradle-plugin/issues/1195#issuecomment-3714801163
        setProperty("useClassDataSharing", false)
    }
    // Keep the development-only source set compiling against model/API changes in CI.
    check {
        dependsOn(dev.classesTaskName)
    }
    named<JavaCompile>(dev.compileJavaTaskName) {
        options.encoding = "UTF-8"
        options.release = 25
        // Error Prone is limited to compileJava: NullAway needs the asset packages marked with @NullMarked.
        options.errorprone.enabled.set(false)
    }
    compileJava {
        options.encoding = "UTF-8"
        options.release = 25
        options.forkOptions.jvmArgs = listOf("-Dmicronaut.openapi.views.spec=rapidoc.enabled=true,openapi-explorer.enabled=true,swagger-ui.enabled=true,swagger-ui.theme=flattop")
        options.errorprone {
            disableWarningsInGeneratedCode.set(true)
            // Micronaut's generated sources would otherwise be checked; they are not hand-written code.
            excludedPaths.set(".*/build/generated/.*")
            // NullAway checks only code marked @NullMarked (the asset packages, see package-info.java).
            option("NullAway:OnlyNullMarked", "true")
            // Only NullAway for now, so the other Error Prone checks do not flag code outside the asset packages.
            disableAllChecks.set(true)
            option("NullAway:JSpecifyMode", "true")
            error("NullAway")
        }
    }
    // Error Prone runs on the production code only; the tests are not null-checked.
    compileTestJava {
        options.errorprone.enabled.set(false)
    }
    jacocoTestReport {
        reports {
            xml.required.set(true)
            html.required.set(true)
            csv.required.set(false)
        }
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        // Only the thin application jar is published. The fat runner jars and the
        // full distribution tar/zip bundle every dependency; the runnable artifact
        // is the Docker image, not a Maven artifact.
        artifact(project.tasks.jar)

        version = rootProject.version as String
        artifactId = "vulpes-backend"
        groupId = rootProject.group as String
        pom {
            name = "Vulpes Backend"
            description = "A backend server for OneLiteFeather's Vulpes project, providing a REST API and database access."
            url = "https://github.com/OneLiteFeatherNET/vulpes-backend"
            licenses {
                license {
                    name = "AGPL-3.0"
                    url = "https://www.gnu.org/licenses/agpl-3.0.en.html"
                }
            }
            developers {
                developer {
                    id = "themeinerlp"
                    name = "Phillipp Glanz"
                    email = "p.glanz@madfix.me"
                }
                developer {
                    id = "theEvilReaper"
                    name = "Steffen Wonning"
                    email = "steffenwx@gmail.com"
                }
            }
            scm {
                connection = "scm:git:git://github.com:OneLiteFeatherNET/vulpes-backend.git"
                developerConnection = "scm:git:ssh://git@github.com:OneLiteFeatherNET/vulpes-backend.git"
                url = "https://github.com/OneLiteFeatherNET/vulpes-backend"
            }
        }
    }

    repositories {
        maven {
            authentication {
                credentials(PasswordCredentials::class) {
                    // Those credentials need to be set under "Settings -> Secrets -> Actions" in your repository
                    username = System.getenv("ONELITEFEATHER_MAVEN_USERNAME")
                    password = System.getenv("ONELITEFEATHER_MAVEN_PASSWORD")
                }
            }

            name = "OneLiteFeatherRepository"
            val releasesRepoUrl = uri("https://repo.onelitefeather.dev/onelitefeather-releases")
            val snapshotsRepoUrl = uri("https://repo.onelitefeather.dev/onelitefeather-snapshots")
            url = if (version.toString().contains("BETA") || version.toString().contains("ALPHA") || version.toString().contains("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
        }
    }
}
