package net.onelitefeather.vulpes.backend.controller.asset;

import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
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
        .when()
                .post(assetPath(""))
        .then()
                .statusCode(201)
                .extract().path("id");
    }

    private String createItem() {
        String body = "{\"uiName\":\"Ruby\",\"key\":\"vulpes:item-" + UUID.randomUUID().toString().substring(0, 8)
                + "\",\"groupName\":\"gems\"}";
        return given()
                .contentType(ContentType.JSON)
                .body(body)
        .when()
                .post("/project/" + projectId + "/item")
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
        .when()
                .post(assetPath(""))
        .then()
                .statusCode(201)
                .body("key", is(key))
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
    void duplicateKeyReturns409() {
        String key = "vulpes:item/dup-" + UUID.randomUUID().toString().substring(0, 8);
        uploadPng(key, AssetFixtures.png(16, 16));

        given()
                .multiPart("file", "ruby.png", AssetFixtures.png(16, 16), "image/png")
                .multiPart("key", key)
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
    void linkTextureToItemIsListedWithEmbeddedAsset() {
        String folder = uniqueFolder();
        String textureId = uploadPng("vulpes:" + folder + "/ruby", AssetFixtures.png(16, 16));
        String itemId = createItem();

        given()
                .contentType(ContentType.JSON)
                .body("{\"targetType\":\"ITEM\",\"targetId\":\"" + itemId + "\",\"role\":\"TEXTURE\"}")
        .when()
                .post(assetPath("/" + textureId + "/link"))
        .then()
                .statusCode(201)
                .body("assetId", is(textureId))
                .body("role", is("TEXTURE"));

        given()
                .queryParam("targetType", "ITEM")
                .queryParam("targetId", itemId)
        .when()
                .get(assetPath("/link"))
        .then()
                .statusCode(200)
                .body("", hasSize(1))
                .body("[0].asset.id", is(textureId))
                .body("[0].asset.key", is("vulpes:" + folder + "/ruby"));
    }

    @Test
    void roleMismatchReturns400OnRoleField() {
        String textureId = uploadPng(
                "vulpes:item/role-" + UUID.randomUUID().toString().substring(0, 8), AssetFixtures.png(16, 16));
        String itemId = createItem();

        given()
                .contentType(ContentType.JSON)
                .body("{\"targetType\":\"ITEM\",\"targetId\":\"" + itemId + "\",\"role\":\"MODEL\"}")
        .when()
                .post(assetPath("/" + textureId + "/link"))
        .then()
                .statusCode(400)
                .body("code", is("VALIDATION_FAILED"))
                .body("errors.field", hasItem("role"));
    }

    @Test
    void deleteAssetRemovesLinksAndDerivedTextures() {
        String folder = uniqueFolder();
        String bbId = given()
                .multiPart("file", "gem.bbmodel",
                        AssetFixtures.utf8(AssetFixtures.bbmodelWithTextures("Gem Texture")), "application/json")
                .multiPart("key", "vulpes:" + folder + "/gem")
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
        String itemId = createItem();
        given()
                .contentType(ContentType.JSON)
                .body("{\"targetType\":\"ITEM\",\"targetId\":\"" + itemId + "\",\"role\":\"TEXTURE\"}")
        .when()
                .post(assetPath("/" + derivedId + "/link"))
        .then()
                .statusCode(201);

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
        given()
                .queryParam("targetType", "ITEM")
                .queryParam("targetId", itemId)
        .when()
                .get(assetPath("/link"))
        .then()
                .statusCode(200)
                .body("", empty());
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

    /**
     * Replaces the content of an asset. The If-Match header is sent only when one is given.
     */
    private Response replaceContent(String id, byte[] png, String ifMatch) {
        RequestSpecification request = given().multiPart("file", "ruby.png", png, "image/png");
        if (ifMatch != null) {
            request = request.header("If-Match", ifMatch);
        }
        return request.when().post(assetPath("/" + id + "/content"));
    }

    private static String shortId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    @Test
    void uploadResponseCarriesEtagOfStoredBytes() {
        byte[] png = AssetFixtures.png(16, 16);

        given()
                .multiPart("file", "ruby.png", png, "image/png")
                .multiPart("key", "vulpes:item/upload-etag-" + shortId())
        .when()
                .post(assetPath(""))
        .then()
                .statusCode(201)
                .header("ETag", "\"" + Sha1Hex.of(png) + "\"");
    }

    @Test
    void replaceWithMatchingIfMatchReturns200AndNewEtag() {
        byte[] first = AssetFixtures.png(16, 16);
        byte[] second = AssetFixtures.png(16, 8);
        String id = uploadPng("vulpes:item/match-" + shortId(), first);

        replaceContent(id, second, "\"" + Sha1Hex.of(first) + "\"")
        .then()
                .statusCode(200)
                .header("ETag", "\"" + Sha1Hex.of(second) + "\"")
                .body("sha1", is(Sha1Hex.of(second)));
    }

    @Test
    void replaceWithStaleIfMatchReturns412WithCurrentEtagAndKeepsContent() {
        byte[] first = AssetFixtures.png(16, 16);
        byte[] second = AssetFixtures.png(16, 8);
        byte[] third = AssetFixtures.png(8, 8);
        String id = uploadPng("vulpes:item/stale-" + shortId(), first);
        replaceContent(id, second, null).then().statusCode(200);

        replaceContent(id, third, "\"" + Sha1Hex.of(first) + "\"")
        .then()
                .statusCode(412)
                .header("ETag", "\"" + Sha1Hex.of(second) + "\"")
                .body("code", is("ASSET_PRECONDITION_FAILED"));

        byte[] served = given()
        .when()
                .get(assetPath("/" + id + "/content"))
        .then()
                .statusCode(200)
                .extract().asByteArray();
        assertArrayEquals(second, served, "a refused replacement must leave the stored content unchanged");
    }

    @Test
    void replaceWithoutIfMatchKeepsLastWriterWins() {
        byte[] first = AssetFixtures.png(16, 16);
        byte[] second = AssetFixtures.png(16, 8);
        byte[] third = AssetFixtures.png(8, 8);
        String id = uploadPng("vulpes:item/plain-" + shortId(), first);
        replaceContent(id, second, null).then().statusCode(200);

        replaceContent(id, third, null)
        .then()
                .statusCode(200)
                .body("sha1", is(Sha1Hex.of(third)));
    }

    @Test
    void replaceWithWildcardIfMatchReturns200() {
        byte[] first = AssetFixtures.png(16, 16);
        byte[] second = AssetFixtures.png(16, 8);
        String id = uploadPng("vulpes:item/wildcard-" + shortId(), first);

        replaceContent(id, second, "*")
        .then()
                .statusCode(200)
                .body("sha1", is(Sha1Hex.of(second)));
    }

    @Test
    void replaceWithWeakIfMatchReturns412() {
        byte[] first = AssetFixtures.png(16, 16);
        byte[] second = AssetFixtures.png(16, 8);
        String id = uploadPng("vulpes:item/weak-" + shortId(), first);

        replaceContent(id, second, "W/\"" + Sha1Hex.of(first) + "\"")
        .then()
                .statusCode(412);
    }

    @Test
    void replaceWithUnquotedIfMatchReturns412() {
        byte[] first = AssetFixtures.png(16, 16);
        byte[] second = AssetFixtures.png(16, 8);
        String id = uploadPng("vulpes:item/unquoted-" + shortId(), first);

        replaceContent(id, second, Sha1Hex.of(first))
        .then()
                .statusCode(412);
    }

    @Test
    void getUnknownAssetReturns404() {
        given()
        .when()
                .get(assetPath("/" + UUID.randomUUID()))
        .then()
                .statusCode(404);
    }
}
