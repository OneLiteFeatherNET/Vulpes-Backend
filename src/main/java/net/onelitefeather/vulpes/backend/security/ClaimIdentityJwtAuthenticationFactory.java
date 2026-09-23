package net.onelitefeather.vulpes.backend.security;

import com.nimbusds.jwt.JWT;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.token.jwt.validator.DefaultJwtAuthenticationFactory;
import io.micronaut.security.token.jwt.validator.JwtAuthenticationFactory;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Map;
import java.util.Optional;

/**
 * Builds the {@link Authentication} for a validated token, naming the caller by the configured
 * identity claim rather than always by {@code sub}.
 *
 * <p>The framework's own factory uses {@code sub} unconditionally. That is the portable choice and
 * the wrong one here: Entra ID issues a pairwise {@code sub}, unique per app registration, so the
 * value changes for every caller whenever that registration is replaced -- which is exactly what
 * introducing this backend to the tenant does. {@code oid} is stable across registrations. Keycloak
 * has no {@code oid} and uses {@code sub}. Which claim applies is therefore deployment
 * configuration.
 *
 * <p>A token that validates but lacks the configured claim produces no authentication at all, and
 * the request is rejected. Substituting a different claim, or an empty name, would let a caller be
 * recorded as somebody the token never identified.
 *
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 3.1.0
 */
@Singleton
@Replaces(DefaultJwtAuthenticationFactory.class)
public class ClaimIdentityJwtAuthenticationFactory implements JwtAuthenticationFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ClaimIdentityJwtAuthenticationFactory.class);

    private final SecurityProperties securityProperties;

    /**
     * Constructs the factory.
     *
     * @param securityProperties supplies the claim the caller is identified by
     */
    public ClaimIdentityJwtAuthenticationFactory(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Authentication> createAuthentication(JWT token) {
        Map<String, Object> claims;
        try {
            claims = token.getJWTClaimsSet().getClaims();
        } catch (ParseException e) {
            // The signature and the registered claims were already checked; a set that cannot be
            // read at this point is malformed beyond anything worth reporting to the caller.
            LOG.debug("Rejecting a token whose claims could not be parsed", e);
            return Optional.empty();
        }

        String claimName = securityProperties.getIdentityClaim();
        Object identity = claims.get(claimName);
        if (!(identity instanceof String subject) || subject.isBlank()) {
            LOG.warn(
                    "Rejecting a token without a usable '{}' claim. Either the identity provider "
                            + "does not issue it, or vulpes.security.identity-claim names the wrong claim.",
                    claimName);
            return Optional.empty();
        }

        return Optional.of(Authentication.build(subject, claims));
    }
}
