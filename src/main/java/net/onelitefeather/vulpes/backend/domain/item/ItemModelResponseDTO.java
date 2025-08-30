package net.onelitefeather.vulpes.backend.domain.item;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import net.onelitefeather.vulpes.api.model.ItemEntity;
import net.onelitefeather.vulpes.backend.domain.error.ErrorResponse;

import java.util.Collections;
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
    @Schema(
            name = "ResponseItemModelEnchantmentDTO",
            description = "Item model with enchantments"
    )
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
    @Schema(
            name = "ResponseItemModelFlagDTO",
            description = "Item Model with flags"
    )
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
    @Schema(
            name = "ResponseItemModelLoreDTO",
            description = "Item Model with lore"
    )
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

    /**
     * Represents a response DTO for item models that includes all item details.
     *
     * @param id              the unique identifier of the item model
     * @param uiName          the model name for the UI
     * @param variableName    the variable name of the item
     * @param comment         the description of the item
     * @param displayName     the display variableName of the item shown to users
     * @param material        the material type of the item
     * @param groupName       the group category variableName for the item
     * @param customModelData the custom model data value for resource packs
     * @param amount          the quantity of the item
     * @param enchantments    the map of enchantment names and their levels
     * @param lore            the list of text lines displayed in the item tooltip
     * @param flags           the list of item flags that modify item behavior
     */
    @Schema(
            name = "ResponseItemModelDTO",
            description = "Item Model Data"
    )
    @Serdeable
    record ItemModelDTO(
            @Schema(description = "The id of the model") UUID id,
            @Schema(description = "Model Name for the UI") String uiName,
            @Schema(description = "Variable name for the generation") String variableName,
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

        /**
         * Creates a new instance of ItemModelDTO.
         *
         * @param itemEntity the item entity to convert to DTO
         * @return a new ItemModelDTO instance
         */
        public static ItemModelDTO createDTO(@NotNull ItemEntity itemEntity) {
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
                    Collections.emptyMap(),
                    Collections.emptyList(),
                    Collections.emptyList()
            );
        }
    }

    /**
     * Represents an error response DTO for item models.
     *
     * @param errorMessage the error message describing the issue with the item model
     */
    @Schema(description = "Error message for Item Model")
    @Serdeable
    record ItemModelErrorDTO(
            @Schema(description = "Error message") String errorMessage
    ) implements ItemModelResponseDTO, ErrorResponse {
    }
}