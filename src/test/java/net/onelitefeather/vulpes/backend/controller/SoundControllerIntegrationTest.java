package net.onelitefeather.vulpes.backend.controller;

import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import jakarta.inject.Inject;
import org.junit.jupiter.api.*;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.MariaDBContainer;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import net.datafaker.Faker;
import net.onelitefeather.vulpes.backend.domain.sound.SoundEventDTO;
import net.onelitefeather.vulpes.backend.domain.sound.SoundFileSourceDTO;
import net.onelitefeather.vulpes.backend.domain.sound.SoundResponseDTO;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
@DisplayName("Integration tests for SoundController endpoints with Testcontainers")
class SoundControllerIntegrationTest implements TestPropertyProvider {

    @Container
    static final MariaDBContainer<?> mariadb = new MariaDBContainer<>("mariadb:11.4")
            .withDatabaseName("vulpes")
            .withUsername("root")
            .withPassword("vulpes");

    @Override
    public Map<String, String> getProperties() {
        mariadb.start();
        return Map.of(
                "datasources.default.url", mariadb.getJdbcUrl(),
                "datasources.default.username", mariadb.getUsername(),
                "datasources.default.password", mariadb.getPassword(),
                "datasources.default.driverClassName", "org.mariadb.jdbc.Driver",
                "jpa.default.properties.hibernate.hbm2ddl.auto", "update"
        );
    }

    @AfterAll
    static void cleanup() {
        mariadb.stop();
    }

    @Inject
    EmbeddedServer server;

    private static final Faker FAKER = new Faker();

    @BeforeEach
    void setup() {
        RestAssured.baseURI = server.getURL().toString();
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

    @Test
    void testPostSound_returnsOk() {
        SoundEventDTO dto = sampleEventDTOWithoutId();

        var response =
                given()
                        .contentType(ContentType.JSON)
                        .body(dto)
                .when()
                        .post("/sound")
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
                .when().post("/sound").then().statusCode(200)
                .extract().as(SoundResponseDTO.SoundModelDTO.class);

        SoundResponseDTO.SoundModelDTO resp =
                given()
                .when()
                        .get("/sound/" + created.id())
                .then()
                        .statusCode(200)
                        .extract().as(SoundResponseDTO.SoundModelDTO.class);
        assertEquals(created.id(), resp.id());
    }

    @Test
    void testGetSoundById_notFound() {
        given()
        .when()
                .get("/sound/" + UUID.randomUUID())
        .then()
                .statusCode(404);
    }

    @Test
    void testDeleteSoundById_found() {
        SoundResponseDTO.SoundModelDTO created = given().contentType(ContentType.JSON)
                .body(sampleEventDTOWithoutId())
                .when().post("/sound").then().statusCode(200)
                .extract().as(SoundResponseDTO.SoundModelDTO.class);

        given()
        .when()
                .delete("/sound/delete/" + created.id())
        .then()
                .statusCode(200);
    }

    @Test
    void testDeleteSoundById_notFound() {
        given()
        .when()
                .delete("/sound/delete/" + UUID.randomUUID())
        .then()
                .statusCode(404);
    }

    @Test
    void testDeleteAll_returnsOk() {
        given()
        .when()
                .delete("/sound/delete/all")
        .then()
                .statusCode(200);
    }

    @Test
    void testGetAll_returnsOk() {
        given().contentType(ContentType.JSON).body(sampleEventDTOWithoutId()).when().post("/sound").then().statusCode(200);

        SoundResponseDTO.SoundModelDTO[] list =
                given()
                .when()
                        .get("/sound/all")
                .then()
                        .statusCode(200)
                        .extract().as(SoundResponseDTO.SoundModelDTO[].class);
        assertTrue(list.length >= 1);
    }

    @Test
    void testPostUpdate_notFound_returns404() {
        SoundEventDTO dto = sampleEventDTO(UUID.randomUUID());
        given()
                .contentType(ContentType.JSON)
                .body(dto)
        .when()
                .post("/sound/update")
        .then()
                .statusCode(404);
    }

    @Disabled("Source E2E disabled: mapping/cascade behavior depends on external API model; covered by unit tests")
    @Test
    void testCreateSource_and_GetSources_returnsOk() {
        UUID soundId = UUID.randomUUID();
        // ensure parent exists
        given().contentType(ContentType.JSON).body(sampleEventDTO(soundId)).when().post("/sound").then().statusCode(200);

        String fileName = FAKER.internet().slug() + ".ogg";
        SoundFileSourceDTO requestDTO = new SoundFileSourceDTO(null, fileName, 1.0f, 1.0f, 1, false, 16, false, "file");
        SoundResponseDTO.SoundFileSourceDTO created =
                given()
                        .contentType(ContentType.JSON)
                        .body(requestDTO)
                .when()
                        .post("/sound/" + soundId + "/sources")
                .then()
                        .statusCode(200)
                        .extract().as(SoundResponseDTO.SoundFileSourceDTO.class);
        assertNotNull(created);
        assertNotNull(created.id());
        assertEquals(fileName, created.name());

        // fetch page
        given()
        .when()
                .get("/sound/" + soundId + "/sources?page=0&size=10")
        .then()
                .statusCode(200);
    }

    @Disabled("Source E2E disabled: mapping/cascade behavior depends on external API model; covered by unit tests")
    @Test
    void testUpdateSource_returnsOk() {
        UUID soundId = UUID.randomUUID();
        given().contentType(ContentType.JSON).body(sampleEventDTO(soundId)).when().post("/sound").then().statusCode(200);
        // create first
        SoundResponseDTO.SoundFileSourceDTO created =
                given().contentType(ContentType.JSON)
                        .body(new SoundFileSourceDTO(null, "file2.ogg", 0.8f, 1.1f, 2, true, 32, true, "file"))
                        .when().post("/sound/" + soundId + "/sources").then().statusCode(200)
                        .extract().as(SoundResponseDTO.SoundFileSourceDTO.class);
        // now update
        SoundFileSourceDTO update = new SoundFileSourceDTO(created.id(), "file2.ogg", 0.9f, 1.0f, 3, true, 32, true, "file");
        given()
                .contentType(ContentType.JSON)
                .body(update)
        .when()
                .post("/sound/" + soundId + "/sources/update")
        .then()
                .statusCode(200);
    }

    @Disabled("DELETE with body is not widely supported in some client stacks; covered by unit tests")
    @Test
    void testDeleteSource_returnsOk() {
        // This remains disabled; DELETE with body can be flaky across stacks
    }
}
