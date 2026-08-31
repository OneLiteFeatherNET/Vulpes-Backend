package net.onelitefeather.vulpes.backend.controller;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import net.onelitefeather.vulpes.api.model.NotificationEntity;
import net.onelitefeather.vulpes.backend.domain.error.ErrorCode;
import net.onelitefeather.vulpes.backend.domain.notification.NotificationModelDTO;
import net.onelitefeather.vulpes.backend.domain.notification.NotificationModelResponseDTO;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit tests for NotificationController (project-scoped)")
class NotificationControllerTest {

    private static class StubNotificationService implements NotificationService {
        NotificationModelResponseDTO.NotificationModelDTO response;
        Page<NotificationModelResponseDTO.NotificationModelDTO> page;
        Optional<NotificationEntity> findByIdResponse = Optional.empty();

        @Override
        public NotificationModelResponseDTO.NotificationModelDTO create(NotificationModelDTO dto) {
            return (NotificationModelResponseDTO.NotificationModelDTO) response;
        }

        @Override
        public NotificationModelResponseDTO.NotificationModelDTO update(NotificationModelDTO dto) {
            return response;
        }

        @Override
        public NotificationModelResponseDTO.NotificationModelDTO delete(UUID id) {
            return response;
        }

        @Override
        public void deleteAll() {
        }

        @Override
        public Page<NotificationModelResponseDTO.NotificationModelDTO> getAll(Pageable pageable) {
            return page;
        }

        @Override
        public Optional<NotificationEntity> findById(UUID id) {
            return findByIdResponse;
        }

        @Override
        public NotificationModelResponseDTO.NotificationModelDTO create(UUID projectId, NotificationModelDTO dto) {
            return response;
        }

        @Override
        public NotificationModelResponseDTO.NotificationModelDTO update(UUID projectId, NotificationModelDTO dto) {
            return response;
        }

        @Override
        public NotificationModelResponseDTO.NotificationModelDTO delete(UUID projectId, UUID id) {
            return response;
        }

        @Override
        public void deleteAll(UUID projectId) {
        }

        @Override
        public Page<NotificationModelResponseDTO.NotificationModelDTO> getAll(UUID projectId, Pageable pageable) {
            return page;
        }

        @Override
        public Optional<NotificationEntity> findById(UUID projectId, UUID id) {
            return findByIdResponse;
        }
    }

    @Test
    void add_success_returnsOk() {
        StubNotificationService stub = new StubNotificationService();
        UUID projectId = UUID.randomUUID();
        NotificationModelDTO dto = new NotificationModelDTO(null, "UI", "var", "comment", "STONE", "frame", "title");
        stub.response = new NotificationModelResponseDTO.NotificationModelDTO(UUID.randomUUID(), "UI", "var", "comment", "STONE", "frame", "title", projectId);
        NotificationController controller = new NotificationController(stub);

        HttpResponse<NotificationModelResponseDTO.NotificationModelDTO> resp = controller.add(projectId, dto);

        assertEquals(200, resp.getStatus().getCode());
        assertInstanceOf(NotificationModelResponseDTO.NotificationModelDTO.class, resp.body());
    }

    @Test
    @DisplayName("add() lets a PROJECT_NOT_FOUND from the service reach the exception handler")
    void add_unknownProject_propagates() {
        StubNotificationService stub = new StubNotificationService() {
            @Override
            public NotificationModelResponseDTO.NotificationModelDTO create(UUID projectId, NotificationModelDTO dto) {
                throw ApiException.projectNotFound();
            }
        };
        NotificationController controller = new NotificationController(stub);
        NotificationModelDTO dto = new NotificationModelDTO(null, "UI", "var", "comment", "STONE", "frame", "title");
        UUID projectId = UUID.randomUUID();

        ApiException exception = assertThrows(ApiException.class, () -> controller.add(projectId, dto));

        assertEquals(ErrorCode.PROJECT_NOT_FOUND, exception.code());
    }

    @Test
    @DisplayName("getById() raises RESOURCE_NOT_FOUND when the entity belongs to another project")
    void getById_crossProject_raisesNotFound() {
        StubNotificationService stub = new StubNotificationService();
        stub.findByIdResponse = Optional.empty();
        NotificationController controller = new NotificationController(stub);
        UUID projectId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        ApiException exception = assertThrows(ApiException.class, () -> controller.getById(projectId, id));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.code());
    }

    @Test
    @DisplayName("remove() lets a RESOURCE_NOT_FOUND from the service reach the exception handler")
    void delete_crossProject_propagates() {
        StubNotificationService stub = new StubNotificationService() {
            @Override
            public NotificationModelResponseDTO.NotificationModelDTO delete(UUID projectId, UUID id) {
                throw ApiException.notFound("Notification");
            }
        };
        NotificationController controller = new NotificationController(stub);
        UUID projectId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        ApiException exception = assertThrows(ApiException.class, () -> controller.remove(projectId, id));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.code());
    }

    @Test
    void getAll_returnsScopedPage() {
        StubNotificationService stub = new StubNotificationService();
        UUID projectId = UUID.randomUUID();
        var dto = new NotificationModelResponseDTO.NotificationModelDTO(UUID.randomUUID(), "UI", "var", "comment", "STONE", "frame", "title", projectId);
        stub.page = Page.of(List.of(dto), Pageable.from(0, 10), 1L);
        NotificationController controller = new NotificationController(stub);

        HttpResponse<Page<NotificationModelResponseDTO.NotificationModelDTO>> resp = controller.getAll(projectId, Pageable.from(0, 10));

        assertEquals(200, resp.getStatus().getCode());
        assertEquals(1, resp.body().getTotalSize());
    }
}
