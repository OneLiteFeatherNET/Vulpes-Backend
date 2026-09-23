package net.onelitefeather.vulpes.backend.security;

import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Fails the application at startup when the resource-server configuration is incomplete.
 *
 * <p>Micronaut builds the JWKS source and the claims validators lazily, on the first request that
 * needs them. An unset issuer therefore does not stop the application from starting: it surfaces as
 * a 500 on <em>every</em> route, {@code /health} included. A pod in that state never becomes ready,
 * and the failure reads as a stuck rollout rather than as a missing environment variable.
 *
 * <p>Being a {@link Context} bean, this class is constructed while the application starts, so the
 * same mistake stops the process immediately and names what is missing.
 *
 * <p>The three properties are declared in {@code application.yml} with an empty fallback rather than
 * with none. Without the fallback the failure is Micronaut's own placeholder error, which names the
 * variable but not what it is for. The empty value never reaches the validators; this bean rejects
 * it first.
 *
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 3.1.0
 */
@Context
public class SecurityConfigurationCheck {

    /**
     * Constructs the check and rejects an incomplete configuration.
     *
     * @param issuer   the expected {@code iss} claim
     * @param audience the expected {@code aud} claim
     * @param jwksUrl  where the issuer publishes its signing keys
     * @throws ConfigurationException if any of them is missing or blank
     */
    public SecurityConfigurationCheck(
            @Property(name = "micronaut.security.token.jwt.claims-validators.issuer") String issuer,
            @Property(name = "micronaut.security.token.jwt.claims-validators.audience") String audience,
            @Property(name = "micronaut.security.token.jwt.signatures.jwks.provider.url") String jwksUrl
    ) {
        List<String> missing = new ArrayList<>();
        if (StringUtils.isEmpty(issuer)) {
            missing.add("OIDC_ISSUER (the exact 'iss' claim of the tokens this API accepts)");
        }
        if (StringUtils.isEmpty(audience)) {
            missing.add("OIDC_AUDIENCE (the 'aud' claim identifying this backend)");
        }
        if (StringUtils.isEmpty(jwksUrl)) {
            missing.add("OIDC_JWKS_URL (where the issuer publishes its signing keys)");
        }
        if (!missing.isEmpty()) {
            throw new ConfigurationException(
                    "The resource-server configuration is incomplete; refusing to start. Missing: "
                            + String.join(", ", missing)
                            + ". Without an audience check the API would accept every token the "
                            + "issuer ever minted for any application.");
        }
    }
}
