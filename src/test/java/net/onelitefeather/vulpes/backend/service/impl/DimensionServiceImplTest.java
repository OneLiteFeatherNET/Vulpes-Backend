package net.onelitefeather.vulpes.backend.service.impl;

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
import net.onelitefeather.vulpes.backend.domain.dimension.DimensionAttributeDTO;
import net.onelitefeather.vulpes.backend.domain.dimension.DimensionAttributeResponseDTO;
import net.onelitefeather.vulpes.backend.domain.dimension.DimensionTimelineDTO;
import net.onelitefeather.vulpes.backend.domain.dimension.DimensionTimelineResponseDTO;
import net.onelitefeather.vulpes.backend.domain.error.ErrorCode;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit tests for DimensionServiceImpl's attribute/timeline sub-resources")
class DimensionServiceImplTest {

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

    private static class FakeProjectRepository extends FakePageableRepository<ProjectEntity, UUID> implements ProjectRepository {
        FakeProjectRepository() {
            super(ProjectEntity::getId);
        }
    }

    private static class FakeDimensionTypeRepository extends FakePageableRepository<DimensionTypeEntity, UUID> implements DimensionTypeRepository {
        FakeDimensionTypeRepository() {
            super(DimensionTypeEntity::getId);
        }

        @Override
        public Page<DimensionTypeEntity> findByProjectId(UUID projectId, Pageable pageable) {
            List<DimensionTypeEntity> matching = store.values().stream()
                    .filter(e -> e.getProject().getId().equals(projectId))
                    .toList();
            return Page.of(matching, pageable, (long) matching.size());
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

    private FakeDimensionTypeRepository dimensionTypeRepository;
    private FakeDimensionAttributeRepository dimensionAttributeRepository;
    private FakeDimensionTimelineRepository dimensionTimelineRepository;
    private FakeProjectRepository projectRepository;
    private DimensionServiceImpl service;
    private ProjectEntity project;
    private DimensionTypeEntity dimensionA;
    private DimensionTypeEntity dimensionB;

    private static DimensionTypeEntity newDimensionType(UUID id, ProjectEntity project) {
        return new DimensionTypeEntity(
                id, "Overworld", "overworld", false, true, false, false,
                1.0, -64, 384, 384, "#minecraft:infiniburn_overworld", 0.0f,
                "constant:11", 15, Skybox.OVERWORLD, CardinalLight.DEFAULT, null,
                new ArrayList<>(), new ArrayList<>(), project
        );
    }

    @BeforeEach
    void setUp() {
        dimensionTypeRepository = new FakeDimensionTypeRepository();
        dimensionAttributeRepository = new FakeDimensionAttributeRepository();
        dimensionTimelineRepository = new FakeDimensionTimelineRepository();
        projectRepository = new FakeProjectRepository();
        service = new DimensionServiceImpl(
                dimensionTypeRepository, dimensionAttributeRepository, dimensionTimelineRepository, projectRepository
        );

        project = new ProjectEntity(UUID.randomUUID(), "Project A", "project-a", null, null, null, false);
        projectRepository.save(project);

        dimensionA = newDimensionType(UUID.randomUUID(), project);
        dimensionB = newDimensionType(UUID.randomUUID(), project);
        dimensionTypeRepository.save(dimensionA);
        dimensionTypeRepository.save(dimensionB);
    }

    @Test
    @DisplayName("createAttributeById() with a known dimension saves the attribute under it")
    void createAttributeById_knownDimension_savesAttribute() {
        DimensionAttributeDTO dto = new DimensionAttributeDTO(null, EnvironmentAttributeKey.FOG_COLOR, AttributeOperator.OVERRIDE, "#FFAA00");

        DimensionAttributeResponseDTO.DimensionAttributeDTO result = service.createAttributeById(dimensionA.getId(), dto);

        assertEquals(EnvironmentAttributeKey.FOG_COLOR, result.attributeKey());
        assertEquals(1, dimensionAttributeRepository.findAll().size());
    }

    @Test
    @DisplayName("createAttributeById() with an unknown dimension raises RESOURCE_NOT_FOUND")
    void createAttributeById_unknownDimension_raisesNotFound() {
        DimensionAttributeDTO dto = new DimensionAttributeDTO(null, EnvironmentAttributeKey.FOG_COLOR, AttributeOperator.OVERRIDE, "#FFAA00");
        UUID unknownDimension = UUID.randomUUID();

        ApiException exception = assertThrows(ApiException.class, () -> service.createAttributeById(unknownDimension, dto));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.code());
    }

    @Test
    @DisplayName("findAttributesById() only returns attributes belonging to the given dimension")
    void findAttributesById_scopesToDimension() {
        service.createAttributeById(dimensionA.getId(), new DimensionAttributeDTO(null, EnvironmentAttributeKey.FOG_COLOR, AttributeOperator.OVERRIDE, "#FFAA00"));
        service.createAttributeById(dimensionB.getId(), new DimensionAttributeDTO(null, EnvironmentAttributeKey.SKY_COLOR, AttributeOperator.OVERRIDE, "#00AAFF"));

        Page<DimensionAttributeResponseDTO.DimensionAttributeDTO> page = service.findAttributesById(dimensionA.getId(), Pageable.from(0, 10));

        assertEquals(1, page.getTotalSize());
        assertEquals(EnvironmentAttributeKey.FOG_COLOR, page.getContent().get(0).attributeKey());
    }

