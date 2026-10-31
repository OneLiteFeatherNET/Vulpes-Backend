package net.onelitefeather.vulpes.backend.service;

import net.onelitefeather.vulpes.backend.domain.session.LiveSessionRequestDTO;
import net.onelitefeather.vulpes.backend.domain.session.LiveSessionResponseDTO;
import net.onelitefeather.vulpes.backend.service.session.AssetEvent;
import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * Service interface for the live edit sessions of project assets and for the event streams of those assets.
 */
public interface LiveSessionService {

    /**
     * Returns the active live session of an asset.
     *
     * @param projectId the project the asset must belong to
     * @param assetId   the asset
     * @return the active session
     * @throws net.onelitefeather.vulpes.backend.exception.ApiException {@code RESOURCE_NOT_FOUND} if the asset is
     *         not in the project or no session is active
     */
    LiveSessionResponseDTO getLiveSession(UUID projectId, UUID assetId);

    /**
     * Creates the live session of an asset, or refreshes it when the token is the one of the active session.
     *
     * @param projectId the project the asset must belong to
     * @param assetId   the asset
     * @param request   the token and the host
     * @return the session after the call
     * @throws net.onelitefeather.vulpes.backend.exception.ApiException {@code RESOURCE_NOT_FOUND} if the asset is
     *         not in the project, {@code RESOURCE_CONFLICT} if another token holds an active session
     */
    LiveSessionResponseDTO startLiveSession(UUID projectId, UUID assetId, LiveSessionRequestDTO request);

    /**
     * Ends the live session of an asset when the token matches it. Nothing happens when no session is active.
     *
     * @param projectId the project the asset must belong to
     * @param assetId   the asset
     * @param token     the token the client holds
     * @throws net.onelitefeather.vulpes.backend.exception.ApiException {@code RESOURCE_NOT_FOUND} if the asset is
     *         not in the project, {@code RESOURCE_CONFLICT} if another token holds an active session
     */
    void endLiveSession(UUID projectId, UUID assetId, String token);

    /**
     * Subscribes to the events of an asset from now on.
     *
     * @param projectId the project the asset must belong to
     * @param assetId   the asset
     * @return the events of the asset
     * @throws net.onelitefeather.vulpes.backend.exception.ApiException {@code RESOURCE_NOT_FOUND} if the asset is
     *         not in the project; raised before any stream exists
     */
    Flux<AssetEvent> events(UUID projectId, UUID assetId);
}
