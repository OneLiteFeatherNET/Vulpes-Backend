package net.onelitefeather.vulpes.backend.controller.asset;

import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import jakarta.inject.Inject;
import net.datafaker.Faker;
import net.onelitefeather.vulpes.backend.domain.asset.AssetFixtures;
import net.onelitefeather.vulpes.backend.domain.asset.Sha1Hex;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelDTO;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelResponseDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Integration tests for AssetController endpoints with Testcontainers")
@EnabledIfDockerAvailable
class AssetControllerIntegrationTest implements TestPropertyProvider {

    private static final Faker FAKER = new Faker();

    @Inject
    EmbeddedServer server;

    /**
     * Object storage writes into a fresh temp directory per test context, never into the working tree.
     */
    @Override
    public Map<String, String> getProperties() {
        try {
            return Map.of("micronaut.object-storage.local.assets.path",
                    Files.createTempDirectory("vulpes-assets-").toString());
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    /**
     * Every request in this class needs a persisted project; one project is shared, every test uses unique keys.
     */
    private UUID projectId;

    @BeforeAll
    void createProject() {
        RestAssured.baseURI = server.getURL().toString();
        ProjectModelDTO project = new ProjectModelDTO(
                null, FAKER.company().name(), "asset-it-" + UUID.randomUUID(), null, null, null, false);
        ProjectModelResponseDTO.ProjectModelDTO created =
                given()
                        .contentType(ContentType.JSON)
                        .body(project)
                .when()
                        .post("/project")
                .then()
                        .statusCode(200)
                        .extract().as(ProjectModelResponseDTO.ProjectModelDTO.class);
        projectId = created.id();
    }

    @BeforeEach
    void setup() {
        RestAssured.baseURI = server.getURL().toString();
    }

    private String assetPath(String suffix) {
        return "/project/" + projectId + "/asset" + suffix;
    }

    /**
     * A unique folder per test keeps derived texture keys from colliding across tests.
     */
    private static String uniqueFolder() {
        return "bb" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String uploadPng(String key, byte[] png) {
        return given()
                .multiPart("file", "ruby.png", png, "image/png")
                .multiPart("key", key)
                .multiPart("displayName", "Ruby")
        .when()
                .post(assetPath(""))
        .then()
                .statusCode(201)
                .extract().path("id");
    }

    /**
     * Creates a project of its own, so a test can use fixed asset keys without seeing other tests' assets.
     */
    private UUID newProjectId() {
        ProjectModelDTO project = new ProjectModelDTO(
                null, FAKER.company().name(), "asset-it-" + UUID.randomUUID(), null, null, null, false);
        return given()
                .contentType(ContentType.JSON)
                .body(project)
        .when()
                .post("/project")
        .then()
                .statusCode(200)
                .extract().as(ProjectModelResponseDTO.ProjectModelDTO.class).id();
    }

    private String assetPath(UUID project, String suffix) {
        return "/project/" + project + "/asset" + suffix;
    }

    private String createItem(UUID project) {
        String body = "{\"uiName\":\"Ruby\",\"key\":\"vulpes:item-" + UUID.randomUUID().toString().substring(0, 8)
                + "\",\"groupName\":\"gems\"}";
        return given()
                .contentType(ContentType.JSON)
                .body(body)
        .when()
                .post("/project/" + project + "/item")
        .then()
                .statusCode(200)
                .extract().path("id");
    }

    @Test
    void uploadPngReturns201WithDimensionsAndSha1() {
        byte[] png = AssetFixtures.png(16, 8);
        String key = "vulpes:item/png-" + UUID.randomUUID().toString().substring(0, 8);

        given()
                .multiPart("file", "ruby.png", png, "image/png")
                .multiPart("key", key)
                .multiPart("displayName", "Ruby")
        .when()
                .post(assetPath(""))
        .then()
                .statusCode(201)
                .body("key", is(key))
                .body("displayName", is("Ruby"))
                .body("kind", is("TEXTURE"))
                .body("fileName", is("ruby.png"))
                .body("contentType", is("image/png"))
                .body("size", is(png.length))
                .body("sha1", is(Sha1Hex.of(png)))
                .body("width", is(16))
                .body("height", is(8))
                .body("sourceAssetId", nullValue())
                .body("derivedAssetIds", empty())
                .body("creationDate", notNullValue())
                .body("modificationDate", notNullValue());
    }

    @Test
    void uploadWithoutDisplayNameReturns400OnDisplayNameField() {
        given()
                .multiPart("file", "ruby.png", AssetFixtures.png(16, 16), "image/png")
                .multiPart("key", "vulpes:item/unnamed-" + UUID.randomUUID().toString().substring(0, 8))
        .when()
                .post(assetPath(""))
        .then()
                .statusCode(400)
                .body("errors.field", hasItem("displayName"))
                .body("errors.code", hasItem("ASSET_DISPLAY_NAME_INVALID"));
    }

    @Test
    void duplicateKeyReturns409() {
        String key = "vulpes:item/dup-" + UUID.randomUUID().toString().substring(0, 8);
        uploadPng(key, AssetFixtures.png(16, 16));

        given()
                .multiPart("file", "ruby.png", AssetFixtures.png(16, 16), "image/png")
                .multiPart("key", key)
                .multiPart("displayName", "Ruby")
        .when()
                .post(assetPath(""))
        .then()
                .statusCode(409);
    }

    @Test
    void contentReturnsBytesAndEtagAndAnsweresNotModifiedOnMatch() {
        byte[] png = AssetFixtures.png(16, 16);
        String id = uploadPng("vulpes:item/content-" + UUID.randomUUID().toString().substring(0, 8), png);

        byte[] served = given()
        .when()
                .get(assetPath("/" + id + "/content"))
        .then()
                .statusCode(200)
                .contentType("image/png")
                .header("ETag", "\"" + Sha1Hex.of(png) + "\"")
                .extract().asByteArray();
        assertArrayEquals(png, served, "the content must be the uploaded bytes");

        given()
                .header("If-None-Match", "\"" + Sha1Hex.of(png) + "\"")
        .when()
                .get(assetPath("/" + id + "/content"))
        .then()
                .statusCode(304);
    }

    @Test
    void uploadBbmodelExtractsTextureListedWithSourceAssetId() {
        String folder = uniqueFolder();
        String bbKey = "vulpes:" + folder + "/gem";
        String bbmodel = AssetFixtures.bbmodelWithTextures("Ruby Side.png");

        String bbId = given()
                .multiPart("file", "gem.bbmodel", AssetFixtures.utf8(bbmodel), "application/json")
                .multiPart("key", bbKey)
                .multiPart("displayName", "Ruby")
        .when()
                .post(assetPath(""))
        .then()
                .statusCode(201)
                .body("kind", is("BBMODEL"))
                .body("derivedAssetIds", hasSize(1))
                .extract().path("id");

        given()
        .when()
                .get(assetPath("/?kind=TEXTURE&size=500"))
        .then()
                .statusCode(200)
                .body("content.key", hasItem("vulpes:" + folder + "/ruby_side"))
                .body("content.sourceAssetId", hasItem(bbId));
    }

    @Test
    void deleteAssetRemovesDerivedTextures() {
        String folder = uniqueFolder();
        String bbId = given()
                .multiPart("file", "gem.bbmodel",
                        AssetFixtures.utf8(AssetFixtures.bbmodelWithTextures("Gem Texture")), "application/json")
                .multiPart("key", "vulpes:" + folder + "/gem")
                .multiPart("displayName", "Ruby")
        .when()
                .post(assetPath(""))
        .then()
                .statusCode(201)
                .extract().path("id");
        String derivedId = given()
        .when()
                .get(assetPath("/?kind=TEXTURE&size=500"))
        .then()
                .statusCode(200)
                .extract().path("content.find { it.sourceAssetId == '" + bbId + "' }.id");
        assertNotNull(derivedId, "the extracted texture must be listed with the model as source");

        given()
        .when()
                .delete(assetPath("/delete/" + bbId))
        .then()
                .statusCode(200)
                .body("id", is(bbId));

        given()
        .when()
                .get(assetPath("/" + derivedId))
        .then()
                .statusCode(404);
    }

    @Test
    void deletingModelKeepsItemModelComponentOfItem() {
        UUID project = newProjectId();
        String itemId = createItem(project);
        given()
                .contentType(ContentType.JSON)
                .body("{\"componentKey\":\"minecraft:item_model\",\"value\":\"vulpes:item/ruby\"}")
        .when()
                .put("/item/" + itemId + "/component")
        .then()
                .statusCode(200);
        String modelId = given()
                .multiPart("file", "ruby.json", AssetFixtures.utf8("{}"), "application/json")
                .multiPart("key", "vulpes:item/ruby")
                .multiPart("displayName", "Ruby")
        .when()
                .post(assetPath(project, ""))
        .then()
                .statusCode(201)
                .extract().path("id");

        given()
        .when()
                .delete(assetPath(project, "/delete/" + modelId))
        .then()
                .statusCode(200);

        given()
        .when()
                .get("/item/" + itemId + "/components")
        .then()
                .statusCode(200)
                .body("content.find { it.componentKey == 'minecraft:item_model' }.value", is("vulpes:item/ruby"));
    }

    private ValidatableResponse postAsset(UUID project, String fileName, String contentType, byte[] content, String key) {
        return given()
                .multiPart("file", fileName, content, contentType)
                .multiPart("key", key)
                .multiPart("displayName", "Ruby")
        .when()
                .post(assetPath(project, ""))
        .then();
    }

    @Test
    void textureMayShareKeyWithModel() {
        UUID project = newProjectId();
        postAsset(project, "ruby.json", "application/json", AssetFixtures.utf8("{}"), "vulpes:item/ruby")
                .statusCode(201);

        postAsset(project, "ruby.png", "image/png", AssetFixtures.png(16, 16), "vulpes:item/ruby")
                .statusCode(201);
    }

    @Test
    void bbmodelTextureNamedAfterModelIsStoredWithSameKey() {
        UUID project = newProjectId();
        String bbId = postAsset(project, "ruby.bbmodel", "application/json",
                AssetFixtures.utf8(AssetFixtures.bbmodelWithTextures("ruby")), "vulpes:item/ruby")
                .statusCode(201)
                .extract().path("id");

        given()
        .when()
                .get(assetPath(project, "/" + bbId))
        .then()
                .statusCode(200)
                .body("key", is("vulpes:item/ruby"));
        given()
        .when()
                .get(assetPath(project, "/?kind=TEXTURE&size=500"))
        .then()
                .statusCode(200)
                .body("content.find { it.sourceAssetId == '" + bbId + "' }.key", is("vulpes:item/ruby"));
    }

    @Test
    void duplicateTextureKeyReturns409() {
        UUID project = newProjectId();
        postAsset(project, "ruby.png", "image/png", AssetFixtures.png(16, 16), "vulpes:item/ruby")
                .statusCode(201);

        postAsset(project, "ruby.png", "image/png", AssetFixtures.png(16, 16), "vulpes:item/ruby")
                .statusCode(409);
    }

    @Test
    void bbmodelWithKeyOfModelReturns409() {
        UUID project = newProjectId();
        postAsset(project, "ruby.json", "application/json", AssetFixtures.utf8("{}"), "vulpes:item/ruby")
                .statusCode(201);

        postAsset(project, "ruby.bbmodel", "application/json",
                AssetFixtures.utf8(AssetFixtures.bbmodelWithTextures("gem")), "vulpes:item/ruby")
                .statusCode(409);
    }

    @Test
    void replacingWithOtherKindReturns400OnFileField() {
        String textureId = uploadPng(
                "vulpes:item/kind-" + UUID.randomUUID().toString().substring(0, 8), AssetFixtures.png(16, 16));

        given()
                .multiPart("file", "model.json", AssetFixtures.utf8("{}"), "application/json")
        .when()
                .post(assetPath("/" + textureId + "/content"))
        .then()
                .statusCode(400)
                .body("errors.field", hasItem("file"))
                .body("errors.code", hasItem("ASSET_UNSUPPORTED_TYPE"));
    }

    @Test
    void getUnknownAssetReturns404() {
        given()
        .when()
                .get(assetPath("/" + UUID.randomUUID()))
        .then()
                .statusCode(404);
    }

    @Test
    void uploadAboveMultipartLimitReturns413ProblemAndStoresNothing() {
        UUID project = newProjectId();
        byte[] oversized = new byte[8 * 1024 * 1024 + 1024];

        postAsset(project, "ruby.png", "image/png", oversized, "vulpes:item/oversized")
                .statusCode(413)
                .contentType("application/problem+json")
                .body("type", is("https://vulpes.onelitefeather.net/errors/payload-too-large"))
                .body("status", is(413));

        given()
        .when()
                .get(assetPath(project, "/"))
        .then()
                .statusCode(200)
                .body("content", empty());
    }
}
