package net.onelitefeather.vulpes.backend.domain.advancement;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import net.onelitefeather.vulpes.api.model.advancement.AdvancementEntity;
import net.onelitefeather.vulpes.api.model.advancement.AdvancementFrameType;
import net.onelitefeather.vulpes.api.model.project.ProjectEntity;

import java.util.UUID;

import static net.onelitefeather.vulpes.backend.validation.ValidationGroup.*;

@Schema(requiredProperties = {
        "uiName",
        "key",
        "frameType",
        "x",
        "y",
        "showToast",
        "announceToChat",
        "hidden"
})
@Introspected
@Serdeable
public record AdvancementModelDTO(
        @Schema(description = "ID of the advancement", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Null(groups = Create.class)
        @NotNull(groups = {Update.class})
        UUID id,
        @Schema(description = "Model name for the UI", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(groups = {Create.class, Update.class})
        String uiName,
        @Schema(description = "Key of the advancement", requiredMode = Schema.RequiredMode.REQUIRED)
        @Null(groups = {Create.class, Update.class})
        String key,
        @Schema(description = "Comment of the advancement", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Nullable
        String comment,
        @Schema(description = "Material of the icon", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Nullable
        String material,
        @Schema(description = "Frame type of the advancement", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(groups = {Create.class, Update.class})
        AdvancementFrameType frameType,
        @Schema(description = "Title as vanilla JSON text component", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Nullable
        String title,
        @Schema(description = "Description as vanilla JSON text component", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Nullable
        String description,
        @Schema(description = "Background texture of the tab, only used by root advancements", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Nullable
        String background,
        @Schema(description = "ID of the parent advancement, null for a root advancement", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Nullable
        UUID parentId,
        @Schema(description = "X position in the advancement tree", requiredMode = Schema.RequiredMode.REQUIRED)
        float x,
        @Schema(description = "Y position in the advancement tree", requiredMode = Schema.RequiredMode.REQUIRED)
        float y,
        @Schema(description = "Whether a toast is shown when the advancement is achieved", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean showToast,
        @Schema(description = "Whether the advancement is announced in the chat", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean announceToChat,
        @Schema(description = "Whether the advancement is hidden until it is achieved", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean hidden
) {

    /**
     * Converts this DTO to an {@link AdvancementEntity}.
     *
     * @param project the project this advancement belongs to
     * @param parent  the resolved parent advancement, null for a root advancement
     * @return a new {@link AdvancementEntity} instance with the data from this DTO
     */
    public @NotNull AdvancementEntity toAdvancementEntity(ProjectEntity project, @Nullable AdvancementEntity parent) {
        AdvancementEntity entity = new AdvancementEntity(
                this.id,
                uiName,
                key,
                comment,
                material,
                frameType,
                title,
                description,
                parent,
                project
        );
        entity.setBackground(background);
        entity.setX(x);
        entity.setY(y);
        entity.setShowToast(showToast);
        entity.setAnnounceToChat(announceToChat);
        entity.setHidden(hidden);
        return entity;
    }
}
