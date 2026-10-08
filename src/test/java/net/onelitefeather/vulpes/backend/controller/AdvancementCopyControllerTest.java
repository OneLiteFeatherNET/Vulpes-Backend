package net.onelitefeather.vulpes.backend.controller;

import io.micronaut.http.HttpResponse;
import net.onelitefeather.vulpes.api.model.advancement.AdvancementEntity;
import net.onelitefeather.vulpes.api.model.advancement.AdvancementFrameType;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.backend.domain.error.ErrorCode;
import net.onelitefeather.vulpes.backend.domain.copy.CopyDTO;
import net.onelitefeather.vulpes.backend.domain.advancement.AdvancementModelDTO;
import net.onelitefeather.vulpes.backend.domain.advancement.AdvancementModelResponseDTO;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.copier.EntityCopier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit tests for AdvancementCopyController (project-scoped)")
class AdvancementCopyControllerTest {

    private static class StubAdvancementCopier implements EntityCopier<AdvancementEntity, Void> {
        AdvancementEntity response;
        RuntimeException toThrow;

        @Override
        public AdvancementEntity copy(UUID sourceProjectId, UUID sourceId, UUID targetProjectId, String targetKey, String targetName, Set<Void> relations) {
            if (toThrow != null) {
                throw toThrow;
            }
            return response;
        }
    }

    @Test
    @DisplayName("copy() with no body returns the copier's result wrapped in 200")
    void copy_noBody_returnsOk() {
        StubAdvancementCopier copierStub = new StubAdvancementCopier();
        ProjectEntity project = new ProjectEntity(UUID.randomUUID(), "Project A", "project-a", null, null, null, false);
        copierStub.response = new AdvancementEntity(UUID.randomUUID(), "UI", "key", "comment", "STONE", AdvancementFrameType.TASK, "\"title\"", null, null, project);
        AdvancementCopyController controller = new AdvancementCopyController(copierStub);
        UUID projectId = project.getId();
        UUID id = UUID.randomUUID();

        HttpResponse<AdvancementModelResponseDTO.AdvancementModelDTO> resp = controller.copy(projectId, id, null);

        assertEquals(200, resp.getStatus().getCode());
        assertEquals("key", resp.body().key());
    }

    @Test
    @DisplayName("copy() passes the DTO's fields through to the copier")
    void copy_withBody_passesFieldsThrough() {
        StubAdvancementCopier copierStub = new StubAdvancementCopier() {
            @Override
            public AdvancementEntity copy(UUID sourceProjectId, UUID sourceId, UUID targetProjectId, String targetKey, String targetName) {
                ProjectEntity project = new ProjectEntity(targetProjectId, "Target", "target", null, null, null, false);
                return new AdvancementEntity(UUID.randomUUID(), targetName, targetKey, "comment", "STONE", AdvancementFrameType.TASK, "\"title\"", null, null, project);
            }
        };
        AdvancementCopyController controller = new AdvancementCopyController(copierStub);
        UUID projectId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        UUID targetProjectId = UUID.randomUUID();
        CopyDTO body = new CopyDTO(targetProjectId, "new-key", "New Name");

        HttpResponse<AdvancementModelResponseDTO.AdvancementModelDTO> resp = controller.copy(projectId, id, body);

        assertEquals(200, resp.getStatus().getCode());
        assertEquals("new-key", resp.body().key());
        assertEquals("New Name", resp.body().uiName());
        assertEquals(targetProjectId, resp.body().projectId());
    }

    @Test
    @DisplayName("copy() lets a RESOURCE_CONFLICT from the copier reach the exception handler")
    void copy_keyTaken_propagates() {
        StubAdvancementCopier copierStub = new StubAdvancementCopier();
        copierStub.toThrow = ApiException.conflict("A advancement with key 'x' already exists in the target project.");
        AdvancementCopyController controller = new AdvancementCopyController(copierStub);
        UUID projectId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        ApiException exception = assertThrows(ApiException.class, () -> controller.copy(projectId, id, null));

        assertEquals(ErrorCode.RESOURCE_CONFLICT, exception.code());
    }
}
