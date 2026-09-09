package net.onelitefeather.vulpes.backend.domain.dimension;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import net.onelitefeather.vulpes.api.model.dimension.CardinalLight;
import net.onelitefeather.vulpes.api.model.dimension.DimensionTypeEntity;
import net.onelitefeather.vulpes.api.model.dimension.Skybox;

import java.util.UUID;

@Schema(description = "Response DTO for Dimension Type Model")
@Serdeable
public interface DimensionModelResponseDTO {

    /**
     * Represents a response DTO for a dimension type.
     *
     * @param id                          the unique identifier of the dimension type
     * @param uiName                      the user interface name of the dimension type
     * @param variableName                the variable name of the dimension type
     * @param hasFixedTime                whether the dimension type has a fixed time
     * @param hasSkylight                 whether the dimension type has skylight
     * @param hasCeiling                  whether the dimension type has a ceiling
     * @param hasEnderDragonFight         whether the dimension type has the ender dragon fight
     * @param coordinateScale             the coordinate scale of the dimension type
     * @param minY                        the minimum build height of the dimension type
     * @param height                      the height of the dimension type
     * @param logicalHeight               the logical height of the dimension type
     * @param infiniburn                  the infiniburn block tag of the dimension type
     * @param ambientLight                the ambient light of the dimension type
     * @param monsterSpawnLightLevel      the serialized monster spawn light level provider
     * @param monsterSpawnBlockLightLimit the monster spawn block light limit
     * @param skybox                      the skybox of the dimension type
     * @param cardinalLight               the cardinal light behaviour of the dimension type
     * @param defaultClock                the registry key of the default clock, or {@code null}
     * @param projectId                   the id of the project this dimension type belongs to
     */
    @Schema(
            name = "ResponseDimensionModelDTO",
            description = "Dimension Type Model Data"
    )
    @Serdeable
    record DimensionModelDTO(
            @Schema(description = "UUID of the Dimension Type") UUID id,
            @Schema(description = "User interface name") String uiName,
            @Schema(description = "Variable name") String variableName,
            @Schema(description = "Whether it has a fixed time") boolean hasFixedTime,
            @Schema(description = "Whether it has skylight") boolean hasSkylight,
            @Schema(description = "Whether it has a ceiling") boolean hasCeiling,
            @Schema(description = "Whether it has the ender dragon fight") boolean hasEnderDragonFight,
            @Schema(description = "Coordinate scale") double coordinateScale,
            @Schema(description = "Minimum build height") int minY,
            @Schema(description = "Height") int height,
            @Schema(description = "Logical height") int logicalHeight,
            @Schema(description = "Infiniburn block tag") String infiniburn,
            @Schema(description = "Ambient light") float ambientLight,
            @Schema(description = "Serialized monster spawn light level provider") String monsterSpawnLightLevel,
            @Schema(description = "Monster spawn block light limit") int monsterSpawnBlockLightLimit,
            @Schema(description = "Skybox") Skybox skybox,
            @Schema(description = "Cardinal light behaviour") CardinalLight cardinalLight,
            @Schema(description = "Registry key of the default clock") String defaultClock,
            @Schema(description = "ID of the owning project") UUID projectId
    ) implements DimensionModelResponseDTO {

        /**
         * Creates a DTO from a {@link DimensionTypeEntity}.
         *
         * @param entity the entity to convert
         * @return a new DimensionModelDTO instance
         */
        public static DimensionModelDTO createDTO(DimensionTypeEntity entity) {
            return new DimensionModelDTO(
                    entity.getId(),
                    entity.getUiName(),
                    entity.getVariableName(),
                    entity.isHasFixedTime(),
                    entity.isHasSkylight(),
                    entity.isHasCeiling(),
                    entity.isHasEnderDragonFight(),
                    entity.getCoordinateScale(),
                    entity.getMinY(),
                    entity.getHeight(),
                    entity.getLogicalHeight(),
                    entity.getInfiniburn(),
                    entity.getAmbientLight(),
                    entity.getMonsterSpawnLightLevel(),
                    entity.getMonsterSpawnBlockLightLimit(),
                    entity.getSkybox(),
                    entity.getCardinalLight(),
                    entity.getDefaultClock(),
                    entity.getProject().getId()
            );
        }
    }
}
