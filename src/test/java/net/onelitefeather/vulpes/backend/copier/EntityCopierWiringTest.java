package net.onelitefeather.vulpes.backend.copier;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import net.onelitefeather.vulpes.api.model.AttributeEntity;
import net.onelitefeather.vulpes.api.model.FontEntity;
import net.onelitefeather.vulpes.api.model.ItemEntity;
import net.onelitefeather.vulpes.api.model.NotificationEntity;
import net.onelitefeather.vulpes.api.model.dimension.DimensionTypeEntity;
import net.onelitefeather.vulpes.api.model.sound.SoundEventEntity;
import net.onelitefeather.vulpes.backend.domain.dimension.DimensionRelation;
import net.onelitefeather.vulpes.backend.domain.font.FontRelation;
import net.onelitefeather.vulpes.backend.domain.item.ItemRelation;
import net.onelitefeather.vulpes.backend.domain.sound.SoundRelation;
import net.onelitefeather.vulpes.backend.service.copier.AttributeModelCopier;
import net.onelitefeather.vulpes.backend.service.copier.DimensionModelCopier;
import net.onelitefeather.vulpes.backend.service.copier.FontModelCopier;
import net.onelitefeather.vulpes.backend.service.copier.ItemModelCopier;
import net.onelitefeather.vulpes.backend.service.copier.NotificationModelCopier;
import net.onelitefeather.vulpes.backend.service.copier.SoundModelCopier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Proves that Micronaut resolves each distinct {@link EntityCopier} parameterization to the
 * right concrete singleton, now that {@code AttributeCopier}/{@code NotificationCopier}/
 * {@code ItemCopier} are gone and every controller injects {@code EntityCopier<T, R>} directly.
 * Nothing in the plain-unit controller tests exercises real Micronaut DI, so this is the only
 * place generic bean resolution is actually verified rather than assumed &mdash; this matters
 * because it already caught a real {@code NonUniqueBeanException} once: {@link ItemModelCopier}'s
 * {@code @Transactional} AOP proxy loses its reified generic type info and becomes a false
 * candidate for every {@code EntityCopier<?,?>} injection point unless every bean and every
 * injection point carries an explicit {@code @Named} qualifier.
 */
@MicronautTest(startApplication = false)
@EnabledIfDockerAvailable
@DisplayName("Micronaut resolves each EntityCopier<T, R> parameterization to its concrete bean")
class EntityCopierWiringTest {

    @Inject
    @Named("attribute")
    EntityCopier<AttributeEntity, Void> attributeCopier;

    @Inject
    @Named("notification")
    EntityCopier<NotificationEntity, Void> notificationCopier;

    @Inject
    @Named("item")
    EntityCopier<ItemEntity, ItemRelation> itemCopier;

    @Inject
    @Named("font")
    EntityCopier<FontEntity, FontRelation> fontCopier;

    @Inject
    @Named("dimension")
    EntityCopier<DimensionTypeEntity, DimensionRelation> dimensionCopier;

    @Inject
    @Named("sound")
    EntityCopier<SoundEventEntity, SoundRelation> soundCopier;

    @Test
    @DisplayName("resolves every copier bean by its generic parameterization")
    void resolvesEachParameterization() {
        assertInstanceOf(AttributeModelCopier.class, attributeCopier);
        assertInstanceOf(NotificationModelCopier.class, notificationCopier);
        assertInstanceOf(ItemModelCopier.class, itemCopier);
        assertInstanceOf(FontModelCopier.class, fontCopier);
        assertInstanceOf(DimensionModelCopier.class, dimensionCopier);
        assertInstanceOf(SoundModelCopier.class, soundCopier);
    }
}
