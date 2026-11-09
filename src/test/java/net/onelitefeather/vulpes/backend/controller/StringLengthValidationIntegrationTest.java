package net.onelitefeather.vulpes.backend.controller;

import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import net.onelitefeather.vulpes.api.model.expression.ExpressionVariableType;
import net.onelitefeather.vulpes.api.model.pack.PackType;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionDTO;
import net.onelitefeather.vulpes.backend.domain.expression.ExpressionVariableDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemModelDTO;
import net.onelitefeather.vulpes.backend.domain.pack.PackMetaDTO;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelDTO;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelResponseDTO;
import net.onelitefeather.vulpes.backend.domain.sound.SoundEventDTO;
import net.onelitefeather.vulpes.backend.domain.sound.SoundFileSourceDTO;
import net.onelitefeather.vulpes.backend.validation.ColumnLength;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;

/**
 * Checks that a string longer than its {@code varchar(255)} column is rejected with 400. It used to reach
 * the database and fail there, which answered 500.
 */
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Integration tests for the length of string values")
@EnabledIfDockerAvailable
class StringLengthValidationIntegrationTest {

    private static final String TOO_LONG = "a".repeat(ColumnLength.VARCHAR + 1);
    private static final String LONGEST = "a".repeat(ColumnLength.VARCHAR);

    @Inject
    EmbeddedServer server;

    private UUID projectId;

    @BeforeAll
    void createProject() {
        RestAssured.baseURI = server.getURL().toString();
        projectId = given()
                .contentType(ContentType.JSON)
                .body(new ProjectModelDTO(null, "Lengths", uniqueKey("lengths"), null, null, null, false))
                .post("/project")
                .then().statusCode(200)
                .extract().as(ProjectModelResponseDTO.ProjectModelDTO.class)
                .id();
    }

    private String modelPath(String path) {
        return "/project/" + projectId + "/" + path;
    }

    private static String uniqueKey(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }

    @Test
    @DisplayName("create() of a project with a too long description answers 400")
    void tooLongProjectDescription() {
        given().contentType(ContentType.JSON)
                .body(new ProjectModelDTO(null, "Project", uniqueKey("project"), null, null, TOO_LONG, false))
                .post("/project")
                .then().statusCode(400)
                .body("errors.code", hasItem("Size"));
    }

    @Test
    @DisplayName("create() of a project with a description as long as its column answers 200")
    void longestProjectDescription() {
        given().contentType(ContentType.JSON)
                .body(new ProjectModelDTO(null, "Project", uniqueKey("project"), null, null, LONGEST, false))
                .post("/project")
                .then().statusCode(200);
    }

    @Test
    @DisplayName("create() of an item with a too long comment answers 400")
    void tooLongItemComment() {
        given().contentType(ContentType.JSON)
                .body(new ItemModelDTO(null, "Item", uniqueKey("item"), TOO_LONG, "group"))
                .post(modelPath("item"))
                .then().statusCode(400)
                .body("errors.code", hasItem("Size"));
    }

    @Test
    @DisplayName("update() of an item with a too long ui name answers 400 before looking the model up")
    void tooLongItemUiName() {
        given().contentType(ContentType.JSON)
                .body(new ItemModelDTO(UUID.randomUUID(), TOO_LONG, uniqueKey("item"), null, "group"))
                .post(modelPath("item") + "/update")
                .then().statusCode(400)
                .body("errors.code", hasItem("Size"));
    }

    @Test
    @DisplayName("create() of a sound source with a too long name answers 400")
    void tooLongSoundSourceName() {
        String soundId = given().contentType(ContentType.JSON)
                .body(new SoundEventDTO(null, "Sound", uniqueKey("sound"), "block.stone.step", null, null))
                .post(modelPath("sound"))
                .then().statusCode(200)
                .extract().path("id");

        given().contentType(ContentType.JSON)
                .body(new SoundFileSourceDTO(null, TOO_LONG, 1.0f, 1.0f, 1, false, 16, false, "file", null))
                .post("/sound/" + soundId + "/sources")
                .then().statusCode(400)
                .body("errors.code", hasItem("Size"));
    }

    @Test
    @DisplayName("create() of an expression with a too long variable test value answers 400")
    void tooLongExpressionVariable() {
        given().contentType(ContentType.JSON)
                .body(new ExpressionDTO(null, "Expression", "a * 2", null,
                        List.of(new ExpressionVariableDTO("a", ExpressionVariableType.NUMBER, TOO_LONG)),
                        null, null, null))
                .post(modelPath("expression"))
                .then().statusCode(400)
                .body("errors.code", hasItem("Size"));
    }

    @Test
    @DisplayName("put() of a pack meta with a too long feature answers 400")
    void tooLongPackFeature() {
        given().contentType(ContentType.JSON)
                .body(new PackMetaDTO("{\"text\":\"My pack\"}", 48, 0, 57, null, Set.of(TOO_LONG), null))
                .put(modelPath("pack-meta/" + PackType.RESOURCE))
                .then().statusCode(400)
                .body("errors.code", hasItem("Size"));
    }
}
