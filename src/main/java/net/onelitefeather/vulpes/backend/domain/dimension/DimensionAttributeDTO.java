package net.onelitefeather.vulpes.backend.domain.dimension;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import net.onelitefeather.vulpes.api.model.dimension.AttributeOperator;
import net.onelitefeather.vulpes.api.model.dimension.DimensionAttributeEntity;
import net.onelitefeather.vulpes.api.model.dimension.EnvironmentAttributeKey;

import java.util.UUID;

import static net.onelitefeather.vulpes.backend.validation.ValidationGroup.*;

@Schema()
@Introspected
@Serdeable
public record DimensionAttributeDTO(
        @Schema(description = "ID of the attribute entry", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Null(groups = Create.class)
        @NotNull(groups = {Update.class})
        UUID id,
        @Schema(description = "Key of the environment attribute", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(groups = {Create.class, Update.class})
        EnvironmentAttributeKey attributeKey,
        @Schema(description = "Operator combining the argument with the base value", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(groups = {Create.class, Update.class})
        AttributeOperator operator,
        @Schema(description = "Serialized argument of the attribute", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(groups = {Create.class, Update.class})
        String attributeValue
) {

    public DimensionAttributeEntity toEntity() {
        DimensionAttributeEntity entity = new DimensionAttributeEntity();
        entity.setId(this.id);
        entity.setAttributeKey(this.attributeKey);
        entity.setOperator(this.operator);
        entity.setAttributeValue(this.attributeValue);
        return entity;
    }
}
