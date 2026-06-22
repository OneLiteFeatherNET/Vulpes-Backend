plugins {
    alias(libs.plugins.micronaut.application)
    alias(libs.plugins.micronaut.aot)
    alias(libs.plugins.micronaut.test.resources) // TODO: Needs be fixed, ref: https://github.com/micronaut-projects/micronaut-gradle-plugin/issues/1195
    jacoco
    `maven-publish`
    alias(libs.plugins.cyclonedx)
}

version = (version as String).substringBefore('#').trim()

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

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
    // External Dependencies
    implementation(mn.mariadb.java.client)
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

    testImplementation(mn.junit.jupiter.api)
    testImplementation(mn.junit.jupiter.params)
    testImplementation(mn.testcontainers.core)
    testImplementation(mn.testcontainers.mariadb)
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
    compileJava {
        options.encoding = "UTF-8"
        options.release = 25
        options.forkOptions.jvmArgs = listOf("-Dmicronaut.openapi.views.spec=rapidoc.enabled=true,openapi-explorer.enabled=true,swagger-ui.enabled=true,swagger-ui.theme=flattop")
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
        artifact(project.tasks.optimizedJitJar)
        artifact(project.tasks.optimizedRunnerJitJar)
        artifact(project.tasks.runnerJar)
        artifact(project.tasks.jar)
        artifact(project.tasks.optimizedDistTar)
        artifact(project.tasks.optimizedDistZip)

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
