package net.onelitefeather.vulpes.backend.service.impl;

import net.onelitefeather.vulpes.api.model.advancement.AdvancementEntity;
import net.onelitefeather.vulpes.api.model.advancement.AdvancementFrameType;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.backend.domain.error.ErrorCode;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.service.copier.AdvancementModelCopier;
import net.onelitefeather.vulpes.backend.service.impl.AdvancementFakes.FakeAdvancementRepository;
import net.onelitefeather.vulpes.backend.service.impl.AdvancementFakes.FakeProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit tests for AdvancementModelCopier")
class AdvancementModelCopierTest {

    private FakeAdvancementRepository advancementRepository;
    private FakeProjectRepository projectRepository;
    private AdvancementModelCopier copier;
    private ProjectEntity projectA;
    private ProjectEntity projectB;

    @BeforeEach
    void setUp() {
        advancementRepository = new FakeAdvancementRepository();
        projectRepository = new FakeProjectRepository();
        copier = new AdvancementModelCopier(advancementRepository, projectRepository);

        projectA = new ProjectEntity(UUID.randomUUID(), "Project A", "project-a", null, null, null, false);
        projectB = new ProjectEntity(UUID.randomUUID(), "Project B", "project-b", null, null, null, false);
        projectRepository.save(projectA);
        projectRepository.save(projectB);
    }

    @Test
    @DisplayName("copy() into the same project with a new key succeeds and does not touch the source")
    void copy_sameProjectNewKey_succeeds() {
        AdvancementEntity source = new AdvancementEntity(
                UUID.randomUUID(), "UI", "original-key", "comment", "STONE", AdvancementFrameType.TASK, "\"title\"", null, null, projectA);
        advancementRepository.save(source);

        AdvancementEntity result = copier.copy(projectA.getId(), source.getId(), null, "copied-key", null);

        assertNotEquals(source.getId(), result.getId());
        assertEquals("copied-key", result.getKey());
        assertEquals(projectA.getId(), result.getProject().getId());
        assertEquals(source.getUiName(), result.getUiName());
        assertEquals(source.getComment(), result.getComment());
        assertEquals(source.getMaterial(), result.getMaterial());
        assertEquals(source.getFrameType(), result.getFrameType());
        assertEquals(source.getTitle(), result.getTitle());
        AdvancementEntity stillThere = advancementRepository.findById(source.getId()).orElseThrow();
        assertEquals("original-key", stillThere.getKey());
        assertEquals(projectA.getId(), stillThere.getProject().getId());
    }

    @Test
    @DisplayName("copy() into another project with no targetKey keeps the source's key")
    void copy_otherProjectNoKey_keepsOriginalKey() {
        AdvancementEntity source = new AdvancementEntity(
                UUID.randomUUID(), "UI", "shared-key", "comment", "STONE", AdvancementFrameType.TASK, "\"title\"", null, null, projectA);
        advancementRepository.save(source);

        AdvancementEntity result = copier.copy(projectA.getId(), source.getId(), projectB.getId(), null, null);

        assertEquals("shared-key", result.getKey());
        assertEquals(projectB.getId(), result.getProject().getId());
        AdvancementEntity stillThere = advancementRepository.findById(source.getId()).orElseThrow();
        assertEquals(projectA.getId(), stillThere.getProject().getId(), "the source must not have been moved into the target project");
        assertEquals("shared-key", stillThere.getKey());
    }

    @Test
    @DisplayName("copy() into the same project without a targetKey conflicts with the source's own key")
    void copy_sameProjectNoKey_conflictsWithSelf() {
        AdvancementEntity source = new AdvancementEntity(
                UUID.randomUUID(), "UI", "only-key", "comment", "STONE", AdvancementFrameType.TASK, "\"title\"", null, null, projectA);
        advancementRepository.save(source);

        ApiException exception = assertThrows(ApiException.class,
                () -> copier.copy(projectA.getId(), source.getId(), null, null, null));

        assertEquals(ErrorCode.RESOURCE_CONFLICT, exception.code());
    }

    @Test
    @DisplayName("copy() raises RESOURCE_CONFLICT when the target key is already taken in the target project")
    void copy_keyTaken_raisesConflict() {
        AdvancementEntity source = new AdvancementEntity(
                UUID.randomUUID(), "UI", "key-a", "comment", "STONE", AdvancementFrameType.TASK, "\"title\"", null, null, projectA);
        AdvancementEntity existing = new AdvancementEntity(
                UUID.randomUUID(), "UI2", "key-b", "comment2", "DIRT", AdvancementFrameType.TASK, "\"title2\"", null, null, projectB);
        advancementRepository.save(source);
        advancementRepository.save(existing);

        ApiException exception = assertThrows(ApiException.class,
                () -> copier.copy(projectA.getId(), source.getId(), projectB.getId(), "key-b", null));

        assertEquals(ErrorCode.RESOURCE_CONFLICT, exception.code());
    }

