package net.onelitefeather.vulpes.backend.service.copier;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import io.micronaut.data.repository.PageableRepository;
import net.onelitefeather.vulpes.api.model.dimension.AttributeOperator;
import net.onelitefeather.vulpes.api.model.dimension.CardinalLight;
import net.onelitefeather.vulpes.api.model.dimension.DimensionAttributeEntity;
import net.onelitefeather.vulpes.api.model.dimension.DimensionTimelineEntity;
import net.onelitefeather.vulpes.api.model.dimension.DimensionTypeEntity;
import net.onelitefeather.vulpes.api.model.dimension.EnvironmentAttributeKey;
import net.onelitefeather.vulpes.api.model.dimension.Skybox;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.api.repository.dimension.DimensionAttributeRepository;
import net.onelitefeather.vulpes.api.repository.dimension.DimensionTimelineRepository;
import net.onelitefeather.vulpes.api.repository.dimension.DimensionTypeRepository;
import net.onelitefeather.vulpes.backend.domain.dimension.DimensionRelation;
import net.onelitefeather.vulpes.backend.domain.error.ErrorCode;
import net.onelitefeather.vulpes.backend.exception.ApiException;
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

@DisplayName("Unit tests for DimensionModelCopier")
class DimensionModelCopierTest {

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

    private static class FakeDimensionTypeRepository extends FakePageableRepository<DimensionTypeEntity, UUID> implements DimensionTypeRepository {
        FakeDimensionTypeRepository() {
            super(DimensionTypeEntity::getId);
        }

        @Override
        public <S extends DimensionTypeEntity> S save(S entity) {
            if (entity.getId() == null) {
                entity.setId(UUID.randomUUID());
            }
            return super.save(entity);
        }

