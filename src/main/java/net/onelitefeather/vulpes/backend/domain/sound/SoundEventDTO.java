package net.onelitefeather.vulpes.backend.domain.sound;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import net.onelitefeather.vulpes.api.model.sound.SoundEventEntity;

import java.util.List;
import java.util.UUID;

/**
 * The {@link SoundEventDTO} is a data transfer object that represents a {@link SoundEventEntity}.
 * It is used to transfer sound event data between the backend and frontend.
 *
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
public final class SoundEventDTO {

    private final UUID id;
    private final String uiName;
    private final String variableName;
    private final String keyName;
    private final String subTitle;

    /**
     * Constructs a new {@link SoundEventDTO} with the specified parameters.
     *
     * @param id           the unique identifier of the sound event
     * @param uiName       the name to display in the UI
     * @param variableName the name used for variable generation
     * @param keyName      the key of the sound
     * @param subTitle     the subtitle displayed when the sound is played
     */
    public SoundEventDTO(
            @Schema(description = "Id of the Model", requiredMode = RequiredMode.REQUIRED) UUID id,
            @Schema(description = "Name to display it in the ui", requiredMode = RequiredMode.REQUIRED) String uiName,
            @Schema(description = "The name which is used for the variable generation", requiredMode = RequiredMode.REQUIRED) String variableName,
            @Schema(description = "They key of the sound", requiredMode = RequiredMode.REQUIRED) String keyName,
            @Schema(description = "The subtitle which is display when the sound is played", requiredMode = RequiredMode.REQUIRED) String subTitle
    ) {
        this.id = id;
        this.uiName = uiName;
        this.variableName = variableName;
        this.keyName = keyName;
        this.subTitle = subTitle;
    }

    /**
     * Gets the unique identifier of the sound event.
     *
     * @return the UUID of the sound event
     */
    public UUID getId() {
        return id;
    }

    /**
     * Gets the name to display in the UI.
     *
     * @return the UI name of the sound event
     */
    public String getUiName() {
        return uiName;
    }

    /**
     * Gets the name used for variable generation.
     *
     * @return the variable name of the sound event
     */
    public String getVariableName() {
        return variableName;
    }

    /**
     * Gets the key of the sound.
     *
     * @return the key name of the sound event
     */
    public String getKeyName() {
        return keyName;
    }

    /**
     * Gets the subtitle displayed when the sound is played.
     *
     * @return the subtitle of the sound event
     */
    public String getSubTitle() {
        return subTitle;
    }

    /**
     * Converts this DTO to a {@link SoundEventEntity}.
     *
     * @return a new {@link SoundEventEntity} instance with the data from this DTO
     */
    public SoundEventEntity toEntity() {
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
