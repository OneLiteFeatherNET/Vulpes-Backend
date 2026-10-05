package net.onelitefeather.vulpes.backend.controller.item;

import io.micronaut.context.annotation.Property;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import jakarta.inject.Inject;
import net.onelitefeather.vulpes.api.repository.item.ItemComponentRepository;
import net.onelitefeather.vulpes.backend.domain.item.ItemModelDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemModelResponseDTO;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelDTO;
import net.onelitefeather.vulpes.backend.domain.project.ProjectModelResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Property(name = "datasources.default.schema-generate", value = "NONE")
@DisplayName("Integration tests for the data components of an item")
@EnabledIfDockerAvailable
class ItemComponentControllerIntegrationTest {

    private static final String FOOD = """
            {"componentKey":"minecraft:food","value":{"nutrition":4,"saturation":2.4}}""";

    @Inject
    EmbeddedServer server;

    @Inject
    ItemComponentRepository componentRepository;

    private String componentPath;

    @BeforeEach
    void createItem() {
        RestAssured.baseURI = server.getURL().toString();
        ProjectModelResponseDTO.ProjectModelDTO project = given()
                .contentType(ContentType.JSON)
                .body(new ProjectModelDTO(null, "Components", "components-" + UUID.randomUUID(), null, null, null, false))
                .post("/project")
                .then().statusCode(200)
                .extract().as(ProjectModelResponseDTO.ProjectModelDTO.class);
        ItemModelResponseDTO.ItemModelDTO item = given()
                .contentType(ContentType.JSON)
                .body(new ItemModelDTO(null, "Apple", "apple", null, null, "minecraft:apple", "group", 0, 1))
                .post("/project/" + project.id() + "/item")
                .then().statusCode(200)
                .extract().as(ItemModelResponseDTO.ItemModelDTO.class);
        componentPath = "/item/" + item.id();
    }

    private JsonPath create(String body) {
        return given().contentType(ContentType.JSON).body(body)
                .put(componentPath + "/component")
                .then().statusCode(200)
                .extract().jsonPath();
    }

    @Test
    @DisplayName("a component keeps its JSON value through create and read")
    void create_keepsTheJsonValue() {
        JsonPath created = create(FOOD);
        assertEquals("minecraft:food", created.getString("componentKey"));
        assertEquals(4, created.getInt("value.nutrition"));

        JsonPath page = given().get(componentPath + "/components").then().statusCode(200).extract().jsonPath();
        assertEquals(1, page.getList("content").size());
        assertEquals(Map.of("nutrition", 4, "saturation", 2.4f), page.getMap("content[0].value"));
        assertEquals(created.getString("id"), page.getString("content[0].id"));
    }

    @Test
    @DisplayName("components without a value and plain values are stored as they are")
    void create_storesUnitsAndPlainValues() {
        assertEquals(Map.of(), create("{\"componentKey\":\"minecraft:glider\",\"value\":{}}").getMap("value"));
        assertEquals(16, create("{\"componentKey\":\"minecraft:max_stack_size\",\"value\":16}").getInt("value"));
        assertEquals("minecraft:stick",
                create("{\"componentKey\":\"minecraft:item_model\",\"value\":\"minecraft:stick\"}").getString("value"));
    }

    @Test
    @DisplayName("an item can't have the same component twice")
    void create_rejectsDuplicates() {
        create(FOOD);
        given().contentType(ContentType.JSON).body(FOOD)
                .put(componentPath + "/component")
                .then().statusCode(409);
    }

    @Test
    @DisplayName("components with a dedicated item field are rejected")
    void create_rejectsManagedComponents() {
        given().contentType(ContentType.JSON)
                .body("{\"componentKey\":\"minecraft:lore\",\"value\":[\"text\"]}")
                .put(componentPath + "/component")
                .then().statusCode(400);
    }

    @Test
    @DisplayName("keys which are not namespaced and missing values are rejected")
    void create_rejectsInvalidBodies() {
        given().contentType(ContentType.JSON)
                .body("{\"componentKey\":\"food\",\"value\":{}}")
                .put(componentPath + "/component")
                .then().statusCode(400);
        given().contentType(ContentType.JSON)
                .body("{\"componentKey\":\"minecraft:food\"}")
                .put(componentPath + "/component")
                .then().statusCode(400);
    }

    @Test
    @DisplayName("update changes the value and delete removes the component")
    void updateAndDelete() {
        String id = create(FOOD).getString("id");

        JsonPath updated = given().contentType(ContentType.JSON)
                .body("{\"id\":\"" + id + "\",\"componentKey\":\"minecraft:food\",\"value\":{\"nutrition\":8,\"saturation\":1.0,\"can_always_eat\":true}}")
                .post(componentPath + "/component")
                .then().statusCode(200)
                .extract().jsonPath();
        assertEquals(8, updated.getInt("value.nutrition"));
        assertTrue(updated.getBoolean("value.can_always_eat"));

        given().delete(componentPath + "/component/" + id).then().statusCode(200);
        assertTrue(componentRepository.findById(UUID.fromString(id)).isEmpty());
    }

    @Test
    @DisplayName("a component of another item can't be deleted through this item")
    void delete_rejectsComponentsOfOtherItems() {
        String id = create(FOOD).getString("id");
        String otherComponentPath = componentPath;
        createItem();
        given().delete(componentPath + "/component/" + id).then().statusCode(404);
        given().delete(otherComponentPath + "/component/" + id).then().statusCode(200);
    }
}
