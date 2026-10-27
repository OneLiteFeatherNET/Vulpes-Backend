package net.onelitefeather.vulpes.backend.domain.asset;

import io.micronaut.core.annotation.Introspected;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * The internal request for storing an asset. It is built by the controller from the multipart
 * request and is never serialized. It is introspected, so the service can validate it.
 *
 * @param id          the asset to replace, {@code null} when creating
 * @param key         the asset key, {@code null} when replacing its content
 * @param displayName the name the asset is shown by, {@code null} when replacing its content
 * @param fileName    the file name as the client sent it
 * @param content     the uploaded bytes
 * @param comment     the optional comment of the revision, {@code null} if none
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 3.3.0
 */
@Introspected
public record AssetUploadDTO(
        @Nullable UUID id,
        @Nullable String key,
        @Nullable String displayName,
        String fileName,
        byte[] content,
        @Nullable String comment) {}
