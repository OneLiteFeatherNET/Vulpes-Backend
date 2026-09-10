package net.onelitefeather.vulpes.backend.domain.dimension;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import net.onelitefeather.vulpes.api.model.dimension.AttributeOperator;
import net.onelitefeather.vulpes.api.model.dimension.DimensionAttributeEntity;
import net.onelitefeather.vulpes.api.model.dimension.EnvironmentAttributeKey;

import java.util.UUID;

@Schema(description = "Response DTO for Dimension Attribute Model")
@Serdeable
public interface DimensionAttributeResponseDTO {

    /**
     * Represents a response DTO for a dimension attribute entry.
     *
     * @param id             the unique identifier of the attribute entry
     * @param attributeKey   the key identifying the environment attribute
     * @param operator       the operator used to combine the argument with the base value
     * @param attributeValue the serialized argument of the environment attribute
     */
    @Schema(
            name = "ResponseDimensionAttributeDTO",
            description = "Dimension Attribute DTO"
    )
    @Serdeable
    record DimensionAttributeDTO(
            @Schema(description = "Attribute ID") UUID id,
            @Schema(description = "Attribute Key") EnvironmentAttributeKey attributeKey,
            @Schema(description = "Operator") AttributeOperator operator,
            @Schema(description = "Serialized argument") String attributeValue
    ) implements DimensionAttributeResponseDTO {

        public static DimensionAttributeDTO createDTO(DimensionAttributeEntity entity) {
            return new DimensionAttributeDTO(
                    entity.getId(),
                    entity.getAttributeKey(),
                    entity.getOperator(),
                    entity.getAttributeValue()
            );
        }
    }
}
