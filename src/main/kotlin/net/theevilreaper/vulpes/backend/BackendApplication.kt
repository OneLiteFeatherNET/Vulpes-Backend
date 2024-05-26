package net.theevilreaper.vulpes.backend

import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import net.theevilreaper.vulpes.api.RepoSpec
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

/**
 * The class is the main entry point for the BackendApplication.
 * @author theEvilReaper
 * @version 1.0.0
 * @since
 **/
@EnableMongoRepositories(
    basePackageClasses = [
        RepoSpec::class,
    ]
)
@SpringBootApplication(
    scanBasePackageClasses = [
        BackendApplication::class,
        RepoSpec::class
    ]
)
class BackendApplication

@Bean
fun objectMapperBuilder(): Jackson2ObjectMapperBuilder = Jackson2ObjectMapperBuilder()
    .modulesToInstall(
        KotlinModule.Builder()
            .enable(KotlinFeature.NullToEmptyMap)
            .enable(KotlinFeature.NullToEmptyCollection)
            .build()
    )

fun main(args: Array<String>) {
    // https://stackoverflow.com/a/48988779
    runApplication<BackendApplication>(*args)
}
