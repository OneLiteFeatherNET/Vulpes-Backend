package net.onelitefeather.vulpes.backend.domain.item;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import net.onelitefeather.vulpes.api.model.ItemEntity;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup;

import java.util.List;
import java.util.UUID;

import static net.onelitefeather.vulpes.backend.validation.ValidationGroup.*;

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
        @Schema(description = "ID of the Model", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Null(groups = Create.class)
        @NotNull(groups = {Update.class})
        UUID id,
        @Schema(description = "Name in the UI", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(groups = {Create.class, Update.class})
        String uiName,
        @Schema(description = "Variable name for the entity", requiredMode = Schema.RequiredMode.REQUIRED)
        @Null(groups = {Create.class, Update.class})
        String variableName,
        @Schema(description = "Internal description of the item", requiredMode = Schema.RequiredMode.REQUIRED)
        @Nullable
        String comment,
        @Schema(description = "The display name of the item", requiredMode = Schema.RequiredMode.REQUIRED)
        @Null(groups = {Create.class, Update.class})
        String displayName,
        @Schema(description = "The material from the item", requiredMode = Schema.RequiredMode.REQUIRED)
        @Null(groups = {Create.class, Update.class})
        String material,
        @Schema(description = "The group to identify their basic usage", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(groups = {Create.class, Update.class})
        String groupName,
        @Schema(description = "Integer which refers to the customModelData index", requiredMode = Schema.RequiredMode.REQUIRED)
        @PositiveOrZero
        int customModelData,
        @Schema(description = "The amount of the item", requiredMode = Schema.RequiredMode.REQUIRED)
        @Positive
        int amount
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
                groupName,
                customModelData,
                amount,
                List.of(),
                List.of(),
                List.of()
        );
    }
}
