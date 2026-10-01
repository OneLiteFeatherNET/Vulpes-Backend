package net.onelitefeather.vulpes.backend.controller.font;

import io.micronaut.http.HttpResponse;
import net.onelitefeather.vulpes.api.model.FontEntity;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.backend.copier.EntityCopier;
import net.onelitefeather.vulpes.backend.domain.copy.RelationalCopyDTO;
import net.onelitefeather.vulpes.backend.domain.error.ErrorCode;
import net.onelitefeather.vulpes.backend.domain.font.FontModelDTO;
import net.onelitefeather.vulpes.backend.domain.font.FontModelResponseDTO;
import net.onelitefeather.vulpes.backend.domain.font.FontRelation;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit tests for FontCopyController (project-scoped)")
class FontCopyControllerTest {

    private static class StubFontCopier implements EntityCopier<FontEntity, FontRelation> {
        FontEntity response;
        RuntimeException toThrow;

        @Override
        public FontEntity copy(UUID sourceProjectId, UUID sourceId, UUID targetProjectId, String targetKey, String targetName, Set<FontRelation> relations) {
            if (toThrow != null) {
                throw toThrow;
            }
            return response;
        }
    }

    private static FontEntity sampleEntity(UUID id, ProjectEntity project) {
        return new FontEntity(id, "UI", "key", "provider", "texture", "comment", 1, 1, List.of(), project);
    }

    @Test
    @DisplayName("copy() with no body returns the copier's result wrapped in 200")
    void copy_noBody_returnsOk() {
        StubFontCopier copierStub = new StubFontCopier();
        ProjectEntity project = new ProjectEntity(UUID.randomUUID(), "Project A", "project-a", null, null, null, false);
        copierStub.response = sampleEntity(UUID.randomUUID(), project);
        FontCopyController controller = new FontCopyController(copierStub);
        UUID projectId = project.getId();
        UUID fontId = UUID.randomUUID();

        HttpResponse<FontModelResponseDTO.FontModelDTO> resp = controller.copy(projectId, fontId, null);

        assertEquals(200, resp.getStatus().getCode());
        assertEquals("key", resp.body().key());
    }

    @Test
    @DisplayName("copy() passes the DTO's fields through to the copier")
    void copy_withBody_passesFieldsThrough() {
        StubFontCopier copierStub = new StubFontCopier() {
            @Override
            public FontEntity copy(UUID sourceProjectId, UUID sourceId, UUID targetProjectId, String targetKey, String targetName, Set<FontRelation> relations) {
                ProjectEntity project = new ProjectEntity(targetProjectId, "Target", "target", null, null, null, false);
                return new FontEntity(UUID.randomUUID(), targetName, targetKey, "provider", "texture", "comment", 1, 1, List.of(), project);
            }
        };
        FontCopyController controller = new FontCopyController(copierStub);
        UUID projectId = UUID.randomUUID();
        UUID fontId = UUID.randomUUID();
        UUID targetProjectId = UUID.randomUUID();
        RelationalCopyDTO<FontRelation> body = new RelationalCopyDTO<>(targetProjectId, "new-key", "New Name", Set.of(FontRelation.CHARS));

        HttpResponse<FontModelResponseDTO.FontModelDTO> resp = controller.copy(projectId, fontId, body);

        assertEquals(200, resp.getStatus().getCode());
        assertEquals("new-key", resp.body().key());
        assertEquals("New Name", resp.body().uiName());
        assertEquals(targetProjectId, resp.body().projectId());
    }

    @Test
    @DisplayName("copy() lets a RESOURCE_CONFLICT from the copier reach the exception handler")
    void copy_keyTaken_propagates() {
        StubFontCopier copierStub = new StubFontCopier();
        copierStub.toThrow = ApiException.conflict("A font with key 'x' already exists in the target project.");
        FontCopyController controller = new FontCopyController(copierStub);
        UUID projectId = UUID.randomUUID();
        UUID fontId = UUID.randomUUID();

        ApiException exception = assertThrows(ApiException.class, () -> controller.copy(projectId, fontId, null));

        assertEquals(ErrorCode.RESOURCE_CONFLICT, exception.code());
    }
}
