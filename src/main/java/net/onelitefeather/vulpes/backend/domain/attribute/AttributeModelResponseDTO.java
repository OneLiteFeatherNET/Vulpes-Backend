package net.onelitefeather.vulpes.backend.domain.attribute;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import net.onelitefeather.vulpes.api.model.AttributeEntity;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Response DTO for Attribute Model")
@Serdeable
public interface AttributeModelResponseDTO {

    @Schema(
            name = "ResponseAttributeModelDTO",
            description = "Attribute Model Data"
    )
    @Serdeable
    record AttributeModelDTO(
            @Schema(description = "UUID of the Attribute Model") UUID id,
            @Schema(description = "The name for the ui") String uiName,
            @Schema(description = "The key which represents the variable after the generation") String key,
            @Schema(description = "Default value of the attribute") double defaultValue,
            @Schema(description = "Minimum value of the attribute") double minimumValue,
            @Schema(description = "Maximum value of the attribute") double maximumValue,
            @Schema(description = "Whether the value of the attribute is sent to the client") boolean synced,
            @Schema(description = "Translation key for the name of the attribute") String translationKey,
            @Schema(description = "Internal description of the attribute") String comment,
            @Schema(description = "ID of the project this attribute belongs to") UUID projectId,
            @Schema(description = "The point in time at which the attribute was created") Instant creationDate,
            @Schema(description = "The point in time at which the attribute was last modified") Instant modificationDate
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
                    model.getKey(),
                    model.getDefaultValue(),
                    model.getMinimumValue(),
                    model.getMaximumValue(),
                    model.isSynced(),
                    model.getTranslationKey(),
                    model.getComment(),
                    model.getProject().getId(),
                    model.getCreationDate(),
                    model.getModificationDate()
            );
        }
    }
}
