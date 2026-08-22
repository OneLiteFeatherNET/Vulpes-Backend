package net.onelitefeather.vulpes.backend.domain.item;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

@Schema(description = "Request DTO to reorder a lore entry of an item")
@Introspected
@Serdeable
public record ItemLoreReorderDTO(
        @Schema(description = "ID of the lore entry to move", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        UUID entryId,
        @Schema(description = "Target index of the lore entry within the lore list", requiredMode = Schema.RequiredMode.REQUIRED)
        @PositiveOrZero
        int newIndex
) {
}
