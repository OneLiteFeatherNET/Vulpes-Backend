package net.onelitefeather.vulpes.backend.controller;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import net.onelitefeather.vulpes.api.model.NotificationEntity;
import net.onelitefeather.vulpes.backend.domain.notification.NotificationModelDTO;
import net.onelitefeather.vulpes.backend.domain.notification.NotificationModelResponseDTO;
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
        NotificationModelResponseDTO response;
        Page<NotificationModelResponseDTO.NotificationModelDTO> page;
        Optional<NotificationEntity> findByIdResponse = Optional.empty();

        @Override
        public NotificationModelResponseDTO.NotificationModelDTO create(NotificationModelDTO dto) {
            return (NotificationModelResponseDTO.NotificationModelDTO) response;
        }

        @Override
        public NotificationModelResponseDTO update(NotificationModelDTO dto) {
            return response;
        }

        @Override
        public NotificationModelResponseDTO delete(UUID id) {
            return response;
        }

        @Override
        public List<NotificationModelResponseDTO> deleteAll() {
            return List.of();
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
        public NotificationModelResponseDTO create(UUID projectId, NotificationModelDTO dto) {
            return response;
        }

        @Override
        public NotificationModelResponseDTO update(UUID projectId, NotificationModelDTO dto) {
            return response;
        }

        @Override
        public NotificationModelResponseDTO delete(UUID projectId, UUID id) {
            return response;
        }

        @Override
        public List<NotificationModelResponseDTO> deleteAll(UUID projectId) {
            return List.of();
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

        HttpResponse<NotificationModelResponseDTO> resp = controller.add(projectId, dto);

        assertEquals(200, resp.getStatus().getCode());
        assertInstanceOf(NotificationModelResponseDTO.NotificationModelDTO.class, resp.body());
    }

    @Test
    void add_unknownProject_returns404() {
        StubNotificationService stub = new StubNotificationService();
        stub.response = new NotificationModelResponseDTO.NotificationModelErrorDTO("Project not found");
        NotificationController controller = new NotificationController(stub);
        NotificationModelDTO dto = new NotificationModelDTO(null, "UI", "var", "comment", "STONE", "frame", "title");

        HttpResponse<NotificationModelResponseDTO> resp = controller.add(UUID.randomUUID(), dto);

        assertEquals(404, resp.getStatus().getCode());
    }

    @Test
    void getById_crossProject_returns404() {
        StubNotificationService stub = new StubNotificationService();
        stub.findByIdResponse = Optional.empty();
        NotificationController controller = new NotificationController(stub);

        HttpResponse<NotificationModelResponseDTO> resp = controller.getById(UUID.randomUUID(), UUID.randomUUID());

        assertEquals(404, resp.getStatus().getCode());
    }

    @Test
    void delete_crossProject_returns404() {
        StubNotificationService stub = new StubNotificationService();
        stub.response = new NotificationModelResponseDTO.NotificationModelErrorDTO("Notification not found");
        NotificationController controller = new NotificationController(stub);

        HttpResponse<NotificationModelResponseDTO> resp = controller.remove(UUID.randomUUID(), UUID.randomUUID());

        assertEquals(404, resp.getStatus().getCode());
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
