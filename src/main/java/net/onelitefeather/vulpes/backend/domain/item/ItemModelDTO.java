package net.onelitefeather.vulpes.backend.domain.item;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import net.onelitefeather.vulpes.api.model.ItemEntity;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;
import net.onelitefeather.vulpes.backend.validation.ColumnLength;
import net.onelitefeather.vulpes.backend.validation.KeyFormat;

import java.util.List;
import java.util.UUID;

import static net.onelitefeather.vulpes.backend.validation.ValidationGroup.*;

@Schema(
        requiredProperties = {
                "uiName",
                "key",
                "description",
                "groupName",
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
        @Size(max = ColumnLength.VARCHAR, groups = {Create.class, Update.class})
        String uiName,
        @Schema(description = "Key for the entity", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(groups = {Create.class, Update.class})
        @Size(max = ColumnLength.VARCHAR, groups = {Create.class, Update.class})
        @Pattern(regexp = KeyFormat.PATH_PATTERN, message = KeyFormat.PATH_MESSAGE, groups = {Create.class, Update.class})
        String key,
        @Schema(description = "Internal description of the item", requiredMode = Schema.RequiredMode.REQUIRED)
        @Nullable
        @Size(max = ColumnLength.VARCHAR, groups = {Create.class, Update.class})
        String comment,
        @Schema(description = "The group to identify their basic usage", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(groups = {Create.class, Update.class})
        @Size(max = ColumnLength.VARCHAR, groups = {Create.class, Update.class})
        String groupName
) {

    /**
     * Converts this DTO to an {@link ItemEntity}.
     *
     * @param project the project this item belongs to
     * @return a new {@link ItemEntity} instance with the data from this DTO
     */
    public @NotNull ItemEntity toItemEntity(ProjectEntity project) {
        return new ItemEntity(
                this.id,
                uiName,
                key,
                comment,
                groupName,
                List.of(),
                List.of(),
                project
        );
    }
}
