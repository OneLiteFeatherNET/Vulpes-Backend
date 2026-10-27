package net.onelitefeather.vulpes.backend.domain.asset;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Computes the SHA-1 digest of asset bytes as lowercase hex. It is used as the ETag of an asset.
 *
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 3.3.0
 */
public final class Sha1Hex {

    private Sha1Hex() {}

    /**
     * Returns the SHA-1 digest of the given bytes as 40 lowercase hex characters.
     *
     * @param content the bytes to digest
     * @return the hex digest
     */
    public static String of(byte[] content) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-1").digest(content);
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-1 is not available on this JVM", exception);
        }
    }
}
