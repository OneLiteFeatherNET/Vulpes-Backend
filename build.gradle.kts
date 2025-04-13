plugins {
    alias(libs.plugins.shadowJar)
    alias(libs.plugins.micronaut.application)
    alias(libs.plugins.micronaut.aot)
    jacoco
}

group = "net.theevilreaper"
version = "0.5.0-SNAPSHOT"

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
    implementation(mn.validation)
    implementation(mn.swagger.core)
    // External Dependencies
    implementation(mn.mariadb.java.client)
    implementation(mn.snakeyaml)
    implementation(mn.log4j)
    implementation(mn.slf4j.api)
    implementation(mn.slf4j.simple)
    // Vulpes API
    implementation(libs.vulpes.api)
    // UUID Creator
    implementation(libs.uuid.creator)

    testImplementation(mn.junit.jupiter.api)
    testImplementation(mn.junit.jupiter.params)
    testRuntimeOnly(mn.junit.jupiter.engine)
}


application {
    mainClass.set("net.theevilreaper.vulpes.backend.VulpesBackend")
}

graalvmNative.toolchainDetection = false

micronaut {
    runtime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("net.theevilreaper.*")
    }
    aot {
        optimizeServiceLoading = false
        convertYamlToJava = false
        precomputeOperations = true
        cacheEnvironment = true
        optimizeClassLoading = true
        deduceEnvironment = true
        optimizeNetty = true
        replaceLogbackXml = true
    }
}

tasks.named<io.micronaut.gradle.docker.NativeImageDockerfile>("dockerfileNative") {
    jdkVersion = "21"
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release = 21
        options.forkOptions.jvmArgs = listOf("-Dmicronaut.openapi.views.spec=rapidoc.enabled=true,openapi-explorer.enabled=true,swagger-ui.enabled=true,swagger-ui.theme=flattop")
    }

    jar {
        dependsOn("shadowJar")
    }
}

