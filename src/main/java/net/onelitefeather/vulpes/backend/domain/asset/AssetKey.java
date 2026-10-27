package net.onelitefeather.vulpes.backend.domain.asset;

import java.util.List;
import java.util.regex.Pattern;
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import org.jspecify.annotations.Nullable;

/**
 * Validation rules for asset keys, the Minecraft resource locations an asset is addressed by.
 *
 * <p>A key has the form {@code namespace:path}, for example {@code vulpes:item/ruby}. It is lowercase,
 * and the last path segment must not carry a file extension. The extension is part of the file name,
 * not of the key.
 *
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 3.3.0
 */
public final class AssetKey {

    private static final Pattern PATTERN = Pattern.compile("^[a-z0-9_.-]+:[a-z0-9_./-]+$");
    private static final Pattern EXTENSION = Pattern.compile("\\.[a-z0-9]{1,5}$");

    private AssetKey() {}

    /**
     * Checks whether a key follows the rules.
     *
     * @param key the key to check, may be {@code null}
     * @return {@code true} if the key is a valid asset key
     */
    public static boolean isValid(String key) {
        if (key == null || !PATTERN.matcher(key).matches()) {
            return false;
        }
        String lastSegment = key.substring(key.lastIndexOf('/') + 1);
        return !EXTENSION.matcher(lastSegment).find();
    }

    /**
     * Returns the key unchanged if it is valid.
     *
     * @param key the key from the request
     * @return the same key
     * @throws ApiException {@code VALIDATION_FAILED} with an {@code ASSET_KEY_INVALID} violation on
     *                      field {@code key}
     */
    public static String requireValid(@Nullable String key) {
        if (key == null || !isValid(key)) {
            throw ApiException.validationFailed(
                    List.of(
                            new ProblemDetail.Violation(
                                    "key",
                                    AssetViolationCode.KEY_INVALID,
                                    "must look like 'namespace:path' in lowercase without a file extension, for example 'vulpes:item/ruby'")));
        }
        return key;
    }
}