        @Override
        public Page<DimensionTypeEntity> findByProjectId(UUID projectId, Pageable pageable) {
            List<DimensionTypeEntity> matching = store.values().stream()
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

    private static class FakeDimensionAttributeRepository extends FakePageableRepository<DimensionAttributeEntity, UUID> implements DimensionAttributeRepository {
        FakeDimensionAttributeRepository() {
            super(DimensionAttributeEntity::getId);
        }

        @Override
        public <S extends DimensionAttributeEntity> S save(S entity) {
            if (entity.getId() == null) {
                entity.setId(UUID.randomUUID());
            }
            return super.save(entity);
        }

        @Override
        public Page<DimensionAttributeEntity> findAttributesByDimensionTypeId(UUID id, Pageable pageable) {
            List<DimensionAttributeEntity> matching = store.values().stream()
                    .filter(e -> e.getDimensionType().getId().equals(id))
                    .toList();
            return Page.of(matching, pageable, (long) matching.size());
        }
    }

    private static class FakeDimensionTimelineRepository extends FakePageableRepository<DimensionTimelineEntity, UUID> implements DimensionTimelineRepository {
        FakeDimensionTimelineRepository() {
            super(DimensionTimelineEntity::getId);
        }

        @Override
        public <S extends DimensionTimelineEntity> S save(S entity) {
            if (entity.getId() == null) {
                entity.setId(UUID.randomUUID());
            }
            return super.save(entity);
        }

        @Override
        public Page<DimensionTimelineEntity> findTimelinesByDimensionTypeId(UUID id, Pageable pageable) {
            List<DimensionTimelineEntity> matching = store.values().stream()
                    .filter(e -> e.getDimensionType().getId().equals(id))
                    .toList();
            return Page.of(matching, pageable, (long) matching.size());
        }
    }

    private static class FakeProjectRepository extends FakePageableRepository<ProjectEntity, UUID> implements ProjectRepository {
        FakeProjectRepository() {
            super(ProjectEntity::getId);
        }
    }

    private FakeDimensionTypeRepository dimensionTypeRepository;
    private FakeDimensionAttributeRepository dimensionAttributeRepository;
    private FakeDimensionTimelineRepository dimensionTimelineRepository;
    private FakeProjectRepository projectRepository;
    private DimensionModelCopier copier;
    private ProjectEntity projectA;
    private ProjectEntity projectB;

    @BeforeEach
    void setUp() {
        dimensionTypeRepository = new FakeDimensionTypeRepository();
        dimensionAttributeRepository = new FakeDimensionAttributeRepository();
        dimensionTimelineRepository = new FakeDimensionTimelineRepository();
        projectRepository = new FakeProjectRepository();
        copier = new DimensionModelCopier(dimensionTypeRepository, dimensionAttributeRepository, dimensionTimelineRepository, projectRepository);

        projectA = new ProjectEntity(UUID.randomUUID(), "Project A", "project-a", null, null, null, false);
        projectB = new ProjectEntity(UUID.randomUUID(), "Project B", "project-b", null, null, null, false);
        projectRepository.save(projectA);
        projectRepository.save(projectB);
    }

    private DimensionTypeEntity sampleDimension(String key, ProjectEntity project) {
        DimensionTypeEntity dimension = new DimensionTypeEntity(
                UUID.randomUUID(), "Overworld", key,
                true, false, true, true,
                1.5, -64, 384, 384,
                "#minecraft:infiniburn_overworld", 0.25f, "constant:11", 15,
                Skybox.OVERWORLD, CardinalLight.NETHER, "minecraft:default",
                List.of(), List.of(), project
        );
        dimensionTypeRepository.save(dimension);
        return dimension;
    }

    @Test
    @DisplayName("copy() into the same project with a new key succeeds and does not touch the source")
    void copy_sameProjectNewKey_succeeds() {
        DimensionTypeEntity source = sampleDimension("original-key", projectA);

        DimensionTypeEntity result = copier.copy(projectA.getId(), source.getId(), null, "copied-key", null, Set.of());

        assertNotEquals(source.getId(), result.getId());
        assertEquals("copied-key", result.getKey());
        assertEquals(projectA.getId(), result.getProject().getId());
        assertEquals(source.getUiName(), result.getUiName());
        DimensionTypeEntity stillThere = dimensionTypeRepository.findById(source.getId()).orElseThrow();
        assertEquals("original-key", stillThere.getKey());
        assertEquals(projectA.getId(), stillThere.getProject().getId());
    }

    @Test
    @DisplayName("copy() into another project with no targetKey keeps the source's key and leaves the source in its own project")
    void copy_otherProjectNoKey_keepsOriginalKey() {
        DimensionTypeEntity source = sampleDimension("shared-key", projectA);

        DimensionTypeEntity result = copier.copy(projectA.getId(), source.getId(), projectB.getId(), null, null, Set.of());

        assertEquals("shared-key", result.getKey());
        assertEquals(projectB.getId(), result.getProject().getId());
        DimensionTypeEntity stillThere = dimensionTypeRepository.findById(source.getId()).orElseThrow();
        assertEquals(projectA.getId(), stillThere.getProject().getId(), "the source must not have been moved into the target project");
        assertEquals("shared-key", stillThere.getKey());
    }

    @Test
    @DisplayName("copy() into the same project without a targetKey conflicts with the source's own key")
    void copy_sameProjectNoKey_conflictsWithSelf() {
        DimensionTypeEntity source = sampleDimension("only-key", projectA);

        ApiException exception = assertThrows(ApiException.class,
                () -> copier.copy(projectA.getId(), source.getId(), null, null, null, Set.of()));

        assertEquals(ErrorCode.RESOURCE_CONFLICT, exception.code());
    }

    @Test
    @DisplayName("copy() raises RESOURCE_CONFLICT when the target key is already taken in the target project")
    void copy_keyTaken_raisesConflict() {
        DimensionTypeEntity source = sampleDimension("key-a", projectA);
        sampleDimension("key-b", projectB);

        ApiException exception = assertThrows(ApiException.class,
                () -> copier.copy(projectA.getId(), source.getId(), projectB.getId(), "key-b", null, Set.of()));

        assertEquals(ErrorCode.RESOURCE_CONFLICT, exception.code());
    }

    @Test
    @DisplayName("copy() with an unknown targetProjectId raises PROJECT_NOT_FOUND")
    void copy_unknownTargetProject_raisesProjectNotFound() {
        DimensionTypeEntity source = sampleDimension("key-a", projectA);
        UUID unknownProject = UUID.randomUUID();

        ApiException exception = assertThrows(ApiException.class,
                () -> copier.copy(projectA.getId(), source.getId(), unknownProject, null, null, Set.of()));

        assertEquals(ErrorCode.PROJECT_NOT_FOUND, exception.code());
    }

    @Test
    @DisplayName("copy() when the source belongs to a different project than addressed raises RESOURCE_NOT_FOUND")
    void copy_sourceOwnedByDifferentProject_raisesNotFound() {
        DimensionTypeEntity source = sampleDimension("key-a", projectA);

        ApiException exception = assertThrows(ApiException.class,
                () -> copier.copy(projectB.getId(), source.getId(), null, "new-key", null, Set.of()));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.code());
    }

