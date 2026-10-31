package net.onelitefeather.vulpes.backend.service.impl;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.onelitefeather.vulpes.backend.domain.session.LiveSessionEventDTO;
import net.onelitefeather.vulpes.backend.domain.session.LiveSessionRequestDTO;
import net.onelitefeather.vulpes.backend.domain.session.LiveSessionResponseDTO;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.service.AssetService;
import net.onelitefeather.vulpes.backend.service.LiveSessionService;
import net.onelitefeather.vulpes.backend.service.session.AssetEvent;
import net.onelitefeather.vulpes.backend.service.session.AssetEventHub;
import net.onelitefeather.vulpes.backend.service.session.LiveSessionRegistry;
import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * Implementation of the {@link LiveSessionService} interface.
 *
 * <p>Every operation first checks that the asset belongs to the project in the path, so a session of another
 * project's asset cannot be read or changed. The registry and the hub do the work; this class announces the
 * session changes.
 */
@Singleton
public class LiveSessionServiceImpl implements LiveSessionService {

    private static final String LIVE_SESSION = "Live session";

    private final AssetService assetService;
    private final LiveSessionRegistry registry;
    private final AssetEventHub hub;

    /**
     * Constructs a new LiveSessionServiceImpl.
     *
     * @param assetService the asset lookup that checks ownership
     * @param registry     the registry of live sessions
     * @param hub          the hub that announces session changes
     */
    @Inject
    public LiveSessionServiceImpl(AssetService assetService, LiveSessionRegistry registry, AssetEventHub hub) {
        this.assetService = assetService;
        this.registry = registry;
        this.hub = hub;
    }

    @Override
    public LiveSessionResponseDTO getLiveSession(UUID projectId, UUID assetId) {
        requireAsset(projectId, assetId);
        return registry.active(assetId).orElseThrow(() -> ApiException.notFound(LIVE_SESSION));
    }

    @Override
    public LiveSessionResponseDTO startLiveSession(UUID projectId, UUID assetId, LiveSessionRequestDTO request) {
        requireAsset(projectId, assetId);
        LiveSessionRegistry.Claim claim = registry.claim(assetId, request.token(), request.host());
        if (claim.created()) {
            hub.publish(new AssetEvent.Session(assetId, LiveSessionEventDTO.started(request.token(), request.host())));
        }
        return claim.session();
    }

    @Override
    public void endLiveSession(UUID projectId, UUID assetId, String token) {
        requireAsset(projectId, assetId);
        registry.release(assetId, token).ifPresent(session -> hub.publish(
                new AssetEvent.Session(assetId, LiveSessionEventDTO.ended(session.token(), session.host()))));
    }

    @Override
    public Flux<AssetEvent> events(UUID projectId, UUID assetId) {
        requireAsset(projectId, assetId);
        return hub.subscribe(assetId);
    }

    /**
     * Fails with the usual not-found answer unless the asset belongs to the project.
     */
    private void requireAsset(UUID projectId, UUID assetId) {
        assetService.getAsset(projectId, assetId);
    }
}
