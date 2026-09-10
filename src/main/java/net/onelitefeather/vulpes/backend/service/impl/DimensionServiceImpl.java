package net.onelitefeather.vulpes.backend.service.impl;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.onelitefeather.vulpes.api.model.dimension.DimensionAttributeEntity;
import net.onelitefeather.vulpes.api.model.dimension.DimensionTimelineEntity;
import net.onelitefeather.vulpes.api.model.dimension.DimensionTypeEntity;
import net.onelitefeather.vulpes.api.repository.ProjectRepository;
import net.onelitefeather.vulpes.api.repository.dimension.DimensionAttributeRepository;
import net.onelitefeather.vulpes.api.repository.dimension.DimensionTimelineRepository;
import net.onelitefeather.vulpes.api.repository.dimension.DimensionTypeRepository;
import net.onelitefeather.vulpes.backend.domain.dimension.DimensionAttributeDTO;
import net.onelitefeather.vulpes.backend.domain.dimension.DimensionAttributeResponseDTO;
import net.onelitefeather.vulpes.backend.domain.dimension.DimensionModelDTO;
import net.onelitefeather.vulpes.backend.domain.dimension.DimensionModelResponseDTO;
import net.onelitefeather.vulpes.backend.domain.dimension.DimensionTimelineDTO;
import net.onelitefeather.vulpes.backend.domain.dimension.DimensionTimelineResponseDTO;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.service.DimensionService;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of the {@link DimensionService} interface.
 *
 * <p>The sub-resources of a dimension type &mdash; attributes, timelines &mdash; both follow the same
 * shape: resolve the dimension type, resolve the child, verify the child hangs off that dimension type.
 * A child owned by a different dimension type is answered as "not found" so the endpoint cannot be used
 * to enumerate ids across dimension types; the log records what actually happened.
 */
