import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.text.SimpleDateFormat
import java.util.*

plugins {
    alias(libs.plugins.spring)
    alias(libs.plugins.spring.dependency)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
}

group = "net.theevilreaper"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
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
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
    bootBuildImage {
        if (System.getenv().containsKey("CI")) {
            createdDate.set(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(Date()))
            environment.set(environment.get() + mapOf("BP_JVM_VERSION" to "21"))
            imageName.set("${System.getenv("CI_REGISTRY_IMAGE")}/backend")
            publish.set(true)
            docker {
                publishRegistry {
                    url.set("https://${System.getenv("CI_REGISTRY")}")
                    username.set(System.getenv("CI_REGISTRY_USER"))
                    password.set(System.getenv("CI_REGISTRY_PASSWORD"))
                }
            }
        }

    }
    test {
        useJUnitPlatform()
    }
}
