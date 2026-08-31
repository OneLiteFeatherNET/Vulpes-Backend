package net.onelitefeather.vulpes.backend.domain.item;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import net.onelitefeather.vulpes.api.model.item.ItemEnchantmentEntity;

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
        public static ItemEnchantmentDTO createDTO(ItemEnchantmentEntity entity) {
            return new ItemEnchantmentDTO(entity.getId(), entity.getName(), entity.getLevel(), entity.isUnsafe());
        }
    }
}
