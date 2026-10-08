package net.onelitefeather.vulpes.backend.service.impl;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import io.micronaut.data.repository.PageableRepository;
import net.onelitefeather.vulpes.api.model.advancement.AdvancementEntity;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.api.repository.AdvancementRepository;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

/**
 * In-memory repositories for the advancement tests — no database, no Micronaut context.
 */
final class AdvancementFakes {

    private AdvancementFakes() {
    }

    static class FakePageableRepository<E, ID> implements PageableRepository<E, ID> {
        final Map<ID, E> store = new LinkedHashMap<>();
        final Function<E, ID> idOf;
        final List<E> deleted = new ArrayList<>();

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
            entities.forEach(e -> {
                deleted.add(e);
                store.remove(idOf.apply(e));
            });
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
     * Fake {@link AdvancementRepository} that also simulates database-assigned ids on save,
     * since the create and copy paths pass {@code id = null} and rely on persistence to assign one.
     */
    static class FakeAdvancementRepository extends FakePageableRepository<AdvancementEntity, UUID>
            implements AdvancementRepository {

        FakeAdvancementRepository() {
            super(AdvancementEntity::getId);
        }

        @Override
        public <S extends AdvancementEntity> S save(S entity) {
            if (entity.getId() == null) {
                entity.setId(UUID.randomUUID());
            }
            return super.save(entity);
        }

        @Override
        public Page<AdvancementEntity> findByProjectId(UUID projectId, Pageable pageable) {
            List<AdvancementEntity> matching = store.values().stream()
                    .filter(e -> e.getProject().getId().equals(projectId))
                    .toList();
            return Page.of(matching, pageable, (long) matching.size());
        }

        @Override
        public List<AdvancementEntity> findByProjectIdAndParentIsNull(UUID projectId) {
            return store.values().stream()
                    .filter(e -> e.getProject().getId().equals(projectId) && e.getParent() == null)
                    .toList();
        }

        @Override
        public List<AdvancementEntity> findByParentId(UUID parentId) {
            return store.values().stream()
                    .filter(e -> e.getParent() != null && e.getParent().getId().equals(parentId))
                    .toList();
        }

        @Override
        public boolean existsByProjectIdAndKey(UUID projectId, String key) {
            return store.values().stream()
                    .anyMatch(e -> e.getProject().getId().equals(projectId) && e.getKey().equals(key));
        }
    }

    static class FakeProjectRepository extends FakePageableRepository<ProjectEntity, UUID>
            implements ProjectRepository {
        FakeProjectRepository() {
            super(ProjectEntity::getId);
        }
    }
}
