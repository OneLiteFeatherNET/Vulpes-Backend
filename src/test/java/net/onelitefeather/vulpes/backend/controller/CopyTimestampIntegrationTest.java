package net.onelitefeather.vulpes.backend.controller;

import io.micronaut.context.annotation.Property;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import jakarta.inject.Inject;
import net.onelitefeather.vulpes.api.model.advancement.AdvancementFrameType;
import net.onelitefeather.vulpes.backend.domain.advancement.AdvancementModelDTO;
import net.onelitefeather.vulpes.backend.domain.attribute.AttributeModelDTO;
import net.onelitefeather.vulpes.backend.domain.copy.CopyDTO;
import net.onelitefeather.vulpes.backend.domain.font.FontModelDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemModelDTO;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelDTO;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelResponseDTO;
import net.onelitefeather.vulpes.backend.domain.sound.SoundEventDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * A copy is a new model, so its response has to carry the timestamps just like a create does.
 */
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Property(name = "datasources.default.schema-generate", value = "NONE")
@DisplayName("Integration tests for the timestamps a copy returns")
@EnabledIfDockerAvailable
class CopyTimestampIntegrationTest {

    @Inject
    EmbeddedServer server;

    private UUID projectId;

    @BeforeAll
    void createProject() {
        RestAssured.baseURI = server.getURL().toString();
        projectId = given()
                .contentType(ContentType.JSON)
                .body(new ProjectModelDTO(null, "Copies", "copies-" + UUID.randomUUID(), null, null, null, false))
                .post("/project")
                .then().statusCode(200)
                .extract().as(ProjectModelResponseDTO.ProjectModelDTO.class)
                .id();
    }

    /**
     * Each model as its path below the project and a factory for a valid create body with the given key.
     * Dimensions are left out, their response carries no timestamps at all.
     */
    private static Stream<Arguments> models() {
        return Stream.of(
                model("attribute", key -> new AttributeModelDTO(null, "Speed", key, 1.0, 10.0, null)),
                model("font", key -> new FontModelDTO(null, "Font", key, "bitmap", "mapper", "texture.png", null, 7, 8)),
                model("item", key -> new ItemModelDTO(null, "Item", key, null, "group")),
                model("sound", key -> new SoundEventDTO(null, "Sound", key, "block.stone.step", null, null)),
                model("advancement", key -> new AdvancementModelDTO(null, "Advancement", key, null, "STONE",
                        AdvancementFrameType.TASK, null, null, null, null, 0f, 0f, true, true, false))
        );
    }

    private static Arguments model(String path, Function<String, Object> body) {
        return Arguments.of(path, body);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("models")
    @DisplayName("copy() returns the creation and modification date of the copy")
    void copyReturnsTimestamps(String path, Function<String, Object> body) {
        String modelPath = "/project/" + projectId + "/" + path;
        String id = given().contentType(ContentType.JSON).body(body.apply(path + "-" + UUID.randomUUID()))
                .post(modelPath)
                .then().statusCode(200)
                .extract().jsonPath().getString("id");

        JsonPath copy = given().contentType(ContentType.JSON)
                .body(new CopyDTO(null, path + "-copy-" + UUID.randomUUID(), null))
                .post(modelPath + "/" + id + "/copy")
                .then().statusCode(200)
                .extract().jsonPath();

        assertNotNull(copy.getString("creationDate"), "the copy response must carry its creation date");
        assertNotNull(copy.getString("modificationDate"), "the copy response must carry its modification date");
    }
}
