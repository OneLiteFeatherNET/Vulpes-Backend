package net.onelitefeather.vulpes.backend.controller.font;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import net.onelitefeather.vulpes.api.model.FontEntity;
import net.onelitefeather.vulpes.backend.domain.font.FontModelDTO;
import net.onelitefeather.vulpes.backend.domain.font.FontModelResponseDTO;
import net.onelitefeather.vulpes.backend.domain.font.FontStringDTO;
import net.onelitefeather.vulpes.backend.domain.font.FontStringResponseDTO;
import net.onelitefeather.vulpes.backend.service.FontService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit tests for FontController (project-scoped)")
class FontControllerTest {

    private static class StubFontService implements FontService {
        FontModelResponseDTO response;
        Page<FontModelResponseDTO.FontModelDTO> page;
        Optional<FontEntity> findByIdResponse = Optional.empty();

        @Override
        public FontModelResponseDTO.FontModelDTO create(FontModelDTO dto) {
            return (FontModelResponseDTO.FontModelDTO) response;
        }

        @Override
        public FontModelResponseDTO update(FontModelDTO dto) {
            return response;
        }

        @Override
        public FontModelResponseDTO delete(UUID id) {
            return response;
        }

        @Override
        public List<FontModelResponseDTO> deleteAll() {
            return List.of();
        }

        @Override
        public Page<FontModelResponseDTO.FontModelDTO> getAll(Pageable pageable) {
            return page;
        }

        @Override
        public Optional<FontEntity> findById(UUID id) {
            return findByIdResponse;
        }

        @Override
        public FontModelResponseDTO create(UUID projectId, FontModelDTO dto) {
            return response;
        }

        @Override
        public FontModelResponseDTO update(UUID projectId, FontModelDTO dto) {
            return response;
        }

        @Override
        public FontModelResponseDTO delete(UUID projectId, UUID id) {
            return response;
        }

        @Override
        public List<FontModelResponseDTO> deleteAll(UUID projectId) {
            return List.of();
        }

        @Override
        public Page<FontModelResponseDTO.FontModelDTO> getAll(UUID projectId, Pageable pageable) {
            return page;
        }

        @Override
        public Optional<FontEntity> findById(UUID projectId, UUID id) {
            return findByIdResponse;
        }

        @Override
        public Page<FontStringResponseDTO> findCharsByFontId(UUID id, Pageable pageable) {
            return Page.empty();
        }

        @Override
        public FontStringResponseDTO updateCharByFontId(UUID id, FontStringDTO charModel) {
            return null;
        }

        @Override
        public FontStringResponseDTO createCharByFontId(UUID id, FontStringDTO charModel) {
            return null;
        }

        @Override
        public FontStringResponseDTO deleteCharByFontId(UUID fontId, UUID charId) {
            return null;
        }

        @Override
        public List<FontStringResponseDTO> deleteAllCharByFontId(UUID fontId) {
            return List.of();
        }
    }

    private static FontModelDTO sampleDTO(UUID id) {
        return new FontModelDTO(id, "UI", "var", "provider", "mapper", "texture", "comment", 1, 1);
    }

    private static FontModelResponseDTO.FontModelDTO sampleResponse(UUID id, UUID projectId) {
        return new FontModelResponseDTO.FontModelDTO(id, "UI", "var", "provider", "mapper", "texture", "comment", 1, 1, projectId);
    }

    @Test
    void add_success_returnsOk() {
        StubFontService stub = new StubFontService();
        UUID projectId = UUID.randomUUID();
        stub.response = sampleResponse(UUID.randomUUID(), projectId);
        FontController controller = new FontController(stub);

        HttpResponse<FontModelResponseDTO> resp = controller.add(projectId, sampleDTO(null));

        assertEquals(200, resp.getStatus().getCode());
        assertInstanceOf(FontModelResponseDTO.FontModelDTO.class, resp.body());
    }

    @Test
    void add_unknownProject_returns404() {
        StubFontService stub = new StubFontService();
        stub.response = new FontModelResponseDTO.FontModelErrorDTO("Project not found");
        FontController controller = new FontController(stub);

        HttpResponse<FontModelResponseDTO> resp = controller.add(UUID.randomUUID(), sampleDTO(null));

        assertEquals(404, resp.getStatus().getCode());
    }

    @Test
    void getById_crossProject_returns404() {
        StubFontService stub = new StubFontService();
        stub.findByIdResponse = Optional.empty();
        FontController controller = new FontController(stub);

        HttpResponse<FontModelResponseDTO> resp = controller.getById(UUID.randomUUID(), UUID.randomUUID());

        assertEquals(404, resp.getStatus().getCode());
    }

    @Test
    void getAll_returnsScopedPage() {
        StubFontService stub = new StubFontService();
        UUID projectId = UUID.randomUUID();
        stub.page = Page.of(List.of(sampleResponse(UUID.randomUUID(), projectId)), Pageable.from(0, 10), 1L);
        FontController controller = new FontController(stub);

        HttpResponse<Page<FontModelResponseDTO.FontModelDTO>> resp = controller.getAll(projectId, Pageable.from(0, 10));

        assertEquals(200, resp.getStatus().getCode());
        assertEquals(1, resp.body().getTotalSize());
    }
}
