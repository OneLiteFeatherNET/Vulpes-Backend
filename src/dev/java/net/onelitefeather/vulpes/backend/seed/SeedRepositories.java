package net.onelitefeather.vulpes.backend.seed;

import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;
import net.onelitefeather.vulpes.api.repository.AdvancementRepository;
import net.onelitefeather.vulpes.api.repository.AttributeRepository;
import net.onelitefeather.vulpes.api.repository.FontRepository;
import net.onelitefeather.vulpes.api.repository.ItemRepository;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.api.repository.SoundFileSourceRepository;
import net.onelitefeather.vulpes.api.repository.SoundRepository;
import net.onelitefeather.vulpes.api.repository.dimension.DimensionAttributeRepository;
import net.onelitefeather.vulpes.api.repository.dimension.DimensionTimelineRepository;
import net.onelitefeather.vulpes.api.repository.dimension.DimensionTypeRepository;
import net.onelitefeather.vulpes.api.repository.font.FontStringRepository;
import net.onelitefeather.vulpes.api.repository.item.ItemComponentRepository;
import net.onelitefeather.vulpes.api.repository.item.ItemEnchantmentRepository;
import net.onelitefeather.vulpes.api.repository.item.ItemLoreRepository;

/**
 * All repositories the seeder writes to or wipes.
 */
@Singleton
@Requires(env = SeedRunner.SEED_ENVIRONMENT)
public record SeedRepositories(
        ProjectRepository projects,
        ItemRepository items,
        ItemLoreRepository itemLore,
        ItemEnchantmentRepository itemEnchantments,
        ItemComponentRepository itemComponents,
        FontRepository fonts,
        FontStringRepository fontChars,
        SoundRepository sounds,
        SoundFileSourceRepository soundSources,
        DimensionTypeRepository dimensions,
        DimensionAttributeRepository dimensionAttributes,
        DimensionTimelineRepository dimensionTimelines,
        AttributeRepository attributes,
        AdvancementRepository advancements
) {
}
