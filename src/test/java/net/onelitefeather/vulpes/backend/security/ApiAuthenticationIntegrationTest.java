package net.onelitefeather.vulpes.backend.security;

import com.nimbusds.jwt.JWTClaimsSet;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import net.onelitefeather.vulpes.backend.domain.error.ErrorCode;
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drives the real filter chain over HTTP for every way a request can be accepted or refused.
 *
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 3.1.0
 */
@MicronautTest
@EnabledIfDockerAvailable
@DisplayName("API authentication")
class ApiAuthenticationIntegrationTest {

    private static final String SECURED_PATH = "/project";

    @Inject
    @Client("/")
    HttpClient client;

    /**
     * Sends a request to a secured endpoint.
     *
     * @param authorization the header value, or {@code null} to send none
     * @return the status the server answered with
     */
    private HttpClientResponseException refusalFor(String authorization) {
        MutableHttpRequest<?> request = HttpRequest.GET(SECURED_PATH);
        if (authorization != null) {
            request.header("Authorization", authorization);
        }
        HttpRequest<?> finalRequest = request;
        return assertThrows(
                HttpClientResponseException.class,
                () -> client.toBlocking().exchange(finalRequest, String.class));
    }

    @org.junit.jupiter.params.ParameterizedTest(name = "{0} is closed to an unauthenticated caller")
    @org.junit.jupiter.params.provider.ValueSource(strings = {
            "/project", "/attribute", "/notification", "/font", "/item", "/sound", "/dimension"
    })
    void everyControllerIsSecured(String path) {
        // One representative route per controller family. A controller added later without an
        // annotation is still refused by default, but that default is not what this asserts.
        HttpRequest<?> request = HttpRequest.GET(path);
        HttpClientResponseException refusal = assertThrows(
                HttpClientResponseException.class,
                () -> client.toBlocking().exchange(request, String.class));
        assertEquals(HttpStatus.UNAUTHORIZED, refusal.getStatus());
    }

    @Test
    @DisplayName("a request without a token is refused")
    void noToken() {
        assertEquals(HttpStatus.UNAUTHORIZED, refusalFor(null).getStatus());
    }

    @Test
    @DisplayName("an acceptable token is let through")
    void acceptableToken() {
        MutableHttpRequest<?> request = HttpRequest.GET(SECURED_PATH)
                .header("Authorization", "Bearer " + TestTokens.acceptable());
        assertEquals(HttpStatus.OK, client.toBlocking().exchange(request, String.class).getStatus());
    }

    @Test
    @DisplayName("a token issued for a different audience is refused")
    void wrongAudience() {
        String token = TestTokens.token(claims -> claims.audience("some-other-application"));
        assertEquals(HttpStatus.UNAUTHORIZED, refusalFor("Bearer " + token).getStatus());
    }

    @Test
    @DisplayName("a token from an unconfigured issuer is refused")
    void wrongIssuer() {
        String token = TestTokens.token(claims -> claims.issuer("https://elsewhere.invalid/realms/other"));
        assertEquals(HttpStatus.UNAUTHORIZED, refusalFor("Bearer " + token).getStatus());
    }

    @Test
    @DisplayName("an expired token is refused")
    void expired() {
        String token = TestTokens.token(claims -> claims
                .expirationTime(Date.from(Instant.now().minusSeconds(60)))
                .issueTime(Date.from(Instant.now().minusSeconds(3600))));
        assertEquals(HttpStatus.UNAUTHORIZED, refusalFor("Bearer " + token).getStatus());
    }

    @Test
    @DisplayName("a token whose signature does not verify is refused")
    void badSignature() {
        assertEquals(HttpStatus.UNAUTHORIZED, refusalFor("Bearer " + TestTokens.badSignature()).getStatus());
    }

    @Test
    @DisplayName("a token without the configured identity claim is refused")
    void missingIdentityClaim() {
        // Valid in every other respect, and carrying a 'sub' -- which is precisely the value that
        // must not be silently substituted for the claim this deployment reads.
        String token = TestTokens.token(claims -> claims.claim(TestTokens.IDENTITY_CLAIM, null));
        assertEquals(HttpStatus.UNAUTHORIZED, refusalFor("Bearer " + token).getStatus());
    }

    @Test
    @DisplayName("an Authorization header that is not a bearer token is refused")
    void notABearerToken() {
        assertEquals(HttpStatus.UNAUTHORIZED, refusalFor("Basic dXNlcjpwYXNzd29yZA==").getStatus());
    }

    @Test
    @DisplayName("a refusal is reported in the project's problem format")
    void refusalBody() {
        HttpClientResponseException refusal = refusalFor(null);
        assertEquals(
                MediaType.APPLICATION_JSON_PROBLEM_TYPE,
                refusal.getResponse().getContentType().orElseThrow());

        ProblemDetail problem = refusal.getResponse().getBody(ProblemDetail.class).orElseThrow();
        assertEquals(ErrorCode.UNAUTHENTICATED, problem.code());
        assertEquals(HttpStatus.UNAUTHORIZED.getCode(), problem.status());
        assertNotNull(problem.traceId());
        assertEquals(SECURED_PATH, problem.instance());
    }

    @Test
    @DisplayName("a refusal never says which criterion failed")
    void refusalRevealsNothing() {
        // The bodies of an expired token, a wrong audience and a broken signature have to be
        // indistinguishable. A caller able to tell them apart can probe how close a stale or
        // forged token is to being accepted.
        String expired = TestTokens.token(claims -> claims
                .expirationTime(Date.from(Instant.now().minusSeconds(60)))
                .issueTime(Date.from(Instant.now().minusSeconds(3600))));
        String wrongAudience = TestTokens.token(claims -> claims.audience("some-other-application"));

        List<String> details = List.of(
                detailOf(refusalFor("Bearer " + expired)),
                detailOf(refusalFor("Bearer " + wrongAudience)),
                detailOf(refusalFor("Bearer " + TestTokens.badSignature())),
                detailOf(refusalFor(null)));

        assertEquals(1, details.stream().distinct().count(), "refusal details differ: " + details);
        String detail = details.getFirst();
        assertTrue(detail != null && !detail.isBlank(), "a refusal still has to say something");
    }

    /**
     * Extracts the detail text from a refusal.
     *
     * @param refusal the refusal
     * @return the detail
     */
    private String detailOf(HttpClientResponseException refusal) {
        return refusal.getResponse().getBody(ProblemDetail.class).orElseThrow().detail();
    }
}
