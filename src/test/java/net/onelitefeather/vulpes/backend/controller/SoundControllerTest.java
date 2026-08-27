package net.onelitefeather.vulpes.backend.controller;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import net.datafaker.Faker;
import net.onelitefeather.vulpes.api.model.sound.SoundEventEntity;
import net.onelitefeather.vulpes.backend.controller.sound.SoundController;
import net.onelitefeather.vulpes.backend.controller.sound.SoundSourceController;
import net.onelitefeather.vulpes.backend.domain.error.ErrorCode;
import net.onelitefeather.vulpes.backend.domain.sound.SoundEventDTO;
import net.onelitefeather.vulpes.backend.domain.sound.SoundFileSourceDTO;
import net.onelitefeather.vulpes.backend.domain.sound.SoundResponseDTO;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.service.SoundService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit tests for SoundController (implementation tests)")
class SoundControllerTest {

    private static final Faker FAKER = new Faker();

    private static class StubSoundService implements SoundService {
        SoundResponseDTO.SoundModelDTO response;
        Optional<SoundEventEntity> findByIdResponse;
        Page<SoundResponseDTO.SoundFileSourceDTO> sourcesPage;
        Page<SoundResponseDTO.SoundModelDTO> modelDtoPage;
        SoundResponseDTO.SoundFileSourceDTO sourceResponse;

        @Override
        public SoundResponseDTO.SoundModelDTO create(SoundEventDTO soundEventDTO) {
            return (SoundResponseDTO.SoundModelDTO) response;
        }

        @Override
        public SoundResponseDTO.SoundModelDTO update(SoundEventDTO soundEventDTO) {
            return response;
        }

        @Override
        public SoundResponseDTO.SoundModelDTO delete(UUID id) {
            return response;
        }

        @Override
        public void deleteAll() {
        }

        @Override
        public Page<SoundResponseDTO.SoundModelDTO> getAll(Pageable pageable) {
            return modelDtoPage;
        }

        @Override
        public Optional<SoundEventEntity> findById(UUID id) {
            return findByIdResponse;
        }

        @Override
        public SoundResponseDTO.SoundModelDTO create(UUID projectId, SoundEventDTO soundEventDTO) {
            return response;
        }

        @Override
        public SoundResponseDTO.SoundModelDTO update(UUID projectId, SoundEventDTO soundEventDTO) {
            return response;
        }

        @Override
        public SoundResponseDTO.SoundModelDTO delete(UUID projectId, UUID id) {
            return response;
        }

        @Override
        public void deleteAll(UUID projectId) {
        }

        @Override
        public Page<SoundResponseDTO.SoundModelDTO> getAll(UUID projectId, Pageable pageable) {
            return modelDtoPage;
        }

        @Override
        public Optional<SoundEventEntity> findById(UUID projectId, UUID id) {
            return findByIdResponse;
        }

        @Override
        public Page<SoundResponseDTO.SoundFileSourceDTO> getSoundSourcesById(UUID id, Pageable pageable) {
            return sourcesPage;
        }

        @Override
        public SoundResponseDTO.SoundFileSourceDTO createAndLinkSource(UUID soundEventId, SoundFileSourceDTO sourceDTO) {
            return sourceResponse;
        }

        @Override
        public SoundResponseDTO.SoundFileSourceDTO updateLinkedSource(UUID soundEventId, SoundFileSourceDTO sourceDTO) {
            return sourceResponse;
        }

        @Override
        public SoundResponseDTO.SoundFileSourceDTO deleteLinkedSource(UUID soundEventId, UUID sourceDTO) {
            return sourceResponse;
        }
    }

    private static net.onelitefeather.vulpes.api.model.project.ProjectEntity sampleProject(UUID id) {
        return new net.onelitefeather.vulpes.api.model.project.ProjectEntity(id, "Test Project", "test-project", null, null, null, false);
    }

    private static SoundEventDTO sampleEventDTO(UUID id) {
        String uiName = FAKER.rockBand().name();
        String varName = FAKER.internet().slug();
        String key = "key." + FAKER.lorem().word();
        String subtitle = FAKER.book().title();
        return new SoundEventDTO(id, uiName, varName, key, subtitle);
    }

    @Test
    void testAdd_returnsOk() {
        StubSoundService stub = new StubSoundService();
        UUID projectId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        SoundEventDTO dto = sampleEventDTO(id);
        SoundResponseDTO.SoundModelDTO expected = SoundResponseDTO.SoundModelDTO.createDTO(dto.toEntity(sampleProject(projectId)));
        stub.response = expected;

        SoundController controller = new SoundController(stub);
        HttpResponse<SoundResponseDTO.SoundModelDTO> resp = controller.add(projectId, dto);

        assertEquals(200, resp.getStatus().getCode());
        assertInstanceOf(SoundResponseDTO.SoundModelDTO.class, resp.body());
        SoundResponseDTO.SoundModelDTO body = (SoundResponseDTO.SoundModelDTO) resp.body();
        assertEquals(expected.id(), body.id());
        assertEquals(expected.uiName(), body.uiName());
    }

    @Test
    void testGetById_found_returnsOk() {
        StubSoundService stub = new StubSoundService();
        UUID projectId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        SoundEventEntity entity = sampleEventDTO(id).toEntity(sampleProject(projectId));
        stub.findByIdResponse = Optional.of(entity);
        SoundController controller = new SoundController(stub);

        HttpResponse<SoundResponseDTO.SoundModelDTO> resp = controller.getById(projectId, id);
        assertEquals(200, resp.getStatus().getCode());
        assertInstanceOf(SoundResponseDTO.SoundModelDTO.class, resp.body());
        SoundResponseDTO.SoundModelDTO body = (SoundResponseDTO.SoundModelDTO) resp.body();
        assertEquals(id, body.id());
    }

