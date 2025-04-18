package net.onelitefeather.vulpes.backend.domain.notification;

import java.util.UUID;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import net.theevilreaper.vulpes.api.model.NotificationModel;

@Schema(requiredProperties = {
        "modelName",
        "name",
        "description",
        "material",
        "frameType",
        "title"
})
@Introspected
@Serdeable
public final class NotificationModelDTO {
    private final UUID id;
    private final String modelName;
    private final String name;
    private final String description;
    private final String material;
    private final String frameType;
    private final String title;

    public NotificationModelDTO(
            @Schema(description = "ID of the notification", requiredMode = Schema.RequiredMode.NOT_REQUIRED) UUID id,
            @Schema(description = "Model name for the UI", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @NotBlank @NotEmpty String modelName,
            @Schema(description = "Name in the UI", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @NotBlank @NotEmpty String name,
            @Schema(description = "Description of the notification", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @NotBlank @NotEmpty String description,
            @Schema(description = "Material identifier", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @NotBlank @NotEmpty String material,
            @Schema(description = "Type of frame", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @NotBlank @NotEmpty String frameType,
            @Schema(description = "Title of the notification", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @NotBlank @NotEmpty String title) {
        this.id = id;
        this.modelName = modelName;
        this.name = name;
        this.description = description;
        this.material = material;
        this.frameType = frameType;
        this.title = title;
    }

    public UUID getId() {
        return id;
    }

    public String getModelName() {
        return modelName;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getMaterial() {
        return material;
    }

    public String getFrameType() {
        return frameType;
    }

    public String getTitle() {
        return title;
    }

    public NotificationModel toNotificationModel() {
        return new NotificationModel(
                this.id,
                modelName,
                name,
                description,
                material,
                frameType,
                title
        );
    }
}