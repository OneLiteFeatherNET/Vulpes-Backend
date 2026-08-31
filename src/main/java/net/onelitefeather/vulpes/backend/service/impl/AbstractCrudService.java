package net.onelitefeather.vulpes.backend.service.impl;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.repository.PageableRepository;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.service.CrudService;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Abstract generic implementation of the {@link CrudService} interface.
 *
 * <p>Failure paths raise {@link ApiException}, which the HTTP layer turns into a status and an
 * RFC 9457 body. Two of them deserve attention:
 *
 * <ul>
 *   <li>A request DTO without an identifier is a client bug, not a missing resource, so it answers
 *       {@code INVALID_REQUEST} rather than {@code RESOURCE_NOT_FOUND}. The old behaviour returned a
 *       404 here and left the frontend unable to tell the two apart.</li>
 *   <li>An entity that exists but belongs to a different project answers exactly like one that does
 *       not exist. Anything else would turn the endpoint into an oracle for resource ids in projects
 *       the caller has no business addressing. The real reason is recorded in the log.</li>
 * </ul>
 *
 * @param <E>   the entity type
 * @param <ID>  the identifier type
 * @param <REQ> the request DTO type
 * @param <RES> the response DTO type
 */
public abstract class AbstractCrudService<E, ID, REQ, RES> implements CrudService<E, ID, REQ, RES> {

    protected final PageableRepository<E, ID> repository;
    protected final Function<REQ, E> entityMapper;
    protected final Function<E, RES> dtoMapper;
    protected final Function<E, RES> dtoListMapper;
    protected final Function<REQ, ID> idMapper;
    protected final String entityName;

    protected final boolean projectScoped;
    protected final ProjectRepository projectRepository;
    protected final BiFunction<REQ, ProjectEntity, E> scopedEntityMapper;
    protected final Function<E, UUID> entityProjectIdExtractor;
    protected final BiFunction<UUID, Pageable, Page<E>> findByProjectFn;

    /**
     * Constructs a new AbstractCrudService with identical mapping for single and list representations.
     *
     * @param repository   the pageable repository
     * @param entityMapper to convert a request DTO to an entity
     * @param dtoMapper    to convert an entity to a response DTO
     * @param idMapper     to extract the ID from a request DTO
     * @param entityName   the human-readable entity name for error messages
     */
    protected AbstractCrudService(
            PageableRepository<E, ID> repository,
            Function<REQ, E> entityMapper,
            Function<E, RES> dtoMapper,
            Function<REQ, ID> idMapper,
            String entityName
    ) {
        this(repository, entityMapper, dtoMapper, dtoMapper, idMapper, entityName);
    }

    /**
     * Constructs a new AbstractCrudService with custom mapping for single and list representations.
     *
     * @param repository    the pageable repository
     * @param entityMapper  to convert a request DTO to an entity
     * @param dtoMapper     to convert an entity to a single response DTO
     * @param dtoListMapper to convert an entity to a list response DTO
     * @param idMapper      to extract the ID from a request DTO
     * @param entityName    the human-readable entity name for error messages
     */
    protected AbstractCrudService(
            PageableRepository<E, ID> repository,
            Function<REQ, E> entityMapper,
            Function<E, RES> dtoMapper,
            Function<E, RES> dtoListMapper,
            Function<REQ, ID> idMapper,
            String entityName
    ) {
        this.repository = repository;
        this.entityMapper = entityMapper;
        this.dtoMapper = dtoMapper;
        this.dtoListMapper = dtoListMapper;
        this.idMapper = idMapper;
        this.entityName = entityName;
        this.projectScoped = false;
        this.projectRepository = null;
        this.scopedEntityMapper = null;
        this.entityProjectIdExtractor = null;
        this.findByProjectFn = null;
    }

    /**
     * Constructs a new project-scoped AbstractCrudService with identical mapping for single and list representations.
     *
     * @param repository               the pageable repository
     * @param projectRepository        to resolve the owning {@link ProjectEntity} for a given project id
     * @param scopedEntityMapper       to convert a request DTO plus the resolved project to an entity
     * @param dtoMapper                to convert an entity to a response DTO
     * @param entityProjectIdExtractor to read the owning project's id off an entity
     * @param findByProjectFn          to look up a page of entities belonging to a project
     * @param idMapper                 to extract the ID from a request DTO
     * @param entityName               the human-readable entity name for error messages
     */
    protected AbstractCrudService(
            PageableRepository<E, ID> repository,
            ProjectRepository projectRepository,
            BiFunction<REQ, ProjectEntity, E> scopedEntityMapper,
            Function<E, RES> dtoMapper,
            Function<E, UUID> entityProjectIdExtractor,
            BiFunction<UUID, Pageable, Page<E>> findByProjectFn,
            Function<REQ, ID> idMapper,
            String entityName
    ) {
        this(
                repository, projectRepository, scopedEntityMapper, dtoMapper, dtoMapper,
                entityProjectIdExtractor, findByProjectFn, idMapper, entityName
        );
    }

