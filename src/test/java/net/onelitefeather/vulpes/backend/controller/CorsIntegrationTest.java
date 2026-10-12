package net.onelitefeather.vulpes.backend.controller;

import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

/**
 * The preflight requests a browser sends before calling the backend from the UI on another port.
 * The lore reorder is the one {@code PATCH} the UI makes, so it is the request that needs it allowed.
 */
@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("CORS preflight for the UI")
@EnabledIfDockerAvailable
class CorsIntegrationTest {

    @Inject
    EmbeddedServer server;

    private final String reorderPath = "/item/" + UUID.randomUUID() + "/lore/reorder";

    @BeforeAll
    void setUp() {
        RestAssured.baseURI = server.getURL().toString();
    }

    @ParameterizedTest
    @ValueSource(strings = {"http://localhost:54321", "http://127.0.0.1:54321"})
    @DisplayName("allows a PATCH from a local UI")
    void allowsPatchFromLocalOrigins(String origin) {
        given()
                .header("Origin", origin)
                .header("Access-Control-Request-Method", "PATCH")
                .header("Access-Control-Request-Headers", "content-type")
        .when()
                .options(reorderPath)
        .then()
                .statusCode(200)
                .header("Access-Control-Allow-Origin", is(origin))
                .header("Access-Control-Allow-Methods", containsString("PATCH"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"https://evil.example", "http://localhost.evil.example"})
    @DisplayName("rejects other origins")
    void rejectsOtherOrigins(String origin) {
        given()
                .header("Origin", origin)
                .header("Access-Control-Request-Method", "PATCH")
        .when()
                .options(reorderPath)
        .then()
                .header("Access-Control-Allow-Origin", nullValue());
    }
}
