package net.onelitefeather.vulpes.backend.security;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Guards the endpoints that have to answer without credentials.
 *
 * <p>Probes and the metrics scrape call these anonymously. If they were to receive 401, a pod would
 * never become ready and the deployment would read as a stuck rollout rather than as an
 * authentication problem, which is a slow and confusing way to find out.
 *
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 3.1.0
 */
@MicronautTest
@EnabledIfDockerAvailable
@DisplayName("Endpoints reachable without a token")
class OpenEndpointsIntegrationTest {

    @Inject
    @Client("/")
    HttpClient client;

    @ParameterizedTest(name = "{0} answers without a token")
    @ValueSource(strings = {"/health", "/prometheus"})
    void operationalEndpointsAreOpen(String path) {
        HttpStatus status = client.toBlocking().exchange(HttpRequest.GET(path), String.class).getStatus();
        assertEquals(HttpStatus.OK, status);
    }

    @Test
    @DisplayName("cross-origin preflight is answered without a token")
    void preflightIsNotAuthenticated() {
        // Browsers never attach credentials to a preflight. Answered with 401 it surfaces as an
        // opaque CORS failure, hiding the real cause from whoever is debugging it.
        MutableHttpRequest<?> preflight = HttpRequest.OPTIONS("/project")
                .header("Origin", "http://localhost:18080")
                .header("Access-Control-Request-Method", "GET");

        HttpStatus status = client.toBlocking().exchange(preflight, String.class).getStatus();
        assertEquals(HttpStatus.OK, status);
    }
}
