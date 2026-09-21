package net.onelitefeather.vulpes.backend.controller;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import net.onelitefeather.vulpes.api.model.AttributeEntity;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.backend.domain.copy.CopyDTO;
import net.onelitefeather.vulpes.backend.domain.attribute.AttributeModelDTO;
import net.onelitefeather.vulpes.backend.domain.attribute.AttributeModelResponseDTO;
import net.onelitefeather.vulpes.backend.domain.error.ErrorCode;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.copier.EntityCopier;
import net.onelitefeather.vulpes.backend.service.AttributeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit tests for AttributeController (project-scoped)")
class AttributeControllerTest {

    private static class StubAttributeService implements AttributeService {
        AttributeModelResponseDTO.AttributeModelDTO response;
        Page<AttributeModelResponseDTO.AttributeModelDTO> page;

        @Override
        public AttributeModelResponseDTO.AttributeModelDTO create(AttributeModelDTO dto) {
            return (AttributeModelResponseDTO.AttributeModelDTO) response;
        }

        @Override
        public AttributeModelResponseDTO.AttributeModelDTO update(AttributeModelDTO dto) {
            return response;
        }

        @Override
        public AttributeModelResponseDTO.AttributeModelDTO delete(UUID id) {
            return response;
        }

        @Override
        public void deleteAll() {
        }

        @Override
        public Page<AttributeModelResponseDTO.AttributeModelDTO> getAll(Pageable pageable) {
            return page;
        }

        @Override
        public Optional<AttributeEntity> findById(UUID id) {
            return Optional.empty();
        }

        @Override
        public AttributeModelResponseDTO.AttributeModelDTO create(UUID projectId, AttributeModelDTO dto) {
            return response;
        }

        @Override
        public AttributeModelResponseDTO.AttributeModelDTO update(UUID projectId, AttributeModelDTO dto) {
            return response;
        }

        @Override
        public AttributeModelResponseDTO.AttributeModelDTO delete(UUID projectId, UUID id) {
            return response;
        }

        @Override
        public void deleteAll(UUID projectId) {
        }

        @Override
        public Page<AttributeModelResponseDTO.AttributeModelDTO> getAll(UUID projectId, Pageable pageable) {
            return page;
        }

        @Override
        public Optional<AttributeEntity> findById(UUID projectId, UUID id) {
            return Optional.empty();
        }
    }

    private static class StubAttributeCopier implements EntityCopier<AttributeEntity, Void> {
        AttributeEntity response;
        RuntimeException toThrow;

        @Override
        public AttributeEntity copy(UUID sourceProjectId, UUID sourceId, UUID targetProjectId, String targetKey, Set<Void> relations) {
            if (toThrow != null) {
                throw toThrow;
            }
            return response;
        }
    }

    @Test
    void add_success_returnsOk() {
        StubAttributeService stub = new StubAttributeService();
        UUID projectId = UUID.randomUUID();
        AttributeModelDTO dto = new AttributeModelDTO(null, "UI", "var", 1.0, 10.0);
        stub.response = new AttributeModelResponseDTO.AttributeModelDTO(UUID.randomUUID(), "UI", "var", 1.0, 10.0, projectId, Instant.now(), Instant.now());
        AttributeController controller = new AttributeController(stub, new StubAttributeCopier());

        HttpResponse<AttributeModelResponseDTO.AttributeModelDTO> resp = controller.add(projectId, dto);

        assertEquals(200, resp.getStatus().getCode());
        assertInstanceOf(AttributeModelResponseDTO.AttributeModelDTO.class, resp.body());
    }

    @Test
    @DisplayName("add() lets a PROJECT_NOT_FOUND from the service reach the exception handler")
    void add_unknownProject_propagates() {
        StubAttributeService stub = new StubAttributeService() {
            @Override
            public AttributeModelResponseDTO.AttributeModelDTO create(UUID projectId, AttributeModelDTO dto) {
                throw ApiException.projectNotFound();
            }
        };
        AttributeController controller = new AttributeController(stub, new StubAttributeCopier());
        AttributeModelDTO dto = new AttributeModelDTO(null, "UI", "var", 1.0, 10.0);
        UUID projectId = UUID.randomUUID();

        ApiException exception = assertThrows(ApiException.class, () -> controller.add(projectId, dto));

        assertEquals(ErrorCode.PROJECT_NOT_FOUND, exception.code());
    }

