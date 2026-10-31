package net.onelitefeather.vulpes.backend.controller;

import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.RestAssured;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
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

    private static final String UI_ORIGIN = "http://localhost:8092";

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

    @Test
    @DisplayName("allows a POST preflight to the asset upload from the local UI")
    void allowsPostPreflightForAssetUpload() {
        given()
                .header("Origin", UI_ORIGIN)
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "authorization,content-type")
        .when()
                .options("/project/" + UUID.randomUUID() + "/asset")
        .then()
                .statusCode(allOf(greaterThanOrEqualTo(200), lessThan(300)))
                .header("Access-Control-Allow-Origin", is(UI_ORIGIN))
                .header("Access-Control-Allow-Methods", containsString("POST"));
    }

    @Test
    @DisplayName("exposes the ETag header of asset content to the local UI")
    void exposesEtagOfAssetContent() {
        given()
                .header("Origin", UI_ORIGIN)
        .when()
                .get("/project/" + UUID.randomUUID() + "/asset/" + UUID.randomUUID() + "/content")
        .then()
                .header("Access-Control-Expose-Headers", containsString("ETag"));
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
