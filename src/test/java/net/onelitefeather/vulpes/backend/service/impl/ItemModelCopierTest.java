package net.onelitefeather.vulpes.backend.service.impl;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import io.micronaut.data.repository.PageableRepository;
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
import net.onelitefeather.vulpes.backend.domain.error.ErrorCode;
import net.onelitefeather.vulpes.backend.domain.item.ItemRelation;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.service.copier.ItemModelCopier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit tests for ItemModelCopier")
class ItemModelCopierTest {

    /**
     * Minimal in-memory {@link PageableRepository} fake — no database, no Micronaut context.
     */
    private static class FakePageableRepository<E, ID> implements PageableRepository<E, ID> {
        final Map<ID, E> store = new LinkedHashMap<>();
        final Function<E, ID> idOf;

        FakePageableRepository(Function<E, ID> idOf) {
            this.idOf = idOf;
        }

        @Override
        public <S extends E> S save(S entity) {
            store.put(idOf.apply(entity), entity);
            return entity;
        }

        @Override
        public <S extends E> List<S> saveAll(Iterable<S> entities) {
            List<S> list = new ArrayList<>();
            for (S entity : entities) {
                list.add(save(entity));
            }
            return list;
        }

        @Override
        public <S extends E> S insert(S entity) {
            return save(entity);
        }

        @Override
        public <S extends E> List<S> insertAll(Iterable<S> entities) {
            return saveAll(entities);
        }

        @Override
        public Optional<E> findById(ID id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public boolean existsById(ID id) {
            return store.containsKey(id);
        }

        @Override
        public List<E> findAll() {
            return new ArrayList<>(store.values());
        }

        @Override
        public long count() {
            return store.size();
        }

        @Override
        public <S extends E> S update(S entity) {
            store.put(idOf.apply(entity), entity);
            return entity;
        }

        @Override
        public <S extends E> List<S> updateAll(Iterable<S> entities) {
            List<S> list = new ArrayList<>();
            for (S entity : entities) {
                list.add(update(entity));
            }
            return list;
        }

        @Override
        public void deleteById(ID id) {
            store.remove(id);
        }

        @Override
        public void delete(E entity) {
            store.remove(idOf.apply(entity));
        }

        @Override
        public void deleteAll(Iterable<? extends E> entities) {
            entities.forEach(e -> store.remove(idOf.apply(e)));
        }

        @Override
        public void deleteAll() {
            store.clear();
        }

        @Override
        public List<E> findAll(Sort sort) {
            return findAll();
        }

        @Override
        public Page<E> findAll(Pageable pageable) {
            List<E> all = new ArrayList<>(store.values());
            return Page.of(all, pageable, (long) all.size());
        }
    }

    private static class FakeItemRepository extends FakePageableRepository<ItemEntity, UUID> implements ItemRepository {
        FakeItemRepository() {
            super(ItemEntity::getId);
        }

        @Override
        public <S extends ItemEntity> S save(S entity) {
            if (entity.getId() == null) {
                entity.setId(UUID.randomUUID());
            }
            return super.save(entity);
        }

        @Override
        public List<ItemEntity> findAllWithFetches(Pageable pageable) {
            return findAll();
        }

        @Override
        public Page<ItemEntity> findByProjectId(UUID projectId, Pageable pageable) {
            List<ItemEntity> matching = store.values().stream()
                    .filter(e -> e.getProject().getId().equals(projectId))
                    .toList();
            return Page.of(matching, pageable, (long) matching.size());
        }

        @Override
        public boolean existsByProjectIdAndKey(UUID projectId, String key) {
            return store.values().stream()
                    .anyMatch(e -> e.getProject().getId().equals(projectId) && e.getKey().equals(key));
        }
    }

    private static class FakeItemLoreRepository extends FakePageableRepository<ItemLoreEntity, UUID> implements ItemLoreRepository {
        FakeItemLoreRepository() {
            super(ItemLoreEntity::getId);
        }

        @Override
        public <S extends ItemLoreEntity> S save(S entity) {
            if (entity.getId() == null) {
                entity.setId(UUID.randomUUID());
            }
            return super.save(entity);
        }

        @Override
        public Page<ItemLoreEntity> findLoreById(UUID id, Pageable pageable) {
            List<ItemLoreEntity> matching = store.values().stream()
                    .filter(e -> e.getItem().getId().equals(id))
                    .sorted((a, b) -> Integer.compare(a.getOrderIndex(), b.getOrderIndex()))
                    .toList();
            return Page.of(matching, pageable, (long) matching.size());
        }
    }

