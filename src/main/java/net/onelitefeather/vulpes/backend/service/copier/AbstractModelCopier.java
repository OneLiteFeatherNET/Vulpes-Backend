package net.onelitefeather.vulpes.backend.service.copier;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.repository.PageableRepository;
import net.onelitefeather.vulpes.api.model.AbstractEntity;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.backend.exception.ApiException;

import java.util.Locale;
import java.util.UUID;
import java.util.function.BiPredicate;

/**
 * Generic support for copying a project-scoped model, within the same project or into another one.
 *
 * <p>Handles everything identical across models: resolving the source and verifying it belongs to
 * the addressed project, resolving the target project, deciding the target key, and rejecting a key
 * that is already taken there. A concrete subclass supplies only how to build the new root entity
 * from the source.
 *
 * <p><b>Not transactional yet.</b> This method performs exactly one {@code repository.save(...)}
 * call, which is already atomic. A subclass that copies relations too &mdash; performing more than
 * one write &mdash; must wrap {@link #copy} in a verified transaction boundary; this base class does
 * not provide one.
 *
 * @param <E> the entity type, which must carry the project/key pair every copyable model has
 */
public abstract class AbstractModelCopier<E extends AbstractEntity> {

    protected final PageableRepository<E, UUID> repository;
    protected final ProjectRepository projectRepository;
    protected final BiPredicate<UUID, String> keyExistsInProject;
    protected final String entityName;

    /**
     * Constructs a new AbstractModelCopier.
     *
     * @param repository         the pageable repository for the entity type
     * @param projectRepository  to resolve the target project
     * @param keyExistsInProject checks whether a given key is already taken in a given project,
     *                            typically a method reference to the concrete repository's
     *                            {@code existsByProjectIdAndKey}
     * @param entityName         the human-readable entity name for error messages
     */
    protected AbstractModelCopier(
            PageableRepository<E, UUID> repository,
            ProjectRepository projectRepository,
            BiPredicate<UUID, String> keyExistsInProject,
            String entityName
    ) {
        this.repository = repository;
        this.projectRepository = projectRepository;
        this.keyExistsInProject = keyExistsInProject;
        this.entityName = entityName;
    }

    /**
     * Copies the entity addressed by {@code sourceProjectId}/{@code sourceId} into
     * {@code targetProjectId} (or the source's own project, when {@code null}) under
     * {@code targetKey} (or the source's own key, when {@code null} or blank).
     *
     * @param sourceProjectId the project the request addressed the source through
     * @param sourceId        the identifier of the entity to copy
     * @param targetProjectId the project to copy into, or {@code null} for the same project
     * @param targetKey       the key to give the copy, or {@code null}/blank to reuse the source's key
     * @return the saved copy
     * @throws ApiException {@code RESOURCE_NOT_FOUND} if the source does not exist or belongs to a
     *                       different project than {@code sourceProjectId},
     *                       {@code PROJECT_NOT_FOUND} if {@code targetProjectId} does not exist,
     *                       {@code RESOURCE_CONFLICT} if the resolved key is already taken in the
     *                       target project
     */
    public E copy(UUID sourceProjectId, UUID sourceId, @Nullable UUID targetProjectId, @Nullable String targetKey) {
        E source = requireOwnedByProject(sourceProjectId, sourceId);
        UUID resolvedTargetProjectId = targetProjectId != null ? targetProjectId : sourceProjectId;
        ProjectEntity targetProject = requireProject(resolvedTargetProjectId);
        String resolvedKey = (targetKey != null && !targetKey.isBlank()) ? targetKey : source.getKey();
        requireKeyAvailable(resolvedTargetProjectId, resolvedKey);

        E copy = copyRoot(source, targetProject, resolvedKey);
        return repository.save(copy);
    }

    /**
     * Builds the new root entity from the source. Implementations must construct a fresh instance
     * and never mutate {@code source} &mdash; {@code source} is still managed by the persistence
     * context, and mutating it would move the original instead of copying it.
     *
     * @param source        the entity being copied
     * @param targetProject the project the copy will belong to
     * @param targetKey     the key the copy will have (already checked for uniqueness in the target project)
     * @return the new, unsaved entity
     */
    protected abstract E copyRoot(E source, ProjectEntity targetProject, String targetKey);

    /**
     * Loads an entity that must exist and must belong to the given project.
     *
     * @param projectId the project the request addressed
     * @param id        the identifier of the entity
     * @return the entity
     * @throws ApiException {@code RESOURCE_NOT_FOUND} if it does not exist or belongs elsewhere
     */
    protected E requireOwnedByProject(UUID projectId, UUID id) {
        E entity = repository.findById(id).orElseThrow(() -> ApiException.notFound(entityName));
        if (!projectId.equals(entity.getProject().getId())) {
            throw ApiException.notOwnedByProject(entityName, id, projectId);
        }
        return entity;
    }

    /**
     * Resolves the project a copy request's target addressed.
     *
     * @param projectId the project identifier
     * @return the project entity
     * @throws ApiException {@code PROJECT_NOT_FOUND} if it does not exist
     */
    protected ProjectEntity requireProject(UUID projectId) {
        return projectRepository.findById(projectId).orElseThrow(ApiException::projectNotFound);
    }

    /**
     * Rejects a key that is already taken in the given project.
     *
     * @param projectId the project the key would live in
     * @param key       the key to check
     * @throws ApiException {@code RESOURCE_CONFLICT} if an entity with that key already exists there
     */
    protected void requireKeyAvailable(UUID projectId, String key) {
        if (keyExistsInProject.test(projectId, key)) {
            throw ApiException.conflict(
                    "A " + entityName.toLowerCase(Locale.ROOT) + " with key '" + key
                            + "' already exists in the target project."
            );
        }
    }
}
