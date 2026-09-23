package net.onelitefeather.vulpes.backend.controller.item;

import io.micronaut.http.HttpResponse;
import net.onelitefeather.vulpes.api.model.ItemEntity;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.backend.domain.error.ErrorCode;
import net.onelitefeather.vulpes.backend.domain.copy.RelationalCopyDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemModelDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemModelResponseDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemRelation;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.copier.EntityCopier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit tests for ItemCopyController (project-scoped)")
class ItemCopyControllerTest {

    private static class StubItemCopier implements EntityCopier<ItemEntity, ItemRelation> {
        ItemEntity response;
        RuntimeException toThrow;

        @Override
        public ItemEntity copy(UUID sourceProjectId, UUID sourceId, UUID targetProjectId, String targetKey, String targetName, Set<ItemRelation> relations) {
            if (toThrow != null) {
                throw toThrow;
            }
            return response;
        }
    }

    private static ItemEntity sampleEntity(UUID id, ProjectEntity project) {
        return new ItemEntity(id, "UI", "key", "comment", "display", "STONE", "group", 0, 1, List.of(), List.of(), List.of(), project);
    }

    @Test
    @DisplayName("copy() with no body returns the copier's result wrapped in 200")
    void copy_noBody_returnsOk() {
        StubItemCopier copierStub = new StubItemCopier();
        ProjectEntity project = new ProjectEntity(UUID.randomUUID(), "Project A", "project-a", null, null, null, false);
        copierStub.response = sampleEntity(UUID.randomUUID(), project);
        ItemCopyController controller = new ItemCopyController(copierStub);
        UUID projectId = project.getId();
        UUID itemId = UUID.randomUUID();

        HttpResponse<ItemModelResponseDTO.ItemModelDTO> resp = controller.copy(projectId, itemId, null);

        assertEquals(200, resp.getStatus().getCode());
        assertEquals("key", resp.body().key());
    }

    @Test
    @DisplayName("copy() passes the DTO's fields through to the copier")
    void copy_withBody_passesFieldsThrough() {
        StubItemCopier copierStub = new StubItemCopier() {
            @Override
            public ItemEntity copy(UUID sourceProjectId, UUID sourceId, UUID targetProjectId, String targetKey, String targetName, Set<ItemRelation> relations) {
                ProjectEntity project = new ProjectEntity(targetProjectId, "Target", "target", null, null, null, false);
                return new ItemEntity(UUID.randomUUID(), targetName, targetKey, "comment", "display", "STONE", "group", 0, 1, List.of(), List.of(), List.of(), project);
            }
        };
        ItemCopyController controller = new ItemCopyController(copierStub);
        UUID projectId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        UUID targetProjectId = UUID.randomUUID();
        RelationalCopyDTO<ItemRelation> body = new RelationalCopyDTO<>(targetProjectId, "new-key", "New Name", Set.of(ItemRelation.LORE, ItemRelation.FLAGS));

        HttpResponse<ItemModelResponseDTO.ItemModelDTO> resp = controller.copy(projectId, itemId, body);

        assertEquals(200, resp.getStatus().getCode());
        assertEquals("new-key", resp.body().key());
        assertEquals("New Name", resp.body().uiName());
        assertEquals(targetProjectId, resp.body().projectId());
    }

    @Test
    @DisplayName("copy() lets a RESOURCE_CONFLICT from the copier reach the exception handler")
    void copy_keyTaken_propagates() {
        StubItemCopier copierStub = new StubItemCopier();
        copierStub.toThrow = ApiException.conflict("A item with key 'x' already exists in the target project.");
        ItemCopyController controller = new ItemCopyController(copierStub);
        UUID projectId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        ApiException exception = assertThrows(ApiException.class, () -> controller.copy(projectId, itemId, null));

        assertEquals(ErrorCode.RESOURCE_CONFLICT, exception.code());
    }
}
