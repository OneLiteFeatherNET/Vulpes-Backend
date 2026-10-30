package net.onelitefeather.vulpes.backend.service;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.validation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.Default;
import net.onelitefeather.vulpes.api.model.dimension.DimensionTypeEntity;
import net.onelitefeather.vulpes.backend.domain.dimension.DimensionAttributeDTO;
import net.onelitefeather.vulpes.backend.domain.dimension.DimensionAttributeResponseDTO;
import net.onelitefeather.vulpes.backend.domain.dimension.DimensionModelDTO;
import net.onelitefeather.vulpes.backend.domain.dimension.DimensionModelResponseDTO;
import net.onelitefeather.vulpes.backend.domain.dimension.DimensionTimelineDTO;
import net.onelitefeather.vulpes.backend.domain.dimension.DimensionTimelineResponseDTO;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for managing dimension types.
 */
public interface DimensionService
        extends CrudService<DimensionTypeEntity, UUID, DimensionModelDTO, DimensionModelResponseDTO.DimensionModelDTO> {

    /**
     * Gets the environment attributes of a dimension type by its ID.
     *
     * @param id       the ID of the dimension type
     * @param pageable pagination information
     * @return a page of attributes
     */
    Page<DimensionAttributeResponseDTO.DimensionAttributeDTO> findAttributesById(@NotNull UUID id, @NotNull Pageable pageable);

    /**
     * Creates an environment attribute for a dimension type by its ID.
     *
     * @param id        the ID of the dimension type
     * @param attribute the attribute to create
     * @return the created attribute
     */
    @Validated(groups = {Default.class, ValidationGroup.Create.class})
    DimensionAttributeResponseDTO.DimensionAttributeDTO createAttributeById(@NotNull UUID id, @NotNull @Valid DimensionAttributeDTO attribute);

    /**
     * Updates an environment attribute of a dimension type by its ID.
     *
     * @param id        the ID of the dimension type
     * @param attribute the attribute to update
     * @return the updated attribute
     */
    @Validated(groups = {Default.class, ValidationGroup.Update.class})
    DimensionAttributeResponseDTO.DimensionAttributeDTO updateAttributeById(@NotNull UUID id, @NotNull @Valid DimensionAttributeDTO attribute);

    /**
     * Deletes an environment attribute of a dimension type by its ID.
     *
     * @param id          the ID of the dimension type
     * @param attributeId the ID of the attribute to delete
     * @return the deleted attribute
     */
    DimensionAttributeResponseDTO.DimensionAttributeDTO deleteAttributeById(@NotNull UUID id, @NotNull UUID attributeId);

    /**
     * Deletes all environment attributes of a dimension type by its ID.
     *
     * @param id the ID of the dimension type
     * @return the deleted attributes
     */
    List<DimensionAttributeResponseDTO.DimensionAttributeDTO> deleteAllAttributesById(@NotNull UUID id);

    /**
     * Gets the timeline references of a dimension type by its ID.
     *
     * @param id       the ID of the dimension type
     * @param pageable pagination information
     * @return a page of timeline references
     */
    Page<DimensionTimelineResponseDTO.DimensionTimelineDTO> findTimelinesById(@NotNull UUID id, @NotNull Pageable pageable);

    /**
     * Creates a timeline reference for a dimension type by its ID.
     *
     * @param id       the ID of the dimension type
     * @param timeline the timeline reference to create
     * @return the created timeline reference
     */
    @Validated(groups = {Default.class, ValidationGroup.Create.class})
    DimensionTimelineResponseDTO.DimensionTimelineDTO createTimelineById(@NotNull UUID id, @NotNull @Valid DimensionTimelineDTO timeline);

    /**
     * Updates a timeline reference of a dimension type by its ID.
     *
     * @param id       the ID of the dimension type
     * @param timeline the timeline reference to update
     * @return the updated timeline reference
     */
    @Validated(groups = {Default.class, ValidationGroup.Update.class})
    DimensionTimelineResponseDTO.DimensionTimelineDTO updateTimelineById(@NotNull UUID id, @NotNull @Valid DimensionTimelineDTO timeline);

    /**
     * Deletes a timeline reference of a dimension type by its ID.
     *
     * @param id         the ID of the dimension type
     * @param timelineId the ID of the timeline reference to delete
     * @return the deleted timeline reference
     */
    DimensionTimelineResponseDTO.DimensionTimelineDTO deleteTimelineById(@NotNull UUID id, @NotNull UUID timelineId);

    /**
     * Deletes all timeline references of a dimension type by its ID.
     *
     * @param id the ID of the dimension type
     * @return the deleted timeline references
     */
    List<DimensionTimelineResponseDTO.DimensionTimelineDTO> deleteAllTimelinesById(@NotNull UUID id);
}
