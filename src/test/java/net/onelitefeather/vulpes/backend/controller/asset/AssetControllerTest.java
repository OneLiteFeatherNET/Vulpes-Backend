package net.onelitefeather.vulpes.backend.controller.asset;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.backend.domain.asset.AssetContent;
import net.onelitefeather.vulpes.backend.domain.asset.AssetKind;
import net.onelitefeather.vulpes.backend.domain.asset.AssetResponseDTO;
import net.onelitefeather.vulpes.backend.domain.asset.AssetUploadDTO;
import net.onelitefeather.vulpes.backend.entity.asset.ProjectAssetEntity;
import net.onelitefeather.vulpes.backend.service.AssetService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("Unit tests for AssetController (stub services, no context)")
class AssetControllerTest {

    private static final byte[] PNG_BYTES = {1, 2, 3, 4};
    private static final String SHA1 = "0123456789abcdef0123456789abcdef01234567";

    /**
     * Records the calls the controller makes and returns canned answers.
     */
    private static class StubAssetService implements AssetService {
        AssetResponseDTO response;
        AssetContent content;
        AssetKind lastKind;
        UUID lastDeletedId;

        @Override
        public AssetResponseDTO getAsset(UUID projectId, UUID id) {
            return response;
        }

        @Override
        public Page<AssetResponseDTO> getAll(UUID projectId, AssetKind kind, Pageable pageable) {
            lastKind = kind;
            return Page.of(List.of(response), pageable, 1L);
        }

        @Override
        public AssetContent getContent(UUID projectId, UUID id) {
            return content;
        }

        @Override
        public AssetResponseDTO replaceContent(UUID projectId, UUID id, AssetUploadDTO upload) {
            return response;
        }

        @Override
        public AssetResponseDTO delete(UUID projectId, UUID id) {
            lastDeletedId = id;
            return response;
        }

        @Override
        public void deleteAll(UUID projectId) {
        }

        @Override
        public AssetResponseDTO create(UUID projectId, AssetUploadDTO upload) {
            return response;
        }

        @Override
        public AssetResponseDTO create(AssetUploadDTO dto) {
            throw new UnsupportedOperationException();
        }

        @Override
        public AssetResponseDTO update(AssetUploadDTO dto) {
            throw new UnsupportedOperationException();
        }

        @Override
        public AssetResponseDTO delete(UUID id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteAll() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Page<AssetResponseDTO> getAll(Pageable pageable) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<ProjectAssetEntity> findById(UUID id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public AssetResponseDTO update(UUID projectId, AssetUploadDTO dto) {
            return response;
        }

        @Override
        public Page<AssetResponseDTO> getAll(UUID projectId, Pageable pageable) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<ProjectAssetEntity> findById(UUID projectId, UUID id) {
            throw new UnsupportedOperationException();
        }
    }

    private static AssetResponseDTO sampleAsset(UUID projectId, UUID id) {
        ProjectEntity project = new ProjectEntity(projectId, "Test Project", "test-project", null, null, null, false);
        ProjectAssetEntity entity = new ProjectAssetEntity();
        entity.setProject(project);
        entity.setKey("vulpes:item/ruby");
        entity.setKind(AssetKind.TEXTURE);
        entity.setFileName("ruby.png");
        entity.setContentType("image/png");
        entity.setSize(PNG_BYTES.length);
        entity.setSha1(SHA1);
        entity.setWidth(16);
        entity.setHeight(16);
        return AssetResponseDTO.createDTO(entity, List.of());
    }

    @Test
    void getById_returnsOkWithAsset() {
        UUID projectId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        StubAssetService assets = new StubAssetService();
        assets.response = sampleAsset(projectId, id);

        HttpResponse<AssetResponseDTO> response = new AssetController(assets)
                .getById(projectId, id);

        assertEquals(HttpStatus.OK, response.getStatus(), "a found asset must answer 200");
        assertEquals("vulpes:item/ruby", response.body().key(), "the body must carry the asset key");
    }

    @Test
    void getAll_passesKindFilterToService() {
        UUID projectId = UUID.randomUUID();
        StubAssetService assets = new StubAssetService();
        assets.response = sampleAsset(projectId, UUID.randomUUID());

        new AssetController(assets)
                .getAll(projectId, AssetKind.TEXTURE, Pageable.from(0, 10));

        assertEquals(AssetKind.TEXTURE, assets.lastKind, "the kind query parameter must reach the service");
    }

    @Test
    void getContent_returnsBytesWithEtagOfSha1() {
        UUID projectId = UUID.randomUUID();
        StubAssetService assets = new StubAssetService();
        assets.content = new AssetContent(PNG_BYTES, "image/png", SHA1);

        HttpResponse<byte[]> response = new AssetController(assets)
                .getContent(projectId, UUID.randomUUID(), null);

        assertEquals(HttpStatus.OK, response.getStatus(), "content without a matching If-None-Match must answer 200");
        assertArrayEquals(PNG_BYTES, response.body(), "the body must be the stored bytes");
        assertEquals("\"" + SHA1 + "\"", response.getHeaders().get("ETag"), "the ETag must be the quoted SHA-1");
    }

    @Test
    void getContent_matchingIfNoneMatchAnswersNotModified() {
        UUID projectId = UUID.randomUUID();
        StubAssetService assets = new StubAssetService();
        assets.content = new AssetContent(PNG_BYTES, "image/png", SHA1);

        HttpResponse<byte[]> response = new AssetController(assets)
                .getContent(projectId, UUID.randomUUID(), "\"" + SHA1 + "\"");

        assertEquals(HttpStatus.NOT_MODIFIED, response.getStatus(), "a matching If-None-Match must answer 304");
    }

    @Test
    void getContent_differentIfNoneMatchAnswersOk() {
        UUID projectId = UUID.randomUUID();
        StubAssetService assets = new StubAssetService();
        assets.content = new AssetContent(PNG_BYTES, "image/png", SHA1);

        HttpResponse<byte[]> response = new AssetController(assets)
                .getContent(projectId, UUID.randomUUID(), "\"ffffffffffffffffffffffffffffffffffffffff\"");

        assertEquals(HttpStatus.OK, response.getStatus(), "a stale If-None-Match must still get the content");
    }

    @Test
    void remove_returnsOkWithRemovedAsset() {
        UUID projectId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        StubAssetService assets = new StubAssetService();
        assets.response = sampleAsset(projectId, id);

        HttpResponse<AssetResponseDTO> response = new AssetController(assets)
                .remove(projectId, id);

        assertEquals(HttpStatus.OK, response.getStatus(), "a removed asset must answer 200");
        assertEquals(id, assets.lastDeletedId, "the asset id must reach the service");
        assertNull(response.body().sourceAssetId(), "a texture without source must carry no source id");
    }
}