    /**
     * Constructs a new project-scoped AbstractCrudService with custom mapping for single and list representations.
     *
     * @param repository               the pageable repository
     * @param projectRepository        to resolve the owning {@link ProjectEntity} for a given project id
     * @param scopedEntityMapper       to convert a request DTO plus the resolved project to an entity
     * @param dtoMapper                to convert an entity to a single response DTO
     * @param dtoListMapper            to convert an entity to a list response DTO
     * @param entityProjectIdExtractor to read the owning project's id off an entity
     * @param findByProjectFn          to look up a page of entities belonging to a project
     * @param idMapper                 to extract the ID from a request DTO
     * @param entityName               the human-readable entity name for error messages
     */
    protected AbstractCrudService(
            PageableRepository<E, ID> repository,
            ProjectRepository projectRepository,
            BiFunction<REQ, ProjectEntity, E> scopedEntityMapper,
            Function<E, RES> dtoMapper,
            Function<E, RES> dtoListMapper,
            Function<E, UUID> entityProjectIdExtractor,
            BiFunction<UUID, Pageable, Page<E>> findByProjectFn,
            Function<REQ, ID> idMapper,
            String entityName
    ) {
        this.repository = repository;
        this.entityMapper = null;
        this.dtoMapper = dtoMapper;
        this.dtoListMapper = dtoListMapper;
        this.idMapper = idMapper;
        this.entityName = entityName;
        this.projectScoped = true;
        this.projectRepository = projectRepository;
        this.scopedEntityMapper = scopedEntityMapper;
        this.entityProjectIdExtractor = entityProjectIdExtractor;
        this.findByProjectFn = findByProjectFn;
    }

    private void requireProjectScoped() {
        if (!projectScoped) {
            throw new UnsupportedOperationException(entityName + " is not project-scoped.");
        }
    }

    private void requireNotProjectScoped() {
        if (projectScoped) {
            throw new UnsupportedOperationException(
                    entityName + " is project-scoped; use the projectId-taking overload instead.");
        }
    }

    /**
     * Reads the identifier off a request DTO, rejecting a DTO that carries none.
     *
     * @param dto the request DTO
     * @return the identifier
     * @throws ApiException {@code INVALID_REQUEST} if the DTO carries no identifier
     */
    private ID requireId(REQ dto) {
        ID id = idMapper.apply(dto);
        if (id == null) {
            throw ApiException.invalidRequest(
                    "An id is required to update a " + entityName.toLowerCase(Locale.ROOT) + ".");
        }
        return id;
    }

    /**
     * Loads an entity that must exist and must belong to the given project.
     *
     * @param projectId the project the request addressed
     * @param id        the identifier of the entity
     * @return the entity
     * @throws ApiException {@code RESOURCE_NOT_FOUND} if it does not exist or belongs elsewhere
     */
    private E requireOwnedByProject(UUID projectId, ID id) {
        E entity = repository.findById(id).orElseThrow(() -> ApiException.notFound(entityName));
        if (!projectId.equals(entityProjectIdExtractor.apply(entity))) {
            throw ApiException.notOwnedByProject(entityName, id, projectId);
        }
        return entity;
    }

    /**
     * Resolves the project a scoped request addressed.
     *
     * @param projectId the project identifier
     * @return the project entity
     * @throws ApiException {@code PROJECT_NOT_FOUND} if it does not exist
     */
    private ProjectEntity requireProject(UUID projectId) {
        return projectRepository.findById(projectId).orElseThrow(ApiException::projectNotFound);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RES create(REQ dto) {
        requireNotProjectScoped();
        E entity = entityMapper.apply(dto);
        E saved = repository.save(entity);
        return dtoMapper.apply(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RES update(REQ dto) {
        requireNotProjectScoped();
        ID id = requireId(dto);
        if (repository.findById(id).isEmpty()) {
            throw ApiException.notFound(entityName);
        }
        E entity = entityMapper.apply(dto);
        E updated = repository.update(entity);
        return dtoMapper.apply(updated);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RES delete(ID id) {
        requireNotProjectScoped();
        E existing = repository.findById(id).orElseThrow(() -> ApiException.notFound(entityName));
        repository.deleteById(id);
        return dtoMapper.apply(existing);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAll() {
        requireNotProjectScoped();
        repository.deleteAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<RES> getAll(Pageable pageable) {
        requireNotProjectScoped();
        return repository.findAll(pageable).map(dtoListMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<E> findById(ID id) {
        requireNotProjectScoped();
        return repository.findById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RES create(UUID projectId, REQ dto) {
        requireProjectScoped();
        ProjectEntity project = requireProject(projectId);
        E entity = scopedEntityMapper.apply(dto, project);
        E saved = repository.save(entity);
        return dtoMapper.apply(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RES update(UUID projectId, REQ dto) {
        requireProjectScoped();
        ID id = requireId(dto);
        requireOwnedByProject(projectId, id);
        ProjectEntity project = requireProject(projectId);
        E entity = scopedEntityMapper.apply(dto, project);
        E updated = repository.update(entity);
        return dtoMapper.apply(updated);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RES delete(UUID projectId, ID id) {
        requireProjectScoped();
        E existing = requireOwnedByProject(projectId, id);
        repository.deleteById(id);
        return dtoMapper.apply(existing);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAll(UUID projectId) {
        requireProjectScoped();
        List<E> toDelete = findByProjectFn.apply(projectId, Pageable.unpaged()).getContent();
        repository.deleteAll(toDelete);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<RES> getAll(UUID projectId, Pageable pageable) {
        requireProjectScoped();
        return findByProjectFn.apply(projectId, pageable).map(dtoListMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<E> findById(UUID projectId, ID id) {
        requireProjectScoped();
        return repository.findById(id)
                .filter(entity -> projectId.equals(entityProjectIdExtractor.apply(entity)));
    }
}