    @Test
    @DisplayName("getById() raises RESOURCE_NOT_FOUND for an unknown sound event")
    void testGetById_notFound_raisesNotFound() {
        StubSoundService stub = new StubSoundService();
        stub.findByIdResponse = Optional.empty();
        SoundController controller = new SoundController(stub);
        UUID projectId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        ApiException exception = assertThrows(ApiException.class, () -> controller.getById(projectId, id));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.code());
        assertEquals("Sound event not found.", exception.detail());
    }

    @Test
    @DisplayName("remove() lets a RESOURCE_NOT_FOUND from the service reach the exception handler")
    void testRemove_notFound_propagates() {
        StubSoundService stub = new StubSoundService() {
            @Override
            public SoundResponseDTO.SoundModelDTO delete(UUID projectId, UUID id) {
                throw ApiException.notFound("Sound event");
            }
        };
        SoundController controller = new SoundController(stub);
        UUID projectId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        ApiException exception = assertThrows(ApiException.class, () -> controller.remove(projectId, id));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.code());
    }

    @Test
    void testCreateSource_delegatesAndReturnsOk() {
        StubSoundService stub = new StubSoundService();
        UUID id = UUID.randomUUID();
        String fileName = FAKER.internet().slug() + ".ogg";
        SoundFileSourceDTO requestDTO = new SoundFileSourceDTO(null, fileName, 1.0f, 1.0f, 1, false, 16, false, "file");
        SoundResponseDTO.SoundFileSourceDTO expected = new SoundResponseDTO.SoundFileSourceDTO(UUID.randomUUID(), requestDTO.name(), requestDTO.volume(), requestDTO.pitch(), requestDTO.weight(), requestDTO.stream(), requestDTO.attenuationDistance(), requestDTO.preload(), requestDTO.type());
        stub.sourceResponse = expected;
        SoundSourceController controller = new SoundSourceController(stub);

        HttpResponse<SoundResponseDTO.SoundFileSourceDTO> resp = controller.createSource(id, requestDTO);
        assertEquals(200, resp.getStatus().getCode());
        assertNotNull(resp.body());
        assertInstanceOf(SoundResponseDTO.SoundFileSourceDTO.class, resp.body());
        SoundResponseDTO.SoundFileSourceDTO body = (SoundResponseDTO.SoundFileSourceDTO) resp.body();
        assertEquals(expected.id(), body.id());
        assertEquals(expected.name(), body.name());
    }

    @Test
    void testUpdateSource_delegatesAndReturnsOk() {
        StubSoundService stub = new StubSoundService();
        UUID id = UUID.randomUUID();
        String updateName = FAKER.internet().slug() + ".ogg";
        SoundFileSourceDTO requestDTO = new SoundFileSourceDTO(UUID.randomUUID(), updateName, 0.8f, 1.1f, 2, true, 32, true, "file");
        SoundResponseDTO.SoundFileSourceDTO expected = new SoundResponseDTO.SoundFileSourceDTO(requestDTO.id(), requestDTO.name(), requestDTO.volume(), requestDTO.pitch(), requestDTO.weight(), requestDTO.stream(), requestDTO.attenuationDistance(), requestDTO.preload(), requestDTO.type());
        stub.sourceResponse = expected;
        SoundSourceController controller = new SoundSourceController(stub);

        HttpResponse<SoundResponseDTO.SoundFileSourceDTO> resp = controller.updateSource(id, requestDTO);
        assertNotNull(resp);
        var respBody = resp.body();
        assertNotNull(respBody);
        assertInstanceOf(SoundResponseDTO.SoundFileSourceDTO.class, respBody);
        SoundResponseDTO.SoundFileSourceDTO body = (SoundResponseDTO.SoundFileSourceDTO) respBody;
        assertEquals(expected.id(), body.id());
        assertEquals(expected.name(), body.name());
        assertEquals(200, resp.getStatus().getCode());
    }

    @Test
    void testGetSources_returnsPageOk() {
        StubSoundService stub = new StubSoundService();
        UUID id = UUID.randomUUID();
        String listName = FAKER.internet().slug() + ".ogg";
        SoundResponseDTO.SoundFileSourceDTO s = new SoundResponseDTO.SoundFileSourceDTO(UUID.randomUUID(), listName, 1.0f, 1.0f, 1, false, 16, false, "file");
        stub.sourcesPage = Page.of(List.of(s), Pageable.from(0, 10), 1L);
        SoundSourceController controller = new SoundSourceController(stub);

        HttpResponse<Page<SoundResponseDTO.SoundFileSourceDTO>> resp = controller.get(id, Pageable.from(0, 10));
        assertEquals(200, resp.getStatus().getCode());
        assertNotNull(resp.body());
        assertEquals(1, resp.body().getTotalSize());
        assertEquals(1, resp.body().getContent().size());
        assertNotNull(resp.body().getContent().getFirst());
        assertInstanceOf(SoundResponseDTO.SoundFileSourceDTO.class, resp.body().getContent().getFirst());
        SoundResponseDTO.SoundFileSourceDTO body = resp.body().getContent().getFirst();
        assertEquals(s.id(), body.id());
        assertEquals(s.name(), body.name());
    }
}