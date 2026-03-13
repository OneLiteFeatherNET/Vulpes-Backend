package net.onelitefeather.vulpes.backend.domain.font;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import net.onelitefeather.vulpes.api.model.font.FontStringEntity;
import net.onelitefeather.vulpes.backend.domain.error.ErrorResponse;

import java.util.UUID;

@Schema(description = "Response DTO for Font String Model")
@Serdeable
public interface FontStringResponseDTO {

    /**
     * Represents a response DTO for font string id.
     *
     * @param id    the unique identifier of the font string id
     * @param line  the line of the font string
     * @param orderIndex the order index of the font string
     */
    @Schema(
            name = "ResponseFontStringDTO",
            description = "Font String DTO"
    )
    @Serdeable
    record FontStringDTO(
            @Schema(description = "Font String ID") UUID id,
            @Schema(description = "Font String Line") String line,
            @Schema(description = "Font String order index") int orderIndex
    ) implements FontStringResponseDTO {

        public static FontStringResponseDTO createDTO(FontStringEntity entity) {
            return new FontStringDTO(entity.getId(), entity.getLine(), entity.getOrderIndex());
        }

    }

    /**
     * The {@link FontStringResponseDTO.FontStringErrorDTO} is used to represent an error response for Enchantment events.
     *
     * @param errorMessage the error message describing the issue
     */
    @Schema(
            name = "ResponseFontStringErrorDTO",
            description = "Error message for Font String model"
    )
    @Serdeable
    record FontStringErrorDTO(
            @Schema(description = "Error message") String errorMessage
    ) implements FontStringResponseDTO, ErrorResponse {
    }

}
