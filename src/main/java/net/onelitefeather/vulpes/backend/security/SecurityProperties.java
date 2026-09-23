package net.onelitefeather.vulpes.backend.security;

import io.micronaut.context.annotation.ConfigurationProperties;

/**
 * Configuration this backend adds on top of the framework's own security settings.
 *
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 3.1.0
 */
@ConfigurationProperties(SecurityProperties.PREFIX)
public class SecurityProperties {

    /**
     * The configuration prefix.
     */
    public static final String PREFIX = "vulpes.security";

    /**
     * The portable default: every OpenID Connect provider issues {@code sub}.
     */
    public static final String DEFAULT_IDENTITY_CLAIM = "sub";

    private String identityClaim = DEFAULT_IDENTITY_CLAIM;

    /**
     * Returns the claim the caller's identity is read from.
     *
     * <p>{@code sub} is the portable choice but not always the right one. Entra ID issues a pairwise
     * {@code sub} that is unique per app registration, so replacing that registration renumbers
     * every caller, while its {@code oid} stays stable across registrations. Keycloak has no
     * {@code oid} and uses {@code sub}. Which one applies is deployment configuration, not a
     * constant.
     *
     * @return the claim name
     */
    public String getIdentityClaim() {
        return identityClaim;
    }

    /**
     * Sets the claim the caller's identity is read from.
     *
     * @param identityClaim the claim name
     */
    public void setIdentityClaim(String identityClaim) {
        this.identityClaim = identityClaim;
    }
}
