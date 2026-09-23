package net.onelitefeather.vulpes.backend.controller;

import io.micronaut.http.HttpResponse;
import net.onelitefeather.vulpes.api.model.NotificationEntity;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.backend.domain.error.ErrorCode;
import net.onelitefeather.vulpes.backend.domain.copy.CopyDTO;
import net.onelitefeather.vulpes.backend.domain.notification.NotificationModelDTO;
import net.onelitefeather.vulpes.backend.domain.notification.NotificationModelResponseDTO;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.copier.EntityCopier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit tests for NotificationCopyController (project-scoped)")
class NotificationCopyControllerTest {

    private static class StubNotificationCopier implements EntityCopier<NotificationEntity, Void> {
        NotificationEntity response;
        RuntimeException toThrow;

        @Override
        public NotificationEntity copy(UUID sourceProjectId, UUID sourceId, UUID targetProjectId, String targetKey, String targetName, Set<Void> relations) {
            if (toThrow != null) {
                throw toThrow;
            }
            return response;
        }
    }

    @Test
    @DisplayName("copy() with no body returns the copier's result wrapped in 200")
    void copy_noBody_returnsOk() {
        StubNotificationCopier copierStub = new StubNotificationCopier();
        ProjectEntity project = new ProjectEntity(UUID.randomUUID(), "Project A", "project-a", null, null, null, false);
        copierStub.response = new NotificationEntity(UUID.randomUUID(), "UI", "key", "comment", "STONE", "frame", "title", project);
        NotificationCopyController controller = new NotificationCopyController(copierStub);
        UUID projectId = project.getId();
        UUID id = UUID.randomUUID();

        HttpResponse<NotificationModelResponseDTO.NotificationModelDTO> resp = controller.copy(projectId, id, null);

        assertEquals(200, resp.getStatus().getCode());
        assertEquals("key", resp.body().key());
    }

    @Test
    @DisplayName("copy() passes the DTO's fields through to the copier")
    void copy_withBody_passesFieldsThrough() {
        StubNotificationCopier copierStub = new StubNotificationCopier() {
            @Override
            public NotificationEntity copy(UUID sourceProjectId, UUID sourceId, UUID targetProjectId, String targetKey, String targetName) {
                ProjectEntity project = new ProjectEntity(targetProjectId, "Target", "target", null, null, null, false);
                return new NotificationEntity(UUID.randomUUID(), targetName, targetKey, "comment", "STONE", "frame", "title", project);
            }
        };
        NotificationCopyController controller = new NotificationCopyController(copierStub);
        UUID projectId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        UUID targetProjectId = UUID.randomUUID();
        CopyDTO body = new CopyDTO(targetProjectId, "new-key", "New Name");

        HttpResponse<NotificationModelResponseDTO.NotificationModelDTO> resp = controller.copy(projectId, id, body);

        assertEquals(200, resp.getStatus().getCode());
        assertEquals("new-key", resp.body().key());
        assertEquals("New Name", resp.body().uiName());
        assertEquals(targetProjectId, resp.body().projectId());
    }

    @Test
    @DisplayName("copy() lets a RESOURCE_CONFLICT from the copier reach the exception handler")
    void copy_keyTaken_propagates() {
        StubNotificationCopier copierStub = new StubNotificationCopier();
        copierStub.toThrow = ApiException.conflict("A notification with key 'x' already exists in the target project.");
        NotificationCopyController controller = new NotificationCopyController(copierStub);
        UUID projectId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        ApiException exception = assertThrows(ApiException.class, () -> controller.copy(projectId, id, null));

        assertEquals(ErrorCode.RESOURCE_CONFLICT, exception.code());
    }
}
