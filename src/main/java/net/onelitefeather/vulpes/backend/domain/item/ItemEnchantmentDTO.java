package net.onelitefeather.vulpes.backend.domain.item;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Positive;
import net.onelitefeather.vulpes.api.model.item.ItemEnchantmentEntity;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup;

import java.util.UUID;

import static net.onelitefeather.vulpes.backend.validation.ValidationGroup.*;

@Schema(requiredProperties = {
        "id",
        "name",
        "level",
        "unsafe",
})
@Introspected
@Serdeable
public record ItemEnchantmentDTO(
        @Schema(description = "ID of the Model", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Null(groups = Create.class)
        @NotNull(groups = {Update.class})
        UUID id,
        @Schema(description = "Name of the enchantment", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(groups = {Create.class, Update.class})
        String name,
        @Schema(description = "Level of the enchantment", requiredMode = Schema.RequiredMode.REQUIRED)
        @Positive
        short level,
        @Schema(description = "If the enchantment is unsafe", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean unsafe
) {

    public ItemEnchantmentEntity toEntity() {
        ItemEnchantmentEntity entity = new ItemEnchantmentEntity();
        entity.setId(this.id);
        entity.setName(this.name);
        entity.setLevel(this.level);
        entity.setUnsafe(this.unsafe);
        return entity;
    }
}
