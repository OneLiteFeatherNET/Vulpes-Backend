package net.onelitefeather.vulpes.backend.service.session;

import net.onelitefeather.vulpes.backend.domain.error.ErrorCode;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Live session registry")
class LiveSessionRegistryTest {

    private static final Instant START = Instant.parse("2026-10-27T10:00:00Z");
    private static final Duration TTL = Duration.ofSeconds(60);
    private static final String TOKEN = "abcdefghijklmnop";
    private static final String OTHER_TOKEN = "qrstuvwxyz012345";
    private static final String HOST = "designer-a";

    private final UUID asset = UUID.randomUUID();
    private MutableClock clock;
    private LiveSessionRegistry registry;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(START);
        registry = new LiveSessionRegistry(clock);
    }

    @Test
    @DisplayName("claiming an asset without a session creates one that expires after 60 seconds")
    void claimCreatesSessionWithSixtySecondTtl() {
        LiveSessionRegistry.Claim claim = registry.claim(asset, TOKEN, HOST);

        assertTrue(claim.created());
        assertEquals(START, claim.session().startedAt());
        assertEquals(START.plus(TTL), claim.session().expiresAt());
        assertEquals(TOKEN, claim.session().token());
        assertEquals(HOST, claim.session().host());
    }

    @Test
    @DisplayName("claiming with the token of the active session extends its expiry and keeps its start")
    void claimWithSameTokenRefreshesExpiry() {
        registry.claim(asset, TOKEN, HOST);
        clock.advance(Duration.ofSeconds(20));

        LiveSessionRegistry.Claim refreshed = registry.claim(asset, TOKEN, HOST);

        assertFalse(refreshed.created());
        assertEquals(START, refreshed.session().startedAt());
        assertEquals(START.plus(Duration.ofSeconds(20)).plus(TTL), refreshed.session().expiresAt());
    }

    @Test
    @DisplayName("claiming with another token while the session is active is a conflict")
    void claimWithOtherTokenWhileActiveConflicts() {
        registry.claim(asset, TOKEN, HOST);

        ApiException exception = assertThrows(ApiException.class,
                () -> registry.claim(asset, OTHER_TOKEN, "designer-b"));

        assertEquals(ErrorCode.RESOURCE_CONFLICT, exception.code());
        assertEquals(TOKEN, registry.active(asset).orElseThrow().token(),
                "the active session must stay unchanged after a refused claim");
    }

    @Test
    @DisplayName("claiming with another token replaces a session that has expired")
    void claimWithOtherTokenAfterExpiryReplacesSession() {
        registry.claim(asset, TOKEN, HOST);
        clock.advance(TTL);

        LiveSessionRegistry.Claim claim = registry.claim(asset, OTHER_TOKEN, "designer-b");

        assertTrue(claim.created());
        assertEquals(OTHER_TOKEN, claim.session().token());
    }

    @Test
    @DisplayName("a session is reported as active until its expiry")
    void sessionIsActiveUntilExpiry() {
        registry.claim(asset, TOKEN, HOST);
        clock.advance(TTL.minusSeconds(1));

        assertEquals(TOKEN, registry.active(asset).orElseThrow().token());
    }

    @Test
    @DisplayName("a session is not reported as active at its expiry")
    void sessionIsNotActiveAtExpiry() {
        registry.claim(asset, TOKEN, HOST);
        clock.advance(TTL);

        assertTrue(registry.active(asset).isEmpty());
    }

    @Test
    @DisplayName("releasing with the matching token removes the session and returns it")
    void releaseWithMatchingTokenRemovesSession() {
        registry.claim(asset, TOKEN, HOST);

        var released = registry.release(asset, TOKEN);

        assertEquals(TOKEN, released.orElseThrow().token());
        assertTrue(registry.active(asset).isEmpty());
    }

    @Test
    @DisplayName("releasing with another token while the session is active is a conflict")
    void releaseWithOtherTokenWhileActiveConflicts() {
        registry.claim(asset, TOKEN, HOST);

        ApiException exception = assertThrows(ApiException.class, () -> registry.release(asset, OTHER_TOKEN));

        assertEquals(ErrorCode.RESOURCE_CONFLICT, exception.code());
        assertEquals(TOKEN, registry.active(asset).orElseThrow().token());
    }

    @Test
    @DisplayName("releasing an asset without a session returns nothing and does not fail")
    void releaseWithoutSessionReturnsEmpty() {
        assertTrue(registry.release(asset, TOKEN).isEmpty());
    }

    @Test
    @DisplayName("expiry removes and returns only the sessions past their expiry")
    void expireRemovesOnlyExpiredSessions() {
        UUID other = UUID.randomUUID();
        registry.claim(asset, TOKEN, HOST);
        clock.advance(Duration.ofSeconds(30));
        registry.claim(other, OTHER_TOKEN, "designer-b");

        clock.advance(Duration.ofSeconds(30));
        List<LiveSessionRegistry.Expired> expired = registry.expire();

        assertEquals(1, expired.size());
        assertEquals(asset, expired.getFirst().assetId());
        assertEquals(TOKEN, expired.getFirst().session().token());
        assertTrue(registry.active(asset).isEmpty());
        assertEquals(OTHER_TOKEN, registry.active(other).orElseThrow().token());
    }
}
