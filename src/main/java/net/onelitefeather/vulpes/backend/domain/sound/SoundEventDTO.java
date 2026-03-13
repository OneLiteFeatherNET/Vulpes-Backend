package net.onelitefeather.vulpes.backend.domain.sound;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import net.onelitefeather.vulpes.api.model.sound.SoundEventEntity;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup;

import java.util.List;
import java.util.UUID;

import static net.onelitefeather.vulpes.backend.validation.ValidationGroup.*;

/**
 * The {@link SoundEventDTO} is a data transfer object that represents a {@link SoundEventEntity}.
 * It is used to transfer sound event data between the backend and frontend.
 *
 * @param id           the unique identifier of the sound event
 * @param uiName       the name to display in the UI
 * @param variableName the name used for variable generation
 * @param keyName      the key of the sound
 * @param subTitle     the subtitle displayed when the sound is played
 * @author theEvilReaper
 * @version 1.0.0
 * @since 0.1.0
 */
@Schema(
        requiredProperties = {
                "id",
                "uiName",
                "variableName",
                "keyName",
                "replace",
                "subTitle"
        }
)
@Introspected
@Serdeable
public record SoundEventDTO(
        @Schema(description = "Id of the Model", requiredMode = RequiredMode.REQUIRED)
        @Null(groups = Create.class)
        @NotNull(groups = {Update.class})
        UUID id,
        @Schema(description = "Name to display it in the ui", requiredMode = RequiredMode.REQUIRED)
        @NotBlank(groups = {Create.class, Update.class})
        String uiName,
        @Schema(description = "The name which is used for the variable generation", requiredMode = RequiredMode.REQUIRED)
        @Null(groups = {Create.class, Update.class})
        String variableName,
        @Schema(description = "They key of the sound", requiredMode = RequiredMode.REQUIRED)
        @Null(groups = {Create.class, Update.class})
        String keyName,
        @Schema(description = "The subtitle which is display when the sound is played", requiredMode = RequiredMode.REQUIRED)
        @Null(groups = {Create.class, Update.class})
        String subTitle
) {
    /**
     * Converts this DTO to a {@link SoundEventEntity}.
     *
     * @return a new {@link SoundEventEntity} instance with the data from this DTO
     */
    public @NotNull SoundEventEntity toEntity() {
        return new SoundEventEntity(
                id,
                uiName,
                variableName,
                keyName,
                false,
                subTitle,
                List.of()
        );
    }
}
