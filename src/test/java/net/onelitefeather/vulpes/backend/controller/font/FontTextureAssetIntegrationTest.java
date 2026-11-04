package net.onelitefeather.vulpes.backend.controller.font;

import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import jakarta.inject.Inject;
import net.onelitefeather.vulpes.api.model.asset.AssetKeySpace;
import net.onelitefeather.vulpes.api.model.asset.AssetKind;
import net.onelitefeather.vulpes.api.model.asset.ProjectAssetEntity;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.backend.domain.font.FontModelDTO;
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
@DisplayName("Integration tests for the texture asset of a font")
@EnabledIfDockerAvailable
class FontTextureAssetIntegrationTest {

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
                .body(new ProjectModelDTO(null, "Fonts", "fonts-" + UUID.randomUUID(), null, null, null, false))
                .post("/project")
                .then().statusCode(200)
                .extract().as(ProjectModelResponseDTO.ProjectModelDTO.class)
                .id();
    }

    private ProjectAssetEntity createAsset(UUID project, AssetKind kind, ProjectAssetEntity source) {
        ProjectAssetEntity asset = new ProjectAssetEntity();
        asset.setProject(projectRepository.findById(project).orElseThrow());
        asset.setKey("vulpes:font/ghost-" + UUID.randomUUID().toString().substring(0, 8));
        asset.setDisplayName("Ghost");
        asset.setKind(kind);
        asset.setKeySpace(AssetKeySpace.of(kind));
        asset.setFileName(kind == AssetKind.TEXTURE ? "ghost.png" : "ghost.json");
        asset.setContentType(kind.contentType());
        asset.setSize(1);
        asset.setSha1("0".repeat(40));
        asset.setSourceAsset(source);
        return assetRepository.save(asset);
    }

    private ProjectAssetEntity createTexture(UUID project) {
        return createAsset(project, AssetKind.TEXTURE, null);
    }

    private FontModelDTO font(UUID id, String key, UUID textureAssetId) {
        return new FontModelDTO(id, "Font", key, "bitmap", "font", "texture.png", null, 7, 8, textureAssetId);
    }

    private JsonPath post(String path, FontModelDTO body, int status) {
        return given().contentType(ContentType.JSON).body(body)
                .post("/project/" + projectId + "/font" + path)
                .then().statusCode(status)
                .extract().jsonPath();
    }

    private String violationCode(JsonPath problem) {
        assertEquals("textureAssetId", problem.getString("errors[0].field"));
        return problem.getString("errors[0].code");
    }

    @Test
    @DisplayName("a font is created and read with its texture asset")
    void createAndReadWithTexture() {
        ProjectAssetEntity texture = createTexture(projectId);

        JsonPath created = post("", font(null, "icons", texture.getId()), 200);

        assertEquals(texture.getId().toString(), created.getString("textureAssetId"));
        assertNull(post("", font(null, "plain", null), 200).getString("textureAssetId"));
        String id = created.getString("id");
        JsonPath read = given().get("/project/" + projectId + "/font/" + id).then().statusCode(200).extract().jsonPath();
        assertEquals(texture.getId().toString(), read.getString("textureAssetId"));
    }

    @Test
    @DisplayName("an update sets and removes the texture asset")
    void updateSetsAndRemovesTexture() {
        ProjectAssetEntity texture = createTexture(projectId);
        UUID id = UUID.fromString(post("", font(null, "icons", null), 200).getString("id"));

        assertEquals(texture.getId().toString(),
                post("/update", font(id, "icons", texture.getId()), 200).getString("textureAssetId"));
        assertNull(post("/update", font(id, "icons", null), 200).getString("textureAssetId"));
    }

    @Test
    @DisplayName("a texture asset of another project is refused like a missing one")
    void textureOfAnotherProjectIsRefused() {
        ProjectAssetEntity foreign = createTexture(createProjectId());

        assertEquals("FONT_TEXTURE_NOT_FOUND", violationCode(post("", font(null, "icons", foreign.getId()), 400)));
        assertEquals("FONT_TEXTURE_NOT_FOUND", violationCode(post("", font(null, "icons", UUID.randomUUID()), 400)));
    }

    @Test
    @DisplayName("an asset that is no texture is refused")
    void modelIsRefused() {
        ProjectAssetEntity model = createAsset(projectId, AssetKind.MODEL, null);

        assertEquals("FONT_TEXTURE_NOT_A_TEXTURE", violationCode(post("", font(null, "icons", model.getId()), 400)));
    }

    @Test
    @DisplayName("a texture extracted from a Blockbench model is refused")
    void extractedTextureIsRefused() {
        ProjectAssetEntity model = createAsset(projectId, AssetKind.BBMODEL, null);
        ProjectAssetEntity extracted = createAsset(projectId, AssetKind.TEXTURE, model);

        assertEquals("FONT_TEXTURE_EXTRACTED", violationCode(post("", font(null, "icons", extracted.getId()), 400)));
    }

    @Test
    @DisplayName("deleting the texture asset leaves the font without it")
    void deletingTheTextureLeavesTheFontWithoutIt() {
        ProjectAssetEntity texture = createTexture(projectId);
        String id = post("", font(null, "icons", texture.getId()), 200).getString("id");

        assetRepository.deleteById(texture.getId());

        JsonPath read = given().get("/project/" + projectId + "/font/" + id).then().statusCode(200).extract().jsonPath();
        assertNull(read.getString("textureAssetId"));
    }
}
