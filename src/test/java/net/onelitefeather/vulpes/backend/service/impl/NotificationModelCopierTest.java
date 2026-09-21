package net.onelitefeather.vulpes.backend.service.impl;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import io.micronaut.data.repository.PageableRepository;
import net.onelitefeather.vulpes.api.model.NotificationEntity;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.api.repository.NotificationRepository;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.backend.domain.error.ErrorCode;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.service.copier.NotificationModelCopier;
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

@DisplayName("Unit tests for NotificationModelCopier")
class NotificationModelCopierTest {

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

    /**
     * Fake {@link NotificationRepository} that also simulates database-assigned ids on save,
     * since the copy path always passes {@code id = null} and relies on persistence to assign one.
     */
    private static class FakeNotificationRepository extends FakePageableRepository<NotificationEntity, UUID>
            implements NotificationRepository {

        FakeNotificationRepository() {
            super(NotificationEntity::getId);
        }

        @Override
        public <S extends NotificationEntity> S save(S entity) {
            if (entity.getId() == null) {
                entity.setId(UUID.randomUUID());
            }
            return super.save(entity);
        }

        @Override
        public Page<NotificationEntity> findByProjectId(UUID projectId, Pageable pageable) {
            List<NotificationEntity> matching = store.values().stream()
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

    private static class FakeProjectRepository extends FakePageableRepository<ProjectEntity, UUID>
            implements ProjectRepository {
        FakeProjectRepository() {
            super(ProjectEntity::getId);
        }
    }

    private FakeNotificationRepository notificationRepository;
    private FakeProjectRepository projectRepository;
    private NotificationModelCopier copier;
    private ProjectEntity projectA;
    private ProjectEntity projectB;

    @BeforeEach
    void setUp() {
        notificationRepository = new FakeNotificationRepository();
        projectRepository = new FakeProjectRepository();
        copier = new NotificationModelCopier(notificationRepository, projectRepository);

        projectA = new ProjectEntity(UUID.randomUUID(), "Project A", "project-a", null, null, null, false);
        projectB = new ProjectEntity(UUID.randomUUID(), "Project B", "project-b", null, null, null, false);
        projectRepository.save(projectA);
        projectRepository.save(projectB);
    }

    @Test
    @DisplayName("copy() into the same project with a new key succeeds and does not touch the source")
    void copy_sameProjectNewKey_succeeds() {
        NotificationEntity source = new NotificationEntity(
                UUID.randomUUID(), "UI", "original-key", "comment", "STONE", "frame", "title", projectA);
        notificationRepository.save(source);

        NotificationEntity result = copier.copy(projectA.getId(), source.getId(), null, "copied-key");

        assertNotEquals(source.getId(), result.getId());
        assertEquals("copied-key", result.getKey());
        assertEquals(projectA.getId(), result.getProject().getId());
        assertEquals(source.getUiName(), result.getUiName());
        assertEquals(source.getComment(), result.getComment());
        assertEquals(source.getMaterial(), result.getMaterial());
        assertEquals(source.getFrameType(), result.getFrameType());
        assertEquals(source.getTitle(), result.getTitle());
        NotificationEntity stillThere = notificationRepository.findById(source.getId()).orElseThrow();
        assertEquals("original-key", stillThere.getKey());
        assertEquals(projectA.getId(), stillThere.getProject().getId());
    }

    @Test
    @DisplayName("copy() into another project with no targetKey keeps the source's key")
    void copy_otherProjectNoKey_keepsOriginalKey() {
        NotificationEntity source = new NotificationEntity(
                UUID.randomUUID(), "UI", "shared-key", "comment", "STONE", "frame", "title", projectA);
        notificationRepository.save(source);

        NotificationEntity result = copier.copy(projectA.getId(), source.getId(), projectB.getId(), null);

        assertEquals("shared-key", result.getKey());
        assertEquals(projectB.getId(), result.getProject().getId());
        NotificationEntity stillThere = notificationRepository.findById(source.getId()).orElseThrow();
        assertEquals(projectA.getId(), stillThere.getProject().getId(), "the source must not have been moved into the target project");
        assertEquals("shared-key", stillThere.getKey());
    }

    @Test
    @DisplayName("copy() into the same project without a targetKey conflicts with the source's own key")
    void copy_sameProjectNoKey_conflictsWithSelf() {
        NotificationEntity source = new NotificationEntity(
                UUID.randomUUID(), "UI", "only-key", "comment", "STONE", "frame", "title", projectA);
        notificationRepository.save(source);

        ApiException exception = assertThrows(ApiException.class,
                () -> copier.copy(projectA.getId(), source.getId(), null, null));

        assertEquals(ErrorCode.RESOURCE_CONFLICT, exception.code());
    }

    @Test
    @DisplayName("copy() raises RESOURCE_CONFLICT when the target key is already taken in the target project")
    void copy_keyTaken_raisesConflict() {
        NotificationEntity source = new NotificationEntity(
                UUID.randomUUID(), "UI", "key-a", "comment", "STONE", "frame", "title", projectA);
        NotificationEntity existing = new NotificationEntity(
                UUID.randomUUID(), "UI2", "key-b", "comment2", "DIRT", "frame2", "title2", projectB);
        notificationRepository.save(source);
        notificationRepository.save(existing);

        ApiException exception = assertThrows(ApiException.class,
                () -> copier.copy(projectA.getId(), source.getId(), projectB.getId(), "key-b"));

        assertEquals(ErrorCode.RESOURCE_CONFLICT, exception.code());
    }

    @Test
    @DisplayName("copy() with an unknown targetProjectId raises PROJECT_NOT_FOUND")
    void copy_unknownTargetProject_raisesProjectNotFound() {
        NotificationEntity source = new NotificationEntity(
                UUID.randomUUID(), "UI", "key-a", "comment", "STONE", "frame", "title", projectA);
        notificationRepository.save(source);
        UUID unknownProject = UUID.randomUUID();

        ApiException exception = assertThrows(ApiException.class,
                () -> copier.copy(projectA.getId(), source.getId(), unknownProject, null));

        assertEquals(ErrorCode.PROJECT_NOT_FOUND, exception.code());
    }

    @Test
    @DisplayName("copy() when the source belongs to a different project than addressed raises RESOURCE_NOT_FOUND")
    void copy_sourceOwnedByDifferentProject_raisesNotFound() {
        NotificationEntity source = new NotificationEntity(
                UUID.randomUUID(), "UI", "key-a", "comment", "STONE", "frame", "title", projectA);
        notificationRepository.save(source);

        ApiException exception = assertThrows(ApiException.class,
                () -> copier.copy(projectB.getId(), source.getId(), null, "new-key"));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.code());
    }

    @Test
    @DisplayName("copy() when the source id does not exist at all raises RESOURCE_NOT_FOUND")
    void copy_unknownSource_raisesNotFound() {
        UUID unknownId = UUID.randomUUID();

        ApiException exception = assertThrows(ApiException.class,
                () -> copier.copy(projectA.getId(), unknownId, null, null));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.code());
    }
}
