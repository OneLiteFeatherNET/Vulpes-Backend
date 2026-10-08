package net.onelitefeather.vulpes.backend.service.impl;

import io.micronaut.json.JsonMapper;
import net.onelitefeather.vulpes.api.model.advancement.AdvancementEntity;
import net.onelitefeather.vulpes.api.model.advancement.AdvancementFrameType;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.backend.domain.advancement.AdvancementModelDTO;
import net.onelitefeather.vulpes.backend.domain.advancement.AdvancementModelResponseDTO;
import net.onelitefeather.vulpes.backend.domain.error.ErrorCode;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.service.impl.AdvancementFakes.FakeAdvancementRepository;
import net.onelitefeather.vulpes.backend.service.impl.AdvancementFakes.FakeProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit tests for AdvancementServiceImpl")
class AdvancementServiceImplTest {

    private FakeAdvancementRepository advancementRepository;
    private AdvancementServiceImpl service;
    private ProjectEntity projectA;
    private ProjectEntity projectB;

    @BeforeEach
    void setUp() {
        advancementRepository = new FakeAdvancementRepository();
        FakeProjectRepository projectRepository = new FakeProjectRepository();
        service = new AdvancementServiceImpl(advancementRepository, projectRepository, JsonMapper.createDefault());

        projectA = new ProjectEntity(UUID.randomUUID(), "Project A", "project-a", null, null, null, false);
        projectB = new ProjectEntity(UUID.randomUUID(), "Project B", "project-b", null, null, null, false);
        projectRepository.save(projectA);
        projectRepository.save(projectB);
    }

    private AdvancementEntity stored(String key, AdvancementEntity parent, ProjectEntity project) {
        return advancementRepository.save(new AdvancementEntity(
                null, key, key, null, null, AdvancementFrameType.TASK, null, null, parent, project));
    }

    private static AdvancementModelDTO dto(UUID id, UUID parentId, String title) {
        return new AdvancementModelDTO(id, "UI", "key", null, "minecraft:stone", AdvancementFrameType.GOAL,
                title, null, null, parentId, 1f, 2f, true, false, true);
    }

    @Test
    @DisplayName("create() stores every field and resolves the parent")
    void create_withParent_resolvesParent() {
        AdvancementEntity root = stored("root", null, projectA);

        AdvancementModelResponseDTO.AdvancementModelDTO result =
                service.create(projectA.getId(), dto(null, root.getId(), "{\"text\":\"Title\"}"));

        assertEquals(root.getId(), result.parentId());
        assertEquals(AdvancementFrameType.GOAL, result.frameType());
        assertEquals("{\"text\":\"Title\"}", result.title());
        assertEquals(1f, result.x());
        assertEquals(2f, result.y());
        assertTrue(result.showToast());
        assertFalse(result.announceToChat());
        assertTrue(result.hidden());
    }

    @Test
    @DisplayName("create() without a parent creates a root")
    void create_withoutParent_createsRoot() {
        var result = service.create(projectA.getId(), dto(null, null, null));

        assertNull(result.parentId());
    }

    @Test
    @DisplayName("create() rejects a parent that does not exist")
    void create_unknownParent_raisesNotFound() {
        ApiException exception = assertThrows(ApiException.class,
                () -> service.create(projectA.getId(), dto(null, UUID.randomUUID(), null)));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.code());
    }

    @Test
    @DisplayName("create() rejects a parent from another project")
    void create_parentOfOtherProject_raisesNotFound() {
        AdvancementEntity foreign = stored("foreign", null, projectB);

        ApiException exception = assertThrows(ApiException.class,
                () -> service.create(projectA.getId(), dto(null, foreign.getId(), null)));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.code());
    }

    @Test
    @DisplayName("update() rejects the advancement itself as its parent")
    void update_selfAsParent_raisesInvalidRequest() {
        AdvancementEntity advancement = stored("self", null, projectA);

        ApiException exception = assertThrows(ApiException.class,
                () -> service.update(projectA.getId(), dto(advancement.getId(), advancement.getId(), null)));

        assertEquals(ErrorCode.INVALID_REQUEST, exception.code());
    }

    @Test
    @DisplayName("update() rejects a descendant as the parent")
    void update_descendantAsParent_raisesInvalidRequest() {
        AdvancementEntity root = stored("root", null, projectA);
        AdvancementEntity child = stored("child", root, projectA);
        AdvancementEntity grandChild = stored("grand-child", child, projectA);

        ApiException exception = assertThrows(ApiException.class,
                () -> service.update(projectA.getId(), dto(root.getId(), grandChild.getId(), null)));

        assertEquals(ErrorCode.INVALID_REQUEST, exception.code());
    }

    @Test
    @DisplayName("update() moves an advancement below another branch")
    void update_otherBranchAsParent_succeeds() {
        AdvancementEntity root = stored("root", null, projectA);
        AdvancementEntity left = stored("left", root, projectA);
        AdvancementEntity right = stored("right", root, projectA);

        var result = service.update(projectA.getId(), dto(right.getId(), left.getId(), null));

        assertEquals(left.getId(), result.parentId());
    }

    @Test
    @DisplayName("create() rejects a title that is no JSON")
    void create_titleNoJson_raisesInvalidRequest() {
        ApiException exception = assertThrows(ApiException.class,
                () -> service.create(projectA.getId(), dto(null, null, "Plain title")));

        assertEquals(ErrorCode.INVALID_REQUEST, exception.code());
    }

    @Test
    @DisplayName("deleteAll() deletes the children before their parents")
    void deleteAll_deletesChildrenFirst() {
        AdvancementEntity root = stored("root", null, projectA);
        AdvancementEntity child = stored("child", root, projectA);
        AdvancementEntity grandChild = stored("grand-child", child, projectA);
        AdvancementEntity other = stored("other", null, projectB);

        service.deleteAll(projectA.getId());

        assertEquals(List.of(grandChild, child, root), advancementRepository.deleted);
        assertTrue(advancementRepository.existsById(other.getId()));
    }
}
