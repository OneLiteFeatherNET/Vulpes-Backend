package net.onelitefeather.vulpes.backend.domain.font;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.PositiveOrZero;
import net.onelitefeather.vulpes.api.model.FontEntity;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup.Create;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup.Update;

import java.util.List;
import java.util.UUID;

@Schema
@Serdeable
public record FontModelDTO(
        @Schema(description = "ID of the font model", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Null(groups = Create.class)
        @NotNull(groups = Update.class)
        UUID id,
        @Schema(description = "Model Name for the ui", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(groups = {Create.class, Update.class}) String uiName,
        @Schema(description = "Name in the UI", requiredMode = Schema.RequiredMode.REQUIRED)
        @Null(groups = Create.class)
        @NotBlank(groups = Update.class)
        String variableName,
        @Schema(description = "Which provider should be used", requiredMode = Schema.RequiredMode.REQUIRED)
        @Null(groups = {Create.class, Update.class})
        @NotBlank
        String provider,
        @Schema(description = "Internal mapper variable", requiredMode = Schema.RequiredMode.REQUIRED)
        @Null(groups = {Create.class, Update.class})
        String mapper,
        @Schema(description = "The path to the texture", requiredMode = Schema.RequiredMode.REQUIRED)
        @Null(groups = {Create.class, Update.class})
        String texturePath,
        @Schema(description = "The comment", requiredMode = Schema.RequiredMode.REQUIRED)
        @Nullable
        String comment,
        @Schema(description = "The ascent property", requiredMode = Schema.RequiredMode.REQUIRED)
        @PositiveOrZero
        int ascent,
        @Schema(description = "The height property", requiredMode = Schema.RequiredMode.REQUIRED)
        @PositiveOrZero
        int height
) {

    /**
     * Converts a {@link FontModelDTO} to a {@link FontEntity}.
     *
     * @return the converted entity
     */
    public @NotNull FontEntity toFontModel() {
        return new FontEntity(
                id,
                uiName,
                variableName,
                provider,
                texturePath,
                comment,
                height,
                ascent,
                List.of()
        );
    }
}
