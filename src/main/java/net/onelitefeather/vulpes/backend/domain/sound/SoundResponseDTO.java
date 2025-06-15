package net.onelitefeather.vulpes.backend.domain.sound;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import net.onelitefeather.vulpes.api.model.sound.SoundEventEntity;
import net.onelitefeather.vulpes.backend.domain.error.ErrorResponse;

import java.util.UUID;

@Schema(description = "Response DTO for Sound model")
@Serdeable
public interface SoundResponseDTO {

    @Schema(description = "Notification Model Data")
    @Serdeable
    record SoundModelDTO(
            @Schema(description = "Id of the Model") UUID id,
            @Schema(description = "Name to display it in the ui") String uiName,
            @Schema(description = "The name which is used for the variable generation") String variableName,
            @Schema(description = "They key of the sound") String keyName,
            @Schema(description = "The subtitle which is display when the sound is played") String subTitle

    ) implements SoundResponseDTO {
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

    @Schema(description = "Error message for Sound model")
    @Serdeable
    record SoundErrorDTO(
            @Schema(description = "Error message") String errorMessage
    ) implements SoundResponseDTO, ErrorResponse {
    }
}
