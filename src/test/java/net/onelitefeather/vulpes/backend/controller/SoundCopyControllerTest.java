package net.onelitefeather.vulpes.backend.controller;

import io.micronaut.http.HttpResponse;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.api.model.sound.SoundEventEntity;
import net.onelitefeather.vulpes.backend.controller.sound.SoundCopyController;
import net.onelitefeather.vulpes.backend.copier.EntityCopier;
import net.onelitefeather.vulpes.backend.domain.copy.RelationalCopyDTO;
import net.onelitefeather.vulpes.backend.domain.error.ErrorCode;
import net.onelitefeather.vulpes.backend.domain.sound.SoundRelation;
import net.onelitefeather.vulpes.backend.domain.sound.SoundResponseDTO;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit tests for SoundCopyController (implementation tests)")
class SoundCopyControllerTest {

    private static class StubSoundCopier implements EntityCopier<SoundEventEntity, SoundRelation> {
        SoundEventEntity response;
        RuntimeException toThrow;

        @Override
        public SoundEventEntity copy(UUID sourceProjectId, UUID sourceId, UUID targetProjectId, String targetKey, String targetName, Set<SoundRelation> relations) {
            if (toThrow != null) {
                throw toThrow;
            }
            return response;
        }
    }

    private static net.onelitefeather.vulpes.api.model.project.ProjectEntity sampleProject(UUID id) {
        return new net.onelitefeather.vulpes.api.model.project.ProjectEntity(id, "Test Project", "test-project", null, null, null, false);
    }

    @Test
    @DisplayName("copy() with no body returns the copier's result wrapped in 200")
    void testCopy_noBody_returnsOk() {
        StubSoundCopier copierStub = new StubSoundCopier();
        ProjectEntity project = sampleProject(UUID.randomUUID());
        copierStub.response = new SoundEventEntity(UUID.randomUUID(), "UI", "key", "key-name", false, "subtitle", List.of(), project);
        SoundCopyController controller = new SoundCopyController(copierStub);
        UUID projectId = project.getId();
        UUID id = UUID.randomUUID();

        HttpResponse<SoundResponseDTO.SoundModelDTO> resp = controller.copy(projectId, id, null);

        assertEquals(200, resp.getStatus().getCode());
        assertEquals("key", resp.body().key());
    }

    @Test
    @DisplayName("copy() passes the DTO's fields through to the copier")
    void testCopy_withBody_passesFieldsThrough() {
        StubSoundCopier copierStub = new StubSoundCopier() {
            @Override
            public SoundEventEntity copy(UUID sourceProjectId, UUID sourceId, UUID targetProjectId, String targetKey, String targetName, Set<SoundRelation> relations) {
                ProjectEntity project = sampleProject(targetProjectId);
                return new SoundEventEntity(UUID.randomUUID(), targetName, targetKey, "key-name", false, "subtitle", List.of(), project);
            }
        };
        SoundCopyController controller = new SoundCopyController(copierStub);
        UUID projectId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        UUID targetProjectId = UUID.randomUUID();
        RelationalCopyDTO<SoundRelation> body = new RelationalCopyDTO<>(targetProjectId, "new-key", "New Name", Set.of(SoundRelation.SOURCES));

        HttpResponse<SoundResponseDTO.SoundModelDTO> resp = controller.copy(projectId, id, body);

        assertEquals(200, resp.getStatus().getCode());
        assertEquals("new-key", resp.body().key());
        assertEquals("New Name", resp.body().uiName());
        assertEquals(targetProjectId, resp.body().projectId());
    }

    @Test
    @DisplayName("copy() lets a RESOURCE_CONFLICT from the copier reach the exception handler")
    void testCopy_keyTaken_propagates() {
        StubSoundCopier copierStub = new StubSoundCopier();
        copierStub.toThrow = ApiException.conflict("A sound event with key 'x' already exists in the target project.");
        SoundCopyController controller = new SoundCopyController(copierStub);
        UUID projectId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        ApiException exception = assertThrows(ApiException.class, () -> controller.copy(projectId, id, null));

        assertEquals(ErrorCode.RESOURCE_CONFLICT, exception.code());
    }
}
