package net.onelitefeather.vulpes.backend.entity.asset;

import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
import net.onelitefeather.vulpes.api.model.asset.AssetLinkEntity;

/**
 * Read access to the links between project assets and the elements that use them.
 */
@Repository
public interface AssetLinkRepository extends JpaRepository<AssetLinkEntity, UUID> {

    /**
     * Returns the links of all assets of the project, without paging.
     *
     * @param projectId the project of the assets
     * @return the links, with their asset
     */
    @Query("SELECT l FROM AssetLinkEntity l WHERE l.asset.project.id = :projectId")
    List<AssetLinkEntity> findLinksByProjectId(UUID projectId);
}
