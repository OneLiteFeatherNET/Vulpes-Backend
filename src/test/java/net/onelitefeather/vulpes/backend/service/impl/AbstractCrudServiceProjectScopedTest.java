package net.onelitefeather.vulpes.backend.service.impl;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import io.micronaut.data.repository.PageableRepository;
import net.onelitefeather.vulpes.api.model.AttributeEntity;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.backend.domain.attribute.AttributeModelDTO;
import net.onelitefeather.vulpes.backend.domain.attribute.AttributeModelResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit tests for AbstractCrudService's project-scoped overloads")
class AbstractCrudServiceProjectScopedTest {

    /**
     * Minimal in-memory {@link PageableRepository} fake, keyed by a caller-supplied id extractor.
     * No database, no Micronaut context — just enough surface for AbstractCrudService to drive.
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

    private static class TestAttributeService
            extends AbstractCrudService<AttributeEntity, UUID, AttributeModelDTO, AttributeModelResponseDTO, AttributeModelResponseDTO.AttributeModelDTO> {

        TestAttributeService(
                PageableRepository<AttributeEntity, UUID> repository,
                ProjectRepository projectRepository,
                BiFunction<UUID, Pageable, Page<AttributeEntity>> findByProjectFn
        ) {
            super(
                    repository,
                    projectRepository,
                    AttributeModelDTO::toAttributeModel,
                    AttributeModelResponseDTO.AttributeModelDTO::create,
                    e -> e.getProject().getId(),
                    findByProjectFn,
                    AttributeModelDTO::id,
                    AttributeModelResponseDTO.AttributeModelErrorDTO::new,
                    "Attribute"
            );
        }
    }

    private FakePageableRepository<AttributeEntity, UUID> attributeRepository;
    private FakeProjectRepository projectRepository;
    private TestAttributeService service;
    private ProjectEntity projectA;
    private ProjectEntity projectB;

    private Page<AttributeEntity> findByProject(UUID projectId, Pageable pageable) {
        List<AttributeEntity> matching = attributeRepository.store.values().stream()
                .filter(e -> e.getProject().getId().equals(projectId))
                .toList();
        return Page.of(matching, pageable, (long) matching.size());
    }

    @BeforeEach
    void setUp() {
        attributeRepository = new FakePageableRepository<>(AttributeEntity::getId);
        projectRepository = new FakeProjectRepository();
        service = new TestAttributeService(attributeRepository, projectRepository, this::findByProject);

        projectA = new ProjectEntity(UUID.randomUUID(), "Project A", "project-a", null, null, null, false);
        projectB = new ProjectEntity(UUID.randomUUID(), "Project B", "project-b", null, null, null, false);
        projectRepository.save(projectA);
        projectRepository.save(projectB);
    }

    @Test
    @DisplayName("create() with a known projectId resolves the project and saves the entity")
    void create_knownProject_savesEntity() {
        AttributeModelDTO dto = new AttributeModelDTO(null, "UI", "var", 1.0, 10.0);

        AttributeModelResponseDTO result = service.create(projectA.getId(), dto);

        assertInstanceOf(AttributeModelResponseDTO.AttributeModelDTO.class, result);
        var success = (AttributeModelResponseDTO.AttributeModelDTO) result;
        assertEquals(projectA.getId(), success.projectId());
    }

    @Test
    @DisplayName("create() with an unknown projectId returns an error DTO")
    void create_unknownProject_returnsError() {
        AttributeModelDTO dto = new AttributeModelDTO(null, "UI", "var", 1.0, 10.0);

        AttributeModelResponseDTO result = service.create(UUID.randomUUID(), dto);

        assertInstanceOf(AttributeModelResponseDTO.AttributeModelErrorDTO.class, result);
    }

    @Test
    @DisplayName("update() on an entity owned by a different project returns not-found")
    void update_crossProject_returnsNotFound() {
        AttributeEntity existing = new AttributeEntity(UUID.randomUUID(), "UI", "var", 1.0, 10.0, projectA);
        attributeRepository.save(existing);
        AttributeModelDTO updateDto = new AttributeModelDTO(existing.getId(), "UI2", "var2", 2.0, 20.0);

        AttributeModelResponseDTO result = service.update(projectB.getId(), updateDto);

        assertInstanceOf(AttributeModelResponseDTO.AttributeModelErrorDTO.class, result);
    }

    @Test
    @DisplayName("delete() on an entity owned by a different project returns not-found and does not delete it")
    void delete_crossProject_returnsNotFoundAndKeepsEntity() {
        AttributeEntity existing = new AttributeEntity(UUID.randomUUID(), "UI", "var", 1.0, 10.0, projectA);
        attributeRepository.save(existing);

        AttributeModelResponseDTO result = service.delete(projectB.getId(), existing.getId());

        assertInstanceOf(AttributeModelResponseDTO.AttributeModelErrorDTO.class, result);
        assertTrue(attributeRepository.findById(existing.getId()).isPresent());
    }

    @Test
    @DisplayName("delete() on an entity owned by the matching project succeeds and removes it")
    void delete_sameProject_succeeds() {
        AttributeEntity existing = new AttributeEntity(UUID.randomUUID(), "UI", "var", 1.0, 10.0, projectA);
        attributeRepository.save(existing);

        AttributeModelResponseDTO result = service.delete(projectA.getId(), existing.getId());

        assertInstanceOf(AttributeModelResponseDTO.AttributeModelDTO.class, result);
        assertTrue(attributeRepository.findById(existing.getId()).isEmpty());
    }

    @Test
    @DisplayName("deleteAll() only deletes entities belonging to the given project")
    void deleteAll_scopesToProject() {
        AttributeEntity a1 = new AttributeEntity(UUID.randomUUID(), "A1", "a1", 1.0, 10.0, projectA);
        AttributeEntity b1 = new AttributeEntity(UUID.randomUUID(), "B1", "b1", 1.0, 10.0, projectB);
        attributeRepository.save(a1);
        attributeRepository.save(b1);

        service.deleteAll(projectA.getId());

        assertTrue(attributeRepository.findById(a1.getId()).isEmpty());
        assertTrue(attributeRepository.findById(b1.getId()).isPresent());
    }

    @Test
    @DisplayName("getAll() only returns entities belonging to the given project")
    void getAll_scopesToProject() {
        attributeRepository.save(new AttributeEntity(UUID.randomUUID(), "A1", "a1", 1.0, 10.0, projectA));
        attributeRepository.save(new AttributeEntity(UUID.randomUUID(), "A2", "a2", 1.0, 10.0, projectA));
        attributeRepository.save(new AttributeEntity(UUID.randomUUID(), "B1", "b1", 1.0, 10.0, projectB));

        Page<AttributeModelResponseDTO.AttributeModelDTO> page = service.getAll(projectA.getId(), Pageable.from(0, 10));

        assertEquals(2, page.getTotalSize());
        assertTrue(page.getContent().stream().allMatch(dto -> dto.projectId().equals(projectA.getId())));
    }

    @Test
    @DisplayName("findById() returns empty when the entity belongs to a different project")
    void findById_crossProject_returnsEmpty() {
        AttributeEntity existing = new AttributeEntity(UUID.randomUUID(), "UI", "var", 1.0, 10.0, projectA);
        attributeRepository.save(existing);

        Optional<AttributeEntity> result = service.findById(projectB.getId(), existing.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("the unscoped create(dto) still throws for a project-scoped service")
    void unscopedCreate_throwsUnsupported() {
        AttributeModelDTO dto = new AttributeModelDTO(null, "UI", "var", 1.0, 10.0);
        assertThrows(UnsupportedOperationException.class, () -> service.create(dto));
    }
}
