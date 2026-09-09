package net.onelitefeather.vulpes.backend.controller.dimension;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import net.onelitefeather.vulpes.api.model.dimension.AttributeOperator;
import net.onelitefeather.vulpes.api.model.dimension.DimensionTypeEntity;
import net.onelitefeather.vulpes.api.model.dimension.EnvironmentAttributeKey;
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

@DisplayName("Unit tests for DimensionAttributeController")
class DimensionAttributeControllerTest {

    private static class StubDimensionService implements DimensionService {
        Page<DimensionAttributeResponseDTO.DimensionAttributeDTO> attributePage = Page.empty();
        DimensionAttributeResponseDTO.DimensionAttributeDTO attributeResponse;
        List<DimensionAttributeResponseDTO.DimensionAttributeDTO> deletedAttributes = List.of();

        @Override
        public DimensionModelResponseDTO.DimensionModelDTO create(DimensionModelDTO dto) {
            return null;
        }

        @Override
        public DimensionModelResponseDTO.DimensionModelDTO update(DimensionModelDTO dto) {
            return null;
        }

        @Override
        public DimensionModelResponseDTO.DimensionModelDTO delete(UUID id) {
            return null;
        }

        @Override
        public void deleteAll() {
        }

        @Override
        public Page<DimensionModelResponseDTO.DimensionModelDTO> getAll(Pageable pageable) {
            return Page.empty();
        }

        @Override
        public Optional<DimensionTypeEntity> findById(UUID id) {
            return Optional.empty();
        }

        @Override
        public DimensionModelResponseDTO.DimensionModelDTO create(UUID projectId, DimensionModelDTO dto) {
            return null;
        }

        @Override
        public DimensionModelResponseDTO.DimensionModelDTO update(UUID projectId, DimensionModelDTO dto) {
            return null;
        }

        @Override
        public DimensionModelResponseDTO.DimensionModelDTO delete(UUID projectId, UUID id) {
            return null;
        }

        @Override
        public void deleteAll(UUID projectId) {
        }

        @Override
        public Page<DimensionModelResponseDTO.DimensionModelDTO> getAll(UUID projectId, Pageable pageable) {
            return Page.empty();
        }

        @Override
        public Optional<DimensionTypeEntity> findById(UUID projectId, UUID id) {
            return Optional.empty();
        }

        @Override
        public Page<DimensionAttributeResponseDTO.DimensionAttributeDTO> findAttributesById(UUID id, Pageable pageable) {
            return attributePage;
        }

        @Override
        public DimensionAttributeResponseDTO.DimensionAttributeDTO createAttributeById(UUID id, DimensionAttributeDTO attribute) {
            return attributeResponse;
        }

        @Override
        public DimensionAttributeResponseDTO.DimensionAttributeDTO updateAttributeById(UUID id, DimensionAttributeDTO attribute) {
            return attributeResponse;
        }

        @Override
        public DimensionAttributeResponseDTO.DimensionAttributeDTO deleteAttributeById(UUID id, UUID attributeId) {
            return attributeResponse;
        }

        @Override
        public List<DimensionAttributeResponseDTO.DimensionAttributeDTO> deleteAllAttributesById(UUID id) {
            return deletedAttributes;
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

    private static DimensionAttributeResponseDTO.DimensionAttributeDTO sampleAttribute(UUID id) {
        return new DimensionAttributeResponseDTO.DimensionAttributeDTO(id, EnvironmentAttributeKey.FOG_COLOR, AttributeOperator.OVERRIDE, "#FFAA00");
    }

    @Test
    void getAttributesById_returnsPage() {
        StubDimensionService stub = new StubDimensionService();
        UUID dimensionId = UUID.randomUUID();
        stub.attributePage = Page.of(List.of(sampleAttribute(UUID.randomUUID())), Pageable.from(0, 10), 1L);
        DimensionAttributeController controller = new DimensionAttributeController(stub);

        HttpResponse<Page<DimensionAttributeResponseDTO.DimensionAttributeDTO>> resp =
                controller.getAttributesById(dimensionId, Pageable.from(0, 10));

        assertEquals(200, resp.getStatus().getCode());
        assertNotNull(resp.getBody());
        assertEquals(1, resp.body().getTotalSize());
    }

    @Test
    void createAttribute_success_returnsOk() {
        StubDimensionService stub = new StubDimensionService();
        UUID dimensionId = UUID.randomUUID();
        stub.attributeResponse = sampleAttribute(UUID.randomUUID());
        DimensionAttributeController controller = new DimensionAttributeController(stub);
        DimensionAttributeDTO dto = new DimensionAttributeDTO(null, EnvironmentAttributeKey.FOG_COLOR, AttributeOperator.OVERRIDE, "#FFAA00");

        HttpResponse<DimensionAttributeResponseDTO.DimensionAttributeDTO> resp = controller.createAttribute(dimensionId, dto);

        assertEquals(200, resp.getStatus().getCode());
        assertInstanceOf(DimensionAttributeResponseDTO.DimensionAttributeDTO.class, resp.body());
    }

    @Test
    @DisplayName("createAttribute() lets a RESOURCE_NOT_FOUND from the service reach the exception handler")
    void createAttribute_unknownDimension_propagates() {
        StubDimensionService stub = new StubDimensionService() {
            @Override
            public DimensionAttributeResponseDTO.DimensionAttributeDTO createAttributeById(UUID id, DimensionAttributeDTO attribute) {
                throw ApiException.notFound("Dimension");
            }
        };
        DimensionAttributeController controller = new DimensionAttributeController(stub);
        UUID dimensionId = UUID.randomUUID();
        DimensionAttributeDTO dto = new DimensionAttributeDTO(null, EnvironmentAttributeKey.FOG_COLOR, AttributeOperator.OVERRIDE, "#FFAA00");

        ApiException exception = assertThrows(ApiException.class, () -> controller.createAttribute(dimensionId, dto));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.code());
    }

    @Test
    void deleteAttribute_success_returnsOk() {
        StubDimensionService stub = new StubDimensionService();
        UUID dimensionId = UUID.randomUUID();
        UUID attributeId = UUID.randomUUID();
        stub.attributeResponse = sampleAttribute(attributeId);
        DimensionAttributeController controller = new DimensionAttributeController(stub);

        HttpResponse<DimensionAttributeResponseDTO.DimensionAttributeDTO> resp = controller.deleteAttribute(dimensionId, attributeId);

        assertEquals(200, resp.getStatus().getCode());
        assertNotNull(resp.body());
        assertEquals(attributeId, resp.body().id());
    }

    @Test
    void deleteAttributes_returnsDeletedList() {
        StubDimensionService stub = new StubDimensionService();
        UUID dimensionId = UUID.randomUUID();
        stub.deletedAttributes = List.of(sampleAttribute(UUID.randomUUID()), sampleAttribute(UUID.randomUUID()));
        DimensionAttributeController controller = new DimensionAttributeController(stub);

        HttpResponse<List<DimensionAttributeResponseDTO.DimensionAttributeDTO>> resp = controller.deleteAttributes(dimensionId);

        assertEquals(200, resp.getStatus().getCode());
        assertNotNull(resp.body());
        assertEquals(2, resp.body().size());
    }
}
