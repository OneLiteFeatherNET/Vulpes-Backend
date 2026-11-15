package net.onelitefeather.vulpes.backend.domain.sound;

import io.micronaut.core.annotation.Creator;
import io.micronaut.core.annotation.Introspected;
import jakarta.annotation.Nullable;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.api.model.sound.SoundEventEntity;
import net.onelitefeather.vulpes.api.model.sound.SoundSource;
import net.onelitefeather.vulpes.backend.validation.ColumnLength;
import net.onelitefeather.vulpes.backend.validation.KeyFormat;
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
 * @param key          the key used for variable generation
 * @param keyName      the key of the sound
 * @param subTitle     the subtitle displayed when the sound is played
 * @param comment      an internal description of the sound event
 * @param minimumValue the lowest value the sound event accepts, from 0 to 1
 * @param source       the category the sound event plays in, {@link SoundSource#MASTER} when missing
 * @author theEvilReaper
 * @version 1.0.0
 * @since 0.1.0
 */
@Schema(
        requiredProperties = {
                "id",
                "uiName",
                "key",
                "keyName",
                "replace",
                "subTitle",
                "minimumValue"
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
        @Size(max = ColumnLength.VARCHAR, groups = {Create.class, Update.class})
        String uiName,
        @Schema(description = "The key which is used for the variable generation", requiredMode = RequiredMode.REQUIRED)
        @NotBlank(groups = {Create.class, Update.class})
        @Size(max = ColumnLength.VARCHAR, groups = {Create.class, Update.class})
        @Pattern(regexp = KeyFormat.PATH_PATTERN, message = KeyFormat.PATH_MESSAGE, groups = {Create.class, Update.class})
        String key,
        @Schema(description = "They key of the sound", requiredMode = RequiredMode.REQUIRED)
        @Size(max = ColumnLength.VARCHAR, groups = {Create.class, Update.class})
        String keyName,
        @Schema(description = "The subtitle which is display when the sound is played", requiredMode = RequiredMode.REQUIRED)
        @Size(max = ColumnLength.VARCHAR, groups = {Create.class, Update.class})
        String subTitle,
        @Schema(description = "Internal description of the sound event", requiredMode = RequiredMode.NOT_REQUIRED)
        @Nullable
        @Size(max = ColumnLength.VARCHAR, groups = {Create.class, Update.class})
        String comment,
        @Schema(description = "The lowest value the sound event accepts, from 0 to 1", requiredMode = RequiredMode.REQUIRED)
        @DecimalMin(value = "0.0", groups = {Create.class, Update.class})
        @DecimalMax(value = "1.0", groups = {Create.class, Update.class})
        double minimumValue,
        @Schema(description = "The category the sound event plays in, MASTER when missing", requiredMode = RequiredMode.NOT_REQUIRED)
        @Nullable
        SoundSource source
) {

    /**
     * Falls back to the {@link SoundSource#MASTER} source when none is given, as a client that does not know
     * the field yet sends none. It is the constructor requests are read with.
     */
    @Creator
    public SoundEventDTO {
        if (source == null) {
            source = SoundSource.MASTER;
        }
    }

    /**
     * Creates a sound event that accepts any value and plays in the {@link SoundSource#MASTER} source.
     *
     * @param id       the unique identifier of the sound event
     * @param uiName   the name to display in the UI
     * @param key      the key used for variable generation
     * @param keyName  the key of the sound
     * @param subTitle the subtitle displayed when the sound is played
     * @param comment  an internal description of the sound event
     */
    public SoundEventDTO(UUID id, String uiName, String key, String keyName, String subTitle, @Nullable String comment) {
        this(id, uiName, key, keyName, subTitle, comment, 0.0, SoundSource.MASTER);
    }

    /**
     * Converts this DTO to a {@link SoundEventEntity}.
     *
     * @param project the project this sound event belongs to
     * @return a new {@link SoundEventEntity} instance with the data from this DTO
     */
    public @NotNull SoundEventEntity toEntity(ProjectEntity project) {
        SoundEventEntity entity = new SoundEventEntity(
                id,
                uiName,
                key,
                keyName,
                false,
                subTitle,
                comment,
                List.of(),
                project
        );
        entity.setMinimumValue(minimumValue);
        entity.setSource(source);
        return entity;
    }
}
