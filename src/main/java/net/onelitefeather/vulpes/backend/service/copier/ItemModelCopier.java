package net.onelitefeather.vulpes.backend.service.copier;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.model.Pageable;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import net.onelitefeather.vulpes.api.model.ItemEntity;
import net.onelitefeather.vulpes.api.model.item.ItemComponentEntity;
import net.onelitefeather.vulpes.api.model.item.ItemEnchantmentEntity;
import net.onelitefeather.vulpes.api.model.item.ItemLoreEntity;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.api.repository.ItemRepository;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.api.repository.item.ItemComponentRepository;
import net.onelitefeather.vulpes.api.repository.item.ItemEnchantmentRepository;
import net.onelitefeather.vulpes.api.repository.item.ItemLoreRepository;
import net.onelitefeather.vulpes.backend.copier.EntityCopier;
import net.onelitefeather.vulpes.backend.domain.item.ItemRelation;
import net.onelitefeather.vulpes.backend.service.item.ItemComponentRules;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Copies an {@link ItemEntity} within the same project or into another one, optionally
 * including its lore, enchantments and data components; the required components are always copied.
 *
 * <p>Qualified {@code @Named("item")} — see {@link AttributeModelCopier}'s Javadoc for why.
 */
@Singleton
@Named("item")
public class ItemModelCopier extends AbstractRelationalModelCopier<ItemEntity, ItemRelation> implements EntityCopier<ItemEntity, ItemRelation> {

    private final ItemLoreRepository itemLoreRepository;
    private final ItemEnchantmentRepository itemEnchantmentRepository;
    private final ItemComponentRepository itemComponentRepository;
    private final ItemComponentRules componentRules;

    @Inject
    public ItemModelCopier(
            ItemRepository itemRepository,
            ItemLoreRepository itemLoreRepository,
            ItemEnchantmentRepository itemEnchantmentRepository,
            ItemComponentRepository itemComponentRepository,
            ItemComponentRules componentRules,
            ProjectRepository projectRepository
    ) {
        super(itemRepository, projectRepository, itemRepository::existsByProjectIdAndKey, "Item");
        this.itemLoreRepository = itemLoreRepository;
        this.itemEnchantmentRepository = itemEnchantmentRepository;
        this.itemComponentRepository = itemComponentRepository;
        this.componentRules = componentRules;
    }

    /**
     * Copies the item without its optional relations. Unlike the root only copy of the base class, the
     * required components are copied too, so the copy has a material.
     */
    @Override
    @Transactional
    public ItemEntity copy(
            UUID sourceProjectId,
            UUID sourceId,
            @Nullable UUID targetProjectId,
            @Nullable String targetKey,
            @Nullable String targetName
    ) {
        return copy(sourceProjectId, sourceId, targetProjectId, targetKey, targetName, Set.of());
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
            @Nullable String targetName,
            Set<ItemRelation> relations
    ) {
        return super.copy(sourceProjectId, sourceId, targetProjectId, targetKey, targetName, relations);
    }

    @Override
    protected ItemEntity copyRoot(ItemEntity source, ProjectEntity targetProject, String targetKey, @Nullable String targetName) {
        String resolvedName = (targetName != null && !targetName.isBlank()) ? targetName : source.getUiName();
        return new ItemEntity(
                null,
                resolvedName,
                targetKey,
                source.getComment(),
                source.getGroupName(),
                List.of(),
                List.of(),
                targetProject
        );
    }

    @Override
    protected void copyRelation(ItemRelation relation, ItemEntity source, ItemEntity target) {
        switch (relation) {
            case LORE -> copyLore(source, target);
            case ENCHANTMENTS -> copyEnchantments(source, target);
            case COMPONENTS -> copyComponents(source, target, key -> true);
        }
    }

    /**
     * Copies the required components, e.g. the material, unless all components were copied already.
     */
    @Override
    protected void copyAlways(ItemEntity source, ItemEntity target, Set<ItemRelation> relations) {
        if (!relations.contains(ItemRelation.COMPONENTS)) {
            copyComponents(source, target, componentRules::isRequired);
        }
    }

    private void copyComponents(ItemEntity source, ItemEntity target, Predicate<String> keys) {
        List<ItemComponentEntity> copies = new ArrayList<>();
        for (ItemComponentEntity component : itemComponentRepository.findComponentsById(source.getId(), Pageable.unpaged()).getContent()) {
            if (!keys.test(component.getComponentKey())) continue;
            ItemComponentEntity copy = new ItemComponentEntity(null, component.getComponentKey(), component.getComponentValue());
            copy.setItem(target);
            copies.add(copy);
        }
        itemComponentRepository.saveAll(copies);
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
