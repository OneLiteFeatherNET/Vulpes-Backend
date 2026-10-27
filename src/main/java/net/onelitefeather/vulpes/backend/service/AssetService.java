package net.onelitefeather.vulpes.backend.service;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import java.util.UUID;
import net.onelitefeather.vulpes.api.model.asset.AssetKind;
import net.onelitefeather.vulpes.api.model.asset.ProjectAssetEntity;
import net.onelitefeather.vulpes.backend.domain.asset.AssetContent;
import net.onelitefeather.vulpes.backend.domain.asset.AssetResponseDTO;
import net.onelitefeather.vulpes.backend.domain.asset.AssetRevisionResponseDTO;
import net.onelitefeather.vulpes.backend.domain.asset.AssetUploadDTO;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

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
     * @param ifMatch   the If-Match value the client holds, or {@code null} to replace unconditionally
     * @return the updated asset
     */
    AssetResponseDTO replaceContent(UUID projectId, UUID id, AssetUploadDTO upload, @Nullable String ifMatch);

    /**
     * Returns the revisions of an asset, newest first. An asset created before revisions existed gets its
     * revision 1 here.
     *
     * @param projectId the project the asset must belong to
     * @param id        the asset
     * @param pageable  pagination details
     * @return a page of revisions
     */
    Page<AssetRevisionResponseDTO> getRevisions(UUID projectId, UUID id, Pageable pageable);

    /**
     * Returns the stored bytes of one revision of an asset.
     *
     * @param projectId  the project the asset must belong to
     * @param id         the asset
     * @param revisionId the revision, which must belong to the asset
     * @return the bytes, their media type and the digest of the revision
     */
    AssetContent getRevisionContent(UUID projectId, UUID id, UUID revisionId);

    /**
     * Makes an earlier revision the current content by writing it as a new revision.
     *
     * @param projectId  the project the asset must belong to
     * @param id         the asset
     * @param revisionId the revision to restore
     * @param ifMatch    the If-Match value the client holds, or {@code null} to restore unconditionally
     * @param comment    the comment of the new revision, or {@code null} for the default
     * @return the updated asset
     */
    AssetResponseDTO restoreRevision(
            UUID projectId, UUID id, UUID revisionId, @Nullable String ifMatch, @Nullable String comment);
}
