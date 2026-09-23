package net.onelitefeather.vulpes.backend.seed;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Seeds the database with test data when the application starts with the {@code seed} environment.
 *
 * <p>Runs on {@link StartupEvent}, i.e. before the HTTP server accepts requests. A failure aborts the
 * startup.
 *
 * <ul>
 *   <li>{@code vulpes.seed.reset} (default {@code false}): delete all Vulpes data and seed again.</li>
 *   <li>{@code vulpes.seed.random-seed}: seed for the generated filler data.</li>
 * </ul>
 */
@Singleton
@Requires(env = SeedRunner.SEED_ENVIRONMENT)
public class SeedRunner {

    public static final String SEED_ENVIRONMENT = "seed";

    private static final Logger LOGGER = LoggerFactory.getLogger(SeedRunner.class);

    private final SeedService seedService;
    private final boolean reset;
    private final long randomSeed;

    public SeedRunner(
            SeedService seedService,
            @Value("${vulpes.seed.reset:false}") boolean reset,
            @Value("${vulpes.seed.random-seed:20260923}") long randomSeed
    ) {
        this.seedService = seedService;
        this.reset = reset;
        this.randomSeed = randomSeed;
    }

    @EventListener
    void onStartup(StartupEvent event) {
        switch (seedService.seed(reset, randomSeed)) {
            case SeedService.Outcome.Skipped skipped -> LOGGER.info(
                    "Seeding skipped: the database already contains {} project(s). "
                            + "Start with vulpes.seed.reset=true to delete all data and seed again.",
                    skipped.existingProjects());
            case SeedService.Outcome.Seeded seeded -> LOGGER.info(
                    "Seeded {} projects, {} items, {} fonts, {} sounds, {} dimensions, {} attributes, "
                            + "{} notifications{} (random seed {})",
                    seeded.projects(), seeded.items(), seeded.fonts(), seeded.sounds(), seeded.dimensions(),
                    seeded.attributes(), seeded.notifications(), seeded.reset() ? " after reset" : "", randomSeed);
        }
    }
}
