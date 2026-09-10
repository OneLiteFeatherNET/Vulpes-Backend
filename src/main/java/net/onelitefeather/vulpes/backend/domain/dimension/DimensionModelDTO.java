package net.onelitefeather.vulpes.backend.domain.dimension;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import net.onelitefeather.vulpes.api.model.dimension.CardinalLight;
import net.onelitefeather.vulpes.api.model.dimension.DimensionTypeEntity;
import net.onelitefeather.vulpes.api.model.dimension.Skybox;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;

import java.util.List;
import java.util.UUID;

import static net.onelitefeather.vulpes.backend.validation.ValidationGroup.*;

@Schema
@Introspected
@Serdeable
public record DimensionModelDTO(
        @Schema(description = "ID of the dimension type", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Null(groups = Create.class)
        @NotNull(groups = Update.class)
        UUID id,
        @Schema(description = "User interface name of the dimension type", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(groups = {Create.class, Update.class})
        String uiName,
        @Schema(description = "Variable name of the dimension type", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(groups = {Create.class, Update.class})
        String variableName,
        @Schema(description = "Whether the dimension type has a fixed time", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean hasFixedTime,
        @Schema(description = "Whether the dimension type has skylight", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean hasSkylight,
        @Schema(description = "Whether the dimension type has a ceiling", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean hasCeiling,
        @Schema(description = "Whether the dimension type has the ender dragon fight", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean hasEnderDragonFight,
        @Schema(description = "Coordinate scale of the dimension type", requiredMode = Schema.RequiredMode.REQUIRED)
        @DecimalMin("0.00001")
        @DecimalMax("30000000.0")
        double coordinateScale,
        @Schema(description = "Minimum build height of the dimension type", requiredMode = Schema.RequiredMode.REQUIRED)
        @Min(-2032)
        @Max(2031)
        int minY,
        @Schema(description = "Height of the dimension type", requiredMode = Schema.RequiredMode.REQUIRED)
        @Min(16)
        @Max(4064)
        int height,
        @Schema(description = "Logical height of the dimension type", requiredMode = Schema.RequiredMode.REQUIRED)
        int logicalHeight,
        @Schema(description = "Infiniburn block tag of the dimension type", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(groups = {Create.class, Update.class})
        String infiniburn,
        @Schema(description = "Ambient light of the dimension type", requiredMode = Schema.RequiredMode.REQUIRED)
        float ambientLight,
        @Schema(description = "Serialized monster spawn light level provider", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(groups = {Create.class, Update.class})
        String monsterSpawnLightLevel,
        @Schema(description = "Monster spawn block light limit of the dimension type", requiredMode = Schema.RequiredMode.REQUIRED)
        @Min(0)
        @Max(15)
        int monsterSpawnBlockLightLimit,
        @Schema(description = "Skybox of the dimension type", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(groups = {Create.class, Update.class})
        Skybox skybox,
        @Schema(description = "Cardinal light behaviour of the dimension type", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(groups = {Create.class, Update.class})
        CardinalLight cardinalLight,
        @Schema(description = "Registry key of the default clock", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Nullable
        String defaultClock
) {

    /**
     * Converts this DTO to a {@link DimensionTypeEntity}, scoped to the given project.
     * <p>
     * Attributes and timelines are not part of this DTO - they are managed through their own
     * sub-resource endpoints - so the resulting entity carries empty lists for both.
     *
     * @param project the project this dimension type belongs to
     * @return a new {@link DimensionTypeEntity} instance with the data from this DTO
     */
    public DimensionTypeEntity toEntity(ProjectEntity project) {
        return new DimensionTypeEntity(
                id,
                uiName,
                variableName,
                hasFixedTime,
                hasSkylight,
                hasCeiling,
                hasEnderDragonFight,
                coordinateScale,
                minY,
                height,
                logicalHeight,
                infiniburn,
                ambientLight,
                monsterSpawnLightLevel,
                monsterSpawnBlockLightLimit,
                skybox,
                cardinalLight,
                defaultClock,
                List.of(),
                List.of(),
                project
        );
    }
}
