package net.onelitefeather.vulpes.backend.entity.asset;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.jpa.repository.JpaRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.onelitefeather.vulpes.api.model.asset.ProjectAssetRevisionEntity;

/**
 * Persistence access for {@link ProjectAssetRevisionEntity}.
 *
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 3.3.0
 */
@Repository
public interface ProjectAssetRevisionRepository extends JpaRepository<ProjectAssetRevisionEntity, UUID> {

    boolean existsByAssetId(UUID assetId);

    List<ProjectAssetRevisionEntity> findByAssetId(UUID assetId);

    Page<ProjectAssetRevisionEntity> findByAssetIdOrderByNumberDesc(UUID assetId, Pageable pageable);

    Optional<ProjectAssetRevisionEntity> findFirstByAssetIdOrderByNumberDesc(UUID assetId);

    Optional<ProjectAssetRevisionEntity> findByIdAndAssetId(UUID id, UUID assetId);
}
