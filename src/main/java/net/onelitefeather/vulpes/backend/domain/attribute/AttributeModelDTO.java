package net.onelitefeather.vulpes.backend.domain.attribute;

import io.micronaut.core.annotation.Introspected;
import jakarta.annotation.Nullable;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Positive;
import net.onelitefeather.vulpes.api.model.AttributeEntity;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;

import java.util.UUID;

import static net.onelitefeather.vulpes.backend.validation.ValidationGroup.*;

@Schema
@Serdeable
@Introspected
@ValidValueRange(groups = {Create.class, Update.class})
public record AttributeModelDTO(
        @Schema(description = "ID of the attribute", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Null(groups = Create.class)
        @NotNull(groups = Update.class)
        UUID id,
        @Schema(description = "The name for the ui", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(groups = {Create.class, Update.class})
        String uiName,
        @Schema(description = "The key which represents the variable after the generation", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(groups = {Create.class, Update.class})
        String key,
        @Schema(description = "Default value of the attribute", requiredMode = Schema.RequiredMode.REQUIRED)
        double defaultValue,
        @Schema(description = "Minimum value of the attribute", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        double minimumValue,
        @Schema(description = "Maximum value of the attribute", requiredMode = Schema.RequiredMode.REQUIRED)
        @Positive(groups = {Create.class, Update.class})
        double maximumValue,
        @Schema(description = "Whether the value of the attribute is sent to the client", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        boolean synced,
        @Schema(description = "Translation key for the name of the attribute", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Nullable
        String translationKey,
        @Schema(description = "Internal description of the attribute", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Nullable
        String comment
) {
    /**
     * Converts the dto class to a {@link AttributeEntity}.
     *
     * @param project the project this attribute belongs to
     * @return the created entity
     */
    public @NotNull AttributeEntity toAttributeModel(ProjectEntity project) {
        return new AttributeEntity(id, uiName, key, defaultValue, minimumValue, maximumValue, synced, translationKey,
                comment, project);
    }
}
