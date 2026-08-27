package net.onelitefeather.vulpes.backend.domain.item;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import net.onelitefeather.vulpes.api.model.item.ItemFlagEntity;

import java.util.UUID;

@Schema(description = "Response DTO for Item Flag Model")
@Serdeable
public interface ItemFlagResponseDTO {

    /**
     * Represents a response DTO for item flag.
     *
     * @param id    the unique identifier of the flag
     * @param flag  the flag of the flag
     */
    @Schema(
            name = "ResponseItemFlagDTO",
            description = "Item Flag DTO"
    )
    @Serdeable
    record ItemFlagDTO(
            @Schema(description = "Flag ID") UUID id,
            @Schema(description = "Flag Text") String flag
    ) implements ItemFlagResponseDTO {

        public static ItemFlagDTO createDTO(ItemFlagEntity entity) {
            return new ItemFlagDTO(entity.getId(), entity.getFlag());
        }

    }
}
