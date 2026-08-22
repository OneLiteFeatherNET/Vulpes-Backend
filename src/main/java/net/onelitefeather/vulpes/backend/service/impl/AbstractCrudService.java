package net.onelitefeather.vulpes.backend.service.impl;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.repository.PageableRepository;
import net.onelitefeather.vulpes.backend.service.CrudService;

import java.util.List;
import java.util.Optional;
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SUCCESS create(REQ dto) {
        E entity = entityMapper.apply(dto);
        E saved = repository.save(entity);
        return dtoMapper.apply(saved);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RES update(REQ dto) {
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
        repository.deleteAll();
        return List.of();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<SUCCESS> getAll(Pageable pageable) {
        return repository.findAll(pageable).map(dtoListMapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<E> findById(ID id) {
        return repository.findById(id);
    }
}
