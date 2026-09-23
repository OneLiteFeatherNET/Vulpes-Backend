package net.onelitefeather.vulpes.backend.security;

import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.filters.AuthenticationFetcher;
import jakarta.annotation.PostConstruct;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Lets a developer call the API without a token, and only a developer.
 *
 * <p>Local work has to be possible in three modes: with no token, with one from a local identity
 * provider, and with a real one from the deployed provider. Disabling security for the {@code local}
 * environment would satisfy the first and destroy the other two, since a filter that does not run
 * validates nothing -- a token rejected in production would appear to work locally.
 *
 * <p>So security stays on and this contributes an identity in exactly one situation: <strong>the
 * request carries no {@code Authorization} header at all</strong>. A header that is present but
 * unacceptable is left to fail, which is what makes the "with a token" modes worth running.
 *
 * <p>Three independent conditions must hold for this bean to exist: the {@code local} environment is
 * active, the {@code test} environment is not, and the process is not in a cluster. The middle one
 * matters because the test suite asserts 401s; if this were active there, those tests would pass
 * while proving nothing. The last one matters because the Helm chart derives
 * {@code MICRONAUT_ENVIRONMENTS} from a values list, so naming {@code local} in an overlay by
 * mistake is a plausible accident -- and an inert one, because of {@link NotInClusterCondition}.
 *
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 3.1.0
 */
@Context
@Requires(env = LocalDevelopmentAuthenticationFetcher.LOCAL_ENVIRONMENT)
@Requires(notEnv = Environment.TEST)
@Requires(condition = NotInClusterCondition.class)
public class LocalDevelopmentAuthenticationFetcher implements AuthenticationFetcher<HttpRequest<?>> {

    /**
     * The environment this project uses for local runs, set explicitly and never deduced.
     */
    public static final String LOCAL_ENVIRONMENT = "local";

    /**
     * The subject recorded for an unauthenticated local call.
     *
     * <p>Deliberately not shaped like anything an identity provider would mint. A developer running
     * locally will write rows stamped with this once {@code createdBy} exists, and such a row has to
     * be recognisable as synthetic at a glance rather than pass for a real user id.
     */
    public static final String DEVELOPMENT_SUBJECT = "local-development-unauthenticated";

    /**
     * The issuer recorded alongside it, which is no issuer at all.
     */
    public static final String DEVELOPMENT_ISSUER = "vulpes:local-development";

    private static final Logger LOG = LoggerFactory.getLogger(LocalDevelopmentAuthenticationFetcher.class);
    private static final String ISSUER_CLAIM = "iss";

    /**
     * Announces that this instance accepts unauthenticated requests.
     *
     * <p>A silent bypass is the kind of switch that gets found months later in the wrong place. The
     * bean is a {@link Context} bean rather than a plain singleton so that this runs while the
     * application starts: as a lazy singleton it was first constructed by the first request that
     * needed it, which put the warning after the startup banner and after the bypass had already
     * been used once.
     */
    @PostConstruct
    void announce() {
        LOG.warn(
                "Local development mode: requests without an Authorization header are accepted and "
                        + "attributed to '{}'. A request that does carry a token is still validated "
                        + "normally and rejected when it fails.",
                DEVELOPMENT_SUBJECT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Publisher<Authentication> fetchAuthentication(HttpRequest<?> request) {
        if (request.getHeaders().contains(HttpHeaders.AUTHORIZATION)) {
            // Present but unacceptable is a rejection, never a fallback.
            return Publishers.empty();
        }
        if (System.getenv(NotInClusterCondition.KUBERNETES_SERVICE_HOST) != null) {
            // The bean condition already covers this. Checked again because the cost is one map
            // lookup and the failure mode is an open API in a cluster.
            return Publishers.empty();
        }
        return Publishers.just(
                Authentication.build(DEVELOPMENT_SUBJECT, Map.of(ISSUER_CLAIM, DEVELOPMENT_ISSUER)));
    }

    /**
     * {@inheritDoc}
     *
     * <p>Lowest precedence, so every real authentication path is consulted first.
     */
    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
