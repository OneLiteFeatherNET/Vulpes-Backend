package net.onelitefeather.vulpes.backend.controller.dimension;

import io.micronaut.http.HttpResponse;
import net.onelitefeather.vulpes.api.model.dimension.CardinalLight;
import net.onelitefeather.vulpes.api.model.dimension.DimensionTypeEntity;
import net.onelitefeather.vulpes.api.model.dimension.Skybox;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.backend.copier.EntityCopier;
import net.onelitefeather.vulpes.backend.domain.copy.RelationalCopyDTO;
import net.onelitefeather.vulpes.backend.domain.dimension.DimensionModelDTO;
import net.onelitefeather.vulpes.backend.domain.dimension.DimensionModelResponseDTO;
import net.onelitefeather.vulpes.backend.domain.dimension.DimensionRelation;
import net.onelitefeather.vulpes.backend.domain.error.ErrorCode;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit tests for DimensionCopyController (project-scoped)")
class DimensionCopyControllerTest {

    private static class StubDimensionCopier implements EntityCopier<DimensionTypeEntity, DimensionRelation> {
        DimensionTypeEntity response;
        RuntimeException toThrow;

        @Override
        public DimensionTypeEntity copy(UUID sourceProjectId, UUID sourceId, UUID targetProjectId, String targetKey, String targetName, Set<DimensionRelation> relations) {
            if (toThrow != null) {
                throw toThrow;
            }
            return response;
        }
    }

    private static DimensionTypeEntity sampleEntity(UUID id, String key, ProjectEntity project) {
        return new DimensionTypeEntity(
                id, "Overworld", key, false, true, false, false,
                1.0, -64, 384, 384, "#minecraft:infiniburn_overworld", 0.0f,
                "constant:11", 15, Skybox.OVERWORLD, CardinalLight.DEFAULT, null,
                List.of(), List.of(), project
        );
    }

    @Test
    @DisplayName("copy() with no body returns the copier's result wrapped in 200")
    void copy_noBody_returnsOk() {
        StubDimensionCopier copierStub = new StubDimensionCopier();
        ProjectEntity project = new ProjectEntity(UUID.randomUUID(), "Project A", "project-a", null, null, null, false);
        copierStub.response = sampleEntity(UUID.randomUUID(), "key", project);
        DimensionCopyController controller = new DimensionCopyController(copierStub);
        UUID projectId = project.getId();
        UUID dimensionId = UUID.randomUUID();

        HttpResponse<DimensionModelResponseDTO.DimensionModelDTO> resp = controller.copy(projectId, dimensionId, null);

        assertEquals(200, resp.getStatus().getCode());
        assertEquals("key", resp.body().key());
    }

    @Test
    @DisplayName("copy() passes the DTO's fields through to the copier")
    void copy_withBody_passesFieldsThrough() {
        StubDimensionCopier copierStub = new StubDimensionCopier() {
            @Override
            public DimensionTypeEntity copy(UUID sourceProjectId, UUID sourceId, UUID targetProjectId, String targetKey, String targetName, Set<DimensionRelation> relations) {
                ProjectEntity project = new ProjectEntity(targetProjectId, "Target", "target", null, null, null, false);
                DimensionTypeEntity entity = sampleEntity(UUID.randomUUID(), targetKey, project);
                entity.setUiName(targetName);
                return entity;
            }
        };
        DimensionCopyController controller = new DimensionCopyController(copierStub);
        UUID projectId = UUID.randomUUID();
        UUID dimensionId = UUID.randomUUID();
        UUID targetProjectId = UUID.randomUUID();
        RelationalCopyDTO<DimensionRelation> body = new RelationalCopyDTO<>(targetProjectId, "new-key", "New Name", Set.of(DimensionRelation.ATTRIBUTES, DimensionRelation.TIMELINES));

        HttpResponse<DimensionModelResponseDTO.DimensionModelDTO> resp = controller.copy(projectId, dimensionId, body);

        assertEquals(200, resp.getStatus().getCode());
        assertEquals("new-key", resp.body().key());
        assertEquals("New Name", resp.body().uiName());
        assertEquals(targetProjectId, resp.body().projectId());
    }

    @Test
    @DisplayName("copy() lets a RESOURCE_CONFLICT from the copier reach the exception handler")
    void copy_keyTaken_propagates() {
        StubDimensionCopier copierStub = new StubDimensionCopier();
        copierStub.toThrow = ApiException.conflict("A dimension type with key 'x' already exists in the target project.");
        DimensionCopyController controller = new DimensionCopyController(copierStub);
        UUID projectId = UUID.randomUUID();
        UUID dimensionId = UUID.randomUUID();

        ApiException exception = assertThrows(ApiException.class, () -> controller.copy(projectId, dimensionId, null));

        assertEquals(ErrorCode.RESOURCE_CONFLICT, exception.code());
    }
}
