package net.theevilreaper.vulpes.backend.domain.font;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.ElementCollection;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import net.theevilreaper.vulpes.api.model.FontModel;

import java.util.List;
import java.util.UUID;

@Schema()
@Serdeable
public final class FontModelDTO {

    private final UUID id;
    private final String modelName;
    private final String name;
    private final String description;
    private final String type;
    private final int ascent;
    private final int height;
    private final List<String> chars;
    private final List<Double> shift;

    public FontModelDTO(
            @Schema(description = "ID of the mode", requiredMode = Schema.RequiredMode.NOT_REQUIRED) UUID id,
            @Schema(description = "Model Name for the ui", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @NotEmpty String modelName,
            @Schema(description = "Name in the UI", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @NotEmpty String name,
            @Schema(description = "Example description", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @NotEmpty String description,
            @Schema(description = "Example description", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @NotEmpty String type,
            @Schema(description = "Example description", requiredMode = Schema.RequiredMode.REQUIRED) @PositiveOrZero int ascent,
            @Schema(description = "Example description", requiredMode = Schema.RequiredMode.REQUIRED) @PositiveOrZero int height,
            @Schema(description = "Example description", requiredMode = Schema.RequiredMode.NOT_REQUIRED) List<String> chars,
            @Schema(description = "Example description", requiredMode = Schema.RequiredMode.NOT_REQUIRED) List<Double> shift) {
        this.id = id;
        this.modelName = modelName;
        this.name = name;
        this.description = description;
        this.type = type;
        this.ascent = ascent;
        this.height = height;
        this.chars = chars;
        this.shift = shift;
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

    public String getType() {
        return type;
    }

    public int getAscent() {
        return ascent;
    }

    public int getHeight() {
        return height;
    }

    public List<String> getChars() {
        return chars;
    }

    public List<Double> getShift() {
        return shift;
    }

    public FontModel toFontModel() {
        return new FontModel(
                id,
                modelName,
                name,
                description,
                type,
                ascent,
                height,
                chars,
                shift
        );
    }
}
