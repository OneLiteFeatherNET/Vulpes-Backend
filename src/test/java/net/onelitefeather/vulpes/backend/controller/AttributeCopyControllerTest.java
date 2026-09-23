package net.onelitefeather.vulpes.backend.controller;

import io.micronaut.http.HttpResponse;
import net.onelitefeather.vulpes.api.model.AttributeEntity;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.backend.domain.copy.CopyDTO;
import net.onelitefeather.vulpes.backend.domain.attribute.AttributeModelDTO;
import net.onelitefeather.vulpes.backend.domain.attribute.AttributeModelResponseDTO;
import net.onelitefeather.vulpes.backend.domain.error.ErrorCode;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.copier.EntityCopier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit tests for AttributeCopyController (project-scoped)")
class AttributeCopyControllerTest {

    private static class StubAttributeCopier implements EntityCopier<AttributeEntity, Void> {
        AttributeEntity response;
        RuntimeException toThrow;

        @Override
        public AttributeEntity copy(UUID sourceProjectId, UUID sourceId, UUID targetProjectId, String targetKey, String targetName, Set<Void> relations) {
            if (toThrow != null) {
                throw toThrow;
            }
            return response;
        }
    }

    @Test
    @DisplayName("copy() with no body returns the copier's result wrapped in 200")
    void copy_noBody_returnsOk() {
        StubAttributeCopier copierStub = new StubAttributeCopier();
        ProjectEntity project = new ProjectEntity(UUID.randomUUID(), "Project A", "project-a", null, null, null, false);
        copierStub.response = new AttributeEntity(UUID.randomUUID(), "UI", "key", 1.0, 10.0, project);
        AttributeCopyController controller = new AttributeCopyController(copierStub);
        UUID projectId = project.getId();
        UUID id = UUID.randomUUID();

        HttpResponse<AttributeModelResponseDTO.AttributeModelDTO> resp = controller.copy(projectId, id, null);

        assertEquals(200, resp.getStatus().getCode());
        assertEquals("key", resp.body().key());
    }

    @Test
    @DisplayName("copy() passes the DTO's fields through to the copier")
    void copy_withBody_passesFieldsThrough() {
        StubAttributeCopier copierStub = new StubAttributeCopier() {
            @Override
            public AttributeEntity copy(UUID sourceProjectId, UUID sourceId, UUID targetProjectId, String targetKey, String targetName) {
                ProjectEntity project = new ProjectEntity(targetProjectId, "Target", "target", null, null, null, false);
                return new AttributeEntity(UUID.randomUUID(), targetName, targetKey, 1.0, 10.0, project);
            }
        };
        AttributeCopyController controller = new AttributeCopyController(copierStub);
        UUID projectId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        UUID targetProjectId = UUID.randomUUID();
        CopyDTO body = new CopyDTO(targetProjectId, "new-key", "New Name");

        HttpResponse<AttributeModelResponseDTO.AttributeModelDTO> resp = controller.copy(projectId, id, body);

        assertEquals(200, resp.getStatus().getCode());
        assertEquals("new-key", resp.body().key());
        assertEquals("New Name", resp.body().uiName());
        assertEquals(targetProjectId, resp.body().projectId());
    }

    @Test
    @DisplayName("copy() lets a RESOURCE_CONFLICT from the copier reach the exception handler")
    void copy_keyTaken_propagates() {
        StubAttributeCopier copierStub = new StubAttributeCopier();
        copierStub.toThrow = ApiException.conflict("A attribute with key 'x' already exists in the target project.");
        AttributeCopyController controller = new AttributeCopyController(copierStub);
        UUID projectId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        ApiException exception = assertThrows(ApiException.class, () -> controller.copy(projectId, id, null));

        assertEquals(ErrorCode.RESOURCE_CONFLICT, exception.code());
    }
}
