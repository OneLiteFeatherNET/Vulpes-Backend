package net.onelitefeather.vulpes.backend.security;

import com.nimbusds.jwt.JWTParser;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.security.authentication.Authentication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.text.ParseException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the two pieces of logic that decide who a request belongs to.
 *
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 3.1.0
 */
@DisplayName("Identity derivation")
class IdentityClaimTest {

    /**
     * Builds the factory for a given claim name.
     *
     * @param claim the claim to identify callers by
     * @return the factory
     */
    private ClaimIdentityJwtAuthenticationFactory factoryFor(String claim) {
        SecurityProperties properties = new SecurityProperties();
        properties.setIdentityClaim(claim);
        return new ClaimIdentityJwtAuthenticationFactory(properties);
    }

    /**
     * Parses a token the test suite minted.
     *
     * @param token the encoded token
     * @return the authentication derived from it, if any
     * @throws ParseException never, for tokens this suite produces
     */
    private Optional<Authentication> authenticate(ClaimIdentityJwtAuthenticationFactory factory, String token)
            throws ParseException {
        return factory.createAuthentication(JWTParser.parse(token));
    }

    @Test
    @DisplayName("reads 'oid' when configured to, ignoring 'sub'")
    void readsConfiguredClaim() throws ParseException {
        String token = TestTokens.token(claims -> claims
                .subject("the-pairwise-sub")
                .claim("oid", "the-stable-oid"));

        assertEquals("the-stable-oid", authenticate(factoryFor("oid"), token).orElseThrow().getName());
    }

    @Test
    @DisplayName("reads 'sub' when configured to, ignoring 'oid'")
    void readsPortableDefault() throws ParseException {
        // The same token, a different deployment. Nothing but configuration changes -- which is
        // what keeps Keycloak a configuration change rather than a code change.
        String token = TestTokens.token(claims -> claims
                .subject("the-pairwise-sub")
                .claim("oid", "the-stable-oid"));

        assertEquals("the-pairwise-sub", authenticate(factoryFor("sub"), token).orElseThrow().getName());
        assertEquals(SecurityProperties.DEFAULT_IDENTITY_CLAIM, new SecurityProperties().getIdentityClaim());
    }

    @Test
    @DisplayName("refuses a token lacking the configured claim rather than substituting one")
    void refusesWhenClaimAbsent() throws ParseException {
        String token = TestTokens.token(claims -> claims.subject("a-perfectly-good-sub").claim("oid", null));
        assertTrue(authenticate(factoryFor("oid"), token).isEmpty());
    }

    @Test
    @DisplayName("the fallback answers only when no Authorization header was sent")
    void fallbackOnlyWithoutHeader() {
        LocalDevelopmentAuthenticationFetcher fetcher = new LocalDevelopmentAuthenticationFetcher();

        MutableHttpRequest<?> withHeader = HttpRequest.GET("/project")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TestTokens.badSignature());
        assertFalse(
                Flux.from(fetcher.fetchAuthentication(withHeader)).blockFirst() != null,
                "a token that fails validation must be refused, never replaced by the fallback");

        Authentication withoutHeader =
                Flux.from(fetcher.fetchAuthentication(HttpRequest.GET("/project"))).blockFirst();
        assertEquals(LocalDevelopmentAuthenticationFetcher.DEVELOPMENT_SUBJECT, withoutHeader.getName());
    }
}
