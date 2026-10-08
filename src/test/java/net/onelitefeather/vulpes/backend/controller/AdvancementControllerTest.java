package net.onelitefeather.vulpes.backend.controller;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import net.onelitefeather.vulpes.api.model.advancement.AdvancementEntity;
import net.onelitefeather.vulpes.api.model.advancement.AdvancementFrameType;
import net.onelitefeather.vulpes.backend.domain.error.ErrorCode;
import net.onelitefeather.vulpes.backend.domain.advancement.AdvancementModelDTO;
import net.onelitefeather.vulpes.backend.domain.advancement.AdvancementModelResponseDTO;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.service.AdvancementService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit tests for AdvancementController (project-scoped)")
class AdvancementControllerTest {

    private static class StubAdvancementService implements AdvancementService {
        AdvancementModelResponseDTO.AdvancementModelDTO response;
        Page<AdvancementModelResponseDTO.AdvancementModelDTO> page;
        Optional<AdvancementEntity> findByIdResponse = Optional.empty();

        @Override
        public AdvancementModelResponseDTO.AdvancementModelDTO create(AdvancementModelDTO dto) {
            return (AdvancementModelResponseDTO.AdvancementModelDTO) response;
        }

        @Override
        public AdvancementModelResponseDTO.AdvancementModelDTO update(AdvancementModelDTO dto) {
            return response;
        }

        @Override
        public AdvancementModelResponseDTO.AdvancementModelDTO delete(UUID id) {
            return response;
        }

        @Override
        public void deleteAll() {
        }

        @Override
        public Page<AdvancementModelResponseDTO.AdvancementModelDTO> getAll(Pageable pageable) {
            return page;
        }

        @Override
        public Optional<AdvancementEntity> findById(UUID id) {
            return findByIdResponse;
        }

        @Override
        public AdvancementModelResponseDTO.AdvancementModelDTO create(UUID projectId, AdvancementModelDTO dto) {
            return response;
        }

        @Override
        public AdvancementModelResponseDTO.AdvancementModelDTO update(UUID projectId, AdvancementModelDTO dto) {
            return response;
        }

        @Override
        public AdvancementModelResponseDTO.AdvancementModelDTO delete(UUID projectId, UUID id) {
            return response;
        }

        @Override
        public void deleteAll(UUID projectId) {
        }

        @Override
        public Page<AdvancementModelResponseDTO.AdvancementModelDTO> getAll(UUID projectId, Pageable pageable) {
            return page;
        }

        @Override
        public Optional<AdvancementEntity> findById(UUID projectId, UUID id) {
            return findByIdResponse;
        }
    }

    @Test
    void add_success_returnsOk() {
        StubAdvancementService stub = new StubAdvancementService();
        UUID projectId = UUID.randomUUID();
        AdvancementModelDTO dto = new AdvancementModelDTO(null, "UI", "var", "comment", "STONE", AdvancementFrameType.TASK, "\"title\"", null, null, null, 0f, 0f, true, true, false);
        stub.response = new AdvancementModelResponseDTO.AdvancementModelDTO(UUID.randomUUID(), "UI", "var", "comment", "STONE", AdvancementFrameType.TASK, "\"title\"", null, null, null, 0f, 0f, true, true, false, projectId, Instant.now(), Instant.now());
        AdvancementController controller = new AdvancementController(stub);

        HttpResponse<AdvancementModelResponseDTO.AdvancementModelDTO> resp = controller.add(projectId, dto);

        assertEquals(200, resp.getStatus().getCode());
        assertInstanceOf(AdvancementModelResponseDTO.AdvancementModelDTO.class, resp.body());
    }

    @Test
    @DisplayName("add() lets a PROJECT_NOT_FOUND from the service reach the exception handler")
    void add_unknownProject_propagates() {
        StubAdvancementService stub = new StubAdvancementService() {
            @Override
            public AdvancementModelResponseDTO.AdvancementModelDTO create(UUID projectId, AdvancementModelDTO dto) {
                throw ApiException.projectNotFound();
            }
        };
        AdvancementController controller = new AdvancementController(stub);
        AdvancementModelDTO dto = new AdvancementModelDTO(null, "UI", "var", "comment", "STONE", AdvancementFrameType.TASK, "\"title\"", null, null, null, 0f, 0f, true, true, false);
        UUID projectId = UUID.randomUUID();

        ApiException exception = assertThrows(ApiException.class, () -> controller.add(projectId, dto));

        assertEquals(ErrorCode.PROJECT_NOT_FOUND, exception.code());
    }

    @Test
    @DisplayName("getById() raises RESOURCE_NOT_FOUND when the entity belongs to another project")
    void getById_crossProject_raisesNotFound() {
        StubAdvancementService stub = new StubAdvancementService();
        stub.findByIdResponse = Optional.empty();
        AdvancementController controller = new AdvancementController(stub);
        UUID projectId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        ApiException exception = assertThrows(ApiException.class, () -> controller.getById(projectId, id));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.code());
    }

    @Test
    @DisplayName("remove() lets a RESOURCE_NOT_FOUND from the service reach the exception handler")
    void delete_crossProject_propagates() {
        StubAdvancementService stub = new StubAdvancementService() {
            @Override
            public AdvancementModelResponseDTO.AdvancementModelDTO delete(UUID projectId, UUID id) {
                throw ApiException.notFound("Advancement");
            }
        };
        AdvancementController controller = new AdvancementController(stub);
        UUID projectId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        ApiException exception = assertThrows(ApiException.class, () -> controller.remove(projectId, id));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.code());
    }

    @Test
    void getAll_returnsScopedPage() {
        StubAdvancementService stub = new StubAdvancementService();
        UUID projectId = UUID.randomUUID();
        var dto = new AdvancementModelResponseDTO.AdvancementModelDTO(UUID.randomUUID(), "UI", "var", "comment", "STONE", AdvancementFrameType.TASK, "\"title\"", null, null, null, 0f, 0f, true, true, false, projectId, Instant.now(), Instant.now());
        stub.page = Page.of(List.of(dto), Pageable.from(0, 10), 1L);
        AdvancementController controller = new AdvancementController(stub);

        HttpResponse<Page<AdvancementModelResponseDTO.AdvancementModelDTO>> resp = controller.getAll(projectId, Pageable.from(0, 10));

        assertEquals(200, resp.getStatus().getCode());
        assertEquals(1, resp.body().getTotalSize());
    }
}
