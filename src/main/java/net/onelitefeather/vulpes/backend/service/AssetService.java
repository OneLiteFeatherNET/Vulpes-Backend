package net.onelitefeather.vulpes.backend.service;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import net.onelitefeather.vulpes.backend.domain.asset.AssetContent;
import net.onelitefeather.vulpes.backend.domain.asset.AssetKind;
import net.onelitefeather.vulpes.backend.domain.asset.AssetResponseDTO;
import net.onelitefeather.vulpes.backend.domain.asset.AssetUploadDTO;
import net.onelitefeather.vulpes.backend.entity.asset.ProjectAssetEntity;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * Service interface for managing project assets: textures, models and Blockbench models.
 *
 * <p>The bytes are kept in object storage, the metadata in the database. Uploading a Blockbench model
 * also stores the textures embedded in it as assets of their own.
 */
@NullMarked
public interface AssetService extends CrudService<ProjectAssetEntity, UUID, AssetUploadDTO, AssetResponseDTO> {

    /**
     * Returns one asset of a project.
     *
     * @param projectId the project the asset must belong to
     * @param id        the asset
     * @return the asset
     * @throws net.onelitefeather.vulpes.backend.exception.ApiException {@code RESOURCE_NOT_FOUND} if the
     *         asset does not exist or belongs to another project
     */
    AssetResponseDTO getAsset(UUID projectId, UUID id);

    /**
     * Returns the assets of a project, optionally only one kind.
     *
     * @param projectId the project
     * @param kind      the kind to filter by, {@code null} for all kinds
     * @param pageable  pagination details
     * @return a page of assets
     */
    Page<AssetResponseDTO> getAll(UUID projectId, @Nullable AssetKind kind, Pageable pageable);

    /**
     * Returns the stored bytes of an asset.
     *
     * @param projectId the project the asset must belong to
     * @param id        the asset
     * @return the bytes, their media type and their digest
     */
    AssetContent getContent(UUID projectId, UUID id);

    /**
     * Replaces the bytes of an asset. The new file must have the same kind. For a Blockbench model the
     * textures are extracted again.
     *
     * @param projectId the project the asset must belong to
     * @param id        the asset
     * @param upload    the new file
     * @return the updated asset
     */
    AssetResponseDTO replaceContent(UUID projectId, UUID id, AssetUploadDTO upload);
}
