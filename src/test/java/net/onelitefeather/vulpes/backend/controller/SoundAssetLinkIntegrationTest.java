package net.onelitefeather.vulpes.backend.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import jakarta.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.onelitefeather.vulpes.backend.domain.asset.AssetFixtures;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Integration tests for links between sound sources and sound assets with Testcontainers")
@EnabledIfDockerAvailable
class SoundAssetLinkIntegrationTest implements TestPropertyProvider {

    private static final String WIND = "vulpes:ambient/wind";

    @Inject
    EmbeddedServer server;

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

    private static UUID newProject() {
        String body = "{\"displayName\":\"Sound links\",\"key\":\"sound-link-" + shortId() + "\",\"labor\":false}";
        String id = given().contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/project")
                .then()
                .statusCode(200)
                .extract()
                .path("id");
        return UUID.fromString(id);
    }

    private static UUID newSoundEvent(UUID project) {
        String body = "{\"uiName\":\"Wind\",\"key\":\"wind-" + shortId() + "\",\"keyName\":\"ambient.wind\"}";
        String id = given().contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/project/" + project + "/sound")
                .then()
                .statusCode(200)
                .extract()
                .path("id");
        return UUID.fromString(id);
    }

    private static UUID upload(UUID project, String fileName, String contentType, byte[] content, String key) {
        String id = given().multiPart("file", fileName, content, contentType)
                .multiPart("key", key)
                .multiPart("displayName", "Wind")
                .when()
                .post("/project/" + project + "/asset")
                .then()
                .statusCode(201)
                .extract()
                .path("id");
        return UUID.fromString(id);
    }

    private static UUID uploadSound(UUID project, String key) {
        return upload(project, "wind.ogg", "audio/ogg", AssetFixtures.oggVorbis(1, 44_100, 44_100), key);
    }

    private static Map<String, Object> source(
            @Nullable UUID id, String name, String type, @Nullable UUID assetId) {
        Map<String, Object> body = new HashMap<>();
        body.put("id", id);
        body.put("name", name);
        body.put("volume", 1.0f);
        body.put("pitch", 1.0f);
        body.put("weight", 1);
        body.put("stream", false);
        body.put("attenuationDistance", 16);
        body.put("preload", false);
        body.put("type", type);
        body.put("assetId", assetId);
        return body;
    }

    private static ValidatableResponse createSource(UUID soundEvent, Map<String, Object> body) {
        return given().contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/sound/" + soundEvent + "/sources")
                .then();
    }

    private static UUID createLinkedSource(UUID soundEvent, UUID asset) {
        String id = createSource(soundEvent, source(null, WIND, "file", asset))
                .statusCode(200)
                .extract()
                .path("id");
        return UUID.fromString(id);
    }

    private static ValidatableResponse deleteAsset(UUID project, UUID asset) {
        return given().when().delete("/project/" + project + "/asset/delete/" + asset).then();
    }

    private static ValidatableResponse sources(UUID soundEvent) {
        return given().when().get("/sound/" + soundEvent + "/sources").then().statusCode(200);
    }

    private static String shortId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    @Test
    void sourceLinkedToSoundReturnsAssetId() {
        UUID project = newProject();
        UUID soundEvent = newSoundEvent(project);
        UUID sound = uploadSound(project, WIND);

        createSource(soundEvent, source(null, WIND, "file", sound))
                .statusCode(200)
                .body("assetId", is(sound.toString()));

        sources(soundEvent).body("content.assetId", hasItem(sound.toString()));
    }

    @Test
    void nameOtherThanAssetKeyReturns400() {
        UUID project = newProject();
        UUID soundEvent = newSoundEvent(project);
        UUID sound = uploadSound(project, WIND);

        createSource(soundEvent, source(null, "vulpes:ambient/rain", "file", sound))
                .statusCode(400)
                .body("errors.field", hasItem("assetId"))
                .body("errors.code", hasItem("SOUND_SOURCE_ASSET_MISMATCH"));

        sources(soundEvent).body("content", empty());
    }

