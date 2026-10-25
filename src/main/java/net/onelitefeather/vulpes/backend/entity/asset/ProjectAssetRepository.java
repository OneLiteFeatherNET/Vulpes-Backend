package net.onelitefeather.vulpes.backend.entity.asset;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.jpa.repository.JpaRepository;
import net.onelitefeather.vulpes.backend.domain.asset.AssetKind;

import java.util.List;
import java.util.UUID;

/**
 * Persistence access for {@link ProjectAssetEntity}.
 *
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 3.3.0
 */
@Repository
public interface ProjectAssetRepository extends JpaRepository<ProjectAssetEntity, UUID> {

    Page<ProjectAssetEntity> findByProjectId(UUID projectId, Pageable pageable);

    Page<ProjectAssetEntity> findByProjectIdAndKind(UUID projectId, AssetKind kind, Pageable pageable);

    boolean existsByProjectIdAndKey(UUID projectId, String key);

    List<ProjectAssetEntity> findBySourceAssetId(UUID sourceAssetId);
}
