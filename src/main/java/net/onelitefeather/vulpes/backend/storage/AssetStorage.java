package net.onelitefeather.vulpes.backend.storage;

import io.micronaut.objectstorage.ObjectStorageEntry;
import io.micronaut.objectstorage.ObjectStorageOperations;
import io.micronaut.objectstorage.request.UploadRequest;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import net.onelitefeather.vulpes.backend.domain.error.ErrorCode;
import net.onelitefeather.vulpes.backend.exception.ApiException;

/**
 * Stores the bytes of project assets in the named Micronaut Object Storage bucket {@value #STORE_NAME}.
 *
 * <p>The service talks to this class only, so the backend behind it can change through configuration
 * alone. The PoC uses the local implementation; an S3 or MinIO store under the same name replaces it.
 *
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 3.3.0
 */
@Singleton
public class AssetStorage {

    /** The name of the object storage store, which configuration keys are scoped to. */
    public static final String STORE_NAME = "assets";

    private final ObjectStorageOperations<?, ?, ?> storage;

    /**
     * Constructs the storage with the store named {@value #STORE_NAME}.
     *
     * @param storage the object storage operations of that store
     */
    @Inject
    public AssetStorage(@Named(STORE_NAME) ObjectStorageOperations<?, ?, ?> storage) {
        this.storage = storage;
    }

    /**
     * Returns the object key of an asset, {@code <projectId>/<assetId>}.
     *
     * @param projectId the project the asset belongs to
     * @param assetId   the asset
     * @return the object key
     */
    public static String keyFor(UUID projectId, UUID assetId) {
        return projectId + "/" + assetId;
    }

    /**
     * Returns the object key of one revision of an asset, {@code <projectId>/revisions/<assetId>/<number>}.
     *
     * <p>The key is known before the revision row is inserted, and it is immutable: the number is unique per
     * asset and never reused. The {@code revisions} segment keeps these keys apart from the legacy key of the
     * asset, because the local store cannot hold a file {@code <projectId>/<assetId>} and a directory below it.
     *
     * @param projectId the project the asset belongs to
     * @param assetId   the asset
     * @param number    the position of the revision, starting at 1
     * @return the object key
     */
    public static String revisionKeyFor(UUID projectId, UUID assetId, int number) {
        return projectId + "/revisions/" + assetId + "/" + number;
    }

    /**
     * Stores or overwrites the bytes under the given key.
     *
     * @param key         the object key
     * @param content     the bytes
     * @param contentType the media type the bytes are served with
     */
    public void put(String key, byte[] content, String contentType) {
        storage.upload(UploadRequest.fromBytes(content, key, contentType));
    }

    /**
     * Reads the bytes stored under the given key.
     *
     * @param key the object key
     * @return the bytes, empty if nothing is stored under the key
     * @throws ApiException {@code INTERNAL_ERROR} if the stored object cannot be read
     */
    public Optional<byte[]> get(String key) {
        Optional<ObjectStorageEntry<?>> entry = storage.retrieve(key);
        return entry.map(found -> read(key, found));
    }

    /**
     * Reads the bytes stored under the given key, which must exist.
     *
     * @param key the object key
     * @return the bytes
     * @throws ApiException {@code INTERNAL_ERROR} if nothing is stored under the key, or it cannot be read
     */
    public byte[] require(String key) {
        return get(key).orElseThrow(() -> new ApiException(
                ErrorCode.INTERNAL_ERROR, "Internal server error", "Object storage has no content under " + key));
    }

    /**
     * Removes the bytes stored under the given key. Removing a missing key is not an error.
     *
     * @param key the object key
     */
    public void delete(String key) {
        storage.delete(key);
    }

    private static byte[] read(String key, ObjectStorageEntry<?> entry) {
        try (var input = entry.getInputStream()) {
            return input.readAllBytes();
        } catch (IOException exception) {
            throw new ApiException(
                    ErrorCode.INTERNAL_ERROR,
                    "Internal server error",
                    "Object storage entry " + key + " could not be read: " + exception.getMessage());
        }
    }
}
