import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.spring)
    alias(libs.plugins.spring.dependency)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
}

group = "net.theevilreaper"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        val groupdId = 28 // Gitlab Group
        url = if (System.getenv().containsKey("CI")) {
            val ciApiv4Url = System.getenv("CI_API_V4_URL")
            uri("$ciApiv4Url/groups/$groupdId/-/packages/maven")
        } else {
            uri("https://gitlab.themeinerlp.dev/api/v4/groups/$groupdId/-/packages/maven")
        }
        name = "GitLab"
        credentials(HttpHeaderCredentials::class.java) {
            name = if (System.getenv().containsKey("CI")) {
                "Job-Token"
            } else {
                "Private-Token"
            }
            value = if (System.getenv().containsKey("CI")) {
                System.getenv("CI_JOB_TOKEN")
            } else {
                val gitLabPrivateToken: String? by project
                gitLabPrivateToken
            }
        }
        authentication {
            create<HttpHeaderAuthentication>("header")
        }
    }
}

dependencies {
    implementation(libs.vulpes.api)
    implementation(libs.spring.starter.web)
    implementation(libs.spring.starter.data.mongodb)
    implementation(libs.jackson)
    implementation(libs.annotation)

    testImplementation(libs.embed.mongo)
    testImplementation(libs.spring.starter.test)
    testImplementation(libs.spring.starter.data.mongodb)
}

tasks {
    compileKotlin {
        compilerOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    bootBuildImage {
        builder.set("paketobuildpacks/builder-jammy-base:latest")
    }
    test {
        useJUnitPlatform()
    }
}
