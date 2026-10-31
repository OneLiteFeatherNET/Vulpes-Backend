package net.onelitefeather.vulpes.backend.service.session;

import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.onelitefeather.vulpes.backend.domain.session.LiveSessionEventDTO;

/**
 * Announces the live sessions that expired. Expired sessions are already invisible to the API; the sweep only
 * tells the event streams, so that a participant learns the session ended without asking.
 */
@Singleton
public class LiveSessionSweeper {

    private final LiveSessionRegistry registry;
    private final AssetEventHub hub;

    /**
     * Creates the sweeper.
     *
     * @param registry the registry whose expired sessions are removed
     * @param hub      the hub that announces the expiry
     */
    @Inject
    public LiveSessionSweeper(LiveSessionRegistry registry, AssetEventHub hub) {
        this.registry = registry;
        this.hub = hub;
    }

    /**
     * Removes the expired sessions and sends a {@code session} event with state {@code ended} for each.
     */
    @Scheduled(fixedDelay = "5s")
    public void sweep() {
        for (LiveSessionRegistry.Expired expired : registry.expire()) {
            hub.publish(new AssetEvent.Session(
                    expired.assetId(),
                    LiveSessionEventDTO.ended(expired.session().token(), expired.session().host())
            ));
        }
    }
}
