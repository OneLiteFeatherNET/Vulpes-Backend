package net.onelitefeather.vulpes.backend.domain.asset;

/**
 * The stored bytes of an asset together with the metadata the HTTP layer needs.
 *
 * @param content     the raw bytes
 * @param contentType the media type the bytes are served with
 * @param sha1        the SHA-1 digest, used as the ETag
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 3.3.0
 */
public record AssetContent(byte[] content, String contentType, String sha1) {}
