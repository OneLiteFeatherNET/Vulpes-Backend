package net.onelitefeather.vulpes.backend.domain.item;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import net.onelitefeather.vulpes.api.model.item.ItemFlagEntity;

import java.util.UUID;


@Schema(
        requiredProperties = {
                "id",
                "flag"
        }
)
@Introspected
@Serdeable
public record ItemFlagDTO(
        @Schema(description = "ID of the Model", requiredMode = Schema.RequiredMode.NOT_REQUIRED) UUID id,
        @Schema(description = "Flag of the Model", requiredMode = Schema.RequiredMode.REQUIRED) String flag
) {

    public ItemFlagEntity toEntity() {
        ItemFlagEntity entity = new ItemFlagEntity();
        entity.setId(this.id);
        entity.setFlag(this.flag);
        return entity;
    }
}
