package net.onelitefeather.vulpes.backend.entity.pack;

import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import net.onelitefeather.vulpes.api.model.pack.PackMetaEntity;

import java.util.List;
import java.util.UUID;

/**
 * Reads the pack metadata of a project for the complete project export.
 */
@Repository
public interface PackMetaContentRepository extends JpaRepository<PackMetaEntity, UUID> {

    /**
     * Returns the pack metadata of the project with its enabled features and icon asset loaded, without paging.
     *
     * @param projectId the project of the packs
     * @return the pack metadata
     */
    @Query("SELECT p FROM PackMetaEntity p LEFT JOIN FETCH p.enabledFeatures LEFT JOIN FETCH p.iconAsset WHERE p.project.id = :projectId")
    List<PackMetaEntity> findPacksByProjectId(UUID projectId);
}
