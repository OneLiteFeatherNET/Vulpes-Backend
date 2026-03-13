package net.onelitefeather.vulpes.backend.domain.item;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Positive;
import net.onelitefeather.vulpes.api.model.item.ItemLoreEntity;

import java.util.UUID;

import static net.onelitefeather.vulpes.backend.validation.ValidationGroup.*;

@Schema()
@Introspected
@Serdeable
public record ItemLoreDTO(
        @Schema(description = "ID of the Model", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Null(groups = Create.class)
        @NotNull(groups = {Update.class})
        UUID id,
        @Schema(description = "Text of the lore", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(groups = {Create.class, Update.class})
        String text,
        @Schema(description = "Order index of the lore", requiredMode = Schema.RequiredMode.REQUIRED)
        @Positive
        int orderIndex
) {


    public ItemLoreEntity toEntity() {
        ItemLoreEntity entity = new ItemLoreEntity();
        entity.setId(this.id);
        entity.setText(this.text);
        entity.setOrderIndex(this.orderIndex);
        return entity;
    }
}
