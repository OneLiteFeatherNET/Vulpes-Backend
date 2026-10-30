package net.onelitefeather.vulpes.backend.controller.font;

import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import jakarta.inject.Inject;
import net.onelitefeather.vulpes.backend.domain.font.FontModelDTO;
import net.onelitefeather.vulpes.backend.domain.font.FontModelResponseDTO;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelDTO;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Integration tests for the characters of a font")
@EnabledIfDockerAvailable
class FontCharIntegrationTest {

    @Inject
    EmbeddedServer server;

    private String fontPath;
    private String otherFontPath;

    @BeforeEach
    void createFonts() {
        RestAssured.baseURI = server.getURL().toString();
        ProjectModelResponseDTO.ProjectModelDTO project = given()
                .contentType(ContentType.JSON)
                .body(new ProjectModelDTO(null, "Fonts", "fonts-" + UUID.randomUUID(), null, null, null, false))
                .post("/project")
                .then().statusCode(200)
                .extract().as(ProjectModelResponseDTO.ProjectModelDTO.class);
        fontPath = "/font/chars/" + createFont(project.id(), "default");
        otherFontPath = "/font/chars/" + createFont(project.id(), "other");
    }

    private UUID createFont(UUID projectId, String key) {
        return given()
                .contentType(ContentType.JSON)
                .body(new FontModelDTO(null, "Font", key, "bitmap", "mapper", "texture.png", null, 7, 8))
                .post("/project/" + projectId + "/font")
                .then().statusCode(200)
                .extract().as(FontModelResponseDTO.FontModelDTO.class)
                .id();
    }

    private String createChar(String path, String line) {
        return given().contentType(ContentType.JSON).body("{\"line\":\"" + line + "\",\"orderIndex\":0}")
                .put(path)
                .then().statusCode(200)
                .extract().jsonPath().getString("id");
    }

    @Test
    @DisplayName("a character is updated in place")
    void updateCharChangesTheValues() {
        String id = createChar(fontPath, "abc");

        JsonPath updated = given().contentType(ContentType.JSON)
                .body("{\"id\":\"" + id + "\",\"line\":\"xyz\",\"orderIndex\":2}")
                .post(fontPath)
                .then().statusCode(200)
                .extract().jsonPath();

        assertEquals(id, updated.getString("id"));
        assertEquals("xyz", updated.getString("line"));
        assertEquals(2, updated.getInt("orderIndex"));
    }

    @Test
    @DisplayName("a character of another font can't be updated through this font")
    void updateCharRejectsCharsOfOtherFonts() {
        String foreign = createChar(otherFontPath, "abc");

        given().contentType(ContentType.JSON)
                .body("{\"id\":\"" + foreign + "\",\"line\":\"xyz\",\"orderIndex\":0}")
                .post(fontPath)
                .then().statusCode(404);

        JsonPath other = given().get(otherFontPath).then().statusCode(200).extract().jsonPath();
        assertEquals(List.of(foreign), other.getList("content.id"));
        assertEquals("abc", other.getString("content[0].line"));
    }

    @Test
    @DisplayName("updating a character that doesn't exist is answered with not found")
    void updateCharRejectsUnknownIds() {
        given().contentType(ContentType.JSON)
                .body("{\"id\":\"" + UUID.randomUUID() + "\",\"line\":\"xyz\",\"orderIndex\":0}")
                .post(fontPath)
                .then().statusCode(404);
    }
}
