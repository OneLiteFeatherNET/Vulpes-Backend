package net.onelitefeather.vulpes.backend.controller;

import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import net.datafaker.Faker;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelDTO;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelResponseDTO;
import net.onelitefeather.vulpes.backend.domain.sound.SoundEventDTO;
import net.onelitefeather.vulpes.backend.domain.sound.SoundFileSourceDTO;
import net.onelitefeather.vulpes.backend.domain.sound.SoundResponseDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Integration tests for SoundController endpoints with Testcontainers")
@EnabledIfDockerAvailable
class SoundControllerIntegrationTest {

    @Inject
    EmbeddedServer server;

    private static final Faker FAKER = new Faker();

    /**
     * {@link SoundController} is project-scoped ({@code /project/{projectId}/sound}); every request in
     * this class needs a real, persisted project to route through. Created once for the whole class —
     * the tests below don't need isolation from each other, only from other projects.
     */
    private UUID projectId;

    @BeforeAll
    void createProject() {
        RestAssured.baseURI = server.getURL().toString();
        ProjectModelDTO project = new ProjectModelDTO(
                null, FAKER.company().name(), "sound-it-" + UUID.randomUUID(), null, null, null, false);
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

    private String soundPath(String suffix) {
        return "/project/" + projectId + "/sound" + suffix;
    }

    private static SoundEventDTO sampleEventDTO(UUID id) {
        String uiName = FAKER.rockBand().name();
        String varName = FAKER.internet().slug();
        String key = "key." + FAKER.lorem().word();
        String subtitle = FAKER.book().title();
        return new SoundEventDTO(id, uiName, varName, key, subtitle);
    }

    private static SoundEventDTO sampleEventDTOWithoutId() {
        String uiName = FAKER.rockBand().name();
        String varName = FAKER.internet().slug();
        String key = "key." + FAKER.lorem().word();
        String subtitle = FAKER.book().title();
        return new SoundEventDTO(null, uiName, varName, key, subtitle);
    }

    /**
     * An update DTO's {@code key}/{@code keyName}/{@code subTitle} are {@code @Null} for the Update
     * validation group (only {@code id} and {@code uiName} are settable on update) — unlike
     * {@link #sampleEventDTO}, which builds a Create-shaped DTO that would fail validation here.
     */
    private static SoundEventDTO sampleUpdateDTO(UUID id) {
        return new SoundEventDTO(id, FAKER.rockBand().name(), null, null, null);
    }

    @Test
    void testPostSound_returnsOk() {
        SoundEventDTO dto = sampleEventDTOWithoutId();

        var response =
                given()
                        .contentType(ContentType.JSON)
                        .body(dto)
                .when()
                        .post(soundPath(""))
                .then()
                        .extract().response();
        if (response.statusCode() != 200) {
            System.out.println("[DEBUG_LOG] Response status: " + response.statusCode());
            System.out.println("[DEBUG_LOG] Response body: " + response.asString());
        }
        assertEquals(200, response.statusCode());
        SoundResponseDTO.SoundModelDTO resp = response.as(SoundResponseDTO.SoundModelDTO.class);
        assertNotNull(resp);
        assertNotNull(resp.id());
    }

    @Test
    void testGetSoundById_found() {
        // create first
        SoundResponseDTO.SoundModelDTO created = given().contentType(ContentType.JSON)
                .body(sampleEventDTOWithoutId())
                .when().post(soundPath("")).then().statusCode(200)
                .extract().as(SoundResponseDTO.SoundModelDTO.class);

        SoundResponseDTO.SoundModelDTO resp =
                given()
                .when()
                        .get(soundPath("/" + created.id()))
                .then()
                        .statusCode(200)
                        .extract().as(SoundResponseDTO.SoundModelDTO.class);
        assertEquals(created.id(), resp.id());
    }

    @Test
    void testGetSoundById_notFound() {
        given()
        .when()
                .get(soundPath("/" + UUID.randomUUID()))
        .then()
                .statusCode(404);
    }

    @Test
    void testDeleteSoundById_found() {
        SoundResponseDTO.SoundModelDTO created = given().contentType(ContentType.JSON)
                .body(sampleEventDTOWithoutId())
                .when().post(soundPath("")).then().statusCode(200)
                .extract().as(SoundResponseDTO.SoundModelDTO.class);

        given()
        .when()
                .delete(soundPath("/delete/" + created.id()))
        .then()
                .statusCode(200);
    }

    @Test
    void testDeleteSoundById_notFound() {
        given()
        .when()
                .delete(soundPath("/delete/" + UUID.randomUUID()))
        .then()
                .statusCode(404);
    }

    @Test
    void testDeleteAll_returnsOk() {
        given()
        .when()
                .delete(soundPath("/delete/"))
        .then()
                .statusCode(204);
    }

    @Test
    void testGetAll_returnsOk() {
        given().contentType(ContentType.JSON).body(sampleEventDTOWithoutId()).when().post(soundPath("")).then().statusCode(200);

        given()
        .when()
                .get(soundPath("/"))
        .then()
                .statusCode(200)
                .body("totalSize", greaterThan(0))
                .body("content.size()", greaterThan(0))
                .body("content.id.size()", greaterThan(0))
                .body("content.uiName.size()", greaterThan(0))
                .body("content.key.size()", greaterThan(0))
                .body("content.keyName.size()", greaterThan(0))
                .body("content.subTitle.size()", greaterThan(0))
                .body("pageable.size", greaterThan(0))
                .body("pageable.number", greaterThan(-1))
                .body("pageable.mode", is("OFFSET"));
    }

    @Test
    void testPostUpdate_notFound_returns404() {
        SoundEventDTO dto = sampleUpdateDTO(UUID.randomUUID());
        given()
                .contentType(ContentType.JSON)
                .body(dto)
        .when()
                .post(soundPath("/update"))
        .then()
                .statusCode(404);
    }

    @Disabled("Source E2E disabled: mapping/cascade behavior depends on external API model; covered by unit tests")
    @Test
    void testCreateSource_and_GetSources_returnsOk() {
        // create parent first
        SoundResponseDTO.SoundModelDTO createdSound = given().contentType(ContentType.JSON)
                .body(sampleEventDTOWithoutId())
                .when().post(soundPath("")).then().statusCode(200)
                .extract().as(SoundResponseDTO.SoundModelDTO.class);
        UUID soundId = createdSound.id();

        String fileName = FAKER.internet().slug() + ".ogg";
        SoundFileSourceDTO requestDTO = new SoundFileSourceDTO(null, fileName, 1.0f, 1.0f, 1, false, 16, false, "file");
        SoundResponseDTO.SoundFileSourceDTO created =
                given()
                        .contentType(ContentType.JSON)
                        .body(requestDTO)
                .when()
                        .post(soundPath("/" + soundId + "/sources"))
                .then()
                        .statusCode(200)
                        .extract().as(SoundResponseDTO.SoundFileSourceDTO.class);
        assertNotNull(created);
        assertNotNull(created.id());
        assertEquals(fileName, created.name());

        // fetch page
        given()
        .when()
                .get(soundPath("/" + soundId + "/sources?page=0&size=10"))
        .then()
                .statusCode(200);
    }

    @Disabled("Source E2E disabled: mapping/cascade behavior depends on external API model; covered by unit tests")
    @Test
    void testUpdateSource_returnsOk() {
        SoundResponseDTO.SoundModelDTO createdSound = given().contentType(ContentType.JSON)
                .body(sampleEventDTOWithoutId())
                .when().post(soundPath("")).then().statusCode(200)
                .extract().as(SoundResponseDTO.SoundModelDTO.class);
        UUID soundId = createdSound.id();
        // create first
        SoundResponseDTO.SoundFileSourceDTO created =
                given().contentType(ContentType.JSON)
                        .body(new SoundFileSourceDTO(null, "file2.ogg", 0.8f, 1.1f, 2, true, 32, true, "file"))
                        .when().post(soundPath("/" + soundId + "/sources")).then().statusCode(200)
                        .extract().as(SoundResponseDTO.SoundFileSourceDTO.class);
        // now update
        SoundFileSourceDTO update = new SoundFileSourceDTO(created.id(), "file2.ogg", 0.9f, 1.0f, 3, true, 32, true, "file");
        given()
                .contentType(ContentType.JSON)
                .body(update)
        .when()
                .post(soundPath("/" + soundId + "/sources/update"))
        .then()
                .statusCode(200);
    }

    @Disabled("DELETE with body is not widely supported in some client stacks; covered by unit tests")
    @Test
    void testDeleteSource_returnsOk() {
        // This remains disabled; DELETE with body can be flaky across stacks
    }
}
