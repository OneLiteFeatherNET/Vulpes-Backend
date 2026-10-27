package net.onelitefeather.vulpes.backend.domain.asset;

import java.util.List;
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import org.jspecify.annotations.Nullable;

/**
 * Validation rules for the display name of an asset, the name the frontend shows it by.
 *
 * <p>A display name is required and must not be blank. Surrounding whitespace is dropped.
 *
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 3.3.0
 */
public final class AssetDisplayName {

    /** The length of the {@code display_name} column. */
    public static final int MAX_LENGTH = 255;

    private AssetDisplayName() {}

    /**
     * Returns the display name without surrounding whitespace if it is valid.
     *
     * @param displayName the display name from the request, may be {@code null}
     * @return the trimmed display name
     * @throws ApiException {@code VALIDATION_FAILED} with an {@code ASSET_DISPLAY_NAME_INVALID} violation
     *                      on field {@code displayName}
     */
    public static String requireValid(@Nullable String displayName) {
        String trimmed = displayName == null ? "" : displayName.trim();
        if (trimmed.isEmpty() || trimmed.length() > MAX_LENGTH) {
            throw ApiException.validationFailed(List.of(new ProblemDetail.Violation(
                    "displayName",
                    AssetViolationCode.DISPLAY_NAME_INVALID,
                    "must not be blank and at most " + MAX_LENGTH + " characters long")));
        }
        return trimmed;
    }
}
