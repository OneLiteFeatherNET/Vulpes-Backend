package net.onelitefeather.vulpes.backend.domain.attribute;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import net.onelitefeather.vulpes.api.model.AttributeEntity;
import net.onelitefeather.vulpes.backend.domain.error.ErrorResponse;

import java.util.UUID;

@Schema(description = "Response DTO for Attribute Model")
@Serdeable
public interface AttributeModelResponseDTO {

    /**
     * The {@link AttributeModelDTO} is used to represent an attribute model in the system.
     *
     * @param id           the unique identifier of the attribute model
     * @param uiName       the name to display in the UI
     * @param variableName the name used for variable generation
     * @param defaultValue the default value of the attribute
     * @param maximumValue the maximum value of the attribute
     */
    @Schema(
            name = "ResponseAttributeModelDTO",
            description = "Attribute Model Data"
    )
    @Serdeable
    record AttributeModelDTO(
            @Schema(description = "UUID of the Attribute Model") UUID id,
            @Schema(description = "The name for the ui") String uiName,
            @Schema(description = "The name which represents the variable after the generation") String variableName,
            @Schema(description = "Default value of the attribute") double defaultValue,
            @Schema(description = "Maximum value of the attribute") double maximumValue
    ) implements AttributeModelResponseDTO {

        /**
         * Creates a DTO from an AttributeEntity.
         *
         * @param model the AttributeEntity to convert
         * @return a new AttributeModelDTO instance
         */
        public static AttributeModelDTO create(AttributeEntity model) {
            return new AttributeModelDTO(
                    model.getId(),
                    model.getUiName(),
                    model.getVariableName(),
                    model.getDefaultValue(),
                    model.getMaximumValue()
            );
        }
    }

    /**
     * The {@link AttributeModelErrorDTO} is used to represent an error response for attribute models.
     *
     * @param errorMessage the error message describing the issue
     */
    @Schema(
            name = "AttributeModelErrorDTO",
            description = "Error message for Attribute Model"
    )
    @Serdeable
    record AttributeModelErrorDTO(
            @Schema(description = "Error message") String errorMessage
    ) implements AttributeModelResponseDTO, ErrorResponse {
    }
}
