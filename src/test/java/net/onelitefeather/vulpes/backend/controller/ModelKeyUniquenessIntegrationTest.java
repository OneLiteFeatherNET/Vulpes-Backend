package net.onelitefeather.vulpes.backend.controller;

import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import net.onelitefeather.vulpes.api.model.advancement.AdvancementFrameType;
import net.onelitefeather.vulpes.api.model.dimension.CardinalLight;
import net.onelitefeather.vulpes.api.model.dimension.Skybox;
import net.onelitefeather.vulpes.backend.domain.advancement.AdvancementModelDTO;
import net.onelitefeather.vulpes.backend.domain.attribute.AttributeModelDTO;
import net.onelitefeather.vulpes.backend.domain.dimension.DimensionModelDTO;
import net.onelitefeather.vulpes.backend.domain.font.FontModelDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemModelDTO;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelDTO;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelResponseDTO;
import net.onelitefeather.vulpes.backend.domain.sound.SoundEventDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

/**
 * A key identifies a model within its project and a project globally. These tests make sure a taken
 * key is refused with 409 on create and update instead of surfacing as a database error.
 */
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Integration tests for the uniqueness of model and project keys")
@EnabledIfDockerAvailable
class ModelKeyUniquenessIntegrationTest {

    @Inject
    EmbeddedServer server;

    private UUID projectId;
    private UUID otherProjectId;

    @BeforeAll
    void createProjects() {
        RestAssured.baseURI = server.getURL().toString();
        projectId = createProject("unique-" + UUID.randomUUID());
        otherProjectId = createProject("unique-other-" + UUID.randomUUID());
    }

    /**
     * Each model as its path below the project and a factory for a request body that is valid for
     * the given id and key.
     */
    private static Stream<Arguments> models() {
        return Stream.of(
                model("attribute", (id, key) -> new AttributeModelDTO(id, "Speed", key, 1.0, 0.0, 10.0, false, null, null)),
                model("font", (id, key) -> new FontModelDTO(id, "Font", key, "bitmap", "mapper", "texture.png", null, 1, 1)),
                model("item", (id, key) -> new ItemModelDTO(id, "Item", key, null, "group")),
                model("sound", (id, key) -> new SoundEventDTO(id, "Sound", key, "block.stone.step", null, null)),
                model("advancement", (id, key) -> new AdvancementModelDTO(id, "Advancement", key, null, "STONE",
                        AdvancementFrameType.TASK, null, null, null, null, 0f, 0f, true, true, false)),
                model("dimension", (id, key) -> new DimensionModelDTO(id, "Overworld", key, false, true, false, false,
                        1.0, -64, 384, 384, "#minecraft:infiniburn_overworld", 0.0f,
                        "constant:11", 15, Skybox.OVERWORLD, CardinalLight.DEFAULT, null))
        );
    }

    private static Arguments model(String path, BiFunction<UUID, String, Object> body) {
        return Arguments.of(path, body);
    }

    private UUID createProject(String key) {
        return given()
                .contentType(ContentType.JSON)
                .body(new ProjectModelDTO(null, "Keys", key, null, null, null, false))
                .post("/project")
                .then().statusCode(200)
                .extract().as(ProjectModelResponseDTO.ProjectModelDTO.class)
                .id();
    }

    private UUID createModel(UUID project, String path, BiFunction<UUID, String, Object> body, String key) {
        return UUID.fromString(given().contentType(ContentType.JSON).body(body.apply(null, key))
                .post("/project/" + project + "/" + path)
                .then().statusCode(200)
                .extract().path("id"));
    }

    private static String uniqueKey(String path) {
        return path + "-" + UUID.randomUUID();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("models")
    @DisplayName("create() with a key taken in the same project answers 409")
    void createWithTakenKeyReturns409(String path, BiFunction<UUID, String, Object> body) {
        String key = uniqueKey(path);
        createModel(projectId, path, body, key);

        given().contentType(ContentType.JSON).body(body.apply(null, key))
                .post("/project/" + projectId + "/" + path)
                .then().statusCode(409);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("models")
    @DisplayName("create() with a key taken only in another project answers 200")
    void createWithKeyTakenElsewhereReturns200(String path, BiFunction<UUID, String, Object> body) {
        String key = uniqueKey(path);
        createModel(otherProjectId, path, body, key);

        given().contentType(ContentType.JSON).body(body.apply(null, key))
                .post("/project/" + projectId + "/" + path)
                .then().statusCode(200);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("models")
    @DisplayName("update() to a key taken by another model answers 409")
    void updateToTakenKeyReturns409(String path, BiFunction<UUID, String, Object> body) {
        String takenKey = uniqueKey(path);
        createModel(projectId, path, body, takenKey);
        UUID id = createModel(projectId, path, body, uniqueKey(path));

        given().contentType(ContentType.JSON).body(body.apply(id, takenKey))
                .post("/project/" + projectId + "/" + path + "/update")
                .then().statusCode(409);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("models")
    @DisplayName("update() that keeps the own key answers 200")
    void updateKeepingOwnKeyReturns200(String path, BiFunction<UUID, String, Object> body) {
        String key = uniqueKey(path);
        UUID id = createModel(projectId, path, body, key);

        given().contentType(ContentType.JSON).body(body.apply(id, key))
                .post("/project/" + projectId + "/" + path + "/update")
                .then().statusCode(200);
    }

    @Test
    @DisplayName("creating a project with a taken key answers 409")
    void createProjectWithTakenKeyReturns409() {
        String key = "project-" + UUID.randomUUID();
        createProject(key);

        given().contentType(ContentType.JSON)
                .body(new ProjectModelDTO(null, "Duplicate", key, null, null, null, false))
                .post("/project")
                .then().statusCode(409)
                .body("detail", equalTo("A project with key '" + key + "' already exists."));
    }

    @Test
    @DisplayName("updating a project to a taken key answers 409")
    void updateProjectToTakenKeyReturns409() {
        String takenKey = "project-" + UUID.randomUUID();
        createProject(takenKey);
        UUID id = createProject("project-" + UUID.randomUUID());

        given().contentType(ContentType.JSON)
                .body(new ProjectModelDTO(id, "Renamed", takenKey, null, null, null, false))
                .post("/project/update")
                .then().statusCode(409);
    }

    @Test
    @DisplayName("updating a project that keeps its own key answers 200")
    void updateProjectKeepingOwnKeyReturns200() {
        String key = "project-" + UUID.randomUUID();
        UUID id = createProject(key);

        given().contentType(ContentType.JSON)
                .body(new ProjectModelDTO(id, "Renamed", key, null, null, null, false))
                .post("/project/update")
                .then().statusCode(200);
    }
}