    @Test
    @DisplayName("copy() when the source id does not exist at all raises RESOURCE_NOT_FOUND")
    void copy_unknownSource_raisesNotFound() {
        UUID unknownId = UUID.randomUUID();

        ApiException exception = assertThrows(ApiException.class,
                () -> copier.copy(projectA.getId(), unknownId, null, null, null, Set.of()));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.code());
    }

    @Test
    @DisplayName("copy() copies every relevant scalar field through, including enums and the nullable default clock")
    void copy_copiesScalarFieldsThrough() {
        DimensionTypeEntity source = sampleDimension("scalar-item", projectA);

        DimensionTypeEntity result = copier.copy(projectA.getId(), source.getId(), null, "scalar-copy", null, Set.of());

        assertEquals(source.getUiName(), result.getUiName());
        assertEquals(source.isHasFixedTime(), result.isHasFixedTime());
        assertEquals(source.isHasSkylight(), result.isHasSkylight());
        assertEquals(source.isHasCeiling(), result.isHasCeiling());
        assertEquals(source.isHasEnderDragonFight(), result.isHasEnderDragonFight());
        assertEquals(source.getCoordinateScale(), result.getCoordinateScale());
        assertEquals(source.getMinY(), result.getMinY());
        assertEquals(source.getHeight(), result.getHeight());
        assertEquals(source.getLogicalHeight(), result.getLogicalHeight());
        assertEquals(source.getInfiniburn(), result.getInfiniburn());
        assertEquals(source.getAmbientLight(), result.getAmbientLight());
        assertEquals(source.getMonsterSpawnLightLevel(), result.getMonsterSpawnLightLevel());
        assertEquals(source.getMonsterSpawnBlockLightLimit(), result.getMonsterSpawnBlockLightLimit());
        assertEquals(Skybox.OVERWORLD, result.getSkybox());
        assertEquals(CardinalLight.NETHER, result.getCardinalLight());
        assertEquals("minecraft:default", result.getDefaultClock());
    }

    @Test
    @DisplayName("copy() copies a null defaultClock through as null")
    void copy_nullDefaultClock_staysNull() {
        DimensionTypeEntity source = new DimensionTypeEntity(
                UUID.randomUUID(), "Overworld", "no-clock-item",
                true, false, true, true,
                1.5, -64, 384, 384,
                "#minecraft:infiniburn_overworld", 0.25f, "constant:11", 15,
                Skybox.NONE, CardinalLight.DEFAULT, null,
                List.of(), List.of(), projectA
        );
        dimensionTypeRepository.save(source);

        DimensionTypeEntity result = copier.copy(projectA.getId(), source.getId(), null, "no-clock-copy", null, Set.of());

        assertNull(result.getDefaultClock());
    }

    @Test
    @DisplayName("copy() with ATTRIBUTES copies every attribute under a new id")
    void copy_withAttributesRelation_copiesAttributes() {
        DimensionTypeEntity source = sampleDimension("attributes-item", projectA);
        DimensionAttributeEntity attribute = new DimensionAttributeEntity(
                UUID.randomUUID(), EnvironmentAttributeKey.FOG_COLOR, AttributeOperator.OVERRIDE, "0xFF00FF"
        );
        attribute.setDimensionType(source);
        dimensionAttributeRepository.save(attribute);

        DimensionTypeEntity result = copier.copy(projectA.getId(), source.getId(), null, "attributes-copy", null, Set.of(DimensionRelation.ATTRIBUTES));

        List<DimensionAttributeEntity> copiedAttributes = dimensionAttributeRepository.findAttributesByDimensionTypeId(result.getId(), Pageable.unpaged()).getContent();
        assertEquals(1, copiedAttributes.size());
        assertEquals(EnvironmentAttributeKey.FOG_COLOR, copiedAttributes.get(0).getAttributeKey());
        assertEquals(AttributeOperator.OVERRIDE, copiedAttributes.get(0).getOperator());
        assertEquals("0xFF00FF", copiedAttributes.get(0).getAttributeValue());
        assertNotEquals(attribute.getId(), copiedAttributes.get(0).getId());
        assertEquals(result.getId(), copiedAttributes.get(0).getDimensionType().getId());
    }

