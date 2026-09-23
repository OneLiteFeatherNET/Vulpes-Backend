package net.onelitefeather.vulpes.backend.service.impl;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.backend.domain.error.ErrorCode;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelDTO;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelResponseDTO;
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

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit tests for ProjectServiceImpl")
class ProjectServiceImplTest {

    /**
     * Minimal in-memory {@link ProjectRepository} fake — no database, no Micronaut context.
     */
    private static class FakeProjectRepository implements ProjectRepository {
        final Map<UUID, ProjectEntity> store = new LinkedHashMap<>();

        @Override
        public <S extends ProjectEntity> S save(S entity) {
            if (entity.getId() == null) {
                entity.setId(UUID.randomUUID());
            }
            store.put(entity.getId(), entity);
            return entity;
        }

        @Override
        public <S extends ProjectEntity> List<S> saveAll(Iterable<S> entities) {
            List<S> list = new ArrayList<>();
            for (S entity : entities) {
                list.add(save(entity));
            }
            return list;
        }

        @Override
        public <S extends ProjectEntity> S insert(S entity) {
            return save(entity);
        }

        @Override
        public <S extends ProjectEntity> List<S> insertAll(Iterable<S> entities) {
            return saveAll(entities);
        }

        @Override
        public Optional<ProjectEntity> findById(UUID id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public boolean existsById(UUID id) {
            return store.containsKey(id);
        }

        @Override
        public List<ProjectEntity> findAll() {
            return new ArrayList<>(store.values());
        }

        @Override
        public long count() {
            return store.size();
        }

        @Override
        public <S extends ProjectEntity> S update(S entity) {
            store.put(entity.getId(), entity);
            return entity;
        }

        @Override
        public <S extends ProjectEntity> List<S> updateAll(Iterable<S> entities) {
            List<S> list = new ArrayList<>();
            for (S entity : entities) {
                list.add(update(entity));
            }
            return list;
        }

        @Override
        public void deleteById(UUID id) {
            store.remove(id);
        }

        @Override
        public void delete(ProjectEntity entity) {
            store.remove(entity.getId());
        }

        @Override
        public void deleteAll(Iterable<? extends ProjectEntity> entities) {
            entities.forEach(e -> store.remove(e.getId()));
        }

        @Override
        public void deleteAll() {
            store.clear();
        }

        @Override
        public List<ProjectEntity> findAll(Sort sort) {
            return findAll();
        }

        @Override
        public Page<ProjectEntity> findAll(Pageable pageable) {
            List<ProjectEntity> all = new ArrayList<>(store.values());
            return Page.of(all, pageable, (long) all.size());
        }
    }

    private FakeProjectRepository projectRepository;
    private ProjectServiceImpl service;

    @BeforeEach
    void setUp() {
        projectRepository = new FakeProjectRepository();
        service = new ProjectServiceImpl(projectRepository);
    }

    @Test
    @DisplayName("create() with a new key saves the project")
    void create_newKey_savesProject() {
        ProjectModelDTO dto = new ProjectModelDTO(null, "Project A", "project-a", null, null, null, false);

        ProjectModelResponseDTO result = service.create(dto);

        assertInstanceOf(ProjectModelResponseDTO.ProjectModelDTO.class, result);
        var success = (ProjectModelResponseDTO.ProjectModelDTO) result;
        assertEquals("project-a", success.key());
    }

    @Test
    @DisplayName("create() with an already-used key raises a conflict and does not create a duplicate")
    void create_duplicateKey_raisesConflictAndDoesNotDuplicate() {
        ProjectEntity existing = new ProjectEntity(UUID.randomUUID(), "Project A", "project-a", null, null, null, false);
        projectRepository.save(existing);
        ProjectModelDTO dto = new ProjectModelDTO(null, "Project A2", "project-a", null, null, null, false);

        ApiException exception = assertThrows(ApiException.class, () -> service.create(dto));

        assertEquals(ErrorCode.RESOURCE_CONFLICT, exception.code());
        assertEquals(1, projectRepository.findAll().size());
    }
}
