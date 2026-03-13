package net.onelitefeather.vulpes.backend.domain.notification;

import java.util.UUID;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import net.onelitefeather.vulpes.api.model.NotificationEntity;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup;

import static net.onelitefeather.vulpes.backend.validation.ValidationGroup.*;

@Schema(requiredProperties = {
        "uiName",
        "variableName",
        "comment",
        "material",
        "frameType",
        "title"
})
@Introspected
@Serdeable
public record NotificationModelDTO(
        @Schema(description = "ID of the notification", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Null(groups = Create.class)
        @NotNull(groups = {Update.class})
        UUID id,
        @Schema(description = "Model variableName for the UI", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(groups = {Create.class, Update.class}) String
        uiName,
        @Schema(description = "Name in the UI", requiredMode = Schema.RequiredMode.REQUIRED)
        @Null(groups = {Create.class, Update.class})
        String variableName,
        @Schema(description = "Comment of the notification", requiredMode = Schema.RequiredMode.REQUIRED)
        @Nullable
        String comment,
        @Schema(description = "Material identifier", requiredMode = Schema.RequiredMode.REQUIRED)
        @Null(groups = {Create.class, Update.class})
        String material,
        @Schema(description = "Type of frame", requiredMode = Schema.RequiredMode.REQUIRED)
        @Null(groups = {Create.class, Update.class})
        String frameType,
        @Schema(description = "Title of the notification", requiredMode = Schema.RequiredMode.REQUIRED)
        @Null(groups = {Create.class, Update.class})
        String title
) {

    /**
     * Converts this DTO to a {@link NotificationEntity}.
     *
     * @return a new {@link NotificationEntity} instance with the data from this DTO
     */
    public @NotNull NotificationEntity toNotificationModel() {
        return new NotificationEntity(
                this.id,
                uiName,
                variableName,
                comment,
                material,
                frameType,
                title
        );
    }
}