package net.onelitefeather.vulpes.backend.domain.asset;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * The request body for linking an asset to a database model.
 *
 * @param targetType the kind of the target model
 * @param targetId   the id of the target model
 * @param role       how the target uses the asset
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 3.3.0
 */
@Schema(name = "AssetLinkRequestDTO", description = "Links an asset to an item, font or advancement.")
@Serdeable
public record AssetLinkRequestDTO(
        @Schema(description = "Kind of the target model", requiredMode = RequiredMode.REQUIRED)
        @NotNull
        AssetTargetType targetType,

        @Schema(description = "Id of the target model", requiredMode = RequiredMode.REQUIRED)
        @NotNull
        UUID targetId,

        @Schema(description = "How the target uses the asset", requiredMode = RequiredMode.REQUIRED)
        @NotNull
        AssetLinkRole role
) {
}
