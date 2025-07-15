package net.onelitefeather.vulpes.backend.domain.item;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import net.onelitefeather.vulpes.api.model.ItemEntity;
import net.onelitefeather.vulpes.backend.domain.error.ErrorResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Schema(description = "Response DTO for Item Model")
@Serdeable
public sealed interface ItemModelResponseDTO {

    /**
     * Represents a response DTO for item models that includes enchantments.
     *
     * @param id           the UUID of the item model
     * @param enchantments a map of enchantment names and their levels
     */
    @Schema(description = "Item model with enchantments")
    @Serdeable
    record ItemModelEnchantmentResponseDTO(
            @Schema(description = "UUID of the Item Model", requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
            @Schema(description = "Map which contains the enchantments as string and short") Map<String, Short> enchantments
    ) implements ItemModelResponseDTO {

        /**
         * Creates a new instance of ItemModelEnchantmentResponseDTO.
         *
         * @param id           the UUID of the item model
         * @param enchantments the map of enchantments where the key is the enchantment name and the value is the level
         * @return a new ItemModelEnchantmentResponseDTO instance
         */
        public static @NotNull ItemModelEnchantmentResponseDTO create(@NotNull UUID id, @NotNull Map<String, Short> enchantments) {
            return new ItemModelEnchantmentResponseDTO(id, enchantments);
        }
    }

    /**
     * Represents a response DTO for item models that includes flags.
     *
     * @param id    the UUID of the item model
     * @param flags the list of flags that modify item behavior
     */
    @Schema(description = "Item Model with flags")
    @Serdeable
    record ItemModelFlagResponseDTO(
            @Schema(description = "UUID of the Item Model", requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
            @Schema(description = "List of item flags that modify item behavior") List<String> flags
    ) implements ItemModelResponseDTO {

        /**
         * Creates a new instance of ItemModelFlagResponseDTO.
         *
         * @param id    the UUID of the item model
         * @param flags the list of flags that modify item behavior
         * @return a new ItemModelFlagResponseDTO instance
         */
        public static @NotNull ItemModelFlagResponseDTO createDTO(@NotNull UUID id, @NotNull List<String> flags) {
            return new ItemModelFlagResponseDTO(id, flags);
        }
    }

    /**
     * Represents a response DTO for item models that includes lore.
     *
     * @param id   the UUID of the item model
     * @param lore the list of text lines displayed in the item tooltip
     */
    @Schema(description = "Item Model with lore")
    @Serdeable
    record ItemModelLoreResponseDTO(
            @Schema(description = "UUID of the Item Model", requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
            @Schema(description = "List of text lines displayed in the item tooltip") List<String> lore
    ) implements ItemModelResponseDTO {

        /**
         * Creates a new instance of ItemModelLoreResponseDTO.
         *
         * @param id   the UUID of the item model
         * @param lore the list of text lines displayed in the item tooltip
         * @return a new ItemModelLoreResponseDTO instance
         */
        public static @NotNull ItemModelLoreResponseDTO createDTO(@NotNull UUID id, @NotNull List<String> lore) {
            return new ItemModelLoreResponseDTO(id, lore);
        }
    }

    @Schema(description = "Item Model Data")
    @Serdeable
    record ItemModelDTO(
            @Schema(description = "UUID of the Item Model") UUID uuid,
            @Schema(description = "Model Name for the UI") String modelName,
            @Schema(description = "Name of the item in the UI") String name,
            @Schema(description = "Description of the item") String comment,
            @Schema(description = "Display variableName of the item shown to users") String displayName,
            @Schema(description = "Material type of the item") String material,
            @Schema(description = "Group category variableName for the item") String groupName,
            @Schema(description = "Custom model data value for resource packs") int customModelData,
            @Schema(description = "Quantity of the item") int amount,
            @Schema(description = "Map of enchantment names and their levels") Map<String, Short> enchantments,
            @Schema(description = "List of text lines displayed in the item tooltip") List<String> lore,
            @Schema(description = "List of item flags that modify item behavior") List<String> flags
    ) implements ItemModelResponseDTO {
        public static ItemModelDTO createDTO(ItemEntity itemEntity) {
            return new ItemModelDTO(
                    itemEntity.getId(),
                    itemEntity.getUiName(),
                    itemEntity.getVariableName(),
                    itemEntity.getComment(),
                    itemEntity.getDisplayName(),
                    itemEntity.getMaterial(),
                    itemEntity.getGroupName(),
                    itemEntity.getCustomModelData(),
                    itemEntity.getAmount(),
                    itemEntity.getEnchantments(),
                    itemEntity.getLore(),
                    itemEntity.getFlags()
            );
        }
    }

    @Schema(description = "Error message for Item Model")
    @Serdeable
    record ItemModelErrorDTO(
            @Schema(description = "Error message") String errorMessage
    ) implements ItemModelResponseDTO, ErrorResponse {
    }
}