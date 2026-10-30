package net.onelitefeather.vulpes.backend.entity.asset;

import net.onelitefeather.vulpes.api.model.asset.AssetLinkEntity;
import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import net.onelitefeather.vulpes.api.model.asset.AssetTargetType;

import java.util.List;
import java.util.UUID;

/**
 * Persistence access for {@link AssetLinkEntity}.
 *
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 3.3.0
 */
@Repository
public interface AssetLinkRepository extends JpaRepository<AssetLinkEntity, UUID> {

    List<AssetLinkEntity> findByAssetId(UUID assetId);

    List<AssetLinkEntity> findByTargetTypeAndTargetId(AssetTargetType targetType, UUID targetId);

    boolean existsByAssetIdAndTargetTypeAndTargetId(UUID assetId, AssetTargetType targetType, UUID targetId);
}
