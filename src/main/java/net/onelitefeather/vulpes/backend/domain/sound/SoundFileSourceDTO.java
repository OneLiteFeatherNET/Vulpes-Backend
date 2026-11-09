package net.onelitefeather.vulpes.backend.domain.sound;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import net.onelitefeather.vulpes.api.model.sound.SoundFileSource;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup;
import net.onelitefeather.vulpes.backend.validation.ColumnLength;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

import static net.onelitefeather.vulpes.backend.validation.ValidationGroup.*;

@Schema(
        requiredProperties = {
                "name",
                "volume",
                "pitch",
                "weight",
                "stream",
                "attenuationDistance",
                "preload",
                "type"
        }
)
@Introspected
@Serdeable
public record SoundFileSourceDTO(
        @Null(groups = Create.class)
        @NotNull(groups = {Update.class})
        UUID id,
        @NotBlank(groups = {Create.class, Update.class})
        @Size(max = ColumnLength.VARCHAR, groups = {Create.class, Update.class})
        String name,
        @PositiveOrZero(groups = {Create.class, Update.class})
        float volume,
        @DecimalMin(value = "0.5", groups = {Create.class, Update.class})
        @DecimalMax(value = "2.0", groups = {Create.class, Update.class})
        float pitch,
        @Positive(groups = {Create.class, Update.class})
        int weight,
        boolean stream,
        @Positive(groups = {Create.class, Update.class})
        int attenuationDistance,
        boolean preload,
        @NotBlank(groups = {Create.class, Update.class})
        @Size(max = ColumnLength.VARCHAR, groups = {Create.class, Update.class})
        String type,
        @Schema(description = "Sound asset of the project the source plays. When set, type must be 'file' and"
                + " name the key of the asset. Omit it for a file that is not uploaded, such as a vanilla sound.",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Nullable
        UUID assetId
) {

    /** The type of a source that plays a sound file. */
    public static final String TYPE_FILE = "file";

    /**
     * Converts the DTO to an entity
     *
     * @return the converted entity
     */
    public @NotNull SoundFileSource toEntity() {
        return new SoundFileSource(id, name, volume, pitch, weight, stream, attenuationDistance, preload, type);
    }
}
