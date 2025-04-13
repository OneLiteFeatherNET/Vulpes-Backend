package net.theevilreaper.vulpes.backend.domain.attribute;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import net.theevilreaper.vulpes.api.model.AttributeModel;

import java.util.UUID;

@Schema()
@Serdeable
public class AttributeModelDTO {
    private final UUID id;
    private final String modelName;
    private final String name;
    private final double defaultValue;
    private final double maximumValue;


    public AttributeModelDTO(
            @Schema(description = "ID of the attribute", requiredMode = Schema.RequiredMode.NOT_REQUIRED) UUID id,
            @Schema(description = "Model name of the attribute", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @NotEmpty String modelName,
            @Schema(description = "Name of the attribute", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @NotEmpty String name,
            @Schema(description = "Default value of the attribute", requiredMode = Schema.RequiredMode.REQUIRED) double defaultValue,
            @Schema(description = "Maximum value of the attribute", requiredMode = Schema.RequiredMode.REQUIRED) double maximumValue) {
        this.id = id;
        this.modelName = modelName;
        this.name = name;
        this.defaultValue = defaultValue;
        this.maximumValue = maximumValue;
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

    public double getDefaultValue() {
        return defaultValue;
    }

    public double getMaximumValue() {
        return maximumValue;
    }

    public AttributeModel toAttributeModel() {
        return new AttributeModel(id, modelName, name, defaultValue, maximumValue);
    }
}
