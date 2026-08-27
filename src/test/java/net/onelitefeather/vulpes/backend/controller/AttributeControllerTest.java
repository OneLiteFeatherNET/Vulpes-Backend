package net.onelitefeather.vulpes.backend.controller;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import net.onelitefeather.vulpes.backend.domain.attribute.AttributeModelDTO;
import net.onelitefeather.vulpes.backend.domain.attribute.AttributeModelResponseDTO;
import net.onelitefeather.vulpes.backend.domain.error.ErrorCode;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.service.AttributeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
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
        public Optional<net.onelitefeather.vulpes.api.model.AttributeEntity> findById(UUID id) {
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
        public Optional<net.onelitefeather.vulpes.api.model.AttributeEntity> findById(UUID projectId, UUID id) {
            return Optional.empty();
        }
    }

    @Test
    void add_success_returnsOk() {
        StubAttributeService stub = new StubAttributeService();
        UUID projectId = UUID.randomUUID();
        AttributeModelDTO dto = new AttributeModelDTO(null, "UI", "var", 1.0, 10.0);
        stub.response = new AttributeModelResponseDTO.AttributeModelDTO(UUID.randomUUID(), "UI", "var", 1.0, 10.0, projectId);
        AttributeController controller = new AttributeController(stub);

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
        AttributeController controller = new AttributeController(stub);
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
        AttributeController controller = new AttributeController(stub);
        UUID projectId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        ApiException exception = assertThrows(ApiException.class, () -> controller.delete(projectId, id));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.code());
    }

    @Test
    void getAll_returnsScopedPage() {
        StubAttributeService stub = new StubAttributeService();
        UUID projectId = UUID.randomUUID();
        var dto = new AttributeModelResponseDTO.AttributeModelDTO(UUID.randomUUID(), "UI", "var", 1.0, 10.0, projectId);
        stub.page = Page.of(List.of(dto), Pageable.from(0, 10), 1L);
        AttributeController controller = new AttributeController(stub);

        HttpResponse<Page<AttributeModelResponseDTO.AttributeModelDTO>> resp = controller.getAll(projectId, Pageable.from(0, 10));

        assertEquals(200, resp.getStatus().getCode());
        assertEquals(1, resp.body().getTotalSize());
    }
}
