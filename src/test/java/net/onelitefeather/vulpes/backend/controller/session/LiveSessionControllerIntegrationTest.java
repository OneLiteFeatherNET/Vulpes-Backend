package net.onelitefeather.vulpes.backend.controller.session;

import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import net.datafaker.Faker;
import net.onelitefeather.vulpes.backend.domain.asset.AssetFixtures;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelDTO;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration tests for the live session of an asset. Every test creates its own project and asset, so no test
 * sees the session another test started.
 */
@MicronautTest(transactional = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Integration tests for live sessions with Testcontainers")
@EnabledIfDockerAvailable
class LiveSessionControllerIntegrationTest implements TestPropertyProvider {

    private static final Faker FAKER = new Faker();
    private static final String TOKEN = "abcdefghijklmnop";
    private static final String OTHER_TOKEN = "qrstuvwxyz012345";
    private static final String HOST = "designer-a";

    @Inject
    EmbeddedServer server;

    /**
     * Object storage writes into a fresh temp directory per test context, never into the working tree.
     */
    @Override
    public Map<String, String> getProperties() {
        try {
            return Map.of("micronaut.object-storage.local.assets.path",
                    Files.createTempDirectory("vulpes-live-sessions-").toString());
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    @BeforeEach
    void setup() {
        RestAssured.baseURI = server.getURL().toString();
    }

    /**
     * Creates a project with one asset and returns the path of the live session of that asset.
     */
    private String freshSessionPath() {
        ProjectModelDTO project = new ProjectModelDTO(
                null, FAKER.company().name(), "live-session-it-" + UUID.randomUUID(), null, null, null, false);
        UUID projectId = given()
                .contentType(ContentType.JSON)
                .body(project)
        .when()
                .post("/project")
        .then()
                .statusCode(200)
                .extract().as(ProjectModelResponseDTO.ProjectModelDTO.class).id();
        String assetId = given()
                .multiPart("file", "ruby.png", AssetFixtures.png(4, 4), "image/png")
                .multiPart("key", "vulpes:live/" + UUID.randomUUID().toString().substring(0, 8))
                .multiPart("displayName", "Live")
        .when()
                .post("/project/" + projectId + "/asset")
        .then()
                .statusCode(201)
                .extract().path("id");
        return "/project/" + projectId + "/asset/" + assetId + "/live-session";
    }

    private static String sessionBody(String token, String host) {
        return "{\"token\":\"%s\",\"host\":\"%s\"}".formatted(token, host);
    }

    private static Response putSession(String path, String token, String host) {
        return given()
                .contentType(ContentType.JSON)
                .body(sessionBody(token, host))
        .when()
                .put(path);
    }

    @Test
    @DisplayName("reading the live session of an asset without a session answers 404")
    void getWithoutSessionAnswersNotFound() {
        given()
        .when()
                .get(freshSessionPath())
        .then()
                .statusCode(404)
                .body("code", is("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("reading the live session of an unknown asset answers 404")
    void getForUnknownAssetAnswersNotFound() {
        given()
        .when()
                .get("/project/" + UUID.randomUUID() + "/asset/" + UUID.randomUUID() + "/live-session")
        .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("starting a session answers 200 with the token, host and the start and expiry instants")
    void putStartsSessionAndAnswersWithItsFields() {
        String path = freshSessionPath();

        putSession(path, TOKEN, HOST)
        .then()
                .statusCode(200)
                .body("token", is(TOKEN))
                .body("host", is(HOST))
                .body("startedAt", notNullValue())
                .body("expiresAt", notNullValue());
    }

    @Test
    @DisplayName("a session with the token of the active session is refreshed and keeps its start")
    void putWithSameTokenRefreshesSession() {
        String path = freshSessionPath();
        Instant startedAt = Instant.parse(putSession(path, TOKEN, HOST).then().statusCode(200)
                .extract().path("startedAt"));

        Instant refreshedStart = Instant.parse(putSession(path, TOKEN, HOST).then().statusCode(200)
                .extract().path("startedAt"));

        assertEquals(startedAt, refreshedStart, "a refresh must keep the instant the session started");
    }

    @Test
    @DisplayName("starting with another token while a session is active answers 409")
    void putWithOtherTokenWhileActiveAnswersConflict() {
        String path = freshSessionPath();
        putSession(path, TOKEN, HOST).then().statusCode(200);

        putSession(path, OTHER_TOKEN, "designer-b")
        .then()
                .statusCode(409)
                .body("code", is("RESOURCE_CONFLICT"));
    }

    @Test
    @DisplayName("a token that is not 16 characters of a to z and 0 to 9 answers 400 naming the token")
    void putWithMalformedTokenAnswersValidationFailed() {
        String path = freshSessionPath();

        putSession(path, "ABCDEFGHIJKLMNOP", HOST)
        .then()
                .statusCode(400)
                .body("code", is("VALIDATION_FAILED"))
                .body("errors.field", hasItem("token"));
    }

    @Test
    @DisplayName("an empty host answers 400 naming the host")
    void putWithEmptyHostAnswersValidationFailed() {
        String path = freshSessionPath();

        putSession(path, TOKEN, "")
        .then()
                .statusCode(400)
                .body("code", is("VALIDATION_FAILED"))
                .body("errors.field", hasItem("host"));
    }

    @Test
    @DisplayName("a host longer than 64 characters answers 400 naming the host")
    void putWithLongHostAnswersValidationFailed() {
        String path = freshSessionPath();

        putSession(path, TOKEN, "h".repeat(65))
        .then()
                .statusCode(400)
                .body("code", is("VALIDATION_FAILED"))
                .body("errors.field", hasItem("host"));
    }

    @Test
    @DisplayName("starting a session for an unknown asset answers 404")
    void putForUnknownAssetAnswersNotFound() {
        putSession("/project/" + UUID.randomUUID() + "/asset/" + UUID.randomUUID() + "/live-session", TOKEN, HOST)
        .then()
                .statusCode(404)
                .body("code", is("RESOURCE_NOT_FOUND"));
    }

    @Test
    @DisplayName("ending the session with its token answers 204 and the session is no longer readable")
    void deleteWithMatchingTokenEndsSession() {
        String path = freshSessionPath();
        putSession(path, TOKEN, HOST).then().statusCode(200);

        given().queryParam("token", TOKEN)
        .when()
                .delete(path)
        .then()
                .statusCode(204);
        given()
        .when()
                .get(path)
        .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("ending the session with another token while it is active answers 409")
    void deleteWithOtherTokenWhileActiveAnswersConflict() {
        String path = freshSessionPath();
        putSession(path, TOKEN, HOST).then().statusCode(200);

        given().queryParam("token", OTHER_TOKEN)
        .when()
                .delete(path)
        .then()
                .statusCode(409)
                .body("code", is("RESOURCE_CONFLICT"));
    }

    @Test
    @DisplayName("ending an asset without a session answers 204")
    void deleteWithoutSessionAnswersNoContent() {
        String path = freshSessionPath();

        given().queryParam("token", TOKEN)
        .when()
                .delete(path)
        .then()
                .statusCode(204);
    }
}
