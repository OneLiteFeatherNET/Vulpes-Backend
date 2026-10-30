package net.onelitefeather.vulpes.backend.domain.asset;

import net.onelitefeather.vulpes.api.model.asset.AssetKind;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import net.onelitefeather.vulpes.api.model.asset.ProjectAssetEntity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * The response body of a project asset.
 *
 * @param id              the unique identifier of the asset
 * @param projectId       the project the asset belongs to
 * @param key             the Minecraft resource location, unique per project
 * @param displayName     the name the asset is shown by
 * @param kind            the kind of the asset
 * @param fileName        the file name of the upload
 * @param contentType     the media type the content is served with
 * @param size            the size of the content in bytes
 * @param sha1            the SHA-1 digest of the content in lowercase hex
 * @param width           the width in pixels, only for textures
 * @param height          the height in pixels, only for textures
 * @param sourceAssetId   the Blockbench model a texture was extracted from, otherwise {@code null}
 * @param derivedAssetIds the textures extracted from this Blockbench model, otherwise empty
 * @param creationDate    the point in time at which the asset was created
 * @param modificationDate the point in time at which the asset was last modified
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 3.3.0
 */
@Schema(name = "ResponseAssetDTO", description = "A project asset: texture, model or Blockbench model.")
@Serdeable
@JsonInclude(JsonInclude.Include.ALWAYS)
public record AssetResponseDTO(
        @Schema(description = "Id of the asset") UUID id,
        @Schema(description = "Project the asset belongs to") UUID projectId,
        @Schema(description = "Minecraft resource location without file extension", example = "vulpes:item/ruby") String key,
        @Schema(description = "Name the asset is shown by", example = "Ruby") String displayName,
        @Schema(description = "Kind of the asset") AssetKind kind,
        @Schema(description = "File name of the upload", example = "ruby.png") String fileName,
        @Schema(description = "Media type of the content", example = "image/png") String contentType,
        @Schema(description = "Size of the content in bytes") long size,
        @Schema(description = "SHA-1 digest of the content, lowercase hex") String sha1,
        @Schema(description = "Width in pixels, textures only") @Nullable Integer width,
        @Schema(description = "Height in pixels, textures only") @Nullable Integer height,
        @Schema(description = "Blockbench model this texture was extracted from") @Nullable UUID sourceAssetId,
        @Schema(description = "Textures extracted from this Blockbench model") List<UUID> derivedAssetIds,
        @Schema(description = "The point in time at which the asset was created") Instant creationDate,
        @Schema(description = "The point in time at which the asset was last modified") Instant modificationDate
) {

    /**
     * Creates the response body of an asset.
     *
     * @param asset           the stored asset
     * @param derivedAssetIds the ids of the textures extracted from it, empty for other kinds
     * @return the response body
     */
    public static AssetResponseDTO createDTO(ProjectAssetEntity asset, List<UUID> derivedAssetIds) {
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
                asset.getModificationDate()
        );
    }
}
