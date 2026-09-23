package net.onelitefeather.vulpes.backend.controller;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import net.onelitefeather.vulpes.api.model.NotificationEntity;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.backend.domain.error.ErrorCode;
import net.onelitefeather.vulpes.backend.domain.copy.CopyDTO;
import net.onelitefeather.vulpes.backend.domain.notification.NotificationModelDTO;
import net.onelitefeather.vulpes.backend.domain.notification.NotificationModelResponseDTO;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.copier.EntityCopier;
import net.onelitefeather.vulpes.backend.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

    private static class StubNotificationCopier implements EntityCopier<NotificationEntity, Void> {
        NotificationEntity response;
        RuntimeException toThrow;

        @Override
        public NotificationEntity copy(UUID sourceProjectId, UUID sourceId, UUID targetProjectId, String targetKey, String targetName, Set<Void> relations) {
            if (toThrow != null) {
                throw toThrow;
            }
            return response;
        }
    }

    @Test
    void add_success_returnsOk() {
        StubNotificationService stub = new StubNotificationService();
        UUID projectId = UUID.randomUUID();
        NotificationModelDTO dto = new NotificationModelDTO(null, "UI", "var", "comment", "STONE", "frame", "title");
        stub.response = new NotificationModelResponseDTO.NotificationModelDTO(UUID.randomUUID(), "UI", "var", "comment", "STONE", "frame", "title", projectId, Instant.now(), Instant.now());
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
        var dto = new NotificationModelResponseDTO.NotificationModelDTO(UUID.randomUUID(), "UI", "var", "comment", "STONE", "frame", "title", projectId, Instant.now(), Instant.now());
        stub.page = Page.of(List.of(dto), Pageable.from(0, 10), 1L);
        NotificationController controller = new NotificationController(stub);

        HttpResponse<Page<NotificationModelResponseDTO.NotificationModelDTO>> resp = controller.getAll(projectId, Pageable.from(0, 10));

        assertEquals(200, resp.getStatus().getCode());
        assertEquals(1, resp.body().getTotalSize());
    }

    @Test
    @DisplayName("copy() with no body returns the copier's result wrapped in 200")
    void copy_noBody_returnsOk() {
        StubNotificationCopier copierStub = new StubNotificationCopier();
        ProjectEntity project = new ProjectEntity(UUID.randomUUID(), "Project A", "project-a", null, null, null, false);
        copierStub.response = new NotificationEntity(UUID.randomUUID(), "UI", "key", "comment", "STONE", "frame", "title", project);
        NotificationCopyController controller = new NotificationCopyController(copierStub);
        UUID projectId = project.getId();
        UUID id = UUID.randomUUID();

        HttpResponse<NotificationModelResponseDTO.NotificationModelDTO> resp = controller.copy(projectId, id, null);

        assertEquals(200, resp.getStatus().getCode());
        assertEquals("key", resp.body().key());
    }

    @Test
    @DisplayName("copy() passes the DTO's fields through to the copier")
    void copy_withBody_passesFieldsThrough() {
        StubNotificationCopier copierStub = new StubNotificationCopier() {
            @Override
            public NotificationEntity copy(UUID sourceProjectId, UUID sourceId, UUID targetProjectId, String targetKey, String targetName) {
                ProjectEntity project = new ProjectEntity(targetProjectId, "Target", "target", null, null, null, false);
                return new NotificationEntity(UUID.randomUUID(), targetName, targetKey, "comment", "STONE", "frame", "title", project);
            }
        };
        NotificationCopyController controller = new NotificationCopyController(copierStub);
        UUID projectId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        UUID targetProjectId = UUID.randomUUID();
        CopyDTO body = new CopyDTO(targetProjectId, "new-key", "New Name");

        HttpResponse<NotificationModelResponseDTO.NotificationModelDTO> resp = controller.copy(projectId, id, body);

        assertEquals(200, resp.getStatus().getCode());
        assertEquals("new-key", resp.body().key());
        assertEquals("New Name", resp.body().uiName());
        assertEquals(targetProjectId, resp.body().projectId());
    }

    @Test
    @DisplayName("copy() lets a RESOURCE_CONFLICT from the copier reach the exception handler")
    void copy_keyTaken_propagates() {
        StubNotificationCopier copierStub = new StubNotificationCopier();
        copierStub.toThrow = ApiException.conflict("A notification with key 'x' already exists in the target project.");
        NotificationCopyController controller = new NotificationCopyController(copierStub);
        UUID projectId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        ApiException exception = assertThrows(ApiException.class, () -> controller.copy(projectId, id, null));

        assertEquals(ErrorCode.RESOURCE_CONFLICT, exception.code());
    }
}
