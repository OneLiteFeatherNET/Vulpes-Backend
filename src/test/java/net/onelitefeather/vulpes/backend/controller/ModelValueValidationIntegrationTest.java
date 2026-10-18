package net.onelitefeather.vulpes.backend.controller;

import io.micronaut.context.annotation.Property;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import net.onelitefeather.vulpes.backend.domain.attribute.AttributeModelDTO;
import net.onelitefeather.vulpes.backend.domain.font.FontModelDTO;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelDTO;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelResponseDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

import java.util.UUID;

import static io.restassured.RestAssured.given;

/**
 * Checks that the value constraints of the attribute and font models run on create and update. They
 * used to have no validation group, so the endpoints, which only validate the create and update
 * groups, never applied them.
 */
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Property(name = "datasources.default.schema-generate", value = "NONE")
@DisplayName("Integration tests for the value constraints of attributes and fonts")
@EnabledIfDockerAvailable
class ModelValueValidationIntegrationTest {

    @Inject
    EmbeddedServer server;

    private UUID projectId;

    @BeforeAll
    void createProject() {
        RestAssured.baseURI = server.getURL().toString();
        projectId = given()
                .contentType(ContentType.JSON)
                .body(new ProjectModelDTO(null, "Values", "values-" + UUID.randomUUID(), null, null, null, false))
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
    @DisplayName("create() with an attribute maximum of 0 answers 400")
    void createAttributeWithZeroMaximumReturns400() {
        given().contentType(ContentType.JSON)
                .body(new AttributeModelDTO(null, "Speed", uniqueKey("speed"), 0.0, 0.0, null))
                .post(modelPath("attribute"))
                .then().statusCode(400);
    }

    @Test
    @DisplayName("update() with a negative attribute maximum answers 400 before looking the model up")
    void updateAttributeWithNegativeMaximumReturns400() {
        given().contentType(ContentType.JSON)
                .body(new AttributeModelDTO(UUID.randomUUID(), "Speed", uniqueKey("speed"), 0.0, -1.0, null))
                .post(modelPath("attribute") + "/update")
                .then().statusCode(400);
    }

    @Test
    @DisplayName("create() with a negative attribute default answers 200")
    void createAttributeWithNegativeDefaultReturns200() {
        given().contentType(ContentType.JSON)
                .body(new AttributeModelDTO(null, "Luck", uniqueKey("luck"), -1.0, 1024.0, null))
                .post(modelPath("attribute"))
                .then().statusCode(200);
    }

    @Test
    @DisplayName("create() of a font without a provider answers 400")
    void createFontWithoutProviderReturns400() {
        given().contentType(ContentType.JSON)
                .body(new FontModelDTO(null, "Font", uniqueKey("font"), null, "mapper", "texture.png", null, 7, 8))
                .post(modelPath("font"))
                .then().statusCode(400);
    }

    @Test
    @DisplayName("update() of a font with a blank provider answers 400 before looking the model up")
    void updateFontWithBlankProviderReturns400() {
        given().contentType(ContentType.JSON)
                .body(new FontModelDTO(UUID.randomUUID(), "Font", uniqueKey("font"), " ", "mapper", "texture.png", null, 7, 8))
                .post(modelPath("font") + "/update")
                .then().statusCode(400);
    }
}
