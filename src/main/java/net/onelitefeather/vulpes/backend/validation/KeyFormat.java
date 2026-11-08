package net.onelitefeather.vulpes.backend.validation;

/**
 * The format of the key of a project or a model.
 */
public final class KeyFormat {

    /**
     * Lowercase letters, digits, {@code _}, {@code .} and {@code -}, the characters Minecraft allows in the
     * path of an id, without {@code /}. An empty key is left to {@code @NotBlank}, so it is reported once.
     */
    public static final String PATTERN = "^[a-z0-9_.-]*$";

    /**
     * The message of a violation of {@link #PATTERN}.
     */
    public static final String MESSAGE = "must only contain lowercase letters, digits, '_', '.' and '-'";

    private KeyFormat() {
    }
}
