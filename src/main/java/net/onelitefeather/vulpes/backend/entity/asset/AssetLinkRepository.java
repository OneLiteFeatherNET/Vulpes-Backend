package net.onelitefeather.vulpes.backend.entity.asset;

import io.micronaut.data.annotation.Query;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.onelitefeather.vulpes.api.model.asset.AssetLinkEntity;
import net.onelitefeather.vulpes.api.model.asset.AssetLinkRole;
import net.onelitefeather.vulpes.api.model.asset.AssetTargetType;

/**
 * Access to the links between project assets and the elements that use them.
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

    /**
     * Returns the link of one target in the given role.
     *
     * @param targetType the type of the target
     * @param targetId   the target
     * @param role       how the target uses the asset
     * @return the link, empty if the target has none in that role
     */
    Optional<AssetLinkEntity> findByTargetTypeAndTargetIdAndRole(
            AssetTargetType targetType, UUID targetId, AssetLinkRole role);

    /**
     * Returns the links of several targets in the given role. The collection must not be empty.
     *
     * @param targetType the type of the targets
     * @param targetIds  the targets
     * @param role       how the targets use the asset
     * @return the links, with their asset
     */
    @Query("SELECT l FROM AssetLinkEntity l WHERE l.targetType = :targetType AND l.targetId IN (:targetIds)"
            + " AND l.role = :role")
    List<AssetLinkEntity> findTargetLinks(AssetTargetType targetType, Collection<UUID> targetIds, AssetLinkRole role);

    /**
     * Counts the links of an asset in the given role.
     *
     * @param assetId the asset
     * @param role    how the targets use the asset
     * @return the number of links
     */
    @Query("SELECT COUNT(l) FROM AssetLinkEntity l WHERE l.asset.id = :assetId AND l.role = :role")
    long countAssetLinks(UUID assetId, AssetLinkRole role);

    /**
     * Deletes the links of several targets. The collection must not be empty.
     *
     * @param targetType the type of the targets
     * @param targetIds  the targets
     */
    @Query("DELETE FROM AssetLinkEntity l WHERE l.targetType = :targetType AND l.targetId IN (:targetIds)")
    void deleteTargetLinks(AssetTargetType targetType, Collection<UUID> targetIds);

    /**
     * Deletes the links of all assets of a project.
     *
     * @param projectId the project of the assets
     */
    @Query("DELETE FROM AssetLinkEntity l WHERE l.asset IN"
            + " (SELECT a FROM ProjectAssetEntity a WHERE a.project.id = :projectId)")
    void deleteProjectLinks(UUID projectId);
}
