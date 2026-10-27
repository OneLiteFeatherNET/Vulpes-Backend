package net.onelitefeather.vulpes.backend.service.session;

import net.onelitefeather.vulpes.backend.domain.asset.AssetSavedEventDTO;
import net.onelitefeather.vulpes.backend.domain.session.LiveSessionEventDTO;

import java.util.UUID;

/**
 * An event of one asset, as the event stream of that asset delivers it.
 */
public sealed interface AssetEvent permits AssetEvent.Saved, AssetEvent.Session {

    /**
     * The asset the event belongs to.
     *
     * @return the asset id
     */
    UUID assetId();

    /**
     * A committed save of the asset.
     *
     * @param assetId the asset
     * @param data    the new content
     */
    record Saved(UUID assetId, AssetSavedEventDTO data) implements AssetEvent {
    }

    /**
     * A change of the live session of the asset.
     *
     * @param assetId the asset
     * @param data    the change
     */
    record Session(UUID assetId, LiveSessionEventDTO data) implements AssetEvent {
    }
}
