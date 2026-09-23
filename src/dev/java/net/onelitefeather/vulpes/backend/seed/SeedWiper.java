package net.onelitefeather.vulpes.backend.seed;

import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;

/**
 * Deletes all Vulpes data: child rows first, then the project-scoped aggregates, then the projects.
 */
@Singleton
@Requires(env = SeedRunner.SEED_ENVIRONMENT)
public class SeedWiper {

    private final SeedRepositories repositories;

    public SeedWiper(SeedRepositories repositories) {
        this.repositories = repositories;
    }

    void wipeAll() {
        repositories.itemLore().deleteAll();
        repositories.itemEnchantments().deleteAll();
        repositories.itemFlags().deleteAll();
        repositories.fontChars().deleteAll();
        repositories.soundSources().deleteAll();
        repositories.dimensionAttributes().deleteAll();
        repositories.dimensionTimelines().deleteAll();

        repositories.items().deleteAll();
        repositories.fonts().deleteAll();
        repositories.sounds().deleteAll();
        repositories.dimensions().deleteAll();
        repositories.attributes().deleteAll();
        repositories.notifications().deleteAll();

        repositories.projects().deleteAll();
    }
}
