package net.onelitefeather.vulpes.backend.domain.pack;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import net.onelitefeather.vulpes.api.model.pack.PackMetaEntity;
import net.onelitefeather.vulpes.api.model.pack.PackType;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Schema(description = "Response dto for the Pack Model")
@Serdeable
public sealed interface PackResponse {

    /**
     * Represents the stored {@code pack.mcmeta} content of a project for one pack type.
     *
     * @param id               the ID of the pack meta
     * @param projectId        the ID of the owning project
     * @param packType         the type of pack described
     * @param description      the description as a JSON text component
     * @param minFormatMajor   the minimum supported major format
     * @param minFormatMinor   the minimum supported minor format
     * @param maxFormatMajor   the maximum supported major format
     * @param maxFormatMinor   the maximum supported minor format, {@code null} for any minor version
     * @param enabledFeatures  the identifiers of the enabled experimental feature flags
     * @param creationDate     the point in time at which the pack meta was created
     * @param modificationDate the point in time at which the pack meta was last modified
     */
    @Schema(name = "ResponsePackMetaDTO", description = "Pack meta data")
    @Serdeable
    record PackMetaDTO(
            @Schema(description = "The id of the pack meta", requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
            @Schema(description = "ID of the project this pack meta belongs to", requiredMode = Schema.RequiredMode.REQUIRED) UUID projectId,
            @Schema(description = "The type of pack described", requiredMode = Schema.RequiredMode.REQUIRED) PackType packType,
            @Schema(description = "The description as a JSON text component", requiredMode = Schema.RequiredMode.REQUIRED) String description,
            @Schema(description = "The minimum supported major format", requiredMode = Schema.RequiredMode.REQUIRED) int minFormatMajor,
            @Schema(description = "The minimum supported minor format", requiredMode = Schema.RequiredMode.REQUIRED) int minFormatMinor,
            @Schema(description = "The maximum supported major format", requiredMode = Schema.RequiredMode.REQUIRED) int maxFormatMajor,
            @Schema(description = "The maximum supported minor format, null for any minor version") @Nullable Integer maxFormatMinor,
            @Schema(description = "The identifiers of the enabled experimental feature flags", requiredMode = Schema.RequiredMode.REQUIRED) Set<String> enabledFeatures,
            @Schema(description = "The point in time at which the pack meta was created") Instant creationDate,
            @Schema(description = "The point in time at which the pack meta was last modified") Instant modificationDate
    ) implements PackResponse {

        /**
         * Converts a {@link PackMetaEntity} to a {@link PackMetaDTO}.
         *
         * @param entity the entity to convert
         * @return a new dto instance
         */
        public static PackMetaDTO createDTO(PackMetaEntity entity) {
            return new PackMetaDTO(
                    entity.getId(),
                    entity.getProject().getId(),
                    entity.getPackType(),
                    entity.getDescription(),
                    entity.getMinFormatMajor(),
                    entity.getMinFormatMinor(),
                    entity.getMaxFormatMajor(),
                    entity.getMaxFormatMinor(),
                    Set.copyOf(entity.getEnabledFeatures()),
                    entity.getCreationDate(),
                    entity.getModificationDate()
            );
        }
    }
}