    private static class FakeItemFlagRepository extends FakePageableRepository<ItemFlagEntity, UUID> implements ItemFlagRepository {
        FakeItemFlagRepository() {
            super(ItemFlagEntity::getId);
        }

        @Override
        public <S extends ItemFlagEntity> S save(S entity) {
            if (entity.getId() == null) {
                entity.setId(UUID.randomUUID());
            }
            return super.save(entity);
        }

        @Override
        public Page<ItemFlagEntity> findFlagsById(UUID id, Pageable pageable) {
            List<ItemFlagEntity> matching = store.values().stream()
                    .filter(e -> e.getItem().getId().equals(id))
                    .toList();
            return Page.of(matching, pageable, (long) matching.size());
        }
    }

    private static class FakeItemEnchantmentRepository extends FakePageableRepository<ItemEnchantmentEntity, UUID> implements ItemEnchantmentRepository {
        FakeItemEnchantmentRepository() {
            super(ItemEnchantmentEntity::getId);
        }

        @Override
        public <S extends ItemEnchantmentEntity> S save(S entity) {
            if (entity.getId() == null) {
                entity.setId(UUID.randomUUID());
            }
            return super.save(entity);
        }

        @Override
        public Page<ItemEnchantmentEntity> findEnchantmentsById(UUID id, Pageable pageable) {
            List<ItemEnchantmentEntity> matching = store.values().stream()
                    .filter(e -> e.getItem().getId().equals(id))
                    .toList();
            return Page.of(matching, pageable, (long) matching.size());
        }
    }

    private static class FakeProjectRepository extends FakePageableRepository<ProjectEntity, UUID> implements ProjectRepository {
        FakeProjectRepository() {
            super(ProjectEntity::getId);
        }
    }

    private FakeItemRepository itemRepository;
    private FakeItemLoreRepository itemLoreRepository;
    private FakeItemFlagRepository itemFlagRepository;
    private FakeItemEnchantmentRepository itemEnchantmentRepository;
    private FakeProjectRepository projectRepository;
    private ItemModelCopier copier;
    private ProjectEntity projectA;
    private ProjectEntity projectB;

    @BeforeEach
    void setUp() {
        itemRepository = new FakeItemRepository();
        itemLoreRepository = new FakeItemLoreRepository();
        itemFlagRepository = new FakeItemFlagRepository();
        itemEnchantmentRepository = new FakeItemEnchantmentRepository();
        projectRepository = new FakeProjectRepository();
        copier = new ItemModelCopier(itemRepository, itemLoreRepository, itemFlagRepository, itemEnchantmentRepository, projectRepository);

        projectA = new ProjectEntity(UUID.randomUUID(), "Project A", "project-a", null, null, null, false);
        projectB = new ProjectEntity(UUID.randomUUID(), "Project B", "project-b", null, null, null, false);
        projectRepository.save(projectA);
        projectRepository.save(projectB);
    }

    private ItemEntity sampleItem(String key, ProjectEntity project) {
        ItemEntity item = new ItemEntity(
                UUID.randomUUID(), "UI", key, "comment", "display", "STONE", "group", 0, 1,
                List.of(), List.of(), List.of(), project
        );
        itemRepository.save(item);
        return item;
    }

    @Test
    @DisplayName("copy() into the same project with a new key succeeds and does not touch the source")
    void copy_sameProjectNewKey_succeeds() {
        ItemEntity source = sampleItem("original-key", projectA);

        ItemEntity result = copier.copy(projectA.getId(), source.getId(), null, "copied-key", Set.of());

        assertNotEquals(source.getId(), result.getId());
        assertEquals("copied-key", result.getKey());
        assertEquals(projectA.getId(), result.getProject().getId());
        assertEquals(source.getUiName(), result.getUiName());
        ItemEntity stillThere = itemRepository.findById(source.getId()).orElseThrow();
        assertEquals("original-key", stillThere.getKey());
        assertEquals(projectA.getId(), stillThere.getProject().getId());
    }

    @Test
    @DisplayName("copy() into another project with no targetKey keeps the source's key and leaves the source in its own project")
    void copy_otherProjectNoKey_keepsOriginalKey() {
        ItemEntity source = sampleItem("shared-key", projectA);

        ItemEntity result = copier.copy(projectA.getId(), source.getId(), projectB.getId(), null, Set.of());

        assertEquals("shared-key", result.getKey());
        assertEquals(projectB.getId(), result.getProject().getId());
        ItemEntity stillThere = itemRepository.findById(source.getId()).orElseThrow();
        assertEquals(projectA.getId(), stillThere.getProject().getId(), "the source must not have been moved into the target project");
        assertEquals("shared-key", stillThere.getKey());
    }

