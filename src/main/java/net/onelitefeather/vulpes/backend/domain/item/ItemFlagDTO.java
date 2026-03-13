package net.onelitefeather.vulpes.backend.domain.item;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import net.onelitefeather.vulpes.api.model.item.ItemFlagEntity;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup;

import java.util.UUID;

import static net.onelitefeather.vulpes.backend.validation.ValidationGroup.*;

@Schema()
@Introspected
@Serdeable
public record ItemFlagDTO(
        @Schema(description = "ID of the Model", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Null(groups = Create.class)
        @NotNull(groups = {Update.class})
        UUID id,
        @Schema(description = "Flag of the Model", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(groups = {Create.class, Update.class})
        String flag
) {

    public ItemFlagEntity toEntity() {
        ItemFlagEntity entity = new ItemFlagEntity();
        entity.setId(this.id);
        entity.setFlag(this.flag);
        return entity;
    }
}
