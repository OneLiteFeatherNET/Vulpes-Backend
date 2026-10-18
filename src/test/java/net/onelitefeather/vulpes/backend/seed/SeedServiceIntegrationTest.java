package net.onelitefeather.vulpes.backend.seed;

import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Runs every fixture through the seeder, so seed data that the API would refuse fails here instead
 * of on the next local start. The test transaction rolls the seeded data and the wipe back.
 */
@MicronautTest(environments = SeedRunner.SEED_ENVIRONMENT)
@Property(name = "datasources.default.schema-generate", value = "NONE")
@DisplayName("Integration tests for the dev data seeder")
@EnabledIfDockerAvailable
class SeedServiceIntegrationTest {

    @Inject
    SeedService seedService;

    @Test
    @DisplayName("seed() writes every fixture without a validation problem")
    void seedPassesTheApiValidation() {
        assertInstanceOf(SeedService.Outcome.Seeded.class, seedService.seed(true, 20260923));
    }

    /**
     * Keeps the seeder from running on startup, it would commit its data into the shared test database.
     */
    @Singleton
    @Replaces(SeedRunner.class)
    @Requires(env = SeedRunner.SEED_ENVIRONMENT)
    static class NoStartupSeeding extends SeedRunner {

        NoStartupSeeding(SeedService seedService) {
            super(seedService, false, 0);
        }

        @Override
        void onStartup(StartupEvent event) {
            // Seeds only through the test
        }
    }
}
