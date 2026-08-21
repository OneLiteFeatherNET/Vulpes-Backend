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
    protected final Function<REQ, E> toEntityMapper;
    protected final Function<E, SUCCESS> toSuccessDtoMapper;
    protected final Function<E, SUCCESS> toListSuccessDtoMapper;
    protected final Function<REQ, ID> idExtractor;
    protected final Function<String, RES> errorDtoFactory;
    protected final String entityName;

    /**
     * Constructs a new AbstractCrudService with identical mapping for single and list representations.
     *
     * @param repository        the pageable repository
     * @param toEntityMapper    function to convert a request DTO to an entity
     * @param toSuccessDtoMapper function to convert an entity to a success DTO
     * @param idExtractor       function to extract the ID from a request DTO
     * @param errorDtoFactory   function to create an error response DTO from an error message
     * @param entityName        the human-readable entity name for error messages
     */
    protected AbstractCrudService(
            PageableRepository<E, ID> repository,
            Function<REQ, E> toEntityMapper,
            Function<E, SUCCESS> toSuccessDtoMapper,
            Function<REQ, ID> idExtractor,
            Function<String, RES> errorDtoFactory,
            String entityName
    ) {
        this(repository, toEntityMapper, toSuccessDtoMapper, toSuccessDtoMapper, idExtractor, errorDtoFactory, entityName);
    }

    /**
     * Constructs a new AbstractCrudService with custom mapping for single and list representations.
     *
     * @param repository            the pageable repository
     * @param toEntityMapper        function to convert a request DTO to an entity
     * @param toSuccessDtoMapper     function to convert an entity to a single success DTO
     * @param toListSuccessDtoMapper function to convert an entity to a list success DTO
     * @param idExtractor           function to extract the ID from a request DTO
     * @param errorDtoFactory       function to create an error response DTO from an error message
     * @param entityName            the human-readable entity name for error messages
     */
    protected AbstractCrudService(
            PageableRepository<E, ID> repository,
            Function<REQ, E> toEntityMapper,
            Function<E, SUCCESS> toSuccessDtoMapper,
            Function<E, SUCCESS> toListSuccessDtoMapper,
            Function<REQ, ID> idExtractor,
            Function<String, RES> errorDtoFactory,
            String entityName
    ) {
        this.repository = repository;
        this.toEntityMapper = toEntityMapper;
        this.toSuccessDtoMapper = toSuccessDtoMapper;
        this.toListSuccessDtoMapper = toListSuccessDtoMapper;
        this.idExtractor = idExtractor;
        this.errorDtoFactory = errorDtoFactory;
        this.entityName = entityName;
    }

    @Override
    public SUCCESS create(REQ dto) {
        E entity = toEntityMapper.apply(dto);
        E saved = repository.save(entity);
        return toSuccessDtoMapper.apply(saved);
    }

    @Override
    public RES update(REQ dto) {
        ID id = idExtractor.apply(dto);
        if (id == null || repository.findById(id).isEmpty()) {
            return errorDtoFactory.apply(entityName + " not found");
        }
        E entity = toEntityMapper.apply(dto);
        E updated = repository.update(entity);
        return toSuccessDtoMapper.apply(updated);
    }

    @Override
    public RES delete(ID id) {
        Optional<E> existing = repository.findById(id);
        if (existing.isPresent()) {
            repository.deleteById(id);
            return toSuccessDtoMapper.apply(existing.get());
        }
        return errorDtoFactory.apply(entityName + " not found");
    }

    @Override
    public List<RES> deleteAll() {
        repository.deleteAll();
        return List.of();
    }

    @Override
    public Page<SUCCESS> getAll(Pageable pageable) {
        return repository.findAll(pageable).map(toListSuccessDtoMapper::apply);
    }

    @Override
    public Optional<E> findById(ID id) {
        return repository.findById(id);
    }
}
