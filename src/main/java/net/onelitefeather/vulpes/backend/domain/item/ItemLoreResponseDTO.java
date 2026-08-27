package net.onelitefeather.vulpes.backend.domain.item;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import net.onelitefeather.vulpes.api.model.item.ItemLoreEntity;

import java.util.UUID;

@Schema(description = "Response DTO for Item Lore Model")
@Serdeable
public interface ItemLoreResponseDTO {
    /**
     * Represents a response DTO for item lore.
     *
     * @param id    the unique identifier of the lore
     * @param text  the text of the lore
     * @param orderIndex the orderIndex of the lore
     */
    @Schema(
            name = "ResponseItemLoreDTO",
            description = "Item Lore DTO"
    )
    @Serdeable
    record ItemLoreDTO(
            @Schema(description = "Lore ID") UUID id,
            @Schema(description = "Lore Text") String text,
            @Schema(description = "Lore Sort Index") int orderIndex
    ) implements ItemLoreResponseDTO {

        public static ItemLoreDTO createDTO(ItemLoreEntity entity) {
            return new ItemLoreDTO(entity.getId(), entity.getText(), entity.getOrderIndex());
        }

    }
}
