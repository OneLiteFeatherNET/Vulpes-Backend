package net.onelitefeather.vulpes.backend.service.session;

import net.onelitefeather.vulpes.backend.domain.asset.AssetSavedEventDTO;
import net.onelitefeather.vulpes.backend.domain.session.LiveSessionEventDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@DisplayName("Live session sweeper")
class LiveSessionSweeperTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final String TOKEN = "abcdefghijklmnop";
    private static final String HOST = "designer-a";

    private final UUID asset = UUID.randomUUID();
    private MutableClock clock;
    private LiveSessionRegistry registry;
    private AssetEventHub hub;
    private LiveSessionSweeper sweeper;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-10-27T10:00:00Z"));
        registry = new LiveSessionRegistry(clock);
        hub = new AssetEventHub();
        sweeper = new LiveSessionSweeper(registry, hub);
    }

    @Test
    @DisplayName("a sweep after expiry announces the session as ended")
    void sweepAnnouncesEndedSessionAfterExpiry() {
        registry.claim(asset, TOKEN, HOST);

        StepVerifier.create(hub.subscribe(asset))
                .then(() -> {
                    clock.advance(Duration.ofSeconds(60));
                    sweeper.sweep();
                })
                .expectNext(new AssetEvent.Session(asset, LiveSessionEventDTO.ended(TOKEN, HOST)))
                .thenCancel()
                .verify(TIMEOUT);
    }

    @Test
    @DisplayName("a sweep while the session is active announces nothing")
    void sweepAnnouncesNothingWhileSessionIsActive() {
        registry.claim(asset, TOKEN, HOST);
        AssetEvent.Saved marker = new AssetEvent.Saved(asset, new AssetSavedEventDTO("\"abc\"", "abc", 1));

        StepVerifier.create(hub.subscribe(asset))
                .then(() -> {
                    clock.advance(Duration.ofSeconds(30));
                    sweeper.sweep();
                    hub.publish(marker);
                })
                .expectNext(marker)
                .thenCancel()
                .verify(TIMEOUT);
    }
}
