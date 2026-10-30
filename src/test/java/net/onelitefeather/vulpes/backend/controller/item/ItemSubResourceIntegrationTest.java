package net.onelitefeather.vulpes.backend.controller.item;

import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import jakarta.inject.Inject;
import net.onelitefeather.vulpes.backend.domain.item.ItemModelDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemModelResponseDTO;
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
@DisplayName("Integration tests for the enchantments of an item")
@EnabledIfDockerAvailable
class ItemSubResourceIntegrationTest {

    @Inject
    EmbeddedServer server;

    private String itemPath;
    private String otherItemPath;

    @BeforeEach
    void createItems() {
        RestAssured.baseURI = server.getURL().toString();
        ProjectModelResponseDTO.ProjectModelDTO project = given()
                .contentType(ContentType.JSON)
                .body(new ProjectModelDTO(null, "Items", "items-" + UUID.randomUUID(), null, null, null, false))
                .post("/project")
                .then().statusCode(200)
                .extract().as(ProjectModelResponseDTO.ProjectModelDTO.class);
        itemPath = "/item/" + createItem(project.id(), "apple");
        otherItemPath = "/item/" + createItem(project.id(), "pear");
    }

    private UUID createItem(UUID projectId, String key) {
        return given()
                .contentType(ContentType.JSON)
                .body(new ItemModelDTO(null, key, key, null, "group"))
                .post("/project/" + projectId + "/item")
                .then().statusCode(200)
                .extract().as(ItemModelResponseDTO.ItemModelDTO.class)
                .id();
    }

    private String createEnchantment(String path, String name) {
        return given().contentType(ContentType.JSON)
                .body("{\"name\":\"" + name + "\",\"level\":1,\"unsafe\":false}")
                .put(path + "/enchantment")
                .then().statusCode(200)
                .extract().jsonPath().getString("id");
    }

    @Test
    @DisplayName("an enchantment is updated in place")
    void updateEnchantmentChangesTheValues() {
        String id = createEnchantment(itemPath, "minecraft:sharpness");

        JsonPath updated = given().contentType(ContentType.JSON)
                .body("{\"id\":\"" + id + "\",\"name\":\"minecraft:smite\",\"level\":3,\"unsafe\":true}")
                .post(itemPath + "/enchantment")
                .then().statusCode(200)
                .extract().jsonPath();

        assertEquals(id, updated.getString("id"));
        assertEquals("minecraft:smite", updated.getString("name"));
        assertEquals(3, updated.getInt("level"));
    }

    @Test
    @DisplayName("an enchantment of another item can't be updated through this item")
    void updateEnchantmentRejectsEnchantmentsOfOtherItems() {
        String foreign = createEnchantment(otherItemPath, "minecraft:sharpness");

        given().contentType(ContentType.JSON)
                .body("{\"id\":\"" + foreign + "\",\"name\":\"minecraft:smite\",\"level\":5,\"unsafe\":true}")
                .post(itemPath + "/enchantment")
                .then().statusCode(404);

        JsonPath other = given().get(otherItemPath + "/enchantments").then().statusCode(200).extract().jsonPath();
        assertEquals(List.of(foreign), other.getList("content.id"));
        assertEquals("minecraft:sharpness", other.getString("content[0].name"));
        assertEquals(List.of(), given().get(itemPath + "/enchantments").then().extract().jsonPath().getList("content"));
    }

    @Test
    @DisplayName("updating an enchantment that doesn't exist is answered with not found")
    void updateEnchantmentRejectsUnknownIds() {
        given().contentType(ContentType.JSON)
                .body("{\"id\":\"" + UUID.randomUUID() + "\",\"name\":\"minecraft:smite\",\"level\":1,\"unsafe\":false}")
                .post(itemPath + "/enchantment")
                .then().statusCode(404);
    }
}
