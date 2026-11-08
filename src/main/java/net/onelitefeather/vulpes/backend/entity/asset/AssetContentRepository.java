package net.onelitefeather.vulpes.backend.entity.asset;

import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import net.onelitefeather.vulpes.api.model.asset.ProjectAssetEntity;
import net.onelitefeather.vulpes.api.model.asset.ProjectAssetRevisionEntity;

import java.util.List;
import java.util.UUID;

/**
 * Reads the assets of a project and their revision rows for the complete project export. Reads only:
 * an asset without revision rows stays without them.
 */
@Repository
public interface AssetContentRepository extends JpaRepository<ProjectAssetEntity, UUID> {

    /**
     * Returns all assets of the project, without paging.
     *
     * @param projectId the project of the assets
     * @return the assets
     */
    @Query("SELECT a FROM ProjectAssetEntity a WHERE a.project.id = :projectId")
    List<ProjectAssetEntity> findAssetsByProjectId(UUID projectId);

    /**
     * Returns the revision rows of all assets of the project.
     *
     * @param projectId the project of the assets
     * @return the revision rows, with their asset
     */
    @Query("SELECT r FROM ProjectAssetRevisionEntity r WHERE r.asset.project.id = :projectId")
    List<ProjectAssetRevisionEntity> findRevisionsByProjectId(UUID projectId);
}
