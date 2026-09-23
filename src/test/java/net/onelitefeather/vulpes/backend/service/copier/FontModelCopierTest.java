package net.onelitefeather.vulpes.backend.service.copier;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import io.micronaut.data.repository.PageableRepository;
import net.onelitefeather.vulpes.api.model.FontEntity;
import net.onelitefeather.vulpes.api.model.font.FontStringEntity;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.api.repository.FontRepository;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.api.repository.font.FontStringRepository;
import net.onelitefeather.vulpes.backend.domain.error.ErrorCode;
import net.onelitefeather.vulpes.backend.domain.font.FontRelation;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.service.copier.FontModelCopier;
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

@DisplayName("Unit tests for FontModelCopier")
class FontModelCopierTest {

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

    private static class FakeFontRepository extends FakePageableRepository<FontEntity, UUID> implements FontRepository {
        FakeFontRepository() {
            super(FontEntity::getId);
        }

        @Override
        public <S extends FontEntity> S save(S entity) {
            if (entity.getId() == null) {
                entity.setId(UUID.randomUUID());
            }
            return super.save(entity);
        }

        @Override
        public List<FontEntity> findAll() {
            return super.findAll();
        }

        @Override
        public Page<FontEntity> findByProjectId(UUID projectId, Pageable pageable) {
            List<FontEntity> matching = store.values().stream()
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

    private static class FakeFontStringRepository extends FakePageableRepository<FontStringEntity, UUID> implements FontStringRepository {
        FakeFontStringRepository() {
            super(FontStringEntity::getId);
        }

        @Override
        public <S extends FontStringEntity> S save(S entity) {
            if (entity.getId() == null) {
                entity.setId(UUID.randomUUID());
            }
            return super.save(entity);
        }

        @Override
        public Page<FontStringEntity> findCharsByFontId(UUID id, Pageable pageable) {
            List<FontStringEntity> matching = store.values().stream()
                    .filter(e -> e.getFont().getId().equals(id))
                    .sorted((a, b) -> Integer.compare(a.getOrderIndex(), b.getOrderIndex()))
                    .toList();
            return Page.of(matching, pageable, (long) matching.size());
        }
    }

    private static class FakeProjectRepository extends FakePageableRepository<ProjectEntity, UUID> implements ProjectRepository {
        FakeProjectRepository() {
            super(ProjectEntity::getId);
        }
    }

    private FakeFontRepository fontRepository;
    private FakeFontStringRepository fontStringRepository;
    private FakeProjectRepository projectRepository;
    private FontModelCopier copier;
    private ProjectEntity projectA;
    private ProjectEntity projectB;

    @BeforeEach
    void setUp() {
        fontRepository = new FakeFontRepository();
        fontStringRepository = new FakeFontStringRepository();
        projectRepository = new FakeProjectRepository();
        copier = new FontModelCopier(fontRepository, fontStringRepository, projectRepository);

        projectA = new ProjectEntity(UUID.randomUUID(), "Project A", "project-a", null, null, null, false);
        projectB = new ProjectEntity(UUID.randomUUID(), "Project B", "project-b", null, null, null, false);
        projectRepository.save(projectA);
        projectRepository.save(projectB);
    }

    private FontEntity sampleFont(String key, ProjectEntity project) {
        FontEntity font = new FontEntity(
                UUID.randomUUID(), "UI", key, "provider", "texture", "comment", 12, 8, List.of(), project
        );
        fontRepository.save(font);
        return font;
    }

    @Test
    @DisplayName("copy() into the same project with a new key succeeds and does not touch the source")
    void copy_sameProjectNewKey_succeeds() {
        FontEntity source = sampleFont("original-key", projectA);

        FontEntity result = copier.copy(projectA.getId(), source.getId(), null, "copied-key", null, Set.of());

        assertNotEquals(source.getId(), result.getId());
        assertEquals("copied-key", result.getKey());
        assertEquals(projectA.getId(), result.getProject().getId());
        assertEquals(source.getUiName(), result.getUiName());
        FontEntity stillThere = fontRepository.findById(source.getId()).orElseThrow();
        assertEquals("original-key", stillThere.getKey());
        assertEquals(projectA.getId(), stillThere.getProject().getId());
    }

    @Test
    @DisplayName("copy() into another project with no targetKey keeps the source's key and leaves the source in its own project")
    void copy_otherProjectNoKey_keepsOriginalKey() {
        FontEntity source = sampleFont("shared-key", projectA);

        FontEntity result = copier.copy(projectA.getId(), source.getId(), projectB.getId(), null, null, Set.of());

        assertEquals("shared-key", result.getKey());
        assertEquals(projectB.getId(), result.getProject().getId());
        FontEntity stillThere = fontRepository.findById(source.getId()).orElseThrow();
        assertEquals(projectA.getId(), stillThere.getProject().getId(), "the source must not have been moved into the target project");
        assertEquals("shared-key", stillThere.getKey());
    }

    @Test
    @DisplayName("copy() into the same project without a targetKey conflicts with the source's own key")
    void copy_sameProjectNoKey_conflictsWithSelf() {
        FontEntity source = sampleFont("only-key", projectA);

        ApiException exception = assertThrows(ApiException.class,
                () -> copier.copy(projectA.getId(), source.getId(), null, null, null, Set.of()));

        assertEquals(ErrorCode.RESOURCE_CONFLICT, exception.code());
    }

    @Test
    @DisplayName("copy() raises RESOURCE_CONFLICT when the target key is already taken in the target project")
    void copy_keyTaken_raisesConflict() {
        FontEntity source = sampleFont("key-a", projectA);
        sampleFont("key-b", projectB);

        ApiException exception = assertThrows(ApiException.class,
                () -> copier.copy(projectA.getId(), source.getId(), projectB.getId(), "key-b", null, Set.of()));

        assertEquals(ErrorCode.RESOURCE_CONFLICT, exception.code());
    }

    @Test
    @DisplayName("copy() with an unknown targetProjectId raises PROJECT_NOT_FOUND")
    void copy_unknownTargetProject_raisesProjectNotFound() {
        FontEntity source = sampleFont("key-a", projectA);
        UUID unknownProject = UUID.randomUUID();

        ApiException exception = assertThrows(ApiException.class,
                () -> copier.copy(projectA.getId(), source.getId(), unknownProject, null, null, Set.of()));

        assertEquals(ErrorCode.PROJECT_NOT_FOUND, exception.code());
    }

    @Test
    @DisplayName("copy() when the source belongs to a different project than addressed raises RESOURCE_NOT_FOUND")
    void copy_sourceOwnedByDifferentProject_raisesNotFound() {
        FontEntity source = sampleFont("key-a", projectA);

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
    @DisplayName("copy() preserves a mapper value that was changed away from its default")
    void copy_preservesCustomMapper() {
        FontEntity source = sampleFont("mapper-item", projectA);
        source.setMapper("custom-mapper");

        FontEntity result = copier.copy(projectA.getId(), source.getId(), null, "mapper-copy", null, Set.of());

        assertEquals("custom-mapper", result.getMapper());
    }

    @Test
    @DisplayName("copy() with CHARS copies chars in orderIndex order under new ids")
    void copy_withCharsRelation_copiesCharsInOrder() {
        FontEntity source = sampleFont("chars-item", projectA);
        List<FontStringEntity> chars = List.of(
                new FontStringEntity(UUID.randomUUID(), "first", 0),
                new FontStringEntity(UUID.randomUUID(), "second", 1),
                new FontStringEntity(UUID.randomUUID(), "third", 2)
        );
        chars.forEach(c -> {
            c.setFont(source);
            fontStringRepository.save(c);
        });

        FontEntity result = copier.copy(projectA.getId(), source.getId(), null, "chars-copy", null, Set.of(FontRelation.CHARS));

        List<FontStringEntity> copiedChars = fontStringRepository.findCharsByFontId(result.getId(), Pageable.unpaged()).getContent();
        assertEquals(3, copiedChars.size());
        assertEquals("first", copiedChars.get(0).getLine());
        assertEquals(0, copiedChars.get(0).getOrderIndex());
        assertEquals("second", copiedChars.get(1).getLine());
        assertEquals(1, copiedChars.get(1).getOrderIndex());
        assertEquals("third", copiedChars.get(2).getLine());
        assertEquals(2, copiedChars.get(2).getOrderIndex());
        assertTrue(copiedChars.stream().noneMatch(c -> chars.stream().anyMatch(orig -> orig.getId().equals(c.getId()))),
                "copied chars must have new ids, not the source's");
    }

    @Test
    @DisplayName("copy() with an empty relations set copies only the root")
    void copy_withNoRelations_copiesOnlyRoot() {
        FontEntity source = sampleFont("bare-item", projectA);
        FontStringEntity fontChar = new FontStringEntity(UUID.randomUUID(), "line", 0);
        fontChar.setFont(source);
        fontStringRepository.save(fontChar);

        FontEntity result = copier.copy(projectA.getId(), source.getId(), null, "bare-copy", null, Set.of());

        assertTrue(fontStringRepository.findCharsByFontId(result.getId(), Pageable.unpaged()).getContent().isEmpty());
    }

    @Test
    @DisplayName("copy() with relations does not move the source's own children onto the target")
    void copy_relationCopy_doesNotMutateSourceChildren() {
        FontEntity source = sampleFont("guarded-item", projectA);
        FontStringEntity fontChar = new FontStringEntity(UUID.randomUUID(), "glyph", 0);
        fontChar.setFont(source);
        fontStringRepository.save(fontChar);

        FontEntity result = copier.copy(projectA.getId(), source.getId(), null, "guarded-copy", null, Set.of(FontRelation.CHARS));

        List<FontStringEntity> sourceCharsAfter = fontStringRepository.findCharsByFontId(source.getId(), Pageable.unpaged()).getContent();
        assertEquals(1, sourceCharsAfter.size(), "the source's own char must still be attached to the source");
        assertEquals(fontChar.getId(), sourceCharsAfter.get(0).getId());
        assertEquals(source.getId(), sourceCharsAfter.get(0).getFont().getId());
        List<FontStringEntity> targetChars = fontStringRepository.findCharsByFontId(result.getId(), Pageable.unpaged()).getContent();
        assertEquals(1, targetChars.size());
        assertNotEquals(fontChar.getId(), targetChars.get(0).getId());
    }

    @Test
    @DisplayName("copy() with a targetName uses it instead of the source's uiName")
    void copy_withTargetName_overridesUiName() {
        FontEntity source = sampleFont("name-item", projectA);

        FontEntity result = copier.copy(projectA.getId(), source.getId(), null, "name-copy", "New UI", Set.of());

        assertEquals("New UI", result.getUiName());
        assertEquals("UI", source.getUiName(), "the source must not have been mutated");
    }

    @Test
    @DisplayName("copy() with a blank targetName keeps the source's uiName")
    void copy_blankTargetName_keepsSourceUiName() {
        FontEntity source = sampleFont("blank-name-item", projectA);

        FontEntity result = copier.copy(projectA.getId(), source.getId(), null, "blank-name-copy", "  ", Set.of());

        assertEquals("UI", result.getUiName());
    }
}
