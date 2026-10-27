package net.onelitefeather.vulpes.backend.domain.asset;

/**
 * Violation codes for asset requests. They travel in {@code ProblemDetail.Violation#code()}, next to
 * the bean validation codes such as {@code NotBlank}.
 *
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 3.3.0
 */
public final class AssetViolationCode {

    /** The file extension does not fit the asset kind. */
    public static final String UNSUPPORTED_TYPE = "ASSET_UNSUPPORTED_TYPE";

    /** The file cannot be decoded, or its JSON is not an object. */
    public static final String INVALID_CONTENT = "ASSET_INVALID_CONTENT";

    /** The asset key does not match the Minecraft resource location rules. */
    public static final String KEY_INVALID = "ASSET_KEY_INVALID";

    private AssetViolationCode() {
    }
}
