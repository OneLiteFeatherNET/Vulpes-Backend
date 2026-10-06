package net.onelitefeather.vulpes.backend.domain.item;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import net.onelitefeather.vulpes.api.model.ItemEntity;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Schema(description = "Response DTO for Item Model")
@Serdeable
public sealed interface ItemModelResponseDTO {



    /**
     * Represents a response DTO for item models that includes all item details.
     *
     * @param id              the unique identifier of the item model
     * @param uiName          the model name for the UI
     * @param key             the key of the item
     * @param comment         the description of the item
     * @param groupName       the group category variableName for the item
     * @param enchantments    the map of enchantment names and their levels
     * @param lore            the list of text lines displayed in the item tooltip
     * @param projectId       the ID of the project this item belongs to
     */
    @Schema(
            name = "ResponseItemModelDTO",
            description = "Item Model Data"
    )
    @Serdeable
    record ItemModelDTO(
            @Schema(description = "The id of the model") UUID id,
            @Schema(description = "Model Name for the UI") String uiName,
            @Schema(description = "Key for the generation") String key,
            @Schema(description = "Description of the item") String comment,
            @Schema(description = "Group category variableName for the item") String groupName,
            @Schema(description = "Map of enchantment names and their levels") Map<String, Short> enchantments,
            @Schema(description = "List of text lines displayed in the item tooltip") List<String> lore,
            @Schema(description = "ID of the project this item belongs to") UUID projectId,
            @Schema(description = "The point in time at which the attribute was created") Instant creationDate,
            @Schema(description = "The point in time at which the attribute was last modified") Instant modificationDate
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
                    itemEntity.getKey(),
                    itemEntity.getComment(),
                    itemEntity.getGroupName(),
                    Collections.emptyMap(),
                    Collections.emptyList(),
                    itemEntity.getProject().getId(),
                    itemEntity.getCreationDate(),
                    itemEntity.getModificationDate()
            );
        }
    }
}