package net.onelitefeather.vulpes.backend.security;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.EnabledIfDockerAvailable;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Checks that the caller reaches code below the HTTP layer.
 *
 * <p>This is what the next step consumes: the service layer performs the writes that will carry
 * {@code createdBy} and {@code modifiedBy}, and it takes no request parameter. The endpoint here
 * stands in for that layer -- it asks the resolver, exactly as a service would.
 *
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 3.1.0
 */
@MicronautTest(environments = "identity-spec")
@EnabledIfDockerAvailable
@DisplayName("Caller identity below the HTTP layer")
class CallerIdentityResolverIntegrationTest {

    @Inject
    @Client("/")
    HttpClient client;

    /**
     * Stands in for the service layer, which likewise receives no request.
     */
    @Requires(env = "identity-spec")
    @Secured(SecurityRule.IS_AUTHENTICATED)
    @Controller("/test-only/caller")
    static class CallerEchoController {

        private final CallerIdentityResolver resolver;

        CallerEchoController(CallerIdentityResolver resolver) {
            this.resolver = resolver;
        }

        @Get
        String caller() {
            return resolver.currentCaller()
                    .map(identity -> identity.issuer() + " " + identity.subject())
                    .orElse("none");
        }
    }

    @Test
    @DisplayName("the identity claim and the issuer both arrive")
    void identityReachesTheServiceLayer() {
        MutableHttpRequest<?> request = HttpRequest.GET("/test-only/caller")
                .header("Authorization", "Bearer " + TestTokens.acceptable());

        String body = client.toBlocking().retrieve(request);

        // The subject is read from 'oid', not from 'sub' -- both carry the same value in an
        // acceptable test token, so the claim actually under test is pinned by the refusal case
        // in ApiAuthenticationIntegrationTest, where only 'oid' is removed.
        assertEquals(TestTokens.ISSUER + " " + TestTokens.SUBJECT, body);
    }
}
