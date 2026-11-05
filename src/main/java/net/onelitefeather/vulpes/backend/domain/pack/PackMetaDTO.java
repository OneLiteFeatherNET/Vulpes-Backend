package net.onelitefeather.vulpes.backend.domain.pack;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import net.onelitefeather.vulpes.api.model.asset.ProjectAssetEntity;
import net.onelitefeather.vulpes.api.model.pack.PackMetaEntity;
import net.onelitefeather.vulpes.api.model.pack.PackType;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;

import java.util.Set;
import java.util.UUID;

/**
 * The content of the {@code pack.mcmeta} file of a project for one {@link PackType}.
 *
 * <p>The pack type and the owning project are taken from the request path, so they are not part of the body.
 *
 * @param description     the description as a JSON text component
 * @param minFormatMajor  the minimum supported major format
 * @param minFormatMinor  the minimum supported minor format
 * @param maxFormatMajor  the maximum supported major format
 * @param maxFormatMinor  the maximum supported minor format, {@code null} for any minor version
 * @param enabledFeatures the identifiers of the enabled experimental feature flags, {@code null} for none
 * @param iconAssetId     the ID of the texture asset written as the {@code pack.png}, {@code null} for none
 */
@Schema(description = "Content of the pack.mcmeta of a project for one pack type")
@Introspected
@Serdeable
public record PackMetaDTO(
        @Schema(description = "The description as a JSON text component", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String description,
        @Schema(description = "The minimum supported major format", requiredMode = Schema.RequiredMode.REQUIRED)
        @Min(1)
        int minFormatMajor,
        @Schema(description = "The minimum supported minor format", requiredMode = Schema.RequiredMode.REQUIRED)
        @Min(0)
        int minFormatMinor,
        @Schema(description = "The maximum supported major format", requiredMode = Schema.RequiredMode.REQUIRED)
        @Min(1)
        int maxFormatMajor,
        @Schema(description = "The maximum supported minor format, null for any minor version", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Nullable
        @Min(0)
        Integer maxFormatMinor,
        @Schema(description = "The identifiers of the enabled experimental feature flags", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Nullable
        Set<String> enabledFeatures,
        @Schema(
                description = "ID of the texture asset of the project written as the pack.png, null for none",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        @Nullable
        UUID iconAssetId
) {

    /**
     * Checks that the maximum format is not below the minimum format.
     *
     * @return {@code true} if the range is valid
     */
    public boolean isFormatRangeValid() {
        if (maxFormatMajor != minFormatMajor) {
            return maxFormatMajor > minFormatMajor;
        }
        return maxFormatMinor == null || maxFormatMinor >= minFormatMinor;
    }

    /**
     * Returns the enabled feature flags, an empty set if none were sent.
     *
     * @return the enabled feature flags
     */
    public Set<String> features() {
        return enabledFeatures == null ? Set.of() : enabledFeatures;
    }

    /**
     * Converts this DTO to a new {@link PackMetaEntity}.
     *
     * @param project   the owning project
     * @param packType  the type of pack described
     * @param iconAsset the icon asset {@link #iconAssetId} names, already checked, or {@code null} for none
     * @return the new entity
     */
    public PackMetaEntity toEntity(ProjectEntity project, PackType packType, @Nullable ProjectAssetEntity iconAsset) {
        PackMetaEntity entity = new PackMetaEntity(
                null,
                project,
                packType,
                description,
                minFormatMajor,
                minFormatMinor,
                maxFormatMajor,
                maxFormatMinor,
                features()
        );
        entity.setIconAsset(iconAsset);
        return entity;
    }

    /**
     * Writes the values of this DTO onto an existing entity.
     *
     * @param entity    the entity to update
     * @param iconAsset the icon asset {@link #iconAssetId} names, already checked, or {@code null} for none
     */
    public void applyTo(PackMetaEntity entity, @Nullable ProjectAssetEntity iconAsset) {
        entity.setDescription(description);
        entity.setFormatRange(minFormatMajor, minFormatMinor, maxFormatMajor, maxFormatMinor);
        entity.setEnabledFeatures(features());
        entity.setIconAsset(iconAsset);
    }
}
