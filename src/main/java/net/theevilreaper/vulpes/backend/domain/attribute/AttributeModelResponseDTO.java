package net.theevilreaper.vulpes.backend.domain.attribute;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import net.theevilreaper.vulpes.api.model.AttributeModel;
import net.theevilreaper.vulpes.backend.domain.error.ErrorResponse;

import java.util.UUID;

@Schema(description = "Response DTO for Attribute Model")
@Serdeable
public interface AttributeModelResponseDTO {

    @Schema(description = "Attribute Model Data")
    @Serdeable
    record AttributeModelDTO(
            @Schema(description = "UUID of the Attribute Model") UUID id,
            @Schema(description = "Model Name for the UI") String modelName,
            @Schema(description = "Name of the attribute in the UI") String name,
            @Schema(description = "Default value of the attribute") double defaultValue,
            @Schema(description = "Maximum value of the attribute") double maximumValue
    ) implements AttributeModelResponseDTO {

        public static AttributeModelDTO create(AttributeModel model) {
            return new AttributeModelDTO(
                    model.getId(),
                    model.getModelName(),
                    model.getName(),
                    model.getDefaultValue(),
                    model.getMaximumValue()
            );
        }
    }

    @Schema(description = "Error message for Attribute Model")
    @Serdeable
    record AttributeModelErrorDTO(
            @Schema(description = "Error message") String errorMessage
    ) implements AttributeModelResponseDTO, ErrorResponse {
    }
}
