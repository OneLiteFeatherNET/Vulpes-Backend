package net.onelitefeather.vulpes.backend.domain.item;

import io.micronaut.json.tree.JsonNode;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Response DTO for Item Component Model")
@Serdeable
public interface ItemComponentResponseDTO {

    /**
     * Represents a response DTO for item components.
     *
     * @param id           the unique identifier of the component entry
     * @param componentKey the key of the data component
     * @param value        the value in the vanilla JSON format
     */
    @Schema(
            name = "ResponseItemComponentDTO",
            description = "Item Component DTO"
    )
    @Serdeable
    record ItemComponentDTO(
            @Schema(description = "Component entry ID") UUID id,
            @Schema(description = "Key of the data component") String componentKey,
            @Schema(description = "Value in the vanilla JSON format", implementation = Object.class) JsonNode value
    ) implements ItemComponentResponseDTO {
    }
}
