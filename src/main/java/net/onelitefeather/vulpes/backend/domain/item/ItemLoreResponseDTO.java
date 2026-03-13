package net.onelitefeather.vulpes.backend.domain.item;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import net.onelitefeather.vulpes.api.model.item.ItemLoreEntity;
import net.onelitefeather.vulpes.backend.domain.error.ErrorResponse;

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

        public static ItemLoreResponseDTO createDTO(ItemLoreEntity entity) {
            return new ItemLoreDTO(entity.getId(), entity.getText(), entity.getOrderIndex());
        }

    }

    /**
     * The {@link ItemLoreResponseDTO.ItemLoreErrorDTO} is used to represent an error response for lore entry.
     *
     * @param errorMessage the error message describing the issue
     */
    @Schema(
            name = "ResponseItemLoreErrorDTO",
            description = "Error message for lore model"
    )
    @Serdeable
    record ItemLoreErrorDTO(
            @Schema(description = "Error message") String errorMessage
    ) implements ItemLoreResponseDTO, ErrorResponse {
    }
}
