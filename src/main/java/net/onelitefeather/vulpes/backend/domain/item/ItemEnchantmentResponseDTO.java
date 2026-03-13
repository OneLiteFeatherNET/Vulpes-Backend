package net.onelitefeather.vulpes.backend.domain.item;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import net.onelitefeather.vulpes.api.model.item.ItemEnchantmentEntity;
import net.onelitefeather.vulpes.backend.domain.error.ErrorResponse;

import java.util.UUID;

@Schema(description = "Response DTO for Item Enchantment Model")
@Serdeable
public interface ItemEnchantmentResponseDTO {

    /**
     * Represents a response DTO for item enchantments.
     *
     * @param id    the unique identifier of the enchantment
     * @param name  the name of the enchantment
     * @param level the level of the enchantment
     */
    @Schema(
            name = "ResponseItemEnchantmentDTO",
            description = "Item Enchantment DTO"
    )
    @Serdeable
    record ItemEnchantmentDTO(
            @Schema(description = "Enchantment ID") UUID id,
            @Schema(description = "Enchantment Name") String name,
            @Schema(description = "Enchantment Level") short level,
            @Schema(description = "The unsafe option of the enchantment") boolean unsafe
    ) implements ItemEnchantmentResponseDTO {

        /**
         * Creates a new instance of ItemEnchantmentDTO.
         *
         * @param id     the unique identifier of the enchantment
         * @param name   the name of the enchantment
         * @param level  the level of the enchantment
         * @param unsafe the unsafe option of the enchantment
         * @return a new ItemEnchantmentDTO instance
         */
        public static ItemEnchantmentDTO createDTO(UUID id, String name, short level, boolean unsafe) {
            return new ItemEnchantmentDTO(id, name, level, unsafe);
        }

        /**
         * Creates a new instance of ItemEnchantmentDTO from an {@link ItemEnchantmentEntity}.
         *
         * @param entity the entity to convert
         * @return a new ItemEnchantmentDTO instance
         */
        public static ItemEnchantmentResponseDTO createDTO(ItemEnchantmentEntity entity) {
            return new ItemEnchantmentDTO(entity.getId(), entity.getName(), entity.getLevel(), entity.isUnsafe());
        }
    }

    /**
     * The {@link ItemEnchantmentResponseDTO.ItemEnchantmentErrorDTO} is used to represent an error response for Enchantment events.
     *
     * @param errorMessage the error message describing the issue
     */
    @Schema(
            name = "ResponseItemEnchantmentErrorDTO",
            description = "Error message for Enchantment model"
    )
    @Serdeable
    record ItemEnchantmentErrorDTO(
            @Schema(description = "Error message") String errorMessage
    ) implements ItemEnchantmentResponseDTO, ErrorResponse {
    }
}
