package net.onelitefeather.vulpes.backend.service.copier;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.repository.PageableRepository;
import net.onelitefeather.vulpes.api.model.AbstractEntity;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;

import java.util.Set;
import java.util.UUID;
import java.util.function.BiPredicate;

/**
 * Generic support for copying a project-scoped model together with a selection of its
 * relations, within the same project or into another one.
 *
 * <p>Extends {@link AbstractModelCopier} rather than modifying it: {@code Attribute}/
 * {@code Notification} have nothing to copy beyond their own fields and perform exactly one
 * write, which needs no transaction boundary. A model with relations performs a root write plus
 * one write per relation, and those must all succeed or all fail together.
 *
 * <p><b>{@code @Transactional} does not go on {@link #copy} here.</b> Empirically confirmed:
 * Micronaut's AOP does not intercept {@code @Transactional} when it is declared only on an
 * abstract, never-instantiated superclass method and inherited unoverridden by the concrete
 * {@code @Singleton} bean — every other successful {@code @Transactional} use in this codebase
 * (e.g. {@code ItemServiceImpl}) is declared directly on the concrete class's own method.
 * Every concrete subclass of this class must therefore declare its own public {@code copy}
 * override, annotated {@code @Transactional}, that simply delegates to this class's
 * {@link #copy} &mdash; see {@code ItemModelCopier} for the pattern.
 *
 * @param <E> the entity type, which must carry the project/key pair every copyable model has
 * @param <R> the relation selector type (typically an enum naming the model's copyable relations)
 */
public abstract class AbstractRelationalModelCopier<E extends AbstractEntity, R> extends AbstractModelCopier<E> {

    protected AbstractRelationalModelCopier(
            PageableRepository<E, UUID> repository,
            ProjectRepository projectRepository,
            BiPredicate<UUID, String> keyExistsInProject,
            String entityName
    ) {
        super(repository, projectRepository, keyExistsInProject, entityName);
    }

    /**
     * Copies the entity addressed by {@code sourceProjectId}/{@code sourceId} into
     * {@code targetProjectId} (or the source's own project, when {@code null}) under
     * {@code targetKey} (or the source's own key, when {@code null} or blank), then copies each
     * requested relation onto the saved copy.
     *
     * <p>The whole operation is one transaction: if copying any relation fails, the root save
     * and every relation copied before the failure are rolled back together.
     *
     * @param sourceProjectId the project the request addressed the source through
     * @param sourceId        the identifier of the entity to copy
     * @param targetProjectId the project to copy into, or {@code null} for the same project
     * @param targetKey       the key to give the copy, or {@code null}/blank to reuse the source's key
     * @param relations       which relations to copy alongside the root entity; an empty set copies
     *                        only the root
     * @return the saved copy, with the requested relations already persisted
     */
    protected E copy(
            UUID sourceProjectId,
            UUID sourceId,
            @Nullable UUID targetProjectId,
            @Nullable String targetKey,
            Set<R> relations
    ) {
        E source = requireOwnedByProject(sourceProjectId, sourceId);
        UUID resolvedTargetProjectId = targetProjectId != null ? targetProjectId : sourceProjectId;
        ProjectEntity targetProject = requireProject(resolvedTargetProjectId);
        String resolvedKey = (targetKey != null && !targetKey.isBlank()) ? targetKey : source.getKey();
        requireKeyAvailable(resolvedTargetProjectId, resolvedKey);

        E rootCopy = copyRoot(source, targetProject, resolvedKey);
        E savedRoot = repository.save(rootCopy);

        for (R relation : relations) {
            copyRelation(relation, source, savedRoot);
        }

        return savedRoot;
    }

    /**
     * Copies one relation from the source onto the already-saved target. Implementations must
     * load the source's children through their own repository (never through a lazy collection
     * on {@code source}), construct brand-new child entities, and attach them to {@code target}
     * &mdash; never mutate anything reachable from {@code source}.
     *
     * @param relation which relation to copy
     * @param source   the entity being copied
     * @param target   the already-saved copy to attach the copied relation to
     */
    protected abstract void copyRelation(R relation, E source, E target);
}
