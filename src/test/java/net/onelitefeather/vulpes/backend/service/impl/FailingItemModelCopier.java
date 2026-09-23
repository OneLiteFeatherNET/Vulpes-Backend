package net.onelitefeather.vulpes.backend.service.impl;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import net.onelitefeather.vulpes.api.model.ItemEntity;
import net.onelitefeather.vulpes.api.repository.ItemRepository;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.api.repository.item.ItemEnchantmentRepository;
import net.onelitefeather.vulpes.api.repository.item.ItemFlagRepository;
import net.onelitefeather.vulpes.api.repository.item.ItemLoreRepository;
import net.onelitefeather.vulpes.backend.domain.item.ItemRelation;
import net.onelitefeather.vulpes.backend.service.copier.AbstractRelationalModelCopier;
import net.onelitefeather.vulpes.backend.service.copier.ItemModelCopier;

/**
 * Test-only double that deliberately fails while copying any relation, active only under the
 * {@code rollback-test} environment. Exists to prove {@link AbstractRelationalModelCopier#copy}
 * is genuinely transactional: if it is, the root save this class lets happen before failing must
 * roll back along with it, and {@link ItemCopyRollbackIntegrationTest} asserts exactly that.
 *
 * <p>Deliberately does NOT override {@code copyRoot} — the root save must succeed so the test
 * proves a real rollback, not merely that nothing was ever attempted.
 *
 * <p>Explicitly re-declares {@code @Named("item")}: annotations aren't inherited by subclasses,
 * so without it this bean would lack the qualifier {@link net.onelitefeather.vulpes.backend.controller.item.ItemController}
 * injects by, and {@code @Replaces} alone does not carry it over — the real {@link ItemModelCopier}
 * would keep getting injected under this test environment instead of this failing double.
 */
@Singleton
@Named("item")
@Requires(env = "rollback-test")
@Replaces(ItemModelCopier.class)
public class FailingItemModelCopier extends ItemModelCopier {

    @Inject
    public FailingItemModelCopier(
            ItemRepository itemRepository,
            ItemLoreRepository itemLoreRepository,
            ItemFlagRepository itemFlagRepository,
            ItemEnchantmentRepository itemEnchantmentRepository,
            ProjectRepository projectRepository
    ) {
        super(itemRepository, itemLoreRepository, itemFlagRepository, itemEnchantmentRepository, projectRepository);
    }

    @Override
    protected void copyRelation(ItemRelation relation, ItemEntity source, ItemEntity target) {
        throw new RuntimeException("Simulated failure copying " + relation + " for the rollback test");
    }
}
