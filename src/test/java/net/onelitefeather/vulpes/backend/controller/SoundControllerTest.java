package net.onelitefeather.vulpes.backend.controller;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import net.datafaker.Faker;
import net.onelitefeather.vulpes.api.model.sound.SoundEventEntity;
import net.onelitefeather.vulpes.backend.domain.sound.SoundEventDTO;
import net.onelitefeather.vulpes.backend.domain.sound.SoundFileSourceDTO;
import net.onelitefeather.vulpes.backend.domain.sound.SoundResponseDTO;
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
        SoundResponseDTO response;
        Optional<SoundEventEntity> findByIdResponse;
        Page<SoundResponseDTO> sourcesPage;
        Page<SoundResponseDTO.SoundModelDTO> modelDtoPage;
        SoundResponseDTO.SoundFileSourceDTO sourceResponse;

        @Override
        public SoundResponseDTO.SoundModelDTO createSoundEvent(SoundEventDTO soundEventDTO) {
            return (SoundResponseDTO.SoundModelDTO) response;
        }

        @Override
        public SoundResponseDTO updateSoundEvent(SoundEventDTO soundEventDTO) {
            return response;
        }

        @Override
        public SoundResponseDTO deleteSoundEvent(UUID id) {
            return response;
        }

        @Override
        public List<SoundResponseDTO> deleteAllSoundEvents() {
            return List.of();
        }

        @Override
        public Page<SoundResponseDTO.SoundModelDTO> getAllSoundEvents(Pageable pageable) {
            return modelDtoPage;
        }

        @Override
        public Optional<SoundEventEntity> findSoundEventById(UUID id) {
            return findByIdResponse;
        }

        @Override
        public Page<SoundResponseDTO> getSoundSourcesById(UUID id, Pageable pageable) {
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
        public SoundResponseDTO.SoundFileSourceDTO deleteLinkedSource(UUID soundEventId, SoundFileSourceDTO sourceDTO) {
            return sourceResponse;
        }
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
        UUID id = UUID.randomUUID();
        SoundEventDTO dto = sampleEventDTO(id);
        SoundResponseDTO.SoundModelDTO expected = SoundResponseDTO.SoundModelDTO.createDTO(dto.toEntity());
        stub.response = expected;

        SoundController controller = new SoundController(stub);
        HttpResponse<SoundResponseDTO> resp = controller.add(dto);

        assertEquals(200, resp.getStatus().getCode());
        assertInstanceOf(SoundResponseDTO.SoundModelDTO.class, resp.body());
        SoundResponseDTO.SoundModelDTO body = (SoundResponseDTO.SoundModelDTO) resp.body();
        assertEquals(expected.id(), body.id());
        assertEquals(expected.uiName(), body.uiName());
    }

    @Test
    void testGetById_found_returnsOk() {
        StubSoundService stub = new StubSoundService();
        UUID id = UUID.randomUUID();
        SoundEventEntity entity = sampleEventDTO(id).toEntity();
        stub.findByIdResponse = Optional.of(entity);
        SoundController controller = new SoundController(stub);

        HttpResponse<SoundResponseDTO> resp = controller.getById(id);
        assertEquals(200, resp.getStatus().getCode());
        assertInstanceOf(SoundResponseDTO.SoundModelDTO.class, resp.body());
        SoundResponseDTO.SoundModelDTO body = (SoundResponseDTO.SoundModelDTO) resp.body();
        assertEquals(id, body.id());
    }

    @Test
    void testGetById_notFound_returns404() {
        StubSoundService stub = new StubSoundService();
        stub.findByIdResponse = Optional.empty();
        SoundController controller = new SoundController(stub);

        HttpResponse<SoundResponseDTO> resp = controller.getById(UUID.randomUUID());
        assertEquals(404, resp.getStatus().getCode());
        assertInstanceOf(SoundResponseDTO.SoundErrorDTO.class, resp.body());
    }

    @Test
    void testRemove_notFound_returns404() {
        StubSoundService stub = new StubSoundService();
        stub.response = new SoundResponseDTO.SoundErrorDTO("Sound event not found");
        SoundController controller = new SoundController(stub);

        HttpResponse<SoundResponseDTO> resp = controller.remove(UUID.randomUUID());
        assertEquals(404, resp.getStatus().getCode());
    }

    @Test
    void testCreateSource_delegatesAndReturnsOk() {
        StubSoundService stub = new StubSoundService();
        UUID id = UUID.randomUUID();
        String fileName = FAKER.internet().slug() + ".ogg";
        SoundFileSourceDTO requestDTO = new SoundFileSourceDTO(null, fileName, 1.0f, 1.0f, 1, false, 16, false, "file");
        SoundResponseDTO.SoundFileSourceDTO expected = new SoundResponseDTO.SoundFileSourceDTO(UUID.randomUUID(), requestDTO.name(), requestDTO.volume(), requestDTO.pitch(), requestDTO.weight(), requestDTO.stream(), requestDTO.attenuationDistance(), requestDTO.preload(), requestDTO.type());
        stub.sourceResponse = expected;
        SoundController controller = new SoundController(stub);

        HttpResponse<SoundResponseDTO> resp = controller.createSource(id, requestDTO);
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
        SoundController controller = new SoundController(stub);

        HttpResponse<SoundResponseDTO> resp = controller.updateSource(id, requestDTO);
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
        SoundController controller = new SoundController(stub);

        HttpResponse<Page<SoundResponseDTO>> resp = controller.get(id, Pageable.from(0, 10));
        assertEquals(200, resp.getStatus().getCode());
        assertNotNull(resp.body());
        assertEquals(1, resp.body().getTotalSize());
        assertEquals(1, resp.body().getContent().size());
        assertNotNull(resp.body().getContent().getFirst());
        assertInstanceOf(SoundResponseDTO.SoundFileSourceDTO.class, resp.body().getContent().getFirst());
        SoundResponseDTO.SoundFileSourceDTO body = (SoundResponseDTO.SoundFileSourceDTO) resp.body().getContent().getFirst();
        assertEquals(s.id(), body.id());
        assertEquals(s.name(), body.name());
    }
}