package net.onelitefeather.vulpes.backend.domain.item;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import net.onelitefeather.vulpes.api.model.ItemEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Schema(
        requiredProperties = {
                "uiName",
                "variableName",
                "description",
                "displayName",
                "material",
                "groupName",
                "customModelData",
                "amount"
        }
)
@Introspected
@Serdeable
public record ItemModelDTO(
        @Schema(description = "ID of the Model", requiredMode = Schema.RequiredMode.NOT_REQUIRED) UUID id,
        @Schema(description = "Name in the UI", requiredMode = Schema.RequiredMode.REQUIRED) String uiName,
        @Schema(description = "Variable name for the entity", requiredMode = Schema.RequiredMode.REQUIRED) String variableName,
        @Schema(description = "Internal description of the item", requiredMode = Schema.RequiredMode.REQUIRED) String comment,
        @Schema(description = "The display name of the item", requiredMode = Schema.RequiredMode.REQUIRED) String displayName,
        @Schema(description = "The material from the item", requiredMode = Schema.RequiredMode.REQUIRED) String material,
        @Schema(description = "The group to identify their basic usage", requiredMode = Schema.RequiredMode.REQUIRED) String group,
        @Schema(description = "Integer which refers to the customModelData index", requiredMode = Schema.RequiredMode.REQUIRED) int customModelData,
        @Schema(description = "The amount of the item", requiredMode = Schema.RequiredMode.REQUIRED) int amount,
        @Schema(description = "The given enchantments", requiredMode = Schema.RequiredMode.NOT_REQUIRED) Map<String, Short> enchantments,
        @Schema(description = "The given lore from the item", requiredMode = Schema.RequiredMode.NOT_REQUIRED) List<String> lore,
        @Schema(description = "The flags which the item should have", requiredMode = Schema.RequiredMode.NOT_REQUIRED) List<String> flags
) {

    /**
     * Converts this DTO to an {@link ItemEntity}.
     *
     * @return a new {@link ItemEntity} instance with the data from this DTO
     */
    public @NotNull ItemEntity toItemEntity() {
        return new ItemEntity(
                this.id,
                uiName,
                variableName,
                comment,
                displayName,
                material,
                group,
                customModelData,
                amount,
                enchantments,
                lore,
                flags
        );
    }
}
