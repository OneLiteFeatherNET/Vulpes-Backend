package net.onelitefeather.vulpes.backend.entity.dimension;

import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import net.onelitefeather.vulpes.api.model.dimension.DimensionAttributeEntity;
import net.onelitefeather.vulpes.api.model.dimension.DimensionTimelineEntity;
import net.onelitefeather.vulpes.api.model.dimension.DimensionTypeEntity;

import java.util.List;
import java.util.UUID;

/**
 * Reads every dimension type of a project and the attributes and timelines of those types for the
 * complete project export.
 */
@Repository
public interface DimensionContentRepository extends JpaRepository<DimensionTypeEntity, UUID> {

    /**
     * Returns all dimension types of the project, without paging.
     *
     * @param projectId the project of the dimension types
     * @return the dimension types
     */
    @Query("SELECT d FROM dimension_types d WHERE d.project.id = :projectId")
    List<DimensionTypeEntity> findDimensionsByProjectId(UUID projectId);

    /**
     * Returns the attributes of all dimension types of the project.
     *
     * @param projectId the project of the dimension types
     * @return the attributes, with their dimension type
     */
    @Query("SELECT a FROM dimension_attributes a WHERE a.dimensionType.project.id = :projectId")
    List<DimensionAttributeEntity> findAttributesByProjectId(UUID projectId);

    /**
     * Returns the timelines of all dimension types of the project.
     *
     * @param projectId the project of the dimension types
     * @return the timelines, with their dimension type
     */
    @Query("SELECT t FROM dimension_timelines t WHERE t.dimensionType.project.id = :projectId")
    List<DimensionTimelineEntity> findTimelinesByProjectId(UUID projectId);
}