@Singleton
public class DimensionServiceImpl
        extends AbstractCrudService<DimensionTypeEntity, UUID, DimensionModelDTO, DimensionModelResponseDTO.DimensionModelDTO>
        implements DimensionService {

    private static final String DIMENSION = "Dimension";
    private static final String ATTRIBUTE = "Attribute";
    private static final String TIMELINE = "Timeline";

    private final DimensionAttributeRepository dimensionAttributeRepository;
    private final DimensionTimelineRepository dimensionTimelineRepository;

    @Inject
    public DimensionServiceImpl(
            DimensionTypeRepository dimensionTypeRepository,
            DimensionAttributeRepository dimensionAttributeRepository,
            DimensionTimelineRepository dimensionTimelineRepository,
            ProjectRepository projectRepository
    ) {
        super(
                dimensionTypeRepository,
                projectRepository,
                DimensionModelDTO::toEntity,
                DimensionModelResponseDTO.DimensionModelDTO::createDTO,
                entity -> entity.getProject().getId(),
                dimensionTypeRepository::findByProjectId,
                DimensionModelDTO::id,
                DIMENSION
        );
        this.dimensionAttributeRepository = dimensionAttributeRepository;
        this.dimensionTimelineRepository = dimensionTimelineRepository;
    }

    /**
     * Loads the dimension type a sub-resource request addressed.
     *
     * @param id the dimension type identifier
     * @return the dimension type
     * @throws ApiException {@code RESOURCE_NOT_FOUND} if no dimension type has that identifier
     */
    private DimensionTypeEntity requireDimensionType(UUID id) {
        return this.repository.findById(id).orElseThrow(() -> ApiException.notFound(DIMENSION));
    }

    @Override
    public Page<DimensionAttributeResponseDTO.DimensionAttributeDTO> findAttributesById(UUID id, Pageable pageable) {
        return this.dimensionAttributeRepository.findAttributesByDimensionTypeId(id, pageable)
                .map(DimensionAttributeResponseDTO.DimensionAttributeDTO::createDTO);
    }

    @Override
    public DimensionAttributeResponseDTO.DimensionAttributeDTO createAttributeById(UUID id, DimensionAttributeDTO attribute) {
        var dimensionType = requireDimensionType(id);
        var entity = attribute.toEntity();
        entity.setDimensionType(dimensionType);
        var saved = this.dimensionAttributeRepository.save(entity);
        return DimensionAttributeResponseDTO.DimensionAttributeDTO.createDTO(saved);
    }

    @Override
    public DimensionAttributeResponseDTO.DimensionAttributeDTO updateAttributeById(UUID id, DimensionAttributeDTO attribute) {
        var dimensionType = requireDimensionType(id);
        if (attribute.id() == null) {
            throw ApiException.invalidRequest("An id is required to update an attribute.");
        }
        var entity = this.dimensionAttributeRepository.findById(attribute.id())
                .orElseThrow(() -> ApiException.notFound(ATTRIBUTE));
        if (!entity.getDimensionType().getId().equals(dimensionType.getId())) {
            throw ApiException.notOwnedBy(ATTRIBUTE, attribute.id(), "dimension", id);
        }
        entity.setAttributeKey(attribute.attributeKey());
        entity.setOperator(attribute.operator());
        entity.setAttributeValue(attribute.attributeValue());
        var saved = this.dimensionAttributeRepository.update(entity);
        return DimensionAttributeResponseDTO.DimensionAttributeDTO.createDTO(saved);
    }

    @Override
    public DimensionAttributeResponseDTO.DimensionAttributeDTO deleteAttributeById(UUID id, UUID attributeId) {
        var dimensionType = requireDimensionType(id);
        var entity = this.dimensionAttributeRepository.findById(attributeId)
                .orElseThrow(() -> ApiException.notFound(ATTRIBUTE));
        if (!entity.getDimensionType().getId().equals(dimensionType.getId())) {
            throw ApiException.notOwnedBy(ATTRIBUTE, attributeId, "dimension", id);
        }
        this.dimensionAttributeRepository.deleteById(entity.getId());
        return DimensionAttributeResponseDTO.DimensionAttributeDTO.createDTO(entity);
    }

    @Override
    public List<DimensionAttributeResponseDTO.DimensionAttributeDTO> deleteAllAttributesById(UUID id) {
        var dimensionType = requireDimensionType(id);
        List<DimensionAttributeEntity> attributes =
                this.dimensionAttributeRepository.findAttributesByDimensionTypeId(dimensionType.getId(), Pageable.unpaged()).getContent();
        this.dimensionAttributeRepository.deleteAll(attributes);
        return attributes.stream()
                .map(DimensionAttributeResponseDTO.DimensionAttributeDTO::createDTO)
                .toList();
    }

    @Override
    public Page<DimensionTimelineResponseDTO.DimensionTimelineDTO> findTimelinesById(UUID id, Pageable pageable) {
        return this.dimensionTimelineRepository.findTimelinesByDimensionTypeId(id, pageable)
                .map(DimensionTimelineResponseDTO.DimensionTimelineDTO::createDTO);
    }

    @Override
    public DimensionTimelineResponseDTO.DimensionTimelineDTO createTimelineById(UUID id, DimensionTimelineDTO timeline) {
        var dimensionType = requireDimensionType(id);
        var entity = timeline.toEntity();
        entity.setDimensionType(dimensionType);
        var saved = this.dimensionTimelineRepository.save(entity);
        return DimensionTimelineResponseDTO.DimensionTimelineDTO.createDTO(saved);
    }

    @Override
    public DimensionTimelineResponseDTO.DimensionTimelineDTO updateTimelineById(UUID id, DimensionTimelineDTO timeline) {
        var dimensionType = requireDimensionType(id);
        if (timeline.id() == null) {
            throw ApiException.invalidRequest("An id is required to update a timeline reference.");
        }
        var entity = this.dimensionTimelineRepository.findById(timeline.id())
                .orElseThrow(() -> ApiException.notFound(TIMELINE));
        if (!entity.getDimensionType().getId().equals(dimensionType.getId())) {
            throw ApiException.notOwnedBy(TIMELINE, timeline.id(), "dimension", id);
        }
        entity.setTimelineKey(timeline.timelineKey());
        var saved = this.dimensionTimelineRepository.update(entity);
        return DimensionTimelineResponseDTO.DimensionTimelineDTO.createDTO(saved);
    }

    @Override
    public DimensionTimelineResponseDTO.DimensionTimelineDTO deleteTimelineById(UUID id, UUID timelineId) {
        var dimensionType = requireDimensionType(id);
        var entity = this.dimensionTimelineRepository.findById(timelineId)
                .orElseThrow(() -> ApiException.notFound(TIMELINE));
        if (!entity.getDimensionType().getId().equals(dimensionType.getId())) {
            throw ApiException.notOwnedBy(TIMELINE, timelineId, "dimension", id);
        }
        this.dimensionTimelineRepository.deleteById(entity.getId());
        return DimensionTimelineResponseDTO.DimensionTimelineDTO.createDTO(entity);
    }

    @Override
    public List<DimensionTimelineResponseDTO.DimensionTimelineDTO> deleteAllTimelinesById(UUID id) {
        var dimensionType = requireDimensionType(id);
        List<DimensionTimelineEntity> timelines =
                this.dimensionTimelineRepository.findTimelinesByDimensionTypeId(dimensionType.getId(), Pageable.unpaged()).getContent();
        this.dimensionTimelineRepository.deleteAll(timelines);
        return timelines.stream()
                .map(DimensionTimelineResponseDTO.DimensionTimelineDTO::createDTO)
                .toList();
    }
}
