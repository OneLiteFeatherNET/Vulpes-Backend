package net.onelitefeather.vulpes.backend.controller.pack;

import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import jakarta.inject.Inject;
import net.onelitefeather.vulpes.api.model.asset.AssetKeySpace;
import net.onelitefeather.vulpes.api.model.asset.AssetKind;
import net.onelitefeather.vulpes.api.model.asset.ProjectAssetEntity;
import net.onelitefeather.vulpes.api.model.pack.PackType;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.backend.domain.pack.PackMetaDTO;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelDTO;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelResponseDTO;
import net.onelitefeather.vulpes.backend.entity.asset.ProjectAssetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@MicronautTest(transactional = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Integration tests for the icon asset of a pack meta")
@EnabledIfDockerAvailable
class PackIconAssetIntegrationTest {

    @Inject
    EmbeddedServer server;

    @Inject
    ProjectRepository projectRepository;

    @Inject
    ProjectAssetRepository assetRepository;

    private UUID projectId;

    @BeforeEach
    void createProject() {
        RestAssured.baseURI = server.getURL().toString();
        projectId = createProjectId();
    }

    private UUID createProjectId() {
        return given()
                .contentType(ContentType.JSON)
                .body(new ProjectModelDTO(null, "Packs", "packs-" + UUID.randomUUID(), null, null, null, false))
                .post("/project")
                .then().statusCode(200)
                .extract().as(ProjectModelResponseDTO.ProjectModelDTO.class)
                .id();
    }

    private ProjectAssetEntity createAsset(UUID project, AssetKind kind, ProjectAssetEntity source) {
        ProjectAssetEntity asset = new ProjectAssetEntity();
        asset.setProject(projectRepository.findById(project).orElseThrow());
        asset.setKey("vulpes:pack/icon-" + UUID.randomUUID().toString().substring(0, 8));
        asset.setDisplayName("Icon");
        asset.setKind(kind);
        asset.setKeySpace(AssetKeySpace.of(kind));
        asset.setFileName(kind == AssetKind.TEXTURE ? "pack.png" : "pack.json");
        asset.setContentType(kind.contentType());
        asset.setSize(1);
        asset.setSha1("0".repeat(40));
        asset.setSourceAsset(source);
        return assetRepository.save(asset);
    }

    private ProjectAssetEntity createTexture(UUID project) {
        return createAsset(project, AssetKind.TEXTURE, null);
    }

    private static PackMetaDTO meta(UUID iconAssetId) {
        return new PackMetaDTO("{\"text\":\"My pack\"}", 48, 0, 57, null, null, iconAssetId);
    }

    private JsonPath put(PackType type, PackMetaDTO body, int status) {
        return given().contentType(ContentType.JSON).body(body)
                .put("/project/" + projectId + "/pack-meta/" + type)
                .then().statusCode(status)
                .extract().jsonPath();
    }

    private JsonPath get(PackType type) {
        return given().get("/project/" + projectId + "/pack-meta/" + type)
                .then().statusCode(200)
                .extract().jsonPath();
    }

    private String violationCode(JsonPath problem) {
        assertEquals("iconAssetId", problem.getString("errors[0].field"));
        return problem.getString("errors[0].code");
    }

    @Test
    @DisplayName("a pack meta is created and read with its icon asset")
    void createAndReadWithIcon() {
        ProjectAssetEntity icon = createTexture(projectId);

        JsonPath created = put(PackType.RESOURCE, meta(icon.getId()), 200);

        assertEquals(icon.getId().toString(), created.getString("iconAssetId"));
        assertEquals(icon.getId().toString(), get(PackType.RESOURCE).getString("iconAssetId"));
        assertNull(put(PackType.DATA, meta(null), 200).getString("iconAssetId"));
    }

    @Test
    @DisplayName("replacing a pack meta sets and removes the icon asset")
    void replaceSetsAndRemovesIcon() {
        ProjectAssetEntity icon = createTexture(projectId);
        put(PackType.RESOURCE, meta(null), 200);

        assertEquals(icon.getId().toString(), put(PackType.RESOURCE, meta(icon.getId()), 200).getString("iconAssetId"));
        assertNull(put(PackType.RESOURCE, meta(null), 200).getString("iconAssetId"));
    }

    @Test
    @DisplayName("an icon asset of another project is refused like a missing one")
    void iconOfAnotherProjectIsRefused() {
        ProjectAssetEntity foreign = createTexture(createProjectId());

        assertEquals("PACK_ICON_NOT_FOUND", violationCode(put(PackType.RESOURCE, meta(foreign.getId()), 400)));
        assertEquals("PACK_ICON_NOT_FOUND", violationCode(put(PackType.RESOURCE, meta(UUID.randomUUID()), 400)));
    }

    @Test
    @DisplayName("an asset that is no texture is refused")
    void modelIsRefused() {
        ProjectAssetEntity model = createAsset(projectId, AssetKind.MODEL, null);

        assertEquals("PACK_ICON_NOT_A_TEXTURE", violationCode(put(PackType.RESOURCE, meta(model.getId()), 400)));
    }

    @Test
    @DisplayName("a texture extracted from a Blockbench model is refused")
    void extractedTextureIsRefused() {
        ProjectAssetEntity model = createAsset(projectId, AssetKind.BBMODEL, null);
        ProjectAssetEntity extracted = createAsset(projectId, AssetKind.TEXTURE, model);

        assertEquals("PACK_ICON_EXTRACTED", violationCode(put(PackType.RESOURCE, meta(extracted.getId()), 400)));
    }

    @Test
    @DisplayName("a refused icon leaves the stored pack meta unchanged")
    void refusedIconKeepsStoredPackMeta() {
        ProjectAssetEntity icon = createTexture(projectId);
        put(PackType.RESOURCE, meta(icon.getId()), 200);

        put(PackType.RESOURCE, meta(UUID.randomUUID()), 400);

        assertEquals(icon.getId().toString(), get(PackType.RESOURCE).getString("iconAssetId"));
    }

    @Test
    @DisplayName("deleting the icon asset leaves the pack meta without it")
    void deletingTheIconLeavesThePackMetaWithoutIt() {
        ProjectAssetEntity icon = createTexture(projectId);
        put(PackType.RESOURCE, meta(icon.getId()), 200);

        assetRepository.deleteById(icon.getId());

        assertNull(get(PackType.RESOURCE).getString("iconAssetId"));
    }
}