    @Test
    @DisplayName("copy() with TIMELINES copies every timeline reference under a new id")
    void copy_withTimelinesRelation_copiesTimelines() {
        DimensionTypeEntity source = sampleDimension("timelines-item", projectA);
        DimensionTimelineEntity timeline = new DimensionTimelineEntity(UUID.randomUUID(), "minecraft:day_night_cycle");
        timeline.setDimensionType(source);
        dimensionTimelineRepository.save(timeline);

        DimensionTypeEntity result = copier.copy(projectA.getId(), source.getId(), null, "timelines-copy", null, Set.of(DimensionRelation.TIMELINES));

        List<DimensionTimelineEntity> copiedTimelines = dimensionTimelineRepository.findTimelinesByDimensionTypeId(result.getId(), Pageable.unpaged()).getContent();
        assertEquals(1, copiedTimelines.size());
        assertEquals("minecraft:day_night_cycle", copiedTimelines.get(0).getTimelineKey());
        assertNotEquals(timeline.getId(), copiedTimelines.get(0).getId());
        assertEquals(result.getId(), copiedTimelines.get(0).getDimensionType().getId());
    }

    @Test
    @DisplayName("copy() with both relations copies both")
    void copy_withAllRelations_copiesAll() {
        DimensionTypeEntity source = sampleDimension("full-item", projectA);
        DimensionAttributeEntity attribute = new DimensionAttributeEntity(
                UUID.randomUUID(), EnvironmentAttributeKey.SKY_COLOR, AttributeOperator.ADD, "0x123456"
        );
        attribute.setDimensionType(source);
        dimensionAttributeRepository.save(attribute);
        DimensionTimelineEntity timeline = new DimensionTimelineEntity(UUID.randomUUID(), "minecraft:weather_cycle");
        timeline.setDimensionType(source);
        dimensionTimelineRepository.save(timeline);

        DimensionTypeEntity result = copier.copy(
                projectA.getId(), source.getId(), null, "full-copy", null,
                EnumSet.allOf(DimensionRelation.class)
        );

        assertEquals(1, dimensionAttributeRepository.findAttributesByDimensionTypeId(result.getId(), Pageable.unpaged()).getContent().size());
        assertEquals(1, dimensionTimelineRepository.findTimelinesByDimensionTypeId(result.getId(), Pageable.unpaged()).getContent().size());
    }

    @Test
    @DisplayName("copy() with an empty relations set copies only the root")
    void copy_withNoRelations_copiesOnlyRoot() {
        DimensionTypeEntity source = sampleDimension("bare-item", projectA);
        DimensionAttributeEntity attribute = new DimensionAttributeEntity(
                UUID.randomUUID(), EnvironmentAttributeKey.CLOUD_COLOR, AttributeOperator.OVERRIDE, "0xFFFFFF"
        );
        attribute.setDimensionType(source);
        dimensionAttributeRepository.save(attribute);
        DimensionTimelineEntity timeline = new DimensionTimelineEntity(UUID.randomUUID(), "minecraft:day_night_cycle");
        timeline.setDimensionType(source);
        dimensionTimelineRepository.save(timeline);

        DimensionTypeEntity result = copier.copy(projectA.getId(), source.getId(), null, "bare-copy", null, Set.of());

        assertTrue(dimensionAttributeRepository.findAttributesByDimensionTypeId(result.getId(), Pageable.unpaged()).getContent().isEmpty());
        assertTrue(dimensionTimelineRepository.findTimelinesByDimensionTypeId(result.getId(), Pageable.unpaged()).getContent().isEmpty());
    }

    @Test
    @DisplayName("copy() with only ATTRIBUTES requested does not also copy TIMELINES")
    void copy_onlyAttributes_doesNotCopyTimelines() {
        DimensionTypeEntity source = sampleDimension("attributes-only-item", projectA);
        DimensionAttributeEntity attribute = new DimensionAttributeEntity(
                UUID.randomUUID(), EnvironmentAttributeKey.MOON_ANGLE, AttributeOperator.MULTIPLY, "2"
        );
        attribute.setDimensionType(source);
        dimensionAttributeRepository.save(attribute);
        DimensionTimelineEntity timeline = new DimensionTimelineEntity(UUID.randomUUID(), "minecraft:day_night_cycle");
        timeline.setDimensionType(source);
        dimensionTimelineRepository.save(timeline);

        DimensionTypeEntity result = copier.copy(projectA.getId(), source.getId(), null, "attributes-only-copy", null, Set.of(DimensionRelation.ATTRIBUTES));

        assertEquals(1, dimensionAttributeRepository.findAttributesByDimensionTypeId(result.getId(), Pageable.unpaged()).getContent().size());
        assertTrue(dimensionTimelineRepository.findTimelinesByDimensionTypeId(result.getId(), Pageable.unpaged()).getContent().isEmpty());
    }

