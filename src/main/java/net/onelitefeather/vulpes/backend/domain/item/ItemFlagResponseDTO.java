package net.onelitefeather.vulpes.backend.domain.item;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import net.onelitefeather.vulpes.api.model.item.ItemFlagEntity;
import net.onelitefeather.vulpes.api.model.item.ItemLoreEntity;
import net.onelitefeather.vulpes.backend.domain.error.ErrorResponse;

import java.util.UUID;

@Schema(description = "Response DTO for Item Flag Model")
@Serdeable
public interface ItemFlagResponseDTO {

    /**
     * Represents a response DTO for item lore.
     *
     * @param id    the unique identifier of the flag
     * @param flag  the flag of the flag
     */
    @Schema(
            name = "ItemLoreDTO",
            description = "Item Lore DTO"
    )
    @Serdeable
    record ItemFlagDTO(
            @Schema(description = "Flag ID") UUID id,
            @Schema(description = "Flag Text") String flag
    ) implements ItemFlagResponseDTO {

        public static ItemFlagResponseDTO createDTO(ItemFlagEntity entity) {
            return new ItemFlagDTO(entity.getId(), entity.getFlag());
        }

    }

    /**
     * The {@link ItemFlagResponseDTO.ItemFlagErrorDTO} is used to represent an error response for flag entry.
     *
     * @param errorMessage the error message describing the issue
     */
    @Schema(
            name = "ResponseItemFlagErrorDTO",
            description = "Error message for flag model"
    )
    @Serdeable
    record ItemFlagErrorDTO(
            @Schema(description = "Error message") String errorMessage
    ) implements ItemFlagResponseDTO, ErrorResponse {
    }

}
