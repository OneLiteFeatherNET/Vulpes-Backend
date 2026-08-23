package net.onelitefeather.vulpes.backend.service.impl;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.repository.PageableRepository;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.backend.service.CrudService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Abstract generic implementation of the {@link CrudService} interface.
 *
 * @param <E>       the entity type
 * @param <ID>      the identifier type
 * @param <REQ>     the request DTO type
 * @param <RES>     the response DTO interface type
 * @param <SUCCESS> the concrete success response DTO type
 */
public abstract class AbstractCrudService<E, ID, REQ, RES, SUCCESS extends RES>
        implements CrudService<E, ID, REQ, RES, SUCCESS> {

    protected final PageableRepository<E, ID> repository;
    protected final Function<REQ, E> entityMapper;
    protected final Function<E, SUCCESS> dtoMapper;
    protected final Function<E, SUCCESS> dtoListMapper;
    protected final Function<REQ, ID> idMapper;
    protected final Function<String, RES> errorMapper;
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
     * @param dtoMapper    to convert an entity to a success DTO
     * @param idMapper     to extract the ID from a request DTO
     * @param errorMapper  to create an error response DTO from an error message
     * @param entityName   the human-readable entity name for error messages
     */
    protected AbstractCrudService(
            PageableRepository<E, ID> repository,
            Function<REQ, E> entityMapper,
            Function<E, SUCCESS> dtoMapper,
            Function<REQ, ID> idMapper,
            Function<String, RES> errorMapper,
            String entityName
    ) {
        this(repository, entityMapper, dtoMapper, dtoMapper, idMapper, errorMapper, entityName);
    }

    /**
     * Constructs a new AbstractCrudService with custom mapping for single and list representations.
     *
     * @param repository    the pageable repository
     * @param entityMapper  to convert a request DTO to an entity
     * @param dtoMapper     to convert an entity to a single success DTO
     * @param dtoListMapper to convert an entity to a list success DTO
     * @param idMapper      to extract the ID from a request DTO
     * @param errorMapper   to create an error response DTO from an error message
     * @param entityName    the human-readable entity name for error messages
     */
    protected AbstractCrudService(
            PageableRepository<E, ID> repository,
            Function<REQ, E> entityMapper,
            Function<E, SUCCESS> dtoMapper,
            Function<E, SUCCESS> dtoListMapper,
            Function<REQ, ID> idMapper,
            Function<String, RES> errorMapper,
            String entityName
    ) {
        this.repository = repository;
        this.entityMapper = entityMapper;
        this.dtoMapper = dtoMapper;
        this.dtoListMapper = dtoListMapper;
        this.idMapper = idMapper;
        this.errorMapper = errorMapper;
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
     * @param dtoMapper                to convert an entity to a success DTO
     * @param entityProjectIdExtractor to read the owning project's id off an entity
     * @param findByProjectFn          to look up a page of entities belonging to a project
     * @param idMapper                 to extract the ID from a request DTO
     * @param errorMapper              to create an error response DTO from an error message
     * @param entityName               the human-readable entity name for error messages
     */
    protected AbstractCrudService(
            PageableRepository<E, ID> repository,
            ProjectRepository projectRepository,
            BiFunction<REQ, ProjectEntity, E> scopedEntityMapper,
            Function<E, SUCCESS> dtoMapper,
            Function<E, UUID> entityProjectIdExtractor,
            BiFunction<UUID, Pageable, Page<E>> findByProjectFn,
            Function<REQ, ID> idMapper,
            Function<String, RES> errorMapper,
            String entityName
    ) {
        this(
                repository, projectRepository, scopedEntityMapper, dtoMapper, dtoMapper,
                entityProjectIdExtractor, findByProjectFn, idMapper, errorMapper, entityName
        );
    }

    /**
     * Constructs a new project-scoped AbstractCrudService with custom mapping for single and list representations.
     *
     * @param repository               the pageable repository
     * @param projectRepository        to resolve the owning {@link ProjectEntity} for a given project id
     * @param scopedEntityMapper       to convert a request DTO plus the resolved project to an entity
     * @param dtoMapper                to convert an entity to a single success DTO
     * @param dtoListMapper            to convert an entity to a list success DTO
     * @param entityProjectIdExtractor to read the owning project's id off an entity
     * @param findByProjectFn          to look up a page of entities belonging to a project
     * @param idMapper                 to extract the ID from a request DTO
     * @param errorMapper              to create an error response DTO from an error message
     * @param entityName               the human-readable entity name for error messages
     */
    protected AbstractCrudService(
            PageableRepository<E, ID> repository,
            ProjectRepository projectRepository,
            BiFunction<REQ, ProjectEntity, E> scopedEntityMapper,
            Function<E, SUCCESS> dtoMapper,
            Function<E, SUCCESS> dtoListMapper,
            Function<E, UUID> entityProjectIdExtractor,
            BiFunction<UUID, Pageable, Page<E>> findByProjectFn,
            Function<REQ, ID> idMapper,
            Function<String, RES> errorMapper,
            String entityName
    ) {
        this.repository = repository;
        this.entityMapper = null;
        this.dtoMapper = dtoMapper;
        this.dtoListMapper = dtoListMapper;
        this.idMapper = idMapper;
        this.errorMapper = errorMapper;
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
     * {@inheritDoc}
     */
    @Override
    public SUCCESS create(REQ dto) {
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
        ID id = idMapper.apply(dto);
        if (id == null || repository.findById(id).isEmpty()) {
            return errorMapper.apply(entityName + " not found");
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
        Optional<E> existing = repository.findById(id);
        if (existing.isPresent()) {
            repository.deleteById(id);
            return dtoMapper.apply(existing.get());
        }
        return errorMapper.apply(entityName + " not found");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RES> deleteAll() {
        requireNotProjectScoped();
        repository.deleteAll();
        return List.of();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<SUCCESS> getAll(Pageable pageable) {
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
        Optional<ProjectEntity> project = projectRepository.findById(projectId);
        if (project.isEmpty()) {
            return errorMapper.apply("Project not found");
        }
        E entity = scopedEntityMapper.apply(dto, project.get());
        E saved = repository.save(entity);
        return dtoMapper.apply(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RES update(UUID projectId, REQ dto) {
        requireProjectScoped();
        ID id = idMapper.apply(dto);
        if (id == null) {
            return errorMapper.apply(entityName + " not found");
        }
        Optional<E> existing = repository.findById(id);
        if (existing.isEmpty() || !projectId.equals(entityProjectIdExtractor.apply(existing.get()))) {
            return errorMapper.apply(entityName + " not found");
        }
        Optional<ProjectEntity> project = projectRepository.findById(projectId);
        if (project.isEmpty()) {
            return errorMapper.apply("Project not found");
        }
        E entity = scopedEntityMapper.apply(dto, project.get());
        E updated = repository.update(entity);
        return dtoMapper.apply(updated);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RES delete(UUID projectId, ID id) {
        requireProjectScoped();
        Optional<E> existing = repository.findById(id);
        if (existing.isEmpty() || !projectId.equals(entityProjectIdExtractor.apply(existing.get()))) {
            return errorMapper.apply(entityName + " not found");
        }
        repository.deleteById(id);
        return dtoMapper.apply(existing.get());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RES> deleteAll(UUID projectId) {
        requireProjectScoped();
        List<E> toDelete = findByProjectFn.apply(projectId, Pageable.unpaged()).getContent();
        repository.deleteAll(toDelete);
        return List.of();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<SUCCESS> getAll(UUID projectId, Pageable pageable) {
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
