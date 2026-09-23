package net.onelitefeather.vulpes.backend.security;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.condition.ConditionContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins down when the unauthenticated fallback exists and when it must not.
 *
 * <p>The fallback is the one piece of this change that lets a request through without a token.
 * Every condition guarding it is checked here, because each is a way the API could end up open
 * somewhere it should not be -- and two of them cannot be observed from an ordinary test run:
 * the suite always has the {@code test} environment active, and it never runs in a cluster.
 *
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 3.1.0
 */
@DisplayName("Conditions on the development fallback")
class LocalDevelopmentFallbackConditionsTest {

    /**
     * Enough configuration for the context to start; none of it is exercised.
     */
    private static final Map<String, Object> MINIMAL_CONFIG = Map.of(
            "micronaut.security.token.jwt.claims-validators.issuer", "https://issuer.test.invalid/realms/vulpes",
            "micronaut.security.token.jwt.claims-validators.audience", "vulpes-backend-test",
            "micronaut.security.token.jwt.signatures.jwks.provider.url", "http://jwks.unused.invalid/keys",
            // Hibernate owns schema creation here, as everywhere else. Contexts started without
            // the test environment would otherwise inherit CREATE_DROP from application.yml and
            // fail on an entity Micronaut Data's generator cannot map.
            "datasources.default.schema-generate", "NONE"
    );

    /**
     * Starts a context with the given environments and no deduction, so the environments under test
     * are exactly the ones named.
     *
     * @param environments the environments to activate
     * @return the started context
     */
    private ApplicationContext contextWith(String... environments) {
        return ApplicationContext.builder()
                .deduceEnvironment(false)
                .environments(environments)
                .properties(MINIMAL_CONFIG)
                .start();
    }

    @Test
    @DisplayName("exists in the local environment")
    void presentLocally() {
        try (ApplicationContext context = contextWith("local")) {
            assertTrue(
                    context.containsBean(LocalDevelopmentAuthenticationFetcher.class),
                    "local development would otherwise require a token for every call");
        }
    }

    @Test
    @DisplayName("is absent without the local environment")
    void absentByDefault() {
        try (ApplicationContext context = contextWith("prod")) {
            assertFalse(
                    context.containsBean(LocalDevelopmentAuthenticationFetcher.class),
                    "a deployed instance must never accept an unauthenticated request");
        }
    }

    @Test
    @DisplayName("is absent in the test environment even when local is active")
    void absentUnderTest() {
        // Without this, every test asserting a 401 would pass while proving nothing: the fallback
        // would answer the unauthenticated request instead of the rejection path.
        try (ApplicationContext context = contextWith("local", "test")) {
            assertFalse(
                    context.containsBean(LocalDevelopmentAuthenticationFetcher.class),
                    "the suite's 401 assertions would be meaningless");
        }
    }

    @Test
    @DisplayName("the cluster marker alone keeps it out")
    void clusterMarkerBlocksIt() {
        // The Helm chart derives MICRONAUT_ENVIRONMENTS from a values list, so naming 'local' in an
        // overlay is a plausible mistake. This is the condition that makes such a mistake inert.
        // System.getenv cannot be set from inside the JVM, hence the seam.
        NotInClusterCondition inCluster = new NotInClusterCondition() {
            @Override
            protected String clusterMarker() {
                return "10.96.0.1";
            }
        };
        NotInClusterCondition outsideCluster = new NotInClusterCondition() {
            @Override
            protected String clusterMarker() {
                return null;
            }
        };

        ConditionContext<?> ignored = null;
        assertFalse(inCluster.matches(ignored), "a pod must not get the fallback");
        assertTrue(outsideCluster.matches(ignored), "a developer machine must get it");
    }
}
