package net.onelitefeather.vulpes.backend.copier;

import net.onelitefeather.vulpes.api.model.VulpesModel;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

/**
 * Copies an entity within the same project or into another one, optionally including a
 * selection of its relations.
 *
 * @param <T> the type of the copied model
 * @param <E> the type used to identify relations that can be copied alongside the entity;
 *            {@link Void} for a model that has none
 * @author theEvilReaper
 * @version 2.0.0
 * @since 3.1.0
 */
public interface EntityCopier<T extends VulpesModel, E> {

    /**
     * Copies the entity addressed by {@code sourceProjectId}/{@code sourceId} into
     * {@code targetProjectId} (or the source's own project, when {@code null}) under
     * {@code targetKey} (or the source's own key, when {@code null} or blank) and
     * {@code targetName} (or the source's own name, when {@code null} or blank), including the
     * requested relations.
     *
     * @param sourceProjectId the project containing the source entity
     * @param sourceId        the ID of the entity to copy
     * @param targetProjectId the project to copy into, or {@code null} for the source's own project
     * @param targetKey       the key of the copied entity, or {@code null} to reuse the source's own key
     * @param targetName      the display name of the copied entity, or {@code null} to reuse the source's own name
     * @param relations       which relations to copy alongside the entity; an empty set copies only
     *                        the entity's own fields
     * @return the copied entity
     */
    T copy(UUID sourceProjectId, UUID sourceId, @Nullable UUID targetProjectId, @Nullable String targetKey, @Nullable String targetName, Set<E> relations);

    /**
     * Copies an entity without any relations.
     *
     * @param sourceProjectId the project containing the source entity
     * @param sourceId        the ID of the entity to copy
     * @param targetProjectId the project to copy into, or {@code null} for the source's own project
     * @param targetKey       the key of the copied entity, or {@code null} to reuse the source's own key
     * @param targetName      the display name of the copied entity, or {@code null} to reuse the source's own name
     * @return the copied entity
     */
    default T copy(UUID sourceProjectId, UUID sourceId, @Nullable UUID targetProjectId, @Nullable String targetKey, @Nullable String targetName) {
        return this.copy(sourceProjectId, sourceId, targetProjectId, targetKey, targetName, Set.of());
    }
}
