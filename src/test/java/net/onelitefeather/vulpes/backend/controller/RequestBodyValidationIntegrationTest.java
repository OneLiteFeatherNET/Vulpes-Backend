package net.onelitefeather.vulpes.backend.controller;

import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import net.onelitefeather.vulpes.backend.domain.item.ItemEnchantmentDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemLoreDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemLoreReorderDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

import java.util.UUID;

import static io.restassured.RestAssured.given;

/**
 * Checks that the sub resource endpoints validate their request body. Validation runs before the
 * parent lookup, so an unknown parent id still answers 400 instead of 404.
 */
@MicronautTest
@DisplayName("Request body validation of the sub resource endpoints")
@EnabledIfDockerAvailable
class RequestBodyValidationIntegrationTest {

    @Inject
    EmbeddedServer server;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = server.getURL().toString();
    }

    private static void assertBadRequest(String method, String path, Object body) {
        given()
                .contentType(ContentType.JSON)
                .body(body)
        .when()
                .request(method, path)
        .then()
                .statusCode(400);
    }

    @Test
    void testCreateEnchantment_withZeroLevel_returnsBadRequest() {
        ItemEnchantmentDTO enchantment = new ItemEnchantmentDTO(null, "minecraft:sharpness", (short) 0, false);
        assertBadRequest("PUT", "/item/" + UUID.randomUUID() + "/enchantment", enchantment);
    }

    @Test
    void testUpdateEnchantment_withZeroLevel_returnsBadRequest() {
        ItemEnchantmentDTO enchantment = new ItemEnchantmentDTO(UUID.randomUUID(), "minecraft:sharpness", (short) 0, false);
        assertBadRequest("POST", "/item/" + UUID.randomUUID() + "/enchantment", enchantment);
    }

    @Test
    void testCreateLore_withBlankText_returnsBadRequest() {
        assertBadRequest("PUT", "/item/" + UUID.randomUUID() + "/lore", new ItemLoreDTO(null, " "));
    }

    @Test
    void testUpdateLore_withBlankText_returnsBadRequest() {
        assertBadRequest("POST", "/item/" + UUID.randomUUID() + "/lore", new ItemLoreDTO(UUID.randomUUID(), ""));
    }

    @Test
    void testReorderLore_withNegativeIndex_returnsBadRequest() {
        ItemLoreReorderDTO reorder = new ItemLoreReorderDTO(UUID.randomUUID(), -1);
        assertBadRequest("PATCH", "/item/" + UUID.randomUUID() + "/lore/reorder", reorder);
    }
}
