package net.onelitefeather.vulpes.backend.controller;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import net.onelitefeather.vulpes.backend.domain.attribute.AttributeModelDTO;
import net.onelitefeather.vulpes.backend.domain.attribute.AttributeModelResponseDTO;
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
        AttributeModelResponseDTO response;
        Page<AttributeModelResponseDTO.AttributeModelDTO> page;

        @Override
        public AttributeModelResponseDTO.AttributeModelDTO create(AttributeModelDTO dto) {
            return (AttributeModelResponseDTO.AttributeModelDTO) response;
        }

        @Override
        public AttributeModelResponseDTO update(AttributeModelDTO dto) {
            return response;
        }

        @Override
        public AttributeModelResponseDTO delete(UUID id) {
            return response;
        }

        @Override
        public List<AttributeModelResponseDTO> deleteAll() {
            return List.of();
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
        public AttributeModelResponseDTO create(UUID projectId, AttributeModelDTO dto) {
            return response;
        }

        @Override
        public AttributeModelResponseDTO update(UUID projectId, AttributeModelDTO dto) {
            return response;
        }

        @Override
        public AttributeModelResponseDTO delete(UUID projectId, UUID id) {
            return response;
        }

        @Override
        public List<AttributeModelResponseDTO> deleteAll(UUID projectId) {
            return List.of();
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

        HttpResponse<AttributeModelResponseDTO> resp = controller.add(projectId, dto);

        assertEquals(200, resp.getStatus().getCode());
        assertInstanceOf(AttributeModelResponseDTO.AttributeModelDTO.class, resp.body());
    }

    @Test
    void add_unknownProject_returns404() {
        StubAttributeService stub = new StubAttributeService();
        stub.response = new AttributeModelResponseDTO.AttributeModelErrorDTO("Project not found");
        AttributeController controller = new AttributeController(stub);
        AttributeModelDTO dto = new AttributeModelDTO(null, "UI", "var", 1.0, 10.0);

        HttpResponse<AttributeModelResponseDTO> resp = controller.add(UUID.randomUUID(), dto);

        assertEquals(404, resp.getStatus().getCode());
    }

    @Test
    void delete_crossProject_returns404() {
        StubAttributeService stub = new StubAttributeService();
        stub.response = new AttributeModelResponseDTO.AttributeModelErrorDTO("Attribute not found");
        AttributeController controller = new AttributeController(stub);

        HttpResponse<AttributeModelResponseDTO> resp = controller.delete(UUID.randomUUID(), UUID.randomUUID());

        assertEquals(404, resp.getStatus().getCode());
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
