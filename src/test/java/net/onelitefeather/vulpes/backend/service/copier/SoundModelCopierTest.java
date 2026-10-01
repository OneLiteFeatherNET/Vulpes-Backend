package net.onelitefeather.vulpes.backend.service.copier;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import io.micronaut.data.repository.PageableRepository;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.api.model.sound.SoundEventEntity;
import net.onelitefeather.vulpes.api.model.sound.SoundFileSource;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.api.repository.SoundFileSourceRepository;
import net.onelitefeather.vulpes.api.repository.SoundRepository;
import net.onelitefeather.vulpes.backend.domain.error.ErrorCode;
import net.onelitefeather.vulpes.backend.domain.sound.SoundRelation;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit tests for SoundModelCopier")
class SoundModelCopierTest {

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

    private static class FakeSoundRepository extends FakePageableRepository<SoundEventEntity, UUID> implements SoundRepository {
        FakeSoundRepository() {
            super(SoundEventEntity::getId);
        }

        @Override
        public <S extends SoundEventEntity> S save(S entity) {
            if (entity.getId() == null) {
                entity.setId(UUID.randomUUID());
            }
            return super.save(entity);
        }

        @Override
        public Page<SoundEventEntity> findByProjectId(UUID projectId, Pageable pageable) {
            List<SoundEventEntity> matching = store.values().stream()
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

    private static class FakeSoundFileSourceRepository extends FakePageableRepository<SoundFileSource, UUID> implements SoundFileSourceRepository {
        FakeSoundFileSourceRepository() {
            super(SoundFileSource::getId);
        }

        @Override
        public <S extends SoundFileSource> S save(S entity) {
            if (entity.getId() == null) {
                entity.setId(UUID.randomUUID());
            }
            return super.save(entity);
        }

        @Override
        public Page<SoundFileSource> findSoundFileSourcesBySoundEvent(UUID id, Pageable pageable) {
            List<SoundFileSource> matching = store.values().stream()
                    .filter(e -> e.getSoundEvent().getId().equals(id))
                    .toList();
            return Page.of(matching, pageable, (long) matching.size());
        }
    }

    private static class FakeProjectRepository extends FakePageableRepository<ProjectEntity, UUID> implements ProjectRepository {
        FakeProjectRepository() {
            super(ProjectEntity::getId);
        }
    }

    private FakeSoundRepository soundRepository;
    private FakeSoundFileSourceRepository soundFileSourceRepository;
    private FakeProjectRepository projectRepository;
    private SoundModelCopier copier;
    private ProjectEntity projectA;
    private ProjectEntity projectB;

    @BeforeEach
    void setUp() {
        soundRepository = new FakeSoundRepository();
        soundFileSourceRepository = new FakeSoundFileSourceRepository();
        projectRepository = new FakeProjectRepository();
        copier = new SoundModelCopier(soundRepository, soundFileSourceRepository, projectRepository);

        projectA = new ProjectEntity(UUID.randomUUID(), "Project A", "project-a", null, null, null, false);
        projectB = new ProjectEntity(UUID.randomUUID(), "Project B", "project-b", null, null, null, false);
        projectRepository.save(projectA);
        projectRepository.save(projectB);
    }

    private SoundEventEntity sampleSound(String key, ProjectEntity project) {
        SoundEventEntity sound = new SoundEventEntity(
                UUID.randomUUID(), "UI", key, "key-name", false, "subtitle", List.of(), project
        );
        soundRepository.save(sound);
        return sound;
    }

    @Test
    @DisplayName("copy() into the same project with a new key succeeds and does not touch the source")
    void copy_sameProjectNewKey_succeeds() {
        SoundEventEntity source = sampleSound("original-key", projectA);

        SoundEventEntity result = copier.copy(projectA.getId(), source.getId(), null, "copied-key", null, Set.of());

        assertNotEquals(source.getId(), result.getId());
        assertEquals("copied-key", result.getKey());
        assertEquals(projectA.getId(), result.getProject().getId());
        assertEquals(source.getUiName(), result.getUiName());
        SoundEventEntity stillThere = soundRepository.findById(source.getId()).orElseThrow();
        assertEquals("original-key", stillThere.getKey());
        assertEquals(projectA.getId(), stillThere.getProject().getId());
    }

    @Test
    @DisplayName("copy() into another project with no targetKey keeps the source's key and leaves the source in its own project")
    void copy_otherProjectNoKey_keepsOriginalKey() {
        SoundEventEntity source = sampleSound("shared-key", projectA);

        SoundEventEntity result = copier.copy(projectA.getId(), source.getId(), projectB.getId(), null, null, Set.of());

        assertEquals("shared-key", result.getKey());
        assertEquals(projectB.getId(), result.getProject().getId());
        SoundEventEntity stillThere = soundRepository.findById(source.getId()).orElseThrow();
        assertEquals(projectA.getId(), stillThere.getProject().getId(), "the source must not have been moved into the target project");
        assertEquals("shared-key", stillThere.getKey());
    }

    @Test
    @DisplayName("copy() into the same project without a targetKey conflicts with the source's own key")
    void copy_sameProjectNoKey_conflictsWithSelf() {
        SoundEventEntity source = sampleSound("only-key", projectA);

        ApiException exception = assertThrows(ApiException.class,
                () -> copier.copy(projectA.getId(), source.getId(), null, null, null, Set.of()));

        assertEquals(ErrorCode.RESOURCE_CONFLICT, exception.code());
    }

    @Test
    @DisplayName("copy() raises RESOURCE_CONFLICT when the target key is already taken in the target project")
    void copy_keyTaken_raisesConflict() {
        SoundEventEntity source = sampleSound("key-a", projectA);
        sampleSound("key-b", projectB);

        ApiException exception = assertThrows(ApiException.class,
                () -> copier.copy(projectA.getId(), source.getId(), projectB.getId(), "key-b", null, Set.of()));

        assertEquals(ErrorCode.RESOURCE_CONFLICT, exception.code());
    }

    @Test
    @DisplayName("copy() with an unknown targetProjectId raises PROJECT_NOT_FOUND")
    void copy_unknownTargetProject_raisesProjectNotFound() {
        SoundEventEntity source = sampleSound("key-a", projectA);
        UUID unknownProject = UUID.randomUUID();

        ApiException exception = assertThrows(ApiException.class,
                () -> copier.copy(projectA.getId(), source.getId(), unknownProject, null, null, Set.of()));

        assertEquals(ErrorCode.PROJECT_NOT_FOUND, exception.code());
    }

    @Test
    @DisplayName("copy() when the source belongs to a different project than addressed raises RESOURCE_NOT_FOUND")
    void copy_sourceOwnedByDifferentProject_raisesNotFound() {
        SoundEventEntity source = sampleSound("key-a", projectA);

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
    @DisplayName("copy() with SOURCES copies every sound file source under a new id")
    void copy_withSourcesRelation_copiesSources() {
        SoundEventEntity source = sampleSound("sources-event", projectA);
        SoundFileSource fileSource = new SoundFileSource(UUID.randomUUID(), "hit.ogg", 0.8f, 1.1f, 2, true, 32, true, "file");
        fileSource.setSoundEvent(source);
        soundFileSourceRepository.save(fileSource);

        SoundEventEntity result = copier.copy(projectA.getId(), source.getId(), null, "sources-copy", null, Set.of(SoundRelation.SOURCES));

        List<SoundFileSource> copiedSources = soundFileSourceRepository.findSoundFileSourcesBySoundEvent(result.getId(), Pageable.unpaged()).getContent();
        assertEquals(1, copiedSources.size());
        SoundFileSource copy = copiedSources.get(0);
        assertNotEquals(fileSource.getId(), copy.getId());
        assertEquals("hit.ogg", copy.getName());
        assertEquals(0.8f, copy.getVolume());
        assertEquals(1.1f, copy.getPitch());
        assertEquals(2, copy.getWeight());
        assertTrue(copy.isStreamable());
        assertEquals(32, copy.getAttenuationDistance());
        assertTrue(copy.isPreloadable());
        assertEquals("file", copy.getType());
        assertEquals(result.getId(), copy.getSoundEvent().getId());
    }

    @Test
    @DisplayName("copy() with an empty relations set copies only the root")
    void copy_withNoRelations_copiesOnlyRoot() {
        SoundEventEntity source = sampleSound("bare-event", projectA);
        SoundFileSource fileSource = new SoundFileSource(UUID.randomUUID(), "bare.ogg", 1.0f, 1.0f, 1, false, 16, false, "file");
        fileSource.setSoundEvent(source);
        soundFileSourceRepository.save(fileSource);

        SoundEventEntity result = copier.copy(projectA.getId(), source.getId(), null, "bare-copy", null, Set.of());

        assertTrue(soundFileSourceRepository.findSoundFileSourcesBySoundEvent(result.getId(), Pageable.unpaged()).getContent().isEmpty());
    }

    @Test
    @DisplayName("copy() with SOURCES does not move the source's own sound file sources onto the target")
    void copy_relationCopy_doesNotMutateSourceChildren() {
        SoundEventEntity source = sampleSound("guarded-event", projectA);
        SoundFileSource fileSource = new SoundFileSource(UUID.randomUUID(), "guarded.ogg", 1.0f, 1.0f, 1, false, 16, false, "file");
        fileSource.setSoundEvent(source);
        soundFileSourceRepository.save(fileSource);

        SoundEventEntity result = copier.copy(projectA.getId(), source.getId(), null, "guarded-copy", null, Set.of(SoundRelation.SOURCES));

        List<SoundFileSource> sourceFilesAfter = soundFileSourceRepository.findSoundFileSourcesBySoundEvent(source.getId(), Pageable.unpaged()).getContent();
        assertEquals(1, sourceFilesAfter.size(), "the source's own sound file source must still be attached to the source");
        assertEquals(fileSource.getId(), sourceFilesAfter.get(0).getId());
        assertEquals(source.getId(), sourceFilesAfter.get(0).getSoundEvent().getId());
        List<SoundFileSource> targetFiles = soundFileSourceRepository.findSoundFileSourcesBySoundEvent(result.getId(), Pageable.unpaged()).getContent();
        assertEquals(1, targetFiles.size());
        assertNotEquals(fileSource.getId(), targetFiles.get(0).getId());
    }

    @Test
    @DisplayName("copy() with a targetName uses it instead of the source's uiName")
    void copy_withTargetName_overridesUiName() {
        SoundEventEntity source = sampleSound("name-event", projectA);

        SoundEventEntity result = copier.copy(projectA.getId(), source.getId(), null, "name-copy", "New UI", Set.of());

        assertEquals("New UI", result.getUiName());
        assertEquals("UI", source.getUiName(), "the source must not have been mutated");
    }

    @Test
    @DisplayName("copy() with a blank targetName keeps the source's uiName")
    void copy_blankTargetName_keepsSourceUiName() {
        SoundEventEntity source = sampleSound("blank-name-event", projectA);

        SoundEventEntity result = copier.copy(projectA.getId(), source.getId(), null, "blank-name-copy", "  ", Set.of());

        assertEquals("UI", result.getUiName());
    }
}
