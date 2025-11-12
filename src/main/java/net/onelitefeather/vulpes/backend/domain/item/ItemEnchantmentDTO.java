package net.onelitefeather.vulpes.backend.domain.item;


import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import net.onelitefeather.vulpes.api.model.item.ItemEnchantmentEntity;

import java.util.UUID;

@Schema(
        requiredProperties = {
                "id",
                "name",
                "level"
        }
)
@Introspected
@Serdeable
public record ItemEnchantmentDTO(
        @Schema(description = "ID of the Model", requiredMode = Schema.RequiredMode.NOT_REQUIRED) UUID id,
        @Schema(description = "Name of the enchantment", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank String name,
        @Schema(description = "Level of the enchantment", requiredMode = Schema.RequiredMode.REQUIRED) Short level
) {


    public ItemEnchantmentEntity toEntity() {
        ItemEnchantmentEntity entity = new ItemEnchantmentEntity();
        entity.setId(this.id);
        entity.setName(this.name);
        entity.setLevel(this.level);
        return entity;
    }
}