    @Test
    void eventTypeWithAssetReturns400() {
        UUID project = newProject();
        UUID soundEvent = newSoundEvent(project);
        UUID sound = uploadSound(project, WIND);

        createSource(soundEvent, source(null, WIND, "event", sound))
                .statusCode(400)
                .body("errors.code", hasItem("SOUND_SOURCE_ASSET_MISMATCH"));
    }

    @Test
    void textureAssetReturns400() {
        UUID project = newProject();
        UUID soundEvent = newSoundEvent(project);
        UUID texture = upload(project, "wind.png", "image/png", AssetFixtures.png(16, 16), WIND);

        createSource(soundEvent, source(null, WIND, "file", texture))
                .statusCode(400)
                .body("errors.field", hasItem("assetId"))
                .body("errors.code", hasItem("SOUND_SOURCE_ASSET_INVALID"));
    }

    @Test
    void soundOfAnotherProjectReturns404() {
        UUID project = newProject();
        UUID soundEvent = newSoundEvent(project);
        UUID foreignSound = uploadSound(newProject(), WIND);

        createSource(soundEvent, source(null, WIND, "file", foreignSound)).statusCode(404);

        sources(soundEvent).body("content", empty());
    }

    @Test
    void unknownAssetReturns404() {
        UUID soundEvent = newSoundEvent(newProject());

        createSource(soundEvent, source(null, WIND, "file", UUID.randomUUID())).statusCode(404);
    }

    @Test
    void vanillaSourceWithoutAssetHasNoLink() {
        UUID soundEvent = newSoundEvent(newProject());

        createSource(soundEvent, source(null, "minecraft:ambient/cave/cave1", "file", null))
                .statusCode(200)
                .body("assetId", nullValue());
    }

    @Test
    void updateCanMoveTheLinkToAnotherSound() {
        UUID project = newProject();
        UUID soundEvent = newSoundEvent(project);
        UUID wind = uploadSound(project, WIND);
        UUID rain = uploadSound(project, "vulpes:ambient/rain");
        UUID source = createLinkedSource(soundEvent, wind);

        given().contentType(ContentType.JSON)
                .body(source(source, "vulpes:ambient/rain", "file", rain))
                .when()
                .post("/sound/" + soundEvent + "/sources/update")
                .then()
                .statusCode(200)
                .body("assetId", is(rain.toString()));

        deleteAsset(project, wind).statusCode(200);
        deleteAsset(project, rain).statusCode(409);
    }

    @Test
    void updateWithoutAssetIdRemovesTheLink() {
        UUID project = newProject();
        UUID soundEvent = newSoundEvent(project);
        UUID sound = uploadSound(project, WIND);
        UUID source = createLinkedSource(soundEvent, sound);

        given().contentType(ContentType.JSON)
                .body(source(source, WIND, "file", null))
                .when()
                .post("/sound/" + soundEvent + "/sources/update")
                .then()
                .statusCode(200)
                .body("assetId", nullValue());

        deleteAsset(project, sound).statusCode(200);
    }

    @Test
    void deletingLinkedSoundReturns409AndKeepsIt() {
        UUID project = newProject();
        UUID soundEvent = newSoundEvent(project);
        UUID sound = uploadSound(project, WIND);
        createLinkedSource(soundEvent, sound);

        deleteAsset(project, sound)
                .statusCode(409)
                .contentType("application/problem+json")
                .body("type", is("https://vulpes.onelitefeather.net/errors/asset-in-use"));

        given().when()
                .get("/project/" + project + "/asset/" + sound + "/content")
                .then()
                .statusCode(200);
        sources(soundEvent).body("content.assetId", hasItem(sound.toString()));
    }

