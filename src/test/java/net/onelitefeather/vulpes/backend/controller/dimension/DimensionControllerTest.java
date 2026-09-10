package net.onelitefeather.vulpes.backend.controller.dimension;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import net.onelitefeather.vulpes.api.model.dimension.CardinalLight;
import net.onelitefeather.vulpes.api.model.dimension.DimensionTypeEntity;
import net.onelitefeather.vulpes.api.model.dimension.Skybox;
import net.onelitefeather.vulpes.backend.domain.dimension.DimensionAttributeDTO;
import net.onelitefeather.vulpes.backend.domain.dimension.DimensionAttributeResponseDTO;
import net.onelitefeather.vulpes.backend.domain.dimension.DimensionModelDTO;
import net.onelitefeather.vulpes.backend.domain.dimension.DimensionModelResponseDTO;
import net.onelitefeather.vulpes.backend.domain.dimension.DimensionTimelineDTO;
import net.onelitefeather.vulpes.backend.domain.dimension.DimensionTimelineResponseDTO;
import net.onelitefeather.vulpes.backend.domain.error.ErrorCode;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.service.DimensionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit tests for DimensionController (project-scoped)")
class DimensionControllerTest {

    private static class StubDimensionService implements DimensionService {
        DimensionModelResponseDTO.DimensionModelDTO response;
        Page<DimensionModelResponseDTO.DimensionModelDTO> page;
        Optional<DimensionTypeEntity> findByIdResponse = Optional.empty();

        @Override
        public DimensionModelResponseDTO.DimensionModelDTO create(DimensionModelDTO dto) {
            return response;
        }

        @Override
        public DimensionModelResponseDTO.DimensionModelDTO update(DimensionModelDTO dto) {
            return response;
        }

        @Override
        public DimensionModelResponseDTO.DimensionModelDTO delete(UUID id) {
            return response;
        }

        @Override
        public void deleteAll() {
        }

        @Override
        public Page<DimensionModelResponseDTO.DimensionModelDTO> getAll(Pageable pageable) {
            return page;
        }

        @Override
        public Optional<DimensionTypeEntity> findById(UUID id) {
            return findByIdResponse;
        }

        @Override
        public DimensionModelResponseDTO.DimensionModelDTO create(UUID projectId, DimensionModelDTO dto) {
            return response;
        }

        @Override
        public DimensionModelResponseDTO.DimensionModelDTO update(UUID projectId, DimensionModelDTO dto) {
            return response;
        }

        @Override
        public DimensionModelResponseDTO.DimensionModelDTO delete(UUID projectId, UUID id) {
            return response;
        }

        @Override
        public void deleteAll(UUID projectId) {
        }

        @Override
        public Page<DimensionModelResponseDTO.DimensionModelDTO> getAll(UUID projectId, Pageable pageable) {
            return page;
        }

        @Override
        public Optional<DimensionTypeEntity> findById(UUID projectId, UUID id) {
            return findByIdResponse;
        }

        @Override
        public Page<DimensionAttributeResponseDTO.DimensionAttributeDTO> findAttributesById(UUID id, Pageable pageable) {
            return Page.empty();
        }

        @Override
        public DimensionAttributeResponseDTO.DimensionAttributeDTO createAttributeById(UUID id, DimensionAttributeDTO attribute) {
            return null;
        }

        @Override
        public DimensionAttributeResponseDTO.DimensionAttributeDTO updateAttributeById(UUID id, DimensionAttributeDTO attribute) {
            return null;
        }

        @Override
        public DimensionAttributeResponseDTO.DimensionAttributeDTO deleteAttributeById(UUID id, UUID attributeId) {
            return null;
        }

        @Override
        public List<DimensionAttributeResponseDTO.DimensionAttributeDTO> deleteAllAttributesById(UUID id) {
            return List.of();
        }

        @Override
        public Page<DimensionTimelineResponseDTO.DimensionTimelineDTO> findTimelinesById(UUID id, Pageable pageable) {
            return Page.empty();
        }

