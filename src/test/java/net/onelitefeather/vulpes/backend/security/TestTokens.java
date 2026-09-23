package net.onelitefeather.vulpes.backend.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.time.Instant;
import java.util.Date;
import java.util.function.Consumer;

/**
 * Mints bearer tokens for tests.
 *
 * <p>Signed with a symmetric secret that the test environment configures alongside the remote JWKS
 * source. That lets the suite drive the real filter chain -- the same validators, the same
 * rejection path -- without a network call or a running identity provider, and makes the negative
 * cases expressible: a token can be given the wrong audience, the wrong issuer or an expiry in the
 * past and still be a properly signed token, which is exactly what the rejection matrix needs.
 *
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 3.1.0
 */
public final class TestTokens {

    /**
     * The shared secret, matching {@code src/test/resources/application.yml}. Long enough for HS256.
     */
    public static final String SECRET = "test-only-secret-never-used-outside-the-test-suite";

    /**
     * The issuer the test environment expects.
     */
    public static final String ISSUER = "https://issuer.test.invalid/realms/vulpes";

    /**
     * The audience the test environment expects.
     */
    public static final String AUDIENCE = "vulpes-backend-test";

    /**
     * The identity claim the test environment reads, matching the Entra deployment rather than the
     * portable default, so the configurable path is the one under test.
     */
    public static final String IDENTITY_CLAIM = "oid";

    /**
     * A caller id used by the accepting cases.
     */
    public static final String SUBJECT = "00000000-0000-4000-8000-00000000c0de";

    private TestTokens() {
    }

    /**
     * Returns a token that meets every acceptance criterion.
     *
     * @return the encoded token
     */
    public static String acceptable() {
        return token(claims -> {
        });
    }

    /**
     * Returns a token built from the acceptable one with the given modifications applied.
     *
     * @param customiser adjusts the claims before signing
     * @return the encoded token
     */
    public static String token(Consumer<JWTClaimsSet.Builder> customiser) {
        Instant now = Instant.now();
        JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder()
                .issuer(ISSUER)
                .audience(AUDIENCE)
                .subject(SUBJECT)
                .claim(IDENTITY_CLAIM, SUBJECT)
                .issueTime(Date.from(now))
                .notBeforeTime(Date.from(now.minusSeconds(5)))
                .expirationTime(Date.from(now.plusSeconds(300)));
        customiser.accept(claims);
        return sign(claims.build());
    }

    /**
     * Signs a claims set with the test secret.
     *
     * @param claims the claims to sign
     * @return the encoded token
     */
    private static String sign(JWTClaimsSet claims) {
        try {
            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            jwt.sign(new MACSigner(SECRET.getBytes()));
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("Could not sign a test token", e);
        }
    }

    /**
     * Returns a token whose signature does not verify.
     *
     * @return the encoded token with a corrupted signature
     */
    public static String badSignature() {
        String[] parts = acceptable().split("\\.");
        return parts[0] + "." + parts[1] + ".not-the-right-signature";
    }
}