    @Test
    void deletingTheSourceReleasesTheSound() {
        UUID project = newProject();
        UUID soundEvent = newSoundEvent(project);
        UUID sound = uploadSound(project, WIND);
        UUID source = createLinkedSource(soundEvent, sound);

        given().when()
                .delete("/sound/" + soundEvent + "/sources/delete/" + source)
                .then()
                .statusCode(200);

        deleteAsset(project, sound).statusCode(200);
    }

    @Test
    void deletingTheSoundEventReleasesTheSound() {
        UUID project = newProject();
        UUID soundEvent = newSoundEvent(project);
        UUID sound = uploadSound(project, WIND);
        createLinkedSource(soundEvent, sound);

        given().when()
                .delete("/project/" + project + "/sound/delete/" + soundEvent)
                .then()
                .statusCode(200);

        deleteAsset(project, sound).statusCode(200);
    }

    @Test
    void deletingAllSoundEventsReleasesTheSound() {
        UUID project = newProject();
        UUID soundEvent = newSoundEvent(project);
        UUID sound = uploadSound(project, WIND);
        createLinkedSource(soundEvent, sound);

        given().when().delete("/project/" + project + "/sound/delete/").then().statusCode(204);

        deleteAsset(project, sound).statusCode(200);
    }

    @Test
    void deletingAllAssetsRemovesLinkedSounds() {
        UUID project = newProject();
        UUID soundEvent = newSoundEvent(project);
        UUID sound = uploadSound(project, WIND);
        createLinkedSource(soundEvent, sound);

        given().when().delete("/project/" + project + "/asset/delete/").then().statusCode(204);

        sources(soundEvent).body("content", hasSize(1)).body("content[0].assetId", nullValue());
    }

    @Test
    void copyWithinTheProjectKeepsTheLink() {
        UUID project = newProject();
        UUID soundEvent = newSoundEvent(project);
        UUID sound = uploadSound(project, WIND);
        createLinkedSource(soundEvent, sound);

        String copy = given().contentType(ContentType.JSON)
                .body("{\"targetKey\":\"wind-copy-" + shortId() + "\",\"relations\":[\"SOURCES\"]}")
                .when()
                .post("/project/" + project + "/sound/" + soundEvent + "/copy")
                .then()
                .statusCode(200)
                .extract()
                .path("id");

        sources(UUID.fromString(copy)).body("content.assetId", hasItem(sound.toString()));
    }

    @Test
    void copyIntoAnotherProjectDropsTheLink() {
        UUID project = newProject();
        UUID target = newProject();
        UUID soundEvent = newSoundEvent(project);
        UUID sound = uploadSound(project, WIND);
        createLinkedSource(soundEvent, sound);

        String copy = given().contentType(ContentType.JSON)
                .body("{\"targetProjectId\":\"" + target + "\",\"relations\":[\"SOURCES\"]}")
                .when()
                .post("/project/" + project + "/sound/" + soundEvent + "/copy")
                .then()
                .statusCode(200)
                .extract()
                .path("id");

        sources(UUID.fromString(copy))
                .body("content", hasSize(1))
                .body("content[0].name", is(WIND))
                .body("content[0].assetId", nullValue());
    }

    @Test
    void exportListsTheLinkWithAssetAndSource() {
        UUID project = newProject();
        UUID soundEvent = newSoundEvent(project);
        UUID sound = uploadSound(project, WIND);
        UUID source = createLinkedSource(soundEvent, sound);

        given().when()
                .get("/project/" + project + "/content")
                .then()
                .statusCode(200)
                .body("sounds[0].sources[0].assetId", is(sound.toString()))
                .body("assets.find { it.id == '" + sound + "' }.links[0].targetType", is("SOUND_SOURCE"))
                .body("assets.find { it.id == '" + sound + "' }.links[0].targetId", is(source.toString()))
                .body("assets.find { it.id == '" + sound + "' }.links[0].role", is("SOUND"));
    }
}