    @Test
    @DisplayName("delete() lets a RESOURCE_NOT_FOUND from the service reach the exception handler")
    void delete_crossProject_propagates() {
        StubAttributeService stub = new StubAttributeService() {
            @Override
            public AttributeModelResponseDTO.AttributeModelDTO delete(UUID projectId, UUID id) {
                throw ApiException.notFound("Attribute");
            }
        };
        AttributeController controller = new AttributeController(stub, new StubAttributeCopier());
        UUID projectId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        ApiException exception = assertThrows(ApiException.class, () -> controller.delete(projectId, id));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.code());
    }

    @Test
    void getAll_returnsScopedPage() {
        StubAttributeService stub = new StubAttributeService();
        UUID projectId = UUID.randomUUID();
        var dto = new AttributeModelResponseDTO.AttributeModelDTO(UUID.randomUUID(), "UI", "var", 1.0, 10.0, projectId, Instant.now(), Instant.now());
        stub.page = Page.of(List.of(dto), Pageable.from(0, 10), 1L);
        AttributeController controller = new AttributeController(stub, new StubAttributeCopier());

        HttpResponse<Page<AttributeModelResponseDTO.AttributeModelDTO>> resp = controller.getAll(projectId, Pageable.from(0, 10));

        assertEquals(200, resp.getStatus().getCode());
        assertEquals(1, resp.body().getTotalSize());
    }

    @Test
    @DisplayName("copy() with no body returns the copier's result wrapped in 200")
    void copy_noBody_returnsOk() {
        StubAttributeCopier copierStub = new StubAttributeCopier();
        ProjectEntity project = new ProjectEntity(UUID.randomUUID(), "Project A", "project-a", null, null, null, false);
        copierStub.response = new AttributeEntity(UUID.randomUUID(), "UI", "key", 1.0, 10.0, project);
        AttributeController controller = new AttributeController(new StubAttributeService(), copierStub);
        UUID projectId = project.getId();
        UUID id = UUID.randomUUID();

        HttpResponse<AttributeModelResponseDTO.AttributeModelDTO> resp = controller.copy(projectId, id, null);

        assertEquals(200, resp.getStatus().getCode());
        assertEquals("key", resp.body().key());
    }

    @Test
    @DisplayName("copy() passes the DTO's fields through to the copier")
    void copy_withBody_passesFieldsThrough() {
        StubAttributeCopier copierStub = new StubAttributeCopier() {
            @Override
            public AttributeEntity copy(UUID sourceProjectId, UUID sourceId, UUID targetProjectId, String targetKey) {
                ProjectEntity project = new ProjectEntity(targetProjectId, "Target", "target", null, null, null, false);
                return new AttributeEntity(UUID.randomUUID(), "UI", targetKey, 1.0, 10.0, project);
            }
        };
        AttributeController controller = new AttributeController(new StubAttributeService(), copierStub);
        UUID projectId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        UUID targetProjectId = UUID.randomUUID();
        CopyDTO body = new CopyDTO(targetProjectId, "new-key");

        HttpResponse<AttributeModelResponseDTO.AttributeModelDTO> resp = controller.copy(projectId, id, body);

        assertEquals(200, resp.getStatus().getCode());
        assertEquals("new-key", resp.body().key());
        assertEquals(targetProjectId, resp.body().projectId());
    }

    @Test
    @DisplayName("copy() lets a RESOURCE_CONFLICT from the copier reach the exception handler")
    void copy_keyTaken_propagates() {
        StubAttributeCopier copierStub = new StubAttributeCopier();
        copierStub.toThrow = ApiException.conflict("A attribute with key 'x' already exists in the target project.");
        AttributeController controller = new AttributeController(new StubAttributeService(), copierStub);
        UUID projectId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        ApiException exception = assertThrows(ApiException.class, () -> controller.copy(projectId, id, null));

        assertEquals(ErrorCode.RESOURCE_CONFLICT, exception.code());
    }
}
