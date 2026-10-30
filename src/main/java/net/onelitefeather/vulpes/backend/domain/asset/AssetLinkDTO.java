package net.onelitefeather.vulpes.backend.domain.asset;

import net.onelitefeather.vulpes.api.model.asset.AssetLinkRole;
import net.onelitefeather.vulpes.api.model.asset.AssetTargetType;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import net.onelitefeather.vulpes.api.model.asset.AssetLinkEntity;

import java.util.UUID;

/**
 * The response body of an asset link. The linked asset is embedded, so detail pages need one request.
 *
 * @param id         the unique identifier of the link
 * @param assetId    the linked asset
 * @param targetType the kind of the target model
 * @param targetId   the id of the target model
 * @param role       how the target uses the asset
 * @param asset      the linked asset
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 3.3.0
 */
@Schema(name = "ResponseAssetLinkDTO", description = "A link between an asset and a database model.")
@Serdeable
public record AssetLinkDTO(
        @Schema(description = "Id of the link") UUID id,
        @Schema(description = "Id of the linked asset") UUID assetId,
        @Schema(description = "Kind of the target model") AssetTargetType targetType,
        @Schema(description = "Id of the target model") UUID targetId,
        @Schema(description = "How the target uses the asset") AssetLinkRole role,
        @Schema(description = "The linked asset") AssetResponseDTO asset
) {

    /**
     * Creates the response body of a link.
     *
     * @param link  the stored link
     * @param asset the response body of the linked asset
     * @return the response body
     */
    public static AssetLinkDTO createDTO(AssetLinkEntity link, AssetResponseDTO asset) {
        return new AssetLinkDTO(
                link.getId(),
                link.getAsset().getId(),
                link.getTargetType(),
                link.getTargetId(),
                link.getRole(),
                asset
        );
    }
}
