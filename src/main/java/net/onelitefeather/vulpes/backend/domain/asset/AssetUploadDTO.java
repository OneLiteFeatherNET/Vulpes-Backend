package net.onelitefeather.vulpes.backend.domain.asset;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * The internal request for storing an asset. It is built by the controller from the multipart
 * request and is never serialized.
 *
 * @param id       the asset to replace, {@code null} when creating
 * @param key      the asset key, {@code null} when replacing its content
 * @param fileName the file name as the client sent it
 * @param content  the uploaded bytes
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 3.3.0
 */
public record AssetUploadDTO(@Nullable UUID id, @Nullable String key, String fileName, byte[] content) {}