    @Test
    @DisplayName("copy() with an unknown targetProjectId raises PROJECT_NOT_FOUND")
    void copy_unknownTargetProject_raisesProjectNotFound() {
        AdvancementEntity source = new AdvancementEntity(
                UUID.randomUUID(), "UI", "key-a", "comment", "STONE", AdvancementFrameType.TASK, "\"title\"", null, null, projectA);
        advancementRepository.save(source);
        UUID unknownProject = UUID.randomUUID();

        ApiException exception = assertThrows(ApiException.class,
                () -> copier.copy(projectA.getId(), source.getId(), unknownProject, null, null));

        assertEquals(ErrorCode.PROJECT_NOT_FOUND, exception.code());
    }

    @Test
    @DisplayName("copy() when the source belongs to a different project than addressed raises RESOURCE_NOT_FOUND")
    void copy_sourceOwnedByDifferentProject_raisesNotFound() {
        AdvancementEntity source = new AdvancementEntity(
                UUID.randomUUID(), "UI", "key-a", "comment", "STONE", AdvancementFrameType.TASK, "\"title\"", null, null, projectA);
        advancementRepository.save(source);

        ApiException exception = assertThrows(ApiException.class,
                () -> copier.copy(projectB.getId(), source.getId(), null, "new-key", null));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.code());
    }

    @Test
    @DisplayName("copy() when the source id does not exist at all raises RESOURCE_NOT_FOUND")
    void copy_unknownSource_raisesNotFound() {
        UUID unknownId = UUID.randomUUID();

        ApiException exception = assertThrows(ApiException.class,
                () -> copier.copy(projectA.getId(), unknownId, null, null, null));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.code());
    }

    @Test
    @DisplayName("copy() with a targetName uses it instead of the source's uiName, leaving title untouched")
    void copy_withTargetName_overridesUiNameOnly() {
        AdvancementEntity source = new AdvancementEntity(
                UUID.randomUUID(), "Original UI", "name-key", "comment", "STONE", AdvancementFrameType.TASK, "\"title\"", null, null, projectA);
        advancementRepository.save(source);

        AdvancementEntity result = copier.copy(projectA.getId(), source.getId(), null, "name-key-copy", "New UI");

        assertEquals("New UI", result.getUiName());
        assertEquals(source.getTitle(), result.getTitle());
    }

    @Test
    @DisplayName("copy() with a blank targetName keeps the source's uiName")
    void copy_blankTargetName_keepsSourceUiName() {
        AdvancementEntity source = new AdvancementEntity(
                UUID.randomUUID(), "Original UI", "blank-name-key", "comment", "STONE", AdvancementFrameType.TASK, "\"title\"", null, null, projectA);
        advancementRepository.save(source);

        AdvancementEntity result = copier.copy(projectA.getId(), source.getId(), null, "blank-name-key-copy", "  ");

        assertEquals("Original UI", result.getUiName());
    }

    @Test
    @DisplayName("copy() keeps every display field of the source")
    void copy_keepsDisplayFields() {
        AdvancementEntity source = new AdvancementEntity(
                UUID.randomUUID(), "UI", "display-key", "comment", "STONE", AdvancementFrameType.CHALLENGE,
                "\"title\"", "\"description\"", null, projectA);
        source.setBackground("minecraft:gui/advancements/backgrounds/stone");
        source.setX(1.5f);
        source.setY(-2f);
        source.setShowToast(false);
        source.setAnnounceToChat(false);
        source.setHidden(true);
        advancementRepository.save(source);

        AdvancementEntity result = copier.copy(projectA.getId(), source.getId(), null, "display-key-copy", null);

        assertEquals(AdvancementFrameType.CHALLENGE, result.getFrameType());
        assertEquals(source.getDescription(), result.getDescription());
        assertEquals(source.getBackground(), result.getBackground());
        assertEquals(1.5f, result.getX());
        assertEquals(-2f, result.getY());
        assertFalse(result.isShowToast());
        assertFalse(result.isAnnounceToChat());
        assertTrue(result.isHidden());
    }

    @Test
    @DisplayName("copy() within the same project keeps the parent of the source")
    void copy_sameProject_keepsParent() {
        AdvancementEntity root = new AdvancementEntity(
                UUID.randomUUID(), "Root", "root", null, null, AdvancementFrameType.TASK, null, null, null, projectA);
        AdvancementEntity source = new AdvancementEntity(
                UUID.randomUUID(), "Child", "child", null, null, AdvancementFrameType.TASK, null, null, root, projectA);
        advancementRepository.save(root);
        advancementRepository.save(source);

        AdvancementEntity result = copier.copy(projectA.getId(), source.getId(), null, "child-copy", null);

        assertSame(root, result.getParent());
    }

    @Test
    @DisplayName("copy() into another project makes the copy a root")
    void copy_otherProject_becomesRoot() {
        AdvancementEntity root = new AdvancementEntity(
                UUID.randomUUID(), "Root", "root", null, null, AdvancementFrameType.TASK, null, null, null, projectA);
        AdvancementEntity source = new AdvancementEntity(
                UUID.randomUUID(), "Child", "child", null, null, AdvancementFrameType.TASK, null, null, root, projectA);
        advancementRepository.save(root);
        advancementRepository.save(source);

        AdvancementEntity result = copier.copy(projectA.getId(), source.getId(), projectB.getId(), null, null);

        assertNull(result.getParent());
        assertTrue(result.isRoot());
    }
}
