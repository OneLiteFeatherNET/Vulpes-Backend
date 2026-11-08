package net.onelitefeather.vulpes.backend.controller.item;

import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.ValidatableResponse;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Integration tests for the uniqueness of the custom model data")
@EnabledIfDockerAvailable
class CustomModelDataIntegrationTest {

    private static final String MODEL_DATA = "minecraft:custom_model_data";
    private static final String ITEM_MODEL = "minecraft:item_model";
    private static final String MATERIAL = "stelaris:material";

    @Inject
    EmbeddedServer server;

    private UUID projectId;

    @BeforeEach
    void createProject() {
        RestAssured.baseURI = server.getURL().toString();
        projectId = project();
    }

    private static UUID project() {
        return given()
                .contentType(ContentType.JSON)
                .body(new ProjectModelDTO(null, "Model data", "model-data-" + UUID.randomUUID(), null, null, null, false))
                .post("/project")
                .then().statusCode(200)
                .extract().as(ProjectModelResponseDTO.ProjectModelDTO.class).id();
    }

    private UUID item(String key) {
        return given()
                .contentType(ContentType.JSON)
                .body(new ItemModelDTO(null, key, key, null, "group"))
                .post("/project/" + projectId + "/item")
                .then().statusCode(200)
                .extract().as(ItemModelResponseDTO.ItemModelDTO.class).id();
    }

    private static ValidatableResponse put(UUID itemId, String key, String value) {
        return given().contentType(ContentType.JSON)
                .body("{\"componentKey\":\"" + key + "\",\"value\":" + value + "}")
                .put("/item/" + itemId + "/component")
                .then();
    }

    private static String create(UUID itemId, String key, String value) {
        return put(itemId, key, value).statusCode(200).extract().jsonPath().getString("id");
    }

    private static ValidatableResponse update(UUID itemId, String componentId, String key, String value) {
        return given().contentType(ContentType.JSON)
                .body("{\"id\":\"" + componentId + "\",\"componentKey\":\"" + key + "\",\"value\":" + value + "}")
                .post("/item/" + itemId + "/component")
                .then();
    }

    private static JsonPath components(UUID itemId) {
        return given().get("/item/" + itemId + "/components").then().statusCode(200).extract().jsonPath();
    }

    private static String componentId(UUID itemId, String key) {
        return components(itemId).getString("content.find { it.componentKey == '" + key + "' }.id");
    }

    @Test
    @DisplayName("two items with the same material can't have the same custom model data")
    void createRejectsTheSameModelDataOnTheSameMaterial() {
        create(item("ruby"), MODEL_DATA, "{\"floats\":[1001]}");
        put(item("sapphire"), MODEL_DATA, "{\"floats\":[1001]}").statusCode(409);
    }

    @Test
    @DisplayName("values with the same content are the same custom model data")
    void createComparesTheContent() {
        create(item("ruby"), MODEL_DATA, "{\"floats\":[1001]}");
        put(item("sapphire"), MODEL_DATA, "{\"strings\":[],\"floats\":[1001.0]}").statusCode(409);
    }

    @Test
    @DisplayName("the same custom model data is fine on another material, item model or project")
    void createAcceptsTheSameModelDataOnOtherDefinitions() {
        create(item("ruby"), MODEL_DATA, "{\"floats\":[1001]}");

        UUID stone = item("stone_ruby");
        update(stone, componentId(stone, MATERIAL), MATERIAL, "\"minecraft:stone\"").statusCode(200);
        create(stone, MODEL_DATA, "{\"floats\":[1001]}");

        UUID modelled = item("modelled_ruby");
        create(modelled, ITEM_MODEL, "\"vulpes:item/ruby\"");
        create(modelled, MODEL_DATA, "{\"floats\":[1001]}");

        projectId = project();
        create(item("ruby"), MODEL_DATA, "{\"floats\":[1001]}");
    }

    @Test
    @DisplayName("other custom model data on the same material is fine, also when an item saves its own value again")
    void createAcceptsOtherValues() {
        UUID ruby = item("ruby");
        String id = create(ruby, MODEL_DATA, "{\"floats\":[1001]}");
        create(item("sapphire"), MODEL_DATA, "{\"floats\":[1002]}");
        update(ruby, id, MODEL_DATA, "{\"floats\":[1001]}").statusCode(200);
    }

    @Test
    @DisplayName("an update can't change the custom model data or the material into a taken one")
    void updateRejectsCollisions() {
        create(item("ruby"), MODEL_DATA, "{\"floats\":[1001]}");

        UUID sapphire = item("sapphire");
        String id = create(sapphire, MODEL_DATA, "{\"floats\":[1002]}");
        update(sapphire, id, MODEL_DATA, "{\"floats\":[1001]}").statusCode(409);

        UUID stone = item("stone_ruby");
        update(stone, componentId(stone, MATERIAL), MATERIAL, "\"minecraft:stone\"").statusCode(200);
        create(stone, MODEL_DATA, "{\"floats\":[1001]}");
        update(stone, componentId(stone, MATERIAL), MATERIAL, "\"minecraft:dirt\"").statusCode(409);
    }

    @Test
    @DisplayName("removing the item model can't make the custom model data collide on the material")
    void deleteRejectsCollisions() {
        create(item("ruby"), MODEL_DATA, "{\"floats\":[1001]}");

        UUID modelled = item("modelled_ruby");
        String itemModel = create(modelled, ITEM_MODEL, "\"vulpes:item/ruby\"");
        create(modelled, MODEL_DATA, "{\"floats\":[1001]}");

        given().delete("/item/" + modelled + "/component/" + itemModel).then().statusCode(409);
        assertEquals("vulpes:item/ruby", components(modelled).getString("content.find { it.componentKey == '" + ITEM_MODEL + "' }.value"));
    }

    @Test
    @DisplayName("a copy leaves out the custom model data if it would collide in the target project")
    void copyLeavesOutCollidingModelData() {
        UUID ruby = item("ruby");
        create(ruby, MODEL_DATA, "{\"floats\":[1001]}");
        create(ruby, "minecraft:max_stack_size", "16");

        UUID copy = given().contentType(ContentType.JSON)
                .body("{\"targetKey\":\"ruby_copy\",\"relations\":[\"COMPONENTS\"]}")
                .post("/project/" + projectId + "/item/" + ruby + "/copy")
                .then().statusCode(200)
                .extract().as(ItemModelResponseDTO.ItemModelDTO.class).id();
        List<String> keys = components(copy).getList("content.componentKey");
        assertFalse(keys.contains(MODEL_DATA));
        assertTrue(keys.contains("minecraft:max_stack_size"));

        UUID other = project();
        UUID foreign = given().contentType(ContentType.JSON)
                .body("{\"targetProjectId\":\"" + other + "\",\"relations\":[\"COMPONENTS\"]}")
                .post("/project/" + projectId + "/item/" + ruby + "/copy")
                .then().statusCode(200)
                .extract().as(ItemModelResponseDTO.ItemModelDTO.class).id();
        assertTrue(components(foreign).getList("content.componentKey").contains(MODEL_DATA));
    }
}