    @Test
    @DisplayName("updateAttributeById() on an attribute owned by a different dimension is answered as not-found")
    void updateAttributeById_crossDimension_raisesNotFound() {
        DimensionAttributeResponseDTO.DimensionAttributeDTO created = service.createAttributeById(
                dimensionA.getId(), new DimensionAttributeDTO(null, EnvironmentAttributeKey.FOG_COLOR, AttributeOperator.OVERRIDE, "#FFAA00")
        );
        DimensionAttributeDTO updateDto = new DimensionAttributeDTO(created.id(), EnvironmentAttributeKey.FOG_COLOR, AttributeOperator.ADD, "#000000");

        ApiException exception = assertThrows(ApiException.class, () -> service.updateAttributeById(dimensionB.getId(), updateDto));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.code());
    }

    @Test
    @DisplayName("deleteAttributeById() on an attribute owned by the matching dimension succeeds and removes it")
    void deleteAttributeById_sameDimension_succeeds() {
        DimensionAttributeResponseDTO.DimensionAttributeDTO created = service.createAttributeById(
                dimensionA.getId(), new DimensionAttributeDTO(null, EnvironmentAttributeKey.FOG_COLOR, AttributeOperator.OVERRIDE, "#FFAA00")
        );

        DimensionAttributeResponseDTO.DimensionAttributeDTO deleted = service.deleteAttributeById(dimensionA.getId(), created.id());

        assertEquals(created.id(), deleted.id());
        assertTrue(dimensionAttributeRepository.findById(created.id()).isEmpty());
    }

    @Test
    @DisplayName("deleteAllAttributesById() only deletes attributes belonging to the given dimension")
    void deleteAllAttributesById_scopesToDimension() {
        service.createAttributeById(dimensionA.getId(), new DimensionAttributeDTO(null, EnvironmentAttributeKey.FOG_COLOR, AttributeOperator.OVERRIDE, "#FFAA00"));
        DimensionAttributeResponseDTO.DimensionAttributeDTO bAttr = service.createAttributeById(
                dimensionB.getId(), new DimensionAttributeDTO(null, EnvironmentAttributeKey.SKY_COLOR, AttributeOperator.OVERRIDE, "#00AAFF")
        );

        service.deleteAllAttributesById(dimensionA.getId());

        assertTrue(dimensionAttributeRepository.findAttributesByDimensionTypeId(dimensionA.getId(), Pageable.unpaged()).getContent().isEmpty());
        assertTrue(dimensionAttributeRepository.findById(bAttr.id()).isPresent());
    }

    @Test
    @DisplayName("createTimelineById() with a known dimension saves the timeline reference under it")
    void createTimelineById_knownDimension_savesTimeline() {
        DimensionTimelineDTO dto = new DimensionTimelineDTO(null, "minestom:day_night");

        DimensionTimelineResponseDTO.DimensionTimelineDTO result = service.createTimelineById(dimensionA.getId(), dto);

        assertEquals("minestom:day_night", result.timelineKey());
        assertEquals(1, dimensionTimelineRepository.findAll().size());
    }

    @Test
    @DisplayName("createTimelineById() with an unknown dimension raises RESOURCE_NOT_FOUND")
    void createTimelineById_unknownDimension_raisesNotFound() {
        DimensionTimelineDTO dto = new DimensionTimelineDTO(null, "minestom:day_night");
        UUID unknownDimension = UUID.randomUUID();

        ApiException exception = assertThrows(ApiException.class, () -> service.createTimelineById(unknownDimension, dto));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.code());
    }

    @Test
    @DisplayName("deleteTimelineById() on a timeline owned by a different dimension is answered as not-found and keeps it")
    void deleteTimelineById_crossDimension_raisesNotFoundAndKeepsEntity() {
        DimensionTimelineResponseDTO.DimensionTimelineDTO created = service.createTimelineById(
                dimensionA.getId(), new DimensionTimelineDTO(null, "minestom:day_night")
        );

        ApiException exception = assertThrows(ApiException.class, () -> service.deleteTimelineById(dimensionB.getId(), created.id()));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.code());
        assertTrue(dimensionTimelineRepository.findById(created.id()).isPresent());
    }

    @Test
    @DisplayName("deleteAllTimelinesById() only deletes timelines belonging to the given dimension")
    void deleteAllTimelinesById_scopesToDimension() {
        service.createTimelineById(dimensionA.getId(), new DimensionTimelineDTO(null, "minestom:day_night"));
        DimensionTimelineResponseDTO.DimensionTimelineDTO bTimeline = service.createTimelineById(
                dimensionB.getId(), new DimensionTimelineDTO(null, "minestom:season")
        );

        service.deleteAllTimelinesById(dimensionA.getId());

        assertTrue(dimensionTimelineRepository.findTimelinesByDimensionTypeId(dimensionA.getId(), Pageable.unpaged()).getContent().isEmpty());
        assertTrue(dimensionTimelineRepository.findById(bTimeline.id()).isPresent());
    }
}
