plugins {
    alias(libs.plugins.micronaut.application)
    alias(libs.plugins.micronaut.aot)
    alias(libs.plugins.micronaut.test.resources)
    jacoco
    `maven-publish`
    id("org.openapi.generator") version "7.20.0"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
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
    // External Dependencies
    implementation(mn.mariadb.java.client)
    implementation(mn.postgresql)
    implementation(mn.snakeyaml)
    implementation(mn.logback.core)
    implementation(mn.logback.classic)
    // Vulpes API
    implementation(libs.vulpes.api)
    // UUID Creator
    implementation(libs.uuid.creator)

    testImplementation(mn.junit.jupiter.api)
    testImplementation(mn.junit.jupiter.params)
    testRuntimeOnly(mn.junit.jupiter.engine)
    testImplementation(mn.testcontainers.core)
    testImplementation(mn.testcontainers.mariadb)
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation(mn.micronaut.test.rest.assured)
    testImplementation(mn.micronaut.test.resources.extensions.core)
    testImplementation(mn.micronaut.test.resources.extensions.junit.platform)
    // Faker library for JUnit tests
    testImplementation("net.datafaker:datafaker:2.5.4")
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
        replaceLogbackXml = true
    }
}
tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release = 21
        options.forkOptions.jvmArgs = listOf("-Dmicronaut.openapi.views.spec=rapidoc.enabled=true,openapi-explorer.enabled=true,swagger-ui.enabled=true,swagger-ui.theme=flattop")
    }
    jacocoTestReport {
        reports {
            xml.required.set(true)
            html.required.set(true)
            csv.required.set(false)
        }
    }
    this.openApiGenerate {
        dependsOn("compileJava")
    }
    register("pushDartClient") {
        dependsOn("openApiGenerate")
       doLast {
           val clientDir = file("$projectDir/build/generated/dart-client")
           val version = project.version as String

           // Get GitHub credentials from environment variables
           val githubToken = System.getenv("CLIENT_REPO_TOKEN") ?: System.getenv("GITHUB_TOKEN") ?: throw GradleException("CLIENT_REPO_TOKEN or GITHUB_TOKEN environment variable is required")

           // Create a temporary directory for the Git repository
           val tempDir = file("$projectDir/build/temp/vulpes-client")
           tempDir.mkdirs()
           providers.exec {
               workingDir = tempDir
               commandLine("git", "clone", "https://${githubToken}@github.com/OneLiteFeatherNET/vulpes-backend-client-dart.git", ".")
           }.result?.get()

           // Copy the generated client to the repository
           copy {
               from(clientDir)
               into(tempDir)
           }

//           providers.exec {
//               workingDir = tempDir
//               commandLine("flutter", "pub", "get")
//           }.result?.get()

//           providers.exec {
//               workingDir = tempDir
//               commandLine("flutter", "pub", "run", "build_runner", "build", "--delete-conflicting-outputs")
//           }.result?.get()

           providers.exec {
               workingDir = tempDir
               commandLine("git", "add", ".")
           }.result?.get()

           providers.exec {
               workingDir = tempDir
               commandLine("git", "commit", "-m", "Update client to version $version")
           }.result?.get()

           providers.exec {
               workingDir = tempDir
               commandLine("git", "tag", "-a", "v$version", "-m", "Version $version")
           }.result?.get()

           providers.exec {
               workingDir = tempDir
               commandLine("git", "push", "origin")
           }.result?.get()

           providers.exec {
               workingDir = tempDir
               commandLine("git", "push", "origin", "--tags")
           }.result?.get()
       }
    }
    named("publish") {
        dependsOn("pushDartClient")
    }
}

// OpenAPI Generator configuration
openApiGenerate {
    generatorName.set("dart-dio")
    inputSpec.set("$projectDir/build/classes/java/main/META-INF/swagger/vulpes-backend-1.0.yml")
    outputDir.set("$projectDir/build/generated/dart-client")
    apiPackage.set("net.onelitefeather.vulpes.backend.client.api")
    invokerPackage.set("net.onelitefeather.vulpes.backend.client.invoker")
    modelPackage.set("net.onelitefeather.vulpes.backend.client.model")
    configOptions.set(mapOf(
        "pubName" to "vulpes_backend_client",
        "pubVersion" to (project.version as String),
        "pubDescription" to "Vulpes Backend API Client",
        "pubAuthor" to "OneLiteFeatherNET",
        "pubAuthorEmail" to "p.glanz@madfix.me",
        "pubHomepage" to "https://github.com/OneLiteFeatherNET/vulpes-backend-client-dart",
        "pubRepository" to "https://github.com/OneLiteFeatherNET/vulpes-backend-client-dart",
        "pubPublishTo" to "https://github.com/OneLiteFeatherNET/vulpes-backend-client-dart",
        "dateLibrary" to "core",
        "enumUnknownDefaultCase" to "true"
    ))
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