    @Test
    @DisplayName("copy() with only TIMELINES requested does not also copy ATTRIBUTES")
    void copy_onlyTimelines_doesNotCopyAttributes() {
        DimensionTypeEntity source = sampleDimension("timelines-only-item", projectA);
        DimensionAttributeEntity attribute = new DimensionAttributeEntity(
                UUID.randomUUID(), EnvironmentAttributeKey.STAR_BRIGHTNESS, AttributeOperator.MINIMUM, "0.1"
        );
        attribute.setDimensionType(source);
        dimensionAttributeRepository.save(attribute);
        DimensionTimelineEntity timeline = new DimensionTimelineEntity(UUID.randomUUID(), "minecraft:weather_cycle");
        timeline.setDimensionType(source);
        dimensionTimelineRepository.save(timeline);

        DimensionTypeEntity result = copier.copy(projectA.getId(), source.getId(), null, "timelines-only-copy", null, Set.of(DimensionRelation.TIMELINES));

        assertEquals(1, dimensionTimelineRepository.findTimelinesByDimensionTypeId(result.getId(), Pageable.unpaged()).getContent().size());
        assertTrue(dimensionAttributeRepository.findAttributesByDimensionTypeId(result.getId(), Pageable.unpaged()).getContent().isEmpty());
    }

    @Test
    @DisplayName("copy() with relations does not move the source's own children onto the target")
    void copy_relationCopy_doesNotMutateSourceChildren() {
        DimensionTypeEntity source = sampleDimension("guarded-item", projectA);
        DimensionAttributeEntity attribute = new DimensionAttributeEntity(
                UUID.randomUUID(), EnvironmentAttributeKey.WATER_FOG_COLOR, AttributeOperator.BLEND_TO_GRAY, "0.5"
        );
        attribute.setDimensionType(source);
        dimensionAttributeRepository.save(attribute);

        DimensionTypeEntity result = copier.copy(projectA.getId(), source.getId(), null, "guarded-copy", null, Set.of(DimensionRelation.ATTRIBUTES));

        List<DimensionAttributeEntity> sourceAttributesAfter = dimensionAttributeRepository.findAttributesByDimensionTypeId(source.getId(), Pageable.unpaged()).getContent();
        assertEquals(1, sourceAttributesAfter.size(), "the source's own attribute must still be attached to the source");
        assertEquals(attribute.getId(), sourceAttributesAfter.get(0).getId());
        assertEquals(source.getId(), sourceAttributesAfter.get(0).getDimensionType().getId());
        List<DimensionAttributeEntity> targetAttributes = dimensionAttributeRepository.findAttributesByDimensionTypeId(result.getId(), Pageable.unpaged()).getContent();
        assertEquals(1, targetAttributes.size());
        assertNotEquals(attribute.getId(), targetAttributes.get(0).getId());
    }

    @Test
    @DisplayName("copy() with a targetName uses it instead of the source's uiName")
    void copy_withTargetName_overridesUiName() {
        DimensionTypeEntity source = sampleDimension("name-item", projectA);

        DimensionTypeEntity result = copier.copy(projectA.getId(), source.getId(), null, "name-copy", "New UI", Set.of());

        assertEquals("New UI", result.getUiName());
        assertEquals("Overworld", source.getUiName(), "the source must not have been mutated");
    }

    @Test
    @DisplayName("copy() with a blank targetName keeps the source's uiName")
    void copy_blankTargetName_keepsSourceUiName() {
        DimensionTypeEntity source = sampleDimension("blank-name-item", projectA);

        DimensionTypeEntity result = copier.copy(projectA.getId(), source.getId(), null, "blank-name-copy", "  ", Set.of());

        assertEquals("Overworld", result.getUiName());
    }
}
