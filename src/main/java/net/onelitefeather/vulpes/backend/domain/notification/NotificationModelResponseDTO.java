package net.onelitefeather.vulpes.backend.domain.notification;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import net.onelitefeather.vulpes.api.model.NotificationEntity;
import net.onelitefeather.vulpes.backend.domain.error.ErrorResponse;

import java.util.UUID;

@Schema(description = "Response DTO for Notification Model")
@Serdeable
public sealed interface NotificationModelResponseDTO {

    @Schema(description = "Notification Model Data")
    @Serdeable
    record NotificationModelDTO(
            @Schema(description = "The id of the model") UUID id,
            @Schema(description = "Model Name for the UI") String uiName,
            @Schema(description = "Variable name for the generation") String variableName,
            @Schema(description = "Description of the Notification") String description,
            @Schema(description = "Material type of the Notification") String material,
            @Schema(description = "Frame type of the Notification") String frameType,
            @Schema(description = "Title of the Notification") String title
    ) implements NotificationModelResponseDTO {
        public static NotificationModelDTO createDTO(NotificationEntity notificationModel) {
            return new NotificationModelDTO(
                    notificationModel.getId(),
                    notificationModel.getUiName(),
                    notificationModel.getVariableName(),
                    notificationModel.getComment(),
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
    ) implements NotificationModelResponseDTO, ErrorResponse {
    }
}
