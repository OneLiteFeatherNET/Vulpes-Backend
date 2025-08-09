package net.onelitefeather.vulpes.backend.domain.sound;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import net.onelitefeather.vulpes.api.model.sound.SoundEventEntity;
import net.onelitefeather.vulpes.backend.domain.error.ErrorResponse;

import java.util.UUID;

@Schema(description = "Response DTO for Sound model")
@Serdeable
public interface SoundResponseDTO {

    /**
     * The {@link SoundModelDTO} is used to represent a sound model in the system.
     *
     * @param id           the unique identifier of the sound model
     * @param uiName       the name to display in the UI
     * @param variableName the name used for variable generation
     * @param keyName      the key of the sound
     * @param subTitle     the subtitle displayed when the sound is played
     */
    @Schema(
            name = "SoundModelDTO",
            description = "Data transfer object for Sound models"
    )
    @Serdeable
    record SoundModelDTO(
            @Schema(description = "Id of the Model") UUID id,
            @Schema(description = "Name to display it in the ui") String uiName,
            @Schema(description = "The name which is used for the variable generation") String variableName,
            @Schema(description = "They key of the sound") String keyName,
            @Schema(description = "The subtitle which is display when the sound is played") String subTitle
    ) implements SoundResponseDTO {

        /**
         * Creates a new {@link SoundModelDTO} from a {@link SoundEventEntity}.
         *
         * @param event the SoundEventEntity to convert
         * @return a new SoundModelDTO instance
         */
        public static SoundModelDTO createDTO(SoundEventEntity event) {
            return new SoundModelDTO(
                    event.getId(),
                    event.getUiName(),
                    event.getVariableName(),
                    event.getKeyName(),
                    event.getSubTitle()
            );
        }
    }

    /**
     * The {@link SoundErrorDTO} is used to represent an error response for sound events.
     *
     * @param errorMessage the error message describing the issue
     */
    @Schema(
            name = "SoundErrorDTO",
            description = "Error message for Sound model"
    )
    @Serdeable
    record SoundErrorDTO(
            @Schema(description = "Error message") String errorMessage
    ) implements SoundResponseDTO, ErrorResponse {
    }
}
