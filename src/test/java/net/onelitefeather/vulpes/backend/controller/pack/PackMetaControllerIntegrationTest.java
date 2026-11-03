package net.onelitefeather.vulpes.backend.controller.pack;

import io.micronaut.json.JsonMapper;
import io.micronaut.json.tree.JsonNode;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import net.datafaker.Faker;
import net.onelitefeather.vulpes.api.model.pack.PackType;
import net.onelitefeather.vulpes.backend.domain.pack.PackMetaDTO;
import net.onelitefeather.vulpes.backend.domain.pack.PackResponse;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelDTO;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@MicronautTest
@DisplayName("Integration tests for PackMetaController endpoints with Testcontainers")
@EnabledIfDockerAvailable
class PackMetaControllerIntegrationTest {

    private static final Faker FAKER = new Faker();
    private static final String DESCRIPTION = "{\"text\":\"My pack\"}";

    @Inject
    EmbeddedServer server;

    @Inject
    JsonMapper jsonMapper;

    /**
     * A fresh project per test, since a project holds at most one pack meta per type.
     */
    private UUID projectId;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = server.getURL().toString();
        ProjectModelDTO project = new ProjectModelDTO(
                null, FAKER.company().name(), "pack-meta-it-" + UUID.randomUUID(), null, null, null, false);
        projectId = given()
                .contentType(ContentType.JSON)
                .body(project)
        .when()
                .post("/project")
        .then()
                .statusCode(200)
                .extract().as(ProjectModelResponseDTO.ProjectModelDTO.class)
                .id();
    }

    private String path(String suffix) {
        return "/project/" + projectId + "/pack-meta" + suffix;
    }

    /**
     * Compares two JSON documents by value, since the database stores the description as {@code jsonb}
     * and gives it back reformatted.
     */
    private void assertJsonEquals(String expected, String actual) throws IOException {
        assertEquals(jsonMapper.readValue(expected, JsonNode.class), jsonMapper.readValue(actual, JsonNode.class));
    }

    private static PackMetaDTO sampleDTO() {
        return new PackMetaDTO(DESCRIPTION, 48, 0, 57, null, Set.of("minecraft:trade_rebalance"));
    }

    private PackResponse.PackMetaDTO put(PackType type, PackMetaDTO dto) {
        return given()
                .contentType(ContentType.JSON)
                .body(dto)
        .when()
                .put(path("/" + type))
        .then()
                .statusCode(200)
                .extract().as(PackResponse.PackMetaDTO.class);
    }

    @Test
    void putCreatesPackMeta() throws IOException {
        PackResponse.PackMetaDTO created = put(PackType.DATA, sampleDTO());

        assertNotNull(created.id());
        assertEquals(projectId, created.projectId());
        assertEquals(PackType.DATA, created.packType());
        assertJsonEquals(DESCRIPTION, created.description());
        assertEquals(48, created.minFormatMajor());
        assertEquals(57, created.maxFormatMajor());
        assertNull(created.maxFormatMinor());
        assertEquals(Set.of("minecraft:trade_rebalance"), created.enabledFeatures());
        assertNotNull(created.creationDate());
    }

    @Test
    void putReplacesExistingPackMeta() throws IOException {
        PackResponse.PackMetaDTO created = put(PackType.RESOURCE, sampleDTO());

        PackResponse.PackMetaDTO replaced = put(PackType.RESOURCE,
                new PackMetaDTO("{\"text\":\"Other\"}", 60, 1, 61, 2, null));

        assertEquals(created.id(), replaced.id());
        assertJsonEquals("{\"text\":\"Other\"}", replaced.description());
        assertEquals(60, replaced.minFormatMajor());
        assertEquals(1, replaced.minFormatMinor());
        assertEquals(61, replaced.maxFormatMajor());
        assertEquals(2, replaced.maxFormatMinor());
        // empty collections are left out of the response body
        assertNull(replaced.enabledFeatures());
    }

    @Test
    void getReturnsStoredPackMeta() {
        PackResponse.PackMetaDTO created = put(PackType.DATA, sampleDTO());

        PackResponse.PackMetaDTO found = given()
        .when()
                .get(path("/DATA"))
        .then()
                .statusCode(200)
                .extract().as(PackResponse.PackMetaDTO.class);

        assertEquals(created.id(), found.id());
    }

    @Test
    void getReturnsNotFoundWithoutPackMeta() {
        given()
        .when()
                .get(path("/DATA"))
        .then()
                .statusCode(404)
                .body("code", is("RESOURCE_NOT_FOUND"));
    }

    @Test
    void getAllReturnsOnePackMetaPerType() {
        put(PackType.DATA, sampleDTO());
        put(PackType.RESOURCE, sampleDTO());

        given()
        .when()
                .get(path("/"))
        .then()
                .statusCode(200)
                .body("$", hasSize(2));
    }

    @Test
    void getAllReturnsNotFoundForUnknownProject() {
        given()
        .when()
                .get("/project/" + UUID.randomUUID() + "/pack-meta/")
        .then()
                .statusCode(404)
                .body("code", is("PROJECT_NOT_FOUND"));
    }

    @Test
    void putReturnsNotFoundForUnknownProject() {
        given()
                .contentType(ContentType.JSON)
                .body(sampleDTO())
        .when()
                .put("/project/" + UUID.randomUUID() + "/pack-meta/DATA")
        .then()
                .statusCode(404)
                .body("code", is("PROJECT_NOT_FOUND"));
    }

    @Test
    void putRejectsInvertedFormatRange() {
        given()
                .contentType(ContentType.JSON)
                .body(new PackMetaDTO(DESCRIPTION, 57, 3, 57, 2, null))
        .when()
                .put(path("/DATA"))
        .then()
                .statusCode(400)
                .body("code", is("INVALID_REQUEST"));
    }

    @Test
    void putRejectsDescriptionThatIsNoJson() {
        given()
                .contentType(ContentType.JSON)
                .body(new PackMetaDTO("{not json", 48, 0, 57, null, null))
        .when()
                .put(path("/DATA"))
        .then()
                .statusCode(400)
                .body("code", is("INVALID_REQUEST"));
    }

    @Test
    void putRejectsInvalidBody() {
        given()
                .contentType(ContentType.JSON)
                .body(new PackMetaDTO(" ", 0, 0, 57, null, null))
        .when()
                .put(path("/DATA"))
        .then()
                .statusCode(400)
                .body("code", is("VALIDATION_FAILED"));
    }

    @Test
    void deleteRemovesPackMeta() {
        PackResponse.PackMetaDTO created = put(PackType.DATA, sampleDTO());

        PackResponse.PackMetaDTO deleted = given()
        .when()
                .delete(path("/DATA"))
        .then()
                .statusCode(200)
                .extract().as(PackResponse.PackMetaDTO.class);

        assertEquals(created.id(), deleted.id());
        given()
        .when()
                .get(path("/DATA"))
        .then()
                .statusCode(404);
    }

    @Test
    void deleteReturnsNotFoundWithoutPackMeta() {
        given()
        .when()
                .delete(path("/RESOURCE"))
        .then()
                .statusCode(404)
                .body("code", is("RESOURCE_NOT_FOUND"));
    }
}
