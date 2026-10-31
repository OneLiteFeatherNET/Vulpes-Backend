package net.onelitefeather.vulpes.backend.service.session;

import java.util.UUID;

/**
 * Announces that a write stored new content for an asset.
 *
 * <p>Services publish this event inside the transaction of the write. The {@link AssetEventHub} listens with
 * {@code @TransactionalEventListener(AFTER_COMMIT)}, so subscribers learn about the content only once it is
 * committed, and never for a write that rolled back.
 *
 * @param assetId  the asset whose content changed
 * @param sha1     the SHA-1 digest of the new content in lowercase hex
 * @param revision the number of the revision the write created
 */
public record AssetContentSaved(UUID assetId, String sha1, int revision) {
}