    @Test
    @DisplayName("copy() into the same project without a targetKey conflicts with the source's own key")
    void copy_sameProjectNoKey_conflictsWithSelf() {
        ItemEntity source = sampleItem("only-key", projectA);

        ApiException exception = assertThrows(ApiException.class,
                () -> copier.copy(projectA.getId(), source.getId(), null, null, Set.of()));

        assertEquals(ErrorCode.RESOURCE_CONFLICT, exception.code());
    }

    @Test
    @DisplayName("copy() raises RESOURCE_CONFLICT when the target key is already taken in the target project")
    void copy_keyTaken_raisesConflict() {
        ItemEntity source = sampleItem("key-a", projectA);
        sampleItem("key-b", projectB);

        ApiException exception = assertThrows(ApiException.class,
                () -> copier.copy(projectA.getId(), source.getId(), projectB.getId(), "key-b", Set.of()));

        assertEquals(ErrorCode.RESOURCE_CONFLICT, exception.code());
    }

    @Test
    @DisplayName("copy() with an unknown targetProjectId raises PROJECT_NOT_FOUND")
    void copy_unknownTargetProject_raisesProjectNotFound() {
        ItemEntity source = sampleItem("key-a", projectA);
        UUID unknownProject = UUID.randomUUID();

        ApiException exception = assertThrows(ApiException.class,
                () -> copier.copy(projectA.getId(), source.getId(), unknownProject, null, Set.of()));

        assertEquals(ErrorCode.PROJECT_NOT_FOUND, exception.code());
    }

    @Test
    @DisplayName("copy() when the source belongs to a different project than addressed raises RESOURCE_NOT_FOUND")
    void copy_sourceOwnedByDifferentProject_raisesNotFound() {
        ItemEntity source = sampleItem("key-a", projectA);

        ApiException exception = assertThrows(ApiException.class,
                () -> copier.copy(projectB.getId(), source.getId(), null, "new-key", Set.of()));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.code());
    }

