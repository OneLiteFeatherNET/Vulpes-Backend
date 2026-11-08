package net.onelitefeather.vulpes.backend.entity.attribute;

import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import net.onelitefeather.vulpes.api.model.AttributeEntity;

import java.util.List;
import java.util.UUID;

/**
 * Reads every attribute of a project for the complete project export.
 */
@Repository
public interface AttributeContentRepository extends JpaRepository<AttributeEntity, UUID> {

    /**
     * Returns all attributes of the project, without paging.
     *
     * @param projectId the project of the attributes
     * @return the attributes
     */
    @Query("SELECT a FROM attributes a WHERE a.project.id = :projectId")
    List<AttributeEntity> findAttributesByProjectId(UUID projectId);
}
