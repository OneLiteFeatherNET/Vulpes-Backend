package net.onelitefeather.vulpes.backend.service.impl;

import java.util.List;
import java.util.UUID;
import net.onelitefeather.vulpes.api.model.asset.ProjectAssetEntity;
import net.onelitefeather.vulpes.backend.domain.asset.AssetResponseDTO;
import org.jspecify.annotations.NullMarked;

/**
 * Maps stored assets to their response bodies. The mapping lives in the service layer, so the
 * response DTO does not depend on the entity.
 */
@NullMarked
public final class AssetResponseMapper {

    private AssetResponseMapper() {}

    /**
     * Creates the response body of an asset.
     *
     * @param asset           the stored asset
     * @param derivedAssetIds the ids of the textures extracted from it, empty for other kinds
     * @return the response body
     */
    public static AssetResponseDTO toDto(ProjectAssetEntity asset, List<UUID> derivedAssetIds) {
        return new AssetResponseDTO(
                asset.getId(),
                asset.getProject().getId(),
                asset.getKey(),
                asset.getDisplayName(),
                asset.getKind(),
                asset.getFileName(),
                asset.getContentType(),
                asset.getSize(),
                asset.getSha1(),
                asset.getWidth(),
                asset.getHeight(),
                asset.getSourceAsset() == null ? null : asset.getSourceAsset().getId(),
                List.copyOf(derivedAssetIds),
                asset.getCreationDate(),
                asset.getModificationDate());
    }
}
