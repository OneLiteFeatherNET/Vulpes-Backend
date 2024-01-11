import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.springframework.boot") version "3.1.5"
    id("io.spring.dependency-management") version "1.1.3"
    kotlin("jvm") version "1.9.20"
    kotlin("plugin.spring") version "1.9.20"
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
    implementation("net.theevilreaper.vulpes.api:vulpes-spring-api:0.0.1+1f9ff402")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.+")

    testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo.spring30x:4.6.2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-mongodb")
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
