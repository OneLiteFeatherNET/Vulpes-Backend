package net.onelitefeather.vulpes.backend.domain.attribute;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import net.onelitefeather.vulpes.api.model.AttributeEntity;

import java.util.UUID;

import static net.onelitefeather.vulpes.backend.validation.ValidationGroup.*;

@Schema
@Serdeable
@Introspected
public record AttributeModelDTO(
        @Schema(description = "ID of the attribute", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Null(groups = Create.class)
        @NotNull(groups = Update.class)
        UUID id,
        @Schema(description = "The name for the ui", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(groups = {Create.class, Update.class})
        String uiName,
        @Schema(description = "The name which represents the variable after the generation", requiredMode = Schema.RequiredMode.REQUIRED)
        @Null(groups = {Create.class})
        @NotBlank(groups = {Update.class})
        String variableName,
        @Schema(description = "Default value of the attribute", requiredMode = Schema.RequiredMode.REQUIRED)
        @PositiveOrZero double defaultValue,
        @Schema(description = "Maximum value of the attribute", requiredMode = Schema.RequiredMode.REQUIRED)
        @Positive double maximumValue
) {
    /**
     * Converts the dto class to a {@link AttributeEntity}.
     *
     * @return the created entity
     */
    public @NotNull AttributeEntity toAttributeModel() {
        return new AttributeEntity(id, uiName, variableName, defaultValue, maximumValue);
    }
}
