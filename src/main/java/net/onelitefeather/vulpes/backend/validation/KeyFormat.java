package net.onelitefeather.vulpes.backend.validation;

/**
 * The format of the key of a project or a model. The project key is the namespace of a Minecraft id and a
 * model key its path, so a model {@code weapons/sword} of a project {@code my_project} becomes
 * {@code my_project:weapons/sword}.
 *
 * <p>An empty key matches both patterns. It is left to {@code @NotBlank}, so it is reported once.
 */
public final class KeyFormat {

    /**
     * The characters Minecraft allows in the namespace of an id: lowercase letters, digits, {@code _},
     * {@code .} and {@code -}. Two dots in a row are rejected, the generator writes the key into a file path
     * and {@code ..} would leave its folder.
     */
    public static final String NAMESPACE_PATTERN = "^(?!.*\\.\\.)[a-z0-9_.-]*$";

    /**
     * The message of a violation of {@link #NAMESPACE_PATTERN}.
     */
    public static final String NAMESPACE_MESSAGE = "must only contain lowercase letters, digits, '_', '.' and '-' and no '..'";

    /**
     * The characters Minecraft allows in the path of an id: those of {@link #NAMESPACE_PATTERN} and {@code /}.
     * Two dots in a row are rejected as in {@link #NAMESPACE_PATTERN}.
     */
    public static final String PATH_PATTERN = "^(?!.*\\.\\.)[a-z0-9_./-]*$";

    /**
     * The message of a violation of {@link #PATH_PATTERN}.
     */
    public static final String PATH_MESSAGE = "must only contain lowercase letters, digits, '_', '.', '-' and '/' and no '..'";

    private KeyFormat() {
    }
}
