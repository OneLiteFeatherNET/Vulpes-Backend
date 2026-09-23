package net.onelitefeather.vulpes.backend.seed;

import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import net.onelitefeather.vulpes.backend.seed.data.Filler;
import net.onelitefeather.vulpes.backend.seed.fixture.EdgeCaseFixture;
import net.onelitefeather.vulpes.backend.seed.fixture.EldoriaFixture;
import net.onelitefeather.vulpes.backend.seed.fixture.EmptyProjectFixture;
import net.onelitefeather.vulpes.backend.seed.fixture.ProjectFixture;
import net.onelitefeather.vulpes.backend.seed.fixture.SkyblockFixture;

import java.util.List;

/**
 * Seeds the database in a single transaction, so a failure (including a validation failure) leaves
 * the previous state untouched.
 */
@Singleton
@Requires(env = SeedRunner.SEED_ENVIRONMENT)
public class SeedService {

    /**
     * Fixtures run in this order and share one {@link Filler}; changing the order changes the data.
     */
    private static final List<ProjectFixture> FIXTURES = List.of(
            new EldoriaFixture(),
            new SkyblockFixture(),
            new EdgeCaseFixture(),
            new EmptyProjectFixture()
    );

    public sealed interface Outcome {
        record Skipped(long existingProjects) implements Outcome {
        }

        record Seeded(boolean reset, int projects, int items, int fonts, int sounds, int dimensions,
                      int attributes, int notifications) implements Outcome {
        }
    }

    private final SeedRepositories repositories;
    private final SeedWiper wiper;
    private final SeedValidator validator;

    public SeedService(SeedRepositories repositories, SeedWiper wiper, SeedValidator validator) {
        this.repositories = repositories;
        this.wiper = wiper;
        this.validator = validator;
    }

    @Transactional
    public Outcome seed(boolean reset, long randomSeed) {
        long existingProjects = repositories.projects().count();
        if (existingProjects > 0 && !reset) {
            return new Outcome.Skipped(existingProjects);
        }
        if (reset) {
            wiper.wipeAll();
        }

        SeedWriter writer = new SeedWriter(repositories, validator);
        Filler filler = new Filler(randomSeed);
        FIXTURES.forEach(fixture -> fixture.seed(writer, filler));

        return new Outcome.Seeded(reset, writer.projects.size(), writer.items.size(), writer.fonts.size(),
                writer.sounds.size(), writer.dimensions.size(), writer.attributes.size(),
                writer.notifications.size());
    }
}
