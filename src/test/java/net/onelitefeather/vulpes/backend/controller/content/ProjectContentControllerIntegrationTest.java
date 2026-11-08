package net.onelitefeather.vulpes.backend.controller.content;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.micronaut.json.JsonMapper;
import io.micronaut.json.tree.JsonNode;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.onelitefeather.vulpes.backend.domain.asset.AssetFixtures;
import net.onelitefeather.vulpes.backend.domain.content.ProjectContentDTO;
import net.onelitefeather.vulpes.backend.domain.pack.PackMetaDTO;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelDTO;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelResponseDTO;
import net.onelitefeather.vulpes.backend.service.content.ContentRevision;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

/**
 * Integration tests for {@code GET /project/{projectId}/content}. Every test creates its own project with
 * unique keys, so no test depends on the content or the order of another test.
 */
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Integration tests for ProjectContentController endpoints with Testcontainers")
@EnabledIfDockerAvailable
class ProjectContentControllerIntegrationTest implements TestPropertyProvider {

    private static final String CONTENT_PATH = "/project/%s/content";
    private static final String ETAG = "ETag";

    @Inject
    EmbeddedServer server;

    @Inject
    JsonMapper jsonMapper;

    /**
     * Object storage writes into a fresh temp directory per test context, never into the working tree.
     */
    @Override
    public Map<String, String> getProperties() {
        try {
            return Map.of(
                    "micronaut.object-storage.local.assets.path",
                    Files.createTempDirectory("vulpes-content-assets-").toString());
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    @BeforeEach
    void setup() {
        RestAssured.baseURI = server.getURL().toString();
    }

    private static String uniqueSuffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private static String uniqueKey(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }

    private UUID createProject() {
        ProjectModelDTO project = new ProjectModelDTO(
                null, "Content export", "content-it-" + UUID.randomUUID(), null, null, null, false);
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

    private UUID createItem(UUID projectId, String key) {
        String body = "{\"uiName\":\"Gem\",\"key\":\"" + key + "\",\"groupName\":\"gems\"}";
        return UUID.fromString(given().contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/project/" + projectId + "/item")
                .then()
                .statusCode(200)
                .extract()
                .path("id"));
    }

    private void addLore(UUID itemId, String text) {
        given().contentType(ContentType.JSON)
                .body("{\"text\":\"" + text + "\"}")
                .when()
                .put("/item/" + itemId + "/lore")
                .then()
                .statusCode(200);
    }

    private ProjectContentDTO export(UUID projectId) {
        return parseContent(exportResponse(projectId).asString());
    }

    private ProjectContentDTO parseContent(String body) {
        try {
            return jsonMapper.readValue(body, ProjectContentDTO.class);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private Response exportResponse(UUID projectId) {
        return given()
        .when()
                .get(CONTENT_PATH.formatted(projectId))
        .then()
                .statusCode(200)
                .extract()
                .response();
    }

    private JsonNode parse(String body) throws IOException {
        return jsonMapper.readValue(body, JsonNode.class);
    }

    private void updateLoreText(UUID itemId, UUID loreId, String text) {
        given().contentType(ContentType.JSON)
                .body("{\"id\":\"" + loreId + "\",\"text\":\"" + text + "\"}")
                .when()
                .post("/item/" + itemId + "/lore")
                .then()
                .statusCode(200);
    }

    @Test
    void exportContainsEveryContentTypeWithItsChildren() {
        UUID projectId = createProject();
        UUID itemId = createItem(projectId, "gem-" + uniqueSuffix());
        given().contentType(ContentType.JSON)
                .body("{\"componentKey\":\"minecraft:food\",\"value\":{\"nutrition\":4,\"saturation\":2.4}}")
                .when()
                .put("/item/" + itemId + "/component")
                .then()
                .statusCode(200);
        addLore(itemId, "first line");
        addLore(itemId, "second line");
        given().contentType(ContentType.JSON)
                .body("{\"name\":\"minecraft:sharpness\",\"level\":5,\"unsafe\":false}")
                .when()
                .put("/item/" + itemId + "/enchantment")
                .then()
                .statusCode(200);
        given().contentType(ContentType.JSON)
                .body("{\"uiName\":\"Speed\",\"key\":\"" + uniqueKey("speed") + "\",\"defaultValue\":1.0,"
                        + "\"minimumValue\":0.0,\"maximumValue\":10.0,\"synced\":false,"
                        + "\"translationKey\":null,\"comment\":null}")
                .when()
                .post("/project/" + projectId + "/attribute")
                .then()
                .statusCode(200);
        UUID fontId = UUID.fromString(given().contentType(ContentType.JSON)
                .body("{\"uiName\":\"Font\",\"key\":\"" + uniqueKey("font") + "\",\"provider\":\"bitmap\","
                        + "\"mapper\":\"mapper\",\"texturePath\":\"texture.png\",\"comment\":null,"
                        + "\"ascent\":7,\"height\":8,\"textureAssetId\":null}")
                .when()
                .post("/project/" + projectId + "/font")
                .then()
                .statusCode(200)
                .extract()
                .path("id"));
        for (int index = 0; index < 2; index++) {
            given().contentType(ContentType.JSON)
                    .body("{\"line\":\"char" + index + "\",\"orderIndex\":" + index + "}")
                    .when()
                    .put("/font/chars/" + fontId)
                    .then()
                    .statusCode(200);
        }
        UUID soundId = UUID.fromString(given().contentType(ContentType.JSON)
                .body("{\"uiName\":\"Step\",\"key\":\"" + uniqueKey("sound")
                        + "\",\"keyName\":\"block.stone.step\",\"subTitle\":null,\"comment\":null}")
                .when()
                .post("/project/" + projectId + "/sound")
                .then()
                .statusCode(200)
                .extract()
                .path("id"));
        given().contentType(ContentType.JSON)
                .body("{\"name\":\"step1\",\"volume\":1.0,\"pitch\":1.0,\"weight\":1,\"stream\":false,"
                        + "\"attenuationDistance\":16,\"preload\":false,\"type\":\"file\"}")
                .when()
                .post("/sound/" + soundId + "/sources")
                .then()
                .statusCode(200);
        given().contentType(ContentType.JSON)
                .body("{\"uiName\":\"Double\",\"name\":\"double_it\",\"kind\":\"FORMULA\",\"returnType\":\"NUMBER\","
                        + "\"parameters\":[{\"name\":\"x\",\"type\":\"NUMBER\",\"testValue\":\"1\"}],"
                        + "\"body\":\"x * 2\",\"comment\":null}")
                .when()
                .post("/project/" + projectId + "/expression-function")
                .then()
                .statusCode(200);
        given().contentType(ContentType.JSON)
                .body("{\"uiName\":\"Expression\",\"expression\":\"double_it(x) + 1\",\"comment\":null,"
                        + "\"variables\":[{\"name\":\"x\",\"type\":\"NUMBER\",\"testValue\":\"3\"}]}")
                .when()
                .post("/project/" + projectId + "/expression")
                .then()
                .statusCode(200);
        given().multiPart("file", "gem.png", AssetFixtures.png(16, 16), "image/png")
                .multiPart("key", "vulpes:export/" + uniqueSuffix())
                .multiPart("displayName", "Gem")
                .when()
                .post("/project/" + projectId + "/asset")
                .then()
                .statusCode(201);
        given().contentType(ContentType.JSON)
                .body("{\"uiName\":\"Advancement\",\"key\":\"" + uniqueKey("adv") + "\",\"comment\":null,"
                        + "\"material\":\"STONE\",\"frameType\":\"TASK\",\"title\":\"{\\\"text\\\":\\\"Title\\\"}\","
                        + "\"description\":\"{\\\"text\\\":\\\"Desc\\\"}\",\"background\":null,\"parentId\":null,"
                        + "\"x\":0,\"y\":0,\"showToast\":true,\"announceToChat\":true,\"hidden\":false}")
                .when()
                .post("/project/" + projectId + "/advancement")
                .then()
                .statusCode(200);
        UUID dimensionId = UUID.fromString(given().contentType(ContentType.JSON)
                .body("{\"uiName\":\"Overworld\",\"key\":\"" + uniqueKey("dimension") + "\","
                        + "\"hasFixedTime\":false,\"hasSkylight\":true,\"hasCeiling\":false,"
                        + "\"hasEnderDragonFight\":false,\"coordinateScale\":1.0,\"minY\":-64,\"height\":384,"
                        + "\"logicalHeight\":384,\"infiniburn\":\"#minecraft:infiniburn_overworld\","
                        + "\"ambientLight\":0.0,\"monsterSpawnLightLevel\":\"constant:11\","
                        + "\"monsterSpawnBlockLightLimit\":15,\"skybox\":\"OVERWORLD\",\"cardinalLight\":\"DEFAULT\","
                        + "\"defaultClock\":null}")
                .when()
                .post("/project/" + projectId + "/dimension")
                .then()
                .statusCode(200)
                .extract()
                .path("id"));
        given().contentType(ContentType.JSON)
                .body("{\"attributeKey\":\"FOG_COLOR\",\"operator\":\"OVERRIDE\",\"attributeValue\":\"#FFAA00\"}")
                .when()
                .put("/dimension/" + dimensionId + "/attribute")
                .then()
                .statusCode(200);
        given().contentType(ContentType.JSON)
                .body("{\"timelineKey\":\"minestom:day_night\"}")
                .when()
                .put("/dimension/" + dimensionId + "/timeline")
                .then()
                .statusCode(200);
        given().contentType(ContentType.JSON)
                .body(new PackMetaDTO("{\"text\":\"My pack\"}", 48, 0, 57, null,
                        Set.of("minecraft:trade_rebalance"), null))
                .when()
                .put("/project/" + projectId + "/pack-meta/DATA")
                .then()
                .statusCode(200);

        ProjectContentDTO content = export(projectId);

        assertEquals(1, content.items().size(), "the project has one item");
        ProjectContentDTO.ItemDTO item = content.items().get(0);
        ProjectContentDTO.ComponentDTO food = item.components().stream()
                .filter(component -> component.componentKey().equals("minecraft:food"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("the component minecraft:food must be exported"));
        assertTrue(food.value().isObject(), "the component value is a JSON object");
        assertEquals(4, food.value().get("nutrition").getNumberValue().intValue(), "the component value is kept");
        assertEquals(List.of("first line", "second line"),
                item.lore().stream().map(ProjectContentDTO.LoreDTO::text).toList(),
                "the lore lines are exported in their position order");
        assertEquals(1, item.enchantments().size(), "the item has one enchantment");
        assertEquals((short) 5, item.enchantments().get(0).level(), "the enchantment level is exported");

        assertEquals(1, content.attributes().size(), "the project has one attribute");
        assertEquals(1, content.fonts().size(), "the project has one font");
        assertEquals(2, content.fonts().get(0).chars().size(), "the font carries both char lines");
        assertEquals(1, content.sounds().size(), "the project has one sound");
        assertEquals(1, content.sounds().get(0).sources().size(), "the sound carries its source");
        assertFalse(content.sounds().get(0).replace(), "the replace flag of the sound is exported");
        assertEquals(1, content.expressionFunctions().size(), "the project has one expression function");
        assertEquals(1, content.expressionFunctions().get(0).parameters().size(),
                "the function carries its parameter");
        assertEquals(1, content.expressions().size(), "the project has one expression");
        assertEquals(1, content.expressions().get(0).variables().size(), "the expression carries its variable");
        assertEquals(1, content.assets().size(), "the project has one asset");
        assertNotNull(content.assets().get(0).revisions(), "the asset carries a revisions list");
        assertNotNull(content.assets().get(0).links(), "the asset carries a links list");
        assertEquals(1, content.advancements().size(), "the project has one advancement");
        assertEquals(1, content.dimensions().size(), "the project has one dimension");
        assertEquals(1, content.dimensions().get(0).attributes().size(), "the dimension carries its attribute");
        assertEquals(1, content.dimensions().get(0).timelines().size(), "the dimension carries its timeline");
        assertEquals(1, content.packs().size(), "the project has one pack meta");
        assertEquals(Set.of("minecraft:trade_rebalance"), Set.copyOf(content.packs().get(0).enabledFeatures()),
                "the enabled features of the pack are exported");
    }

    @Test
    void exportOmitsContentOfOtherProjects() {
        UUID ownProject = createProject();
        UUID otherProject = createProject();
        UUID ownItem = createItem(ownProject, "own-" + uniqueSuffix());
        UUID otherItem = createItem(otherProject, "other-" + uniqueSuffix());

        String body = exportResponse(ownProject).asString();

        assertEquals(List.of(ownItem), export(ownProject).items().stream().map(ProjectContentDTO.ItemDTO::id).toList(),
                "only the item of the exported project is listed");
        assertFalse(body.contains(otherItem.toString()), "no element of the other project may appear in the body");
    }

    @Test
    void emptyProjectHasEveryListEmpty() {
        UUID projectId = createProject();
        String body = exportResponse(projectId).asString();
        ProjectContentDTO content = parseContent(body);

        assertTrue(body.contains("\"items\":[]"), "an empty list must be written as [] in the body");
        assertTrue(body.contains("\"packs\":[]"), "an empty list must be written as [] in the body");
        assertTrue(body.contains("\"docuUrl\":null"), "a null field must be written as null in the body");
        assertNotNull(content.project(), "the project is exported even without content");
        assertTrue(content.items().isEmpty(), "items must be an empty list");
        assertTrue(content.attributes().isEmpty(), "attributes must be an empty list");
        assertTrue(content.fonts().isEmpty(), "fonts must be an empty list");
        assertTrue(content.sounds().isEmpty(), "sounds must be an empty list");
        assertTrue(content.expressionFunctions().isEmpty(), "expression functions must be an empty list");
        assertTrue(content.expressions().isEmpty(), "expressions must be an empty list");
        assertTrue(content.assets().isEmpty(), "assets must be an empty list");
        assertTrue(content.advancements().isEmpty(), "advancements must be an empty list");
        assertTrue(content.dimensions().isEmpty(), "dimensions must be an empty list");
        assertTrue(content.packs().isEmpty(), "packs must be an empty list");
    }

    @Test
    void unknownProjectReturnsNotFound() {
        given()
        .when()
                .get(CONTENT_PATH.formatted(UUID.randomUUID()))
        .then()
                .statusCode(404)
                .body("code", is("PROJECT_NOT_FOUND"));
    }

    @Test
    void itemsAreListedByKeyInsteadOfInsertionOrder() {
        UUID projectId = createProject();
        createItem(projectId, "b");
        createItem(projectId, "a");
        createItem(projectId, "c");

        List<String> keys = export(projectId).items().stream().map(ProjectContentDTO.ItemDTO::key).toList();

        assertEquals(List.of("a", "b", "c"), keys, "items are ordered by key");
    }

    @Test
    void repeatedExportOfUnchangedContentIsByteIdentical() {
        UUID projectId = createProject();
        createItem(projectId, "gem-" + uniqueSuffix());

        String first = exportResponse(projectId).asString();
        String second = exportResponse(projectId).asString();

        assertEquals(first, second, "two exports of unchanged content must be byte-identical");
    }

    @Test
    void etagHeaderEqualsRevisionInQuotes() {
        UUID projectId = createProject();

        Response response = exportResponse(projectId);
        String revision = response.path("revision");

        assertEquals("\"" + revision + "\"", response.header(ETAG), "the ETag is the revision in double quotes");
    }

    @Test
    void revisionEqualsCanonicalHashOfBodyWithoutRevision() throws IOException {
        UUID projectId = createProject();
        createItem(projectId, "gem-" + uniqueSuffix());

        Response response = exportResponse(projectId);
        String revision = response.path("revision");

        assertEquals(ContentRevision.of(parse(response.asString())), revision,
                "the revision must be the canonical hash of the body without the revision field");
    }

    @Test
    void loreChangeChangesRevision() {
        UUID projectId = createProject();
        UUID itemId = createItem(projectId, "gem-" + uniqueSuffix());
        addLore(itemId, "before");
        ProjectContentDTO before = export(projectId);
        UUID loreId = before.items().get(0).lore().get(0).id();

        updateLoreText(itemId, loreId, "after");
        ProjectContentDTO after = export(projectId);

        assertFalse(before.revision().equals(after.revision()), "a changed lore line must change the revision");
    }

    @Test
    void matchingIfNoneMatchAnswersNotModifiedWithoutBody() {
        UUID projectId = createProject();
        createItem(projectId, "gem-" + uniqueSuffix());
        String etag = exportResponse(projectId).header(ETAG);

        Response response = given().header("If-None-Match", etag)
        .when()
                .get(CONTENT_PATH.formatted(projectId))
        .then()
                .statusCode(304)
                .extract()
                .response();

        assertEquals("", response.asString(), "a 304 answer must not carry a body");
        assertEquals(etag, response.header(ETAG), "a 304 answer must carry the current ETag");
    }

    @Test
    void staleIfNoneMatchAnswersWithNewBodyAndEtag() {
        UUID projectId = createProject();
        UUID itemId = createItem(projectId, "gem-" + uniqueSuffix());
        addLore(itemId, "before");
        ProjectContentDTO before = export(projectId);
        String staleEtag = "\"" + before.revision() + "\"";
        updateLoreText(itemId, before.items().get(0).lore().get(0).id(), "after");

        Response response = given().header("If-None-Match", staleEtag)
        .when()
                .get(CONTENT_PATH.formatted(projectId))
        .then()
                .statusCode(200)
                .extract()
                .response();

        assertFalse(staleEtag.equals(response.header(ETAG)), "the answer must carry the new ETag");
        assertEquals(response.header(ETAG), "\"" + response.path("revision") + "\"",
                "the new ETag must equal the revision of the new body");
    }

    @Test
    void exportBodyHasNoAuditTimestamps() {
        UUID projectId = createProject();
        UUID itemId = createItem(projectId, "gem-" + uniqueSuffix());
        addLore(itemId, "line");

        String body = exportResponse(projectId).asString();

        assertFalse(body.contains("creationDate"), "the body must not carry creationDate");
        assertFalse(body.contains("modificationDate"), "the body must not carry modificationDate");
    }
}
