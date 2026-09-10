package net.onelitefeather.vulpes.backend.controller.dimension;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import net.onelitefeather.vulpes.api.model.dimension.DimensionTypeEntity;
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

@DisplayName("Unit tests for DimensionTimelineController")
class DimensionTimelineControllerTest {

    private static class StubDimensionService implements DimensionService {
        Page<DimensionTimelineResponseDTO.DimensionTimelineDTO> timelinePage = Page.empty();
        DimensionTimelineResponseDTO.DimensionTimelineDTO timelineResponse;
        List<DimensionTimelineResponseDTO.DimensionTimelineDTO> deletedTimelines = List.of();

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
            return timelinePage;
        }

        @Override
        public DimensionTimelineResponseDTO.DimensionTimelineDTO createTimelineById(UUID id, DimensionTimelineDTO timeline) {
            return timelineResponse;
        }

        @Override
        public DimensionTimelineResponseDTO.DimensionTimelineDTO updateTimelineById(UUID id, DimensionTimelineDTO timeline) {
            return timelineResponse;
        }

        @Override
        public DimensionTimelineResponseDTO.DimensionTimelineDTO deleteTimelineById(UUID id, UUID timelineId) {
            return timelineResponse;
        }

        @Override
        public List<DimensionTimelineResponseDTO.DimensionTimelineDTO> deleteAllTimelinesById(UUID id) {
            return deletedTimelines;
        }
    }

    private static DimensionTimelineResponseDTO.DimensionTimelineDTO sampleTimeline(UUID id) {
        return new DimensionTimelineResponseDTO.DimensionTimelineDTO(id, "minestom:day_night");
    }

    @Test
    void getTimelinesById_returnsPage() {
        StubDimensionService stub = new StubDimensionService();
        UUID dimensionId = UUID.randomUUID();
        stub.timelinePage = Page.of(List.of(sampleTimeline(UUID.randomUUID())), Pageable.from(0, 10), 1L);
        DimensionTimelineController controller = new DimensionTimelineController(stub);

        HttpResponse<Page<DimensionTimelineResponseDTO.DimensionTimelineDTO>> resp =
                controller.getTimelinesById(dimensionId, Pageable.from(0, 10));

        assertEquals(200, resp.getStatus().getCode());
        assertEquals(1, resp.body().getTotalSize());
    }

    @Test
    void createTimeline_success_returnsOk() {
        StubDimensionService stub = new StubDimensionService();
        UUID dimensionId = UUID.randomUUID();
        stub.timelineResponse = sampleTimeline(UUID.randomUUID());
        DimensionTimelineController controller = new DimensionTimelineController(stub);
        DimensionTimelineDTO dto = new DimensionTimelineDTO(null, "minestom:day_night");

        HttpResponse<DimensionTimelineResponseDTO.DimensionTimelineDTO> resp = controller.createTimeline(dimensionId, dto);

        assertEquals(200, resp.getStatus().getCode());
        assertInstanceOf(DimensionTimelineResponseDTO.DimensionTimelineDTO.class, resp.body());
    }

    @Test
    @DisplayName("createTimeline() lets a RESOURCE_NOT_FOUND from the service reach the exception handler")
    void createTimeline_unknownDimension_propagates() {
        StubDimensionService stub = new StubDimensionService() {
            @Override
            public DimensionTimelineResponseDTO.DimensionTimelineDTO createTimelineById(UUID id, DimensionTimelineDTO timeline) {
                throw ApiException.notFound("Dimension");
            }
        };
        DimensionTimelineController controller = new DimensionTimelineController(stub);
        UUID dimensionId = UUID.randomUUID();
        DimensionTimelineDTO dto = new DimensionTimelineDTO(null, "minestom:day_night");

        ApiException exception = assertThrows(ApiException.class, () -> controller.createTimeline(dimensionId, dto));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.code());
    }

    @Test
    void deleteTimeline_success_returnsOk() {
        StubDimensionService stub = new StubDimensionService();
        UUID dimensionId = UUID.randomUUID();
        UUID timelineId = UUID.randomUUID();
        stub.timelineResponse = sampleTimeline(timelineId);
        DimensionTimelineController controller = new DimensionTimelineController(stub);

        HttpResponse<DimensionTimelineResponseDTO.DimensionTimelineDTO> resp = controller.deleteTimeline(dimensionId, timelineId);

        assertEquals(200, resp.getStatus().getCode());
        assertEquals(timelineId, resp.body().id());
    }

    @Test
    void deleteTimelines_returnsDeletedList() {
        StubDimensionService stub = new StubDimensionService();
        UUID dimensionId = UUID.randomUUID();
        stub.deletedTimelines = List.of(sampleTimeline(UUID.randomUUID()), sampleTimeline(UUID.randomUUID()));
        DimensionTimelineController controller = new DimensionTimelineController(stub);

        HttpResponse<List<DimensionTimelineResponseDTO.DimensionTimelineDTO>> resp = controller.deleteTimelines(dimensionId);

        assertEquals(200, resp.getStatus().getCode());
        assertEquals(2, resp.body().size());
    }
}
