package net.onelitefeather.vulpes.backend.controller.font;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import net.onelitefeather.vulpes.api.model.FontEntity;
import net.onelitefeather.vulpes.backend.domain.error.ErrorCode;
import net.onelitefeather.vulpes.backend.domain.font.FontModelDTO;
import net.onelitefeather.vulpes.backend.domain.font.FontModelResponseDTO;
import net.onelitefeather.vulpes.backend.domain.font.FontStringDTO;
import net.onelitefeather.vulpes.backend.domain.font.FontStringResponseDTO;
import net.onelitefeather.vulpes.backend.exception.ApiException;
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
        FontModelResponseDTO.FontModelDTO response;
        Page<FontModelResponseDTO.FontModelDTO> page;
        Optional<FontEntity> findByIdResponse = Optional.empty();

        @Override
        public FontModelResponseDTO.FontModelDTO create(FontModelDTO dto) {
            return (FontModelResponseDTO.FontModelDTO) response;
        }

        @Override
        public FontModelResponseDTO.FontModelDTO update(FontModelDTO dto) {
            return response;
        }

        @Override
        public FontModelResponseDTO.FontModelDTO delete(UUID id) {
            return response;
        }

        @Override
        public void deleteAll() {
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
        public FontModelResponseDTO.FontModelDTO create(UUID projectId, FontModelDTO dto) {
            return response;
        }

        @Override
        public FontModelResponseDTO.FontModelDTO update(UUID projectId, FontModelDTO dto) {
            return response;
        }

        @Override
        public FontModelResponseDTO.FontModelDTO delete(UUID projectId, UUID id) {
            return response;
        }

        @Override
        public void deleteAll(UUID projectId) {
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
        public Page<FontStringResponseDTO.FontStringDTO> findCharsByFontId(UUID id, Pageable pageable) {
            return Page.empty();
        }

        @Override
        public FontStringResponseDTO.FontStringDTO updateCharByFontId(UUID id, FontStringDTO charModel) {
            return null;
        }

        @Override
        public FontStringResponseDTO.FontStringDTO createCharByFontId(UUID id, FontStringDTO charModel) {
            return null;
        }

        @Override
        public FontStringResponseDTO.FontStringDTO deleteCharByFontId(UUID fontId, UUID charId) {
            return null;
        }

        @Override
        public List<FontStringResponseDTO.FontStringDTO> deleteAllCharByFontId(UUID fontId) {
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

        HttpResponse<FontModelResponseDTO.FontModelDTO> resp = controller.add(projectId, sampleDTO(null));

        assertEquals(200, resp.getStatus().getCode());
        assertInstanceOf(FontModelResponseDTO.FontModelDTO.class, resp.body());
    }

    @Test
    @DisplayName("add() lets a PROJECT_NOT_FOUND from the service reach the exception handler")
    void add_unknownProject_propagates() {
        StubFontService stub = new StubFontService() {
            @Override
            public FontModelResponseDTO.FontModelDTO create(UUID projectId, FontModelDTO dto) {
                throw ApiException.projectNotFound();
            }
        };
        FontController controller = new FontController(stub);
        FontModelDTO dto = sampleDTO(null);
        UUID projectId = UUID.randomUUID();

        ApiException exception = assertThrows(ApiException.class, () -> controller.add(projectId, dto));

        assertEquals(ErrorCode.PROJECT_NOT_FOUND, exception.code());
    }

    @Test
    @DisplayName("getById() raises RESOURCE_NOT_FOUND when the entity belongs to another project")
    void getById_crossProject_raisesNotFound() {
        StubFontService stub = new StubFontService();
        stub.findByIdResponse = Optional.empty();
        FontController controller = new FontController(stub);
        UUID projectId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        ApiException exception = assertThrows(ApiException.class, () -> controller.getById(projectId, id));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.code());
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