    @Test
    @DisplayName("copy() when the source id does not exist at all raises RESOURCE_NOT_FOUND")
    void copy_unknownSource_raisesNotFound() {
        UUID unknownId = UUID.randomUUID();

        ApiException exception = assertThrows(ApiException.class,
                () -> copier.copy(projectA.getId(), unknownId, null, null, Set.of()));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.code());
    }

    @Test
    @DisplayName("copy() with LORE copies lore lines in order under new ids")
    void copy_withLoreRelation_copiesLoreInOrder() {
        ItemEntity source = sampleItem("lore-item", projectA);
        List<ItemLoreEntity> lore = List.of(
                new ItemLoreEntity(UUID.randomUUID(), "first", 0),
                new ItemLoreEntity(UUID.randomUUID(), "second", 1),
                new ItemLoreEntity(UUID.randomUUID(), "third", 2)
        );
        lore.forEach(l -> {
            l.setItem(source);
            itemLoreRepository.save(l);
        });

        ItemEntity result = copier.copy(projectA.getId(), source.getId(), null, "lore-copy", Set.of(ItemRelation.LORE));

        List<ItemLoreEntity> copiedLore = itemLoreRepository.findLoreById(result.getId(), Pageable.unpaged()).getContent();
        assertEquals(3, copiedLore.size());
        assertEquals("first", copiedLore.get(0).getText());
        assertEquals(0, copiedLore.get(0).getOrderIndex());
        assertEquals("second", copiedLore.get(1).getText());
        assertEquals(1, copiedLore.get(1).getOrderIndex());
        assertEquals("third", copiedLore.get(2).getText());
        assertEquals(2, copiedLore.get(2).getOrderIndex());
        assertTrue(copiedLore.stream().noneMatch(l -> lore.stream().anyMatch(orig -> orig.getId().equals(l.getId()))),
                "copied lore must have new ids, not the source's");
    }

    @Test
    @DisplayName("copy() with FLAGS copies every flag under a new id")
    void copy_withFlagsRelation_copiesFlags() {
        ItemEntity source = sampleItem("flags-item", projectA);
        ItemFlagEntity flag = new ItemFlagEntity(UUID.randomUUID(), "HIDE_ATTRIBUTES");
        flag.setItem(source);
        itemFlagRepository.save(flag);

        ItemEntity result = copier.copy(projectA.getId(), source.getId(), null, "flags-copy", Set.of(ItemRelation.FLAGS));

        List<ItemFlagEntity> copiedFlags = itemFlagRepository.findFlagsById(result.getId(), Pageable.unpaged()).getContent();
        assertEquals(1, copiedFlags.size());
        assertEquals("HIDE_ATTRIBUTES", copiedFlags.get(0).getFlag());
        assertNotEquals(flag.getId(), copiedFlags.get(0).getId());
    }

    @Test
    @DisplayName("copy() with ENCHANTMENTS copies every enchantment under a new id")
    void copy_withEnchantmentsRelation_copiesEnchantments() {
        ItemEntity source = sampleItem("enchant-item", projectA);
        ItemEnchantmentEntity enchantment = new ItemEnchantmentEntity(UUID.randomUUID(), "SHARPNESS", (short) 5, false);
        enchantment.setItem(source);
        itemEnchantmentRepository.save(enchantment);

        ItemEntity result = copier.copy(projectA.getId(), source.getId(), null, "enchant-copy", Set.of(ItemRelation.ENCHANTMENTS));

        List<ItemEnchantmentEntity> copiedEnchantments = itemEnchantmentRepository.findEnchantmentsById(result.getId(), Pageable.unpaged()).getContent();
        assertEquals(1, copiedEnchantments.size());
        assertEquals("SHARPNESS", copiedEnchantments.get(0).getName());
        assertEquals((short) 5, copiedEnchantments.get(0).getLevel());
        assertFalse(copiedEnchantments.get(0).isUnsafe());
        assertNotEquals(enchantment.getId(), copiedEnchantments.get(0).getId());
    }

    @Test
    @DisplayName("copy() with all three relations copies all three")
    void copy_withAllRelations_copiesAll() {
        ItemEntity source = sampleItem("full-item", projectA);
        ItemLoreEntity lore = new ItemLoreEntity(UUID.randomUUID(), "only line", 0);
        lore.setItem(source);
        itemLoreRepository.save(lore);
        ItemFlagEntity flag = new ItemFlagEntity(UUID.randomUUID(), "UNBREAKABLE");
        flag.setItem(source);
        itemFlagRepository.save(flag);
        ItemEnchantmentEntity enchantment = new ItemEnchantmentEntity(UUID.randomUUID(), "MENDING", (short) 1, false);
        enchantment.setItem(source);
        itemEnchantmentRepository.save(enchantment);

        ItemEntity result = copier.copy(
                projectA.getId(), source.getId(), null, "full-copy",
                EnumSet.allOf(ItemRelation.class)
        );

        assertEquals(1, itemLoreRepository.findLoreById(result.getId(), Pageable.unpaged()).getContent().size());
        assertEquals(1, itemFlagRepository.findFlagsById(result.getId(), Pageable.unpaged()).getContent().size());
        assertEquals(1, itemEnchantmentRepository.findEnchantmentsById(result.getId(), Pageable.unpaged()).getContent().size());
    }

    @Test
    @DisplayName("copy() with an empty relations set copies only the root")
    void copy_withNoRelations_copiesOnlyRoot() {
        ItemEntity source = sampleItem("bare-item", projectA);
        ItemLoreEntity lore = new ItemLoreEntity(UUID.randomUUID(), "line", 0);
        lore.setItem(source);
        itemLoreRepository.save(lore);

        ItemEntity result = copier.copy(projectA.getId(), source.getId(), null, "bare-copy", Set.of());

        assertTrue(itemLoreRepository.findLoreById(result.getId(), Pageable.unpaged()).getContent().isEmpty());
    }

    @Test
    @DisplayName("copy() with relations does not move the source's own children onto the target")
    void copy_relationCopy_doesNotMutateSourceChildren() {
        ItemEntity source = sampleItem("guarded-item", projectA);
        ItemFlagEntity flag = new ItemFlagEntity(UUID.randomUUID(), "GLOWING");
        flag.setItem(source);
        itemFlagRepository.save(flag);

        ItemEntity result = copier.copy(projectA.getId(), source.getId(), null, "guarded-copy", Set.of(ItemRelation.FLAGS));

        List<ItemFlagEntity> sourceFlagsAfter = itemFlagRepository.findFlagsById(source.getId(), Pageable.unpaged()).getContent();
        assertEquals(1, sourceFlagsAfter.size(), "the source's own flag must still be attached to the source");
        assertEquals(flag.getId(), sourceFlagsAfter.get(0).getId());
        assertEquals(source.getId(), sourceFlagsAfter.get(0).getItem().getId());
        List<ItemFlagEntity> targetFlags = itemFlagRepository.findFlagsById(result.getId(), Pageable.unpaged()).getContent();
        assertEquals(1, targetFlags.size());
        assertNotEquals(flag.getId(), targetFlags.get(0).getId());
    }
}
