package net.theevilreaper.vulpes.backend.domain.notification;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import net.theevilreaper.vulpes.api.model.NotificationModel;

import java.util.UUID;

@Schema(description = "Response DTO for Notification Model")
@Serdeable
public sealed interface NotificationModelResponseDTO {

    @Schema(description = "Notification Model Data")
    @Serdeable
    record NotificationModelDTO(
            @Schema(description = "UUID of the Notification Model") UUID id,
            @Schema(description = "Model Name for the UI") String modelName,
            @Schema(description = "Name in the UI") String name,
            @Schema(description = "Description of the Notification") String description,
            @Schema(description = "Material type of the Notification") String material,
            @Schema(description = "Frame type of the Notification") String frameType,
            @Schema(description = "Title of the Notification") String title
    ) implements NotificationModelResponseDTO {
        public static NotificationModelDTO createDTO(NotificationModel notificationModel) {
            return new NotificationModelDTO(
                    notificationModel.getId(),
                    notificationModel.getModelName(),
                    notificationModel.getName(),
                    notificationModel.getDescription(),
                    notificationModel.getMaterial(),
                    notificationModel.getFrameType(),
                    notificationModel.getTitle()
            );
        }
    }

    @Schema(description = "Error message for Notification Model")
    @Serdeable
    record NotificationModelErrorDTO(
            @Schema(description = "Error message") String errorMessage
    ) implements NotificationModelResponseDTO {
    }
}
