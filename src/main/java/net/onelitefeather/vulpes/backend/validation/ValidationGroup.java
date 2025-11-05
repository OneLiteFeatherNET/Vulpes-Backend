package net.onelitefeather.vulpes.backend.validation;

/**
 * The {@link ValidationGroup} interface defines validation groups for use with JSR 303.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 0.1.0
 */
public interface ValidationGroup {

    /**
     * Validation group for creating a new entity.
     */
    interface Create {
    }

    /**
     * Validation group for updating an existing entity.
     */
    interface Update {
    }
}
