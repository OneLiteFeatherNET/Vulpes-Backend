package net.onelitefeather.vulpes.backend.security;

/**
 * Who made a request, as this backend will record it.
 *
 * <p>The subject alone is not enough to persist. Subject values are only unique within the issuer
 * that minted them, and this deployment's issuer is expected to change at least once in the life of
 * the data: an Entra ID tenant today, possibly a Keycloak realm later. Rows written under the old
 * issuer and rows written under the new one would otherwise share a column while meaning different
 * things, with nothing left to tell them apart.
 *
 * <p>Nothing persists this yet. It exists so that the step which adds {@code createdBy} and
 * {@code modifiedBy} to the model finds an identity already shaped for storage.
 *
 * @param issuer  the {@code iss} claim of the token that carried this identity
 * @param subject the value of the configured identity claim
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 3.1.0
 */
public record CallerIdentity(String issuer, String subject) {
}
