package net.onelitefeather.vulpes.backend.controller.asset;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import jakarta.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.datafaker.Faker;
import net.onelitefeather.vulpes.api.model.asset.AssetKeySpace;
import net.onelitefeather.vulpes.api.model.asset.AssetKind;
import net.onelitefeather.vulpes.api.model.asset.ProjectAssetEntity;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.backend.domain.asset.AssetFixtures;
import net.onelitefeather.vulpes.backend.domain.asset.Sha1Hex;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelDTO;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelResponseDTO;
import net.onelitefeather.vulpes.backend.entity.asset.ProjectAssetRepository;
import net.onelitefeather.vulpes.backend.storage.AssetStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

/**
 * Integration tests for the revisions of project assets. Every test creates its own project, so the
 * revision lists it reads contain only what the test wrote.
 */
/**
 * Not transactional: the tests talk to the server over HTTP, and the legacy fixture is inserted directly, so
 * it must be committed to be visible to the server thread.
 */
@MicronautTest(transactional = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Integration tests for asset revisions with Testcontainers")
@EnabledIfDockerAvailable
class AssetRevisionIntegrationTest implements TestPropertyProvider {

    private static final Faker FAKER = new Faker();

    @Inject
    EmbeddedServer server;

    @Inject
    ProjectRepository projectRepository;

    @Inject
    ProjectAssetRepository assetRepository;

    @Inject
    AssetStorage storage;

    /**
     * Object storage writes into a fresh temp directory per test context, never into the working tree.
     */
    @Override
    public Map<String, String> getProperties() {
        try {
            return Map.of(
                    "micronaut.object-storage.local.assets.path",
                    Files.createTempDirectory("vulpes-assets-").toString());
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    @BeforeEach
    void setup() {
        RestAssured.baseURI = server.getURL().toString();
    }

    private UUID newProjectId() {
        ProjectModelDTO project = new ProjectModelDTO(
                null, FAKER.company().name(), "asset-rev-it-" + UUID.randomUUID(), null, null, null, false);
        return given().contentType(ContentType.JSON)
                .body(project)
                .when()
                .post("/project")
                .then()
                .statusCode(200)
                .extract()
                .as(ProjectModelResponseDTO.ProjectModelDTO.class)
                .id();
    }

    private static String assetPath(UUID project, String suffix) {
        return "/project/" + project + "/asset" + suffix;
    }

    private static String shortId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private String uploadPng(UUID project, String key, byte[] png, String comment) {
        RequestSpecification request = given().multiPart("file", "ruby.png", png, "image/png")
                .multiPart("key", key)
                .multiPart("displayName", "Ruby");
        if (comment != null) {
            request = request.multiPart("comment", comment);
        }
        return request.when()
                .post(assetPath(project, ""))
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }

    private Response replacePng(UUID project, String id, byte[] png, String ifMatch) {
        RequestSpecification request = given().multiPart("file", "ruby.png", png, "image/png");
        if (ifMatch != null) {
            request = request.header("If-Match", ifMatch);
        }
        return request.when().post(assetPath(project, "/" + id + "/content"));
    }

    private String currentEtag(UUID project, String id) {
        return given().when()
                .get(assetPath(project, "/" + id))
                .then()
                .statusCode(200)
                .extract()
                .path("sha1");
    }

    private Response listRevisions(UUID project, String id) {
        return given().queryParam("size", 500)
                .when()
                .get(assetPath(project, "/" + id + "/revision"))
                .then()
                .statusCode(200)
                .extract()
                .response();
    }

    private byte[] revisionContent(UUID project, String id, String revisionId) {
        return given().when()
                .get(assetPath(project, "/" + id + "/revision/" + revisionId + "/content"))
                .then()
                .statusCode(200)
                .extract()
                .asByteArray();
    }

    @Test
    void uploadCreatesRevisionOneWithComment() {
        UUID project = newProjectId();
        byte[] png = AssetFixtures.png(16, 16);
        String id = uploadPng(project, "vulpes:item/rev-one-" + shortId(), png, "first pass");

        listRevisions(project, id)
                .then()
                .body("content", hasSize(1))
                .body("content[0].number", is(1))
                .body("content[0].sha1", is(Sha1Hex.of(png)))
                .body("content[0].size", is(png.length))
                .body("content[0].comment", is("first pass"))
                .body("content[0].current", is(true))
                .body("content[0].creationDate", notNullValue());
    }

    @Test
    void replaceCreatesRevisionTwoAndServesNewBytes() {
        UUID project = newProjectId();
        String id = uploadPng(project, "vulpes:item/rev-two-" + shortId(), AssetFixtures.png(16, 16), null);
        byte[] second = AssetFixtures.png(16, 8);

        replacePng(project, id, second, null).then().statusCode(200);

        listRevisions(project, id).then().body("content", hasSize(2)).body("content[0].number", is(2));
        byte[] served = given().when()
                .get(assetPath(project, "/" + id + "/content"))
                .then()
                .statusCode(200)
                .extract()
                .asByteArray();
        assertArrayEquals(second, served, "the content endpoint must serve the newest revision");
    }

    @Test
    void revisionsAreListedNewestFirstWithOnlyNewestMarkedCurrent() {
        UUID project = newProjectId();
        String id = uploadPng(project, "vulpes:item/rev-order-" + shortId(), AssetFixtures.png(16, 16), null);
        replacePng(project, id, AssetFixtures.png(16, 8), null).then().statusCode(200);
        replacePng(project, id, AssetFixtures.png(8, 8), null).then().statusCode(200);

        listRevisions(project, id)
                .then()
                .body("content.number", contains(3, 2, 1))
                .body("content.current", contains(true, false, false));
    }

    @Test
    void revisionContentServesOwnBytesAndEtag() {
        UUID project = newProjectId();
        byte[] first = AssetFixtures.png(16, 16);
        String id = uploadPng(project, "vulpes:item/rev-content-" + shortId(), first, null);
        replacePng(project, id, AssetFixtures.png(16, 8), null).then().statusCode(200);
        String firstRevisionId = listRevisions(project, id).path("content.find { it.number == 1 }.id");

        byte[] served = given().when()
                .get(assetPath(project, "/" + id + "/revision/" + firstRevisionId + "/content"))
                .then()
                .statusCode(200)
                .contentType("image/png")
                .header("ETag", "\"" + Sha1Hex.of(first) + "\"")
                .extract()
                .asByteArray();
        assertArrayEquals(first, served, "an earlier revision must serve its own bytes");
    }

    @Test
    void restoreWritesOldBytesAsNewRevisionWithNewEtag() {
        UUID project = newProjectId();
        byte[] first = AssetFixtures.png(16, 16);
        String id = uploadPng(project, "vulpes:item/restore-" + shortId(), first, null);
        replacePng(project, id, AssetFixtures.png(16, 8), null).then().statusCode(200);
        String firstRevisionId = listRevisions(project, id).path("content.find { it.number == 1 }.id");

        given().header("If-Match", "\"" + currentEtag(project, id) + "\"")
                .when()
                .post(assetPath(project, "/" + id + "/revision/" + firstRevisionId + "/restore"))
                .then()
                .statusCode(200)
                .header("ETag", "\"" + Sha1Hex.of(first) + "\"")
                .body("sha1", is(Sha1Hex.of(first)));

        listRevisions(project, id)
                .then()
                .body("content", hasSize(3))
                .body("content[0].number", is(3))
                .body("content[0].comment", is("Restored revision 1"))
                .body("content[0].current", is(true));
        byte[] served = given().when()
                .get(assetPath(project, "/" + id + "/content"))
                .then()
                .statusCode(200)
                .extract()
                .asByteArray();
        assertArrayEquals(first, served, "the restored bytes must be the current content");
    }

    @Test
    void restoreWithStaleIfMatchReturns412AndCreatesNoRevision() {
        UUID project = newProjectId();
        byte[] first = AssetFixtures.png(16, 16);
        byte[] second = AssetFixtures.png(16, 8);
        String id = uploadPng(project, "vulpes:item/restore-stale-" + shortId(), first, null);
        replacePng(project, id, second, null).then().statusCode(200);
        String firstRevisionId = listRevisions(project, id).path("content.find { it.number == 1 }.id");

        given().header("If-Match", "\"" + Sha1Hex.of(first) + "\"")
                .when()
                .post(assetPath(project, "/" + id + "/revision/" + firstRevisionId + "/restore"))
                .then()
                .statusCode(412)
                .header("ETag", "\"" + Sha1Hex.of(second) + "\"")
                .body("code", is("ASSET_PRECONDITION_FAILED"));

        listRevisions(project, id).then().body("content", hasSize(2));
    }

    @Test
    void legacyAssetIsListedWithRevisionOneOnFirstRead() {
        UUID project = newProjectId();
        byte[] legacy = AssetFixtures.png(16, 16);
        String id = createLegacyTexture(project, legacy);

        listRevisions(project, id)
                .then()
                .body("content", hasSize(1))
                .body("content[0].number", is(1))
                .body("content[0].sha1", is(Sha1Hex.of(legacy)))
                .body("content[0].current", is(true));
    }

    @Test
    void legacyBytesStayRevisionOneAfterReplacement() {
        UUID project = newProjectId();
        byte[] legacy = AssetFixtures.png(16, 16);
        byte[] replacement = AssetFixtures.png(16, 8);
        String id = createLegacyTexture(project, legacy);
        listRevisions(project, id).then().statusCode(200);

        replacePng(project, id, replacement, null).then().statusCode(200);

        String firstRevisionId = listRevisions(project, id).path("content.find { it.number == 1 }.id");
        assertArrayEquals(
                legacy,
                revisionContent(project, id, firstRevisionId),
                "revision 1 of a legacy asset must keep the bytes the asset had before");
    }

    @Test
    void legacyAssetWithMissingBytesIsListedAndDownloadFails() {
        UUID project = newProjectId();
        String id = createLegacyTextureRow(project, AssetFixtures.png(16, 16));

        listRevisions(project, id).then().body("content", hasSize(1)).body("content[0].number", is(1));
        String revisionId = listRevisions(project, id).path("content[0].id");
        given().when()
                .get(assetPath(project, "/" + id + "/revision/" + revisionId + "/content"))
                .then()
                .statusCode(500);
    }

    @Test
    void deleteAssetRemovesRevisionsAndTheirBytes() {
        UUID project = newProjectId();
        String id = uploadPng(project, "vulpes:item/delete-" + shortId(), AssetFixtures.png(16, 16), null);
        replacePng(project, id, AssetFixtures.png(16, 8), null).then().statusCode(200);
        List<Integer> numbers = listRevisions(project, id).path("content.number");
        List<String> keysBefore = numbers.stream()
                .map(number -> AssetStorage.revisionKeyFor(project, UUID.fromString(id), number))
                .toList();
        keysBefore.forEach(key -> assertTrue(storage.get(key).isPresent(), "revision bytes must exist before delete"));

        given().when().delete(assetPath(project, "/delete/" + id)).then().statusCode(200);

        keysBefore.forEach(key -> assertTrue(storage.get(key).isEmpty(), "revision bytes must be gone after delete"));
        given().when().get(assetPath(project, "/" + id + "/revision")).then().statusCode(404);
    }

    @Test
    void revisionOfAnotherAssetReturns404() {
        UUID project = newProjectId();
        String ownerId = uploadPng(project, "vulpes:item/owner-" + shortId(), AssetFixtures.png(16, 16), null);
        String otherId = uploadPng(project, "vulpes:item/other-" + shortId(), AssetFixtures.png(16, 16), null);
        String foreignRevisionId = listRevisions(project, otherId).path("content[0].id");

        given().when()
                .get(assetPath(project, "/" + ownerId + "/revision/" + foreignRevisionId + "/content"))
                .then()
                .statusCode(404);
        given().header("If-Match", "\"" + currentEtag(project, ownerId) + "\"")
                .when()
                .post(assetPath(project, "/" + ownerId + "/revision/" + foreignRevisionId + "/restore"))
                .then()
                .statusCode(404);
    }

    @Test
    void commentLongerThan500CharactersReturns400() {
        UUID project = newProjectId();
        String longComment = "x".repeat(501);

        given().multiPart("file", "ruby.png", AssetFixtures.png(16, 16), "image/png")
                .multiPart("key", "vulpes:item/long-comment-" + shortId())
                .multiPart("displayName", "Ruby")
                .multiPart("comment", longComment)
                .when()
                .post(assetPath(project, ""))
                .then()
                .statusCode(400)
                .body("errors.field", hasItem("comment"))
                .body("errors.code", hasItem("ASSET_COMMENT_TOO_LONG"));
    }

    @Test
    void restoringBlockbenchModelExtractsItsTexturesAgain() {
        UUID project = newProjectId();
        String folder = "bb" + shortId();
        String modelKey = "vulpes:" + folder + "/gem";
        String id = given().multiPart(
                        "file",
                        "gem.bbmodel",
                        AssetFixtures.utf8(AssetFixtures.bbmodelWithTextures("Ruby Side.png")),
                        "application/json")
                .multiPart("key", modelKey)
                .multiPart("displayName", "Gem")
                .when()
                .post(assetPath(project, ""))
                .then()
                .statusCode(201)
                .extract()
                .path("id");
        String firstRevisionId = listRevisions(project, id).path("content[0].id");
        given().multiPart(
                        "file",
                        "gem.bbmodel",
                        AssetFixtures.utf8(AssetFixtures.bbmodelWithTextures("Gem Other.png")),
                        "application/json")
                .when()
                .post(assetPath(project, "/" + id + "/content"))
                .then()
                .statusCode(200);

        given().header("If-Match", "\"" + currentEtag(project, id) + "\"")
                .when()
                .post(assetPath(project, "/" + id + "/revision/" + firstRevisionId + "/restore"))
                .then()
                .statusCode(200);

        List<String> derivedKeys = given().queryParam("kind", AssetKind.TEXTURE.name())
                .queryParam("size", 500)
                .when()
                .get(assetPath(project, "/"))
                .then()
                .statusCode(200)
                .extract()
                .path("content.findAll { it.sourceAssetId == '" + id + "' }.key");
        assertEquals(List.of("vulpes:" + folder + "/ruby_side"), derivedKeys);
    }

    /**
     * Stores an asset the way it exists before revisions: a row without revisions and the bytes under the
     * legacy key of the asset.
     */
    private String createLegacyTexture(UUID project, byte[] content) {
        String id = createLegacyTextureRow(project, content);
        storage.put(AssetStorage.keyFor(project, UUID.fromString(id)), content, "image/png");
        return id;
    }

    /**
     * Stores the asset row only, without any bytes in object storage.
     */
    private String createLegacyTextureRow(UUID project, byte[] content) {
        ProjectAssetEntity asset = new ProjectAssetEntity();
        asset.setProject(projectRepository.findById(project).orElseThrow());
        asset.setKey("vulpes:item/legacy-" + shortId());
        asset.setDisplayName("Legacy");
        asset.setKind(AssetKind.TEXTURE);
        asset.setKeySpace(AssetKeySpace.TEXTURE);
        asset.setFileName("ruby.png");
        asset.setContentType("image/png");
        asset.setSize(content.length);
        asset.setSha1(Sha1Hex.of(content));
        asset.setWidth(16);
        asset.setHeight(16);
        return assetRepository.save(asset).getId().toString();
    }
}