        @Override
        public DimensionTimelineResponseDTO.DimensionTimelineDTO createTimelineById(UUID id, DimensionTimelineDTO timeline) {
            return null;
        }

        @Override
        public DimensionTimelineResponseDTO.DimensionTimelineDTO updateTimelineById(UUID id, DimensionTimelineDTO timeline) {
            return null;
        }

        @Override
        public DimensionTimelineResponseDTO.DimensionTimelineDTO deleteTimelineById(UUID id, UUID timelineId) {
            return null;
        }

        @Override
        public List<DimensionTimelineResponseDTO.DimensionTimelineDTO> deleteAllTimelinesById(UUID id) {
            return List.of();
        }
    }

    private static DimensionModelDTO sampleDTO(UUID id) {
        return new DimensionModelDTO(
                id, "Overworld", "overworld", false, true, false, false,
                1.0, -64, 384, 384, "#minecraft:infiniburn_overworld", 0.0f,
                "constant:11", 15, Skybox.OVERWORLD, CardinalLight.DEFAULT, null
        );
    }

    private static DimensionModelResponseDTO.DimensionModelDTO sampleResponse(UUID id, UUID projectId) {
        return new DimensionModelResponseDTO.DimensionModelDTO(
                id, "Overworld", "overworld", false, true, false, false,
                1.0, -64, 384, 384, "#minecraft:infiniburn_overworld", 0.0f,
                "constant:11", 15, Skybox.OVERWORLD, CardinalLight.DEFAULT, null, projectId
        );
    }

    @Test
    void add_success_returnsOk() {
        StubDimensionService stub = new StubDimensionService();
        UUID projectId = UUID.randomUUID();
        stub.response = sampleResponse(UUID.randomUUID(), projectId);
        DimensionController controller = new DimensionController(stub);

        HttpResponse<DimensionModelResponseDTO.DimensionModelDTO> resp = controller.add(projectId, sampleDTO(null));

        assertEquals(200, resp.getStatus().getCode());
        assertInstanceOf(DimensionModelResponseDTO.DimensionModelDTO.class, resp.body());
    }

    @Test
    @DisplayName("add() lets a PROJECT_NOT_FOUND from the service reach the exception handler")
    void add_unknownProject_propagates() {
        StubDimensionService stub = new StubDimensionService() {
            @Override
            public DimensionModelResponseDTO.DimensionModelDTO create(UUID projectId, DimensionModelDTO dto) {
                throw ApiException.projectNotFound();
            }
        };
        DimensionController controller = new DimensionController(stub);
        DimensionModelDTO dto = sampleDTO(null);
        UUID projectId = UUID.randomUUID();

        ApiException exception = assertThrows(ApiException.class, () -> controller.add(projectId, dto));

        assertEquals(ErrorCode.PROJECT_NOT_FOUND, exception.code());
    }

    @Test
    @DisplayName("getById() raises RESOURCE_NOT_FOUND when the entity belongs to another project")
    void getById_crossProject_raisesNotFound() {
        StubDimensionService stub = new StubDimensionService();
        stub.findByIdResponse = Optional.empty();
        DimensionController controller = new DimensionController(stub);
        UUID projectId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        ApiException exception = assertThrows(ApiException.class, () -> controller.getById(projectId, id));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.code());
    }

    @Test
    void getAll_returnsScopedPage() {
        StubDimensionService stub = new StubDimensionService();
        UUID projectId = UUID.randomUUID();
        stub.page = Page.of(List.of(sampleResponse(UUID.randomUUID(), projectId)), Pageable.from(0, 10), 1L);
        DimensionController controller = new DimensionController(stub);

        HttpResponse<Page<DimensionModelResponseDTO.DimensionModelDTO>> resp = controller.getAll(projectId, Pageable.from(0, 10));

        assertEquals(200, resp.getStatus().getCode());
        assertEquals(1, resp.body().getTotalSize());
    }
}
