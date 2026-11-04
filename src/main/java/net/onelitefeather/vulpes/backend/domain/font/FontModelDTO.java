package net.onelitefeather.vulpes.backend.domain.font;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Positive;
import net.onelitefeather.vulpes.api.model.FontEntity;
import net.onelitefeather.vulpes.api.model.asset.ProjectAssetEntity;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup.Create;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup.Update;

import java.util.List;
import java.util.UUID;

@Schema
@Serdeable
public record FontModelDTO(
        @Schema(description = "ID of the font model", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Null(groups = Create.class)
        @NotNull(groups = Update.class)
        UUID id,
        @Schema(description = "Model Name for the ui", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(groups = {Create.class, Update.class}) String uiName,
        @Schema(description = "Key for the generation", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(groups = {Create.class, Update.class})
        String key,
        @Schema(description = "Which provider should be used", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(groups = {Create.class, Update.class})
        String provider,
        @Schema(description = "Internal mapper variable", requiredMode = Schema.RequiredMode.REQUIRED)
        String mapper,
        @Schema(description = "The path to the texture", requiredMode = Schema.RequiredMode.REQUIRED)
        String texturePath,
        @Schema(description = "The comment", requiredMode = Schema.RequiredMode.REQUIRED)
        @Nullable
        String comment,
        @Schema(description = "The ascent property, negative values shift the glyphs down", requiredMode = Schema.RequiredMode.REQUIRED)
        int ascent,
        @Schema(description = "The height property", requiredMode = Schema.RequiredMode.REQUIRED)
        @Positive(groups = {Create.class, Update.class})
        int height,
        @Schema(
                description = "ID of the texture asset of the project the font uses, null for none",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        @Nullable
        UUID textureAssetId
) {

    /**
     * Converts a {@link FontModelDTO} to a {@link FontEntity} without texture asset.
     *
     * @param project the project this font belongs to
     * @return the converted entity
     */
    public @NotNull FontEntity toFontModel(ProjectEntity project) {
        return toFontModel(project, null);
    }

    /**
     * Converts a {@link FontModelDTO} to a {@link FontEntity}.
     *
     * @param project      the project this font belongs to
     * @param textureAsset the texture asset {@link #textureAssetId} names, already checked, or
     *                     {@code null} for none
     * @return the converted entity
     */
    public @NotNull FontEntity toFontModel(ProjectEntity project, @Nullable ProjectAssetEntity textureAsset) {
        FontEntity font = new FontEntity(
                id,
                uiName,
                key,
                provider,
                texturePath,
                comment,
                height,
                ascent,
                List.of(),
                project
        );
        font.setTextureAsset(textureAsset);
        return font;
    }
}
