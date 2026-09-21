package net.onelitefeather.vulpes.backend.copier;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import net.onelitefeather.vulpes.api.model.AttributeEntity;
import net.onelitefeather.vulpes.api.model.ItemEntity;
import net.onelitefeather.vulpes.api.model.NotificationEntity;
import net.onelitefeather.vulpes.backend.domain.item.ItemRelation;
import net.onelitefeather.vulpes.backend.service.copier.AttributeModelCopier;
import net.onelitefeather.vulpes.backend.service.copier.ItemModelCopier;
import net.onelitefeather.vulpes.backend.service.copier.NotificationModelCopier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Proves that Micronaut resolves each distinct {@link EntityCopier} parameterization to the
 * right concrete singleton, now that {@code AttributeCopier}/{@code NotificationCopier}/
 * {@code ItemCopier} are gone and the three controllers inject {@code EntityCopier<T, R>}
 * directly. Nothing in the plain-unit controller tests exercises real Micronaut DI, so this is
 * the only place generic bean resolution is actually verified rather than assumed.
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

    @Test
    @DisplayName("resolves the attribute, notification and item copier beans by generic parameterization")
    void resolvesEachParameterization() {
        assertInstanceOf(AttributeModelCopier.class, attributeCopier);
        assertInstanceOf(NotificationModelCopier.class, notificationCopier);
        assertInstanceOf(ItemModelCopier.class, itemCopier);
    }
}
