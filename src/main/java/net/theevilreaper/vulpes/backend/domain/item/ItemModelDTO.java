package net.theevilreaper.vulpes.backend.domain.item;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import net.theevilreaper.vulpes.api.model.ItemModel;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Schema(requiredProperties = {
        "modelName",
        "name",
        "description",
        "displayName",
        "material",
        "groupName",
        "customModelData",
        "amount"
})
@Introspected
@Serdeable
public final class ItemModelDTO {
    private final UUID id;
    private final String modelName;
    private final String name;
    private final String description;
    private final String displayName;
    private final String material;
    private final String groupName;
    private final int customModelData;
    private final int amount;
    private final Map<String, Short> enchantments;
    private final List<String> lore;
    private final List<String> flags;

    public ItemModelDTO(
            @Schema(description = "ID of the Model", requiredMode = Schema.RequiredMode.NOT_REQUIRED) UUID id,
            @Schema(description = "Model Name for the ui", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @NotBlank @NotEmpty String modelName,
            @Schema(description = "Name in the UI", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @NotBlank @NotEmpty String name,
            @Schema(description = "Example description", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @NotBlank @NotEmpty String description, // TODO: Add description
            @Schema(description = "Example description", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @NotBlank @NotEmpty String displayName,
            @Schema(description = "Example description", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @NotBlank @NotEmpty String material,
            @Schema(description = "Example description", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @NotBlank @NotEmpty String groupName,
            @Schema(description = "Example description", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @Positive int customModelData,
            @Schema(description = "Example description", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @Positive int amount,
            @Schema(description = "Example description", requiredMode = Schema.RequiredMode.NOT_REQUIRED) Map<String, Short> enchantments,
            @Schema(description = "Example description", requiredMode = Schema.RequiredMode.NOT_REQUIRED) List<String> lore,
            @Schema(description = "Example description", requiredMode = Schema.RequiredMode.NOT_REQUIRED) List<String> flags) {
        this.id = id;
        this.modelName = modelName;
        this.name = name;
        this.description = description;
        this.displayName = displayName;
        this.material = material;
        this.groupName = groupName;
        this.customModelData = customModelData;
        this.amount = amount;
        this.enchantments = enchantments;
        this.lore = lore;
        this.flags = flags;
    }

    public UUID getId() {
        return id;
    }

    public String getModelName() {
        return modelName;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getMaterial() {
        return material;
    }

    public String getGroupName() {
        return groupName;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public int getAmount() {
        return amount;
    }

    public Map<String, Short> getEnchantments() {
        return enchantments;
    }

    public List<String> getLore() {
        return lore;
    }

    public List<String> getFlags() {
        return flags;
    }

    public ItemModel toItemModel() {
        return new ItemModel(
                this.id,
                modelName,
                name,
                description,
                displayName,
                material,
                groupName,
                customModelData,
                amount,
                enchantments,
                lore,
                flags
        );
    }
}
