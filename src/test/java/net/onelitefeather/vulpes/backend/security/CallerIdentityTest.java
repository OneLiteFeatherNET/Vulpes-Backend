package net.onelitefeather.vulpes.backend.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the identity that later steps will persist.
 *
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 3.1.0
 */
@DisplayName("Caller identity")
class CallerIdentityTest {

    @Test
    @DisplayName("keeps the issuer alongside the subject")
    void carriesBoth() {
        CallerIdentity identity = new CallerIdentity("https://issuer.test.invalid/realms/vulpes", "abc");
        assertEquals("https://issuer.test.invalid/realms/vulpes", identity.issuer());
        assertEquals("abc", identity.subject());
    }

    @Test
    @DisplayName("the same subject from two issuers is two identities")
    void subjectIsScopedToItsIssuer() {
        // This is why the issuer is stored. Subject values are unique only within the issuer that
        // minted them, and this deployment is expected to change issuer at least once.
        CallerIdentity fromOne = new CallerIdentity("https://one.invalid", "3f5a9c21");
        CallerIdentity fromAnother = new CallerIdentity("https://another.invalid", "3f5a9c21");
        assertNotEquals(fromOne, fromAnother);
    }

    @Test
    @DisplayName("the development identity is recognisable as synthetic")
    void developmentIdentityIsObvious() {
        // A developer running locally writes rows stamped with this once createdBy exists. It has
        // to be identifiable at a glance rather than pass for a real user id.
        String subject = LocalDevelopmentAuthenticationFetcher.DEVELOPMENT_SUBJECT;
        assertTrue(subject.contains("local-development"), "does not announce itself: " + subject);
        assertTrue(
                LocalDevelopmentAuthenticationFetcher.DEVELOPMENT_ISSUER.startsWith("vulpes:"),
                "the issuer should not look like a real one");
    }
}
