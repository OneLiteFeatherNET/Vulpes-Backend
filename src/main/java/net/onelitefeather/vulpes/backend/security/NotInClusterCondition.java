package net.onelitefeather.vulpes.backend.security;

import io.micronaut.context.condition.Condition;
import io.micronaut.context.condition.ConditionContext;

/**
 * Matches only when the process is not running inside a Kubernetes cluster.
 *
 * <p>Kubernetes injects {@code KUBERNETES_SERVICE_HOST} into every container it starts, so its
 * absence is a direct statement about where this process is. The framework's own deduced
 * {@code kubernetes} environment would be the more idiomatic check, but this application is built
 * with AOT's {@code deduceEnvironment} and {@code cacheEnvironment} both enabled, which makes it
 * fair to ask what was decided at build time versus at runtime. Reading the variable sidesteps the
 * question: it cannot be baked into an image.
 *
 * <p>This guards a security bypass, which is why it does not rely on the more convenient signal.
 *
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 3.1.0
 */
public class NotInClusterCondition implements Condition {

    /**
     * The variable Kubernetes sets in every container.
     */
    public static final String KUBERNETES_SERVICE_HOST = "KUBERNETES_SERVICE_HOST";

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matches(ConditionContext context) {
        return clusterMarker() == null;
    }

    /**
     * Reads the marker Kubernetes injects into every container.
     *
     * <p>Separated from {@link #matches} so a test can supply a value: {@code System.getenv} cannot
     * be set from within the JVM, and a guard on a security bypass should not be the one thing in
     * the codebase that goes unverified because it is inconvenient to reach.
     *
     * @return the value of the marker variable, or {@code null} when it is not set
     */
    protected String clusterMarker() {
        return System.getenv(KUBERNETES_SERVICE_HOST);
    }
}
