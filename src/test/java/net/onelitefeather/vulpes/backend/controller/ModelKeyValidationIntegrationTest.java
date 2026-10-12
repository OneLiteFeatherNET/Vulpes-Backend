package net.onelitefeather.vulpes.backend.controller;

import io.micronaut.context.annotation.Property;
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

/**
 * Every model needs a key: the generator derives the variable from it and the database only keeps
 * it unique per project when it is set. These tests make sure the HTTP layer actually validates the
 * request body and refuses a model without a key, instead of persisting it with {@code key = null}.
 */
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Property(name = "datasources.default.schema-generate", value = "NONE")
@DisplayName("Integration tests for the required key of every model")
@EnabledIfDockerAvailable
class ModelKeyValidationIntegrationTest {

    @Inject
    EmbeddedServer server;

    private UUID projectId;

    @BeforeAll
    void createProject() {
        RestAssured.baseURI = server.getURL().toString();
        projectId = given()
                .contentType(ContentType.JSON)
                .body(new ProjectModelDTO(null, "Keys", "keys-" + UUID.randomUUID(), null, null, null, false))
                .post("/project")
                .then().statusCode(200)
                .extract().as(ProjectModelResponseDTO.ProjectModelDTO.class)
                .id();
    }

    /**
     * Each model as its path below the project and a factory for a request body that is valid except
     * for the given id and key.
     */
    private static Stream<Arguments> models() {
        return Stream.of(
                model("attribute", (id, key) -> new AttributeModelDTO(id, "Speed", key, 1.0, 10.0, null)),
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

    private String modelPath(String path) {
        return "/project/" + projectId + "/" + path;
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("models")
    @DisplayName("create() without a key answers 400")
    void createWithoutKeyReturns400(String path, BiFunction<UUID, String, Object> body) {
        given().contentType(ContentType.JSON).body(body.apply(null, null))
                .post(modelPath(path))
                .then().statusCode(400);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("models")
    @DisplayName("create() with a blank key answers 400")
    void createWithBlankKeyReturns400(String path, BiFunction<UUID, String, Object> body) {
        given().contentType(ContentType.JSON).body(body.apply(null, "  "))
                .post(modelPath(path))
                .then().statusCode(400);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("models")
    @DisplayName("create() with a key answers 200")
    void createWithKeyReturns200(String path, BiFunction<UUID, String, Object> body) {
        given().contentType(ContentType.JSON).body(body.apply(null, path + "-" + UUID.randomUUID()))
                .post(modelPath(path))
                .then().statusCode(200);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("models")
    @DisplayName("update() without a key answers 400 before looking the model up")
    void updateWithoutKeyReturns400(String path, BiFunction<UUID, String, Object> body) {
        given().contentType(ContentType.JSON).body(body.apply(UUID.randomUUID(), null))
                .post(modelPath(path) + "/update")
                .then().statusCode(400);
    }

    @Test
    @DisplayName("creating a project without a key answers 400")
    void createProjectWithoutKeyReturns400() {
        given().contentType(ContentType.JSON)
                .body(new ProjectModelDTO(null, "No key", null, null, null, null, false))
                .post("/project")
                .then().statusCode(400);
    }
}
