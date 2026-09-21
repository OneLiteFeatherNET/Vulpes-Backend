package net.onelitefeather.vulpes.backend.service.copier;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.model.Pageable;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import net.onelitefeather.vulpes.api.model.ItemEntity;
import net.onelitefeather.vulpes.api.model.item.ItemEnchantmentEntity;
import net.onelitefeather.vulpes.api.model.item.ItemFlagEntity;
import net.onelitefeather.vulpes.api.model.item.ItemLoreEntity;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.api.repository.ItemRepository;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.api.repository.item.ItemEnchantmentRepository;
import net.onelitefeather.vulpes.api.repository.item.ItemFlagRepository;
import net.onelitefeather.vulpes.api.repository.item.ItemLoreRepository;
import net.onelitefeather.vulpes.backend.copier.EntityCopier;
import net.onelitefeather.vulpes.backend.domain.item.ItemRelation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Copies an {@link ItemEntity} within the same project or into another one, optionally
 * including its lore, flags, and enchantments.
 *
 * <p>Qualified {@code @Named("item")} — see {@link AttributeModelCopier}'s Javadoc for why.
 */
@Singleton
@Named("item")
public class ItemModelCopier extends AbstractRelationalModelCopier<ItemEntity, ItemRelation> implements EntityCopier<ItemEntity, ItemRelation> {

    private final ItemLoreRepository itemLoreRepository;
    private final ItemFlagRepository itemFlagRepository;
    private final ItemEnchantmentRepository itemEnchantmentRepository;

    @Inject
    public ItemModelCopier(
            ItemRepository itemRepository,
            ItemLoreRepository itemLoreRepository,
            ItemFlagRepository itemFlagRepository,
            ItemEnchantmentRepository itemEnchantmentRepository,
            ProjectRepository projectRepository
    ) {
        super(itemRepository, projectRepository, itemRepository::existsByProjectIdAndKey, "Item");
        this.itemLoreRepository = itemLoreRepository;
        this.itemFlagRepository = itemFlagRepository;
        this.itemEnchantmentRepository = itemEnchantmentRepository;
    }

    /**
     * Widens {@link AbstractRelationalModelCopier#copy} to {@code public} and wraps it in a real
     * transaction boundary. See the class Javadoc on {@link AbstractRelationalModelCopier} for
     * why {@code @Transactional} has to be declared here, on the concrete class, rather than on
     * the inherited method itself.
     */
    @Override
    @Transactional
    public ItemEntity copy(
            UUID sourceProjectId,
            UUID sourceId,
            @Nullable UUID targetProjectId,
            @Nullable String targetKey,
            Set<ItemRelation> relations
    ) {
        return super.copy(sourceProjectId, sourceId, targetProjectId, targetKey, relations);
    }

    @Override
    protected ItemEntity copyRoot(ItemEntity source, ProjectEntity targetProject, String targetKey) {
        return new ItemEntity(
                null,
                source.getUiName(),
                targetKey,
                source.getComment(),
                source.getDisplayName(),
                source.getMaterial(),
                source.getGroupName(),
                source.getCustomModelData(),
                source.getAmount(),
                List.of(),
                List.of(),
                List.of(),
                targetProject
        );
    }

    @Override
    protected void copyRelation(ItemRelation relation, ItemEntity source, ItemEntity target) {
        switch (relation) {
            case LORE -> copyLore(source, target);
            case FLAGS -> copyFlags(source, target);
            case ENCHANTMENTS -> copyEnchantments(source, target);
        }
    }

    private void copyLore(ItemEntity source, ItemEntity target) {
        List<ItemLoreEntity> sourceLore = itemLoreRepository.findLoreById(source.getId(), Pageable.unpaged()).getContent();
        List<ItemLoreEntity> copies = new ArrayList<>();
        for (int i = 0; i < sourceLore.size(); i++) {
            ItemLoreEntity copy = new ItemLoreEntity(null, sourceLore.get(i).getText(), i);
            copy.setItem(target);
            copies.add(copy);
        }
        itemLoreRepository.saveAll(copies);
    }

    private void copyFlags(ItemEntity source, ItemEntity target) {
        List<ItemFlagEntity> copies = new ArrayList<>();
        for (ItemFlagEntity flag : itemFlagRepository.findFlagsById(source.getId(), Pageable.unpaged()).getContent()) {
            ItemFlagEntity copy = new ItemFlagEntity(null, flag.getFlag());
            copy.setItem(target);
            copies.add(copy);
        }
        itemFlagRepository.saveAll(copies);
    }

    private void copyEnchantments(ItemEntity source, ItemEntity target) {
        List<ItemEnchantmentEntity> copies = new ArrayList<>();
        for (ItemEnchantmentEntity enchantment : itemEnchantmentRepository.findEnchantmentsById(source.getId(), Pageable.unpaged()).getContent()) {
            ItemEnchantmentEntity copy = new ItemEnchantmentEntity(null, enchantment.getName(), enchantment.getLevel(), enchantment.isUnsafe());
            copy.setItem(target);
            copies.add(copy);
        }
        itemEnchantmentRepository.saveAll(copies);
    }
}
