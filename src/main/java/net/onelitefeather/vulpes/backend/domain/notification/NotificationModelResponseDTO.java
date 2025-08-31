package net.onelitefeather.vulpes.backend.domain.notification;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import net.onelitefeather.vulpes.api.model.NotificationEntity;
import net.onelitefeather.vulpes.backend.domain.error.ErrorResponse;

import java.util.UUID;

@Schema(description = "Response DTO for Notification Model")
@Serdeable
public sealed interface NotificationModelResponseDTO {

    /**
     * Represents a response DTO for notification models that includes the model's details.
     *
     * @param id            the UUID of the notification model
     * @param uiName        the name to display in the UI
     * @param variableName  the name used for variable generation
     * @param description   a description of the notification
     * @param material      the material type of the notification
     * @param frameType     the frame type of the notification
     * @param title         the title of the notification
     */
    @Schema(
            name = "ResponseNotificationModelDTO",
            description = "Notification Model Data"
    )
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

        /**
         * Creates a DTO from a NotificationEntity.
         *
         * @param notificationModel the NotificationEntity to convert
         * @return a new NotificationModelDTO instance
         */
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

    /**
     * Represents an error response for notification models.
     *
     * @param errorMessage the error message describing the issue
     */
    @Schema(
            name = "NotificationModelErrorDTO",
            description = "Error message for Notification Model"
    )
    @Serdeable
    record NotificationModelErrorDTO(
            @Schema(description = "Error message") String errorMessage
    ) implements NotificationModelResponseDTO, ErrorResponse {
    }
}
