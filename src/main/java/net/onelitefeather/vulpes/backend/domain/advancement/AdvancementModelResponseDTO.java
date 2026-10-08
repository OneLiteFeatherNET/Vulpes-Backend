package net.onelitefeather.vulpes.backend.domain.advancement;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import net.onelitefeather.vulpes.api.model.advancement.AdvancementEntity;
import net.onelitefeather.vulpes.api.model.advancement.AdvancementFrameType;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Response DTO for Advancement Model")
@Serdeable
public sealed interface AdvancementModelResponseDTO {

    /**
     * Represents a response DTO for advancement models that includes the model's details.
     *
     * @param id               the UUID of the advancement
     * @param uiName           the name to display in the UI
     * @param key              the key used for variable generation
     * @param comment          a comment of the advancement
     * @param material         the material of the icon
     * @param frameType        the frame type of the advancement
     * @param title            the title as vanilla JSON text component
     * @param description      the description as vanilla JSON text component
     * @param background       the background texture of the tab, only used by root advancements
     * @param parentId         the ID of the parent advancement, null for a root advancement
     * @param x                the x position in the advancement tree
     * @param y                the y position in the advancement tree
     * @param showToast        whether a toast is shown when the advancement is achieved
     * @param announceToChat   whether the advancement is announced in the chat
     * @param hidden           whether the advancement is hidden until it is achieved
     * @param projectId        the ID of the project this advancement belongs to
     * @param creationDate     the point in time at which the advancement was created
     * @param modificationDate the point in time at which the advancement was last modified
     */
    @Schema(
            name = "ResponseAdvancementModelDTO",
            description = "Advancement Model Data"
    )
    @Serdeable
    record AdvancementModelDTO(
            @Schema(description = "The id of the model") UUID id,
            @Schema(description = "Model Name for the UI") String uiName,
            @Schema(description = "Key for the generation") String key,
            @Schema(description = "Comment of the advancement") String comment,
            @Schema(description = "Material of the icon") String material,
            @Schema(description = "Frame type of the advancement") AdvancementFrameType frameType,
            @Schema(description = "Title as vanilla JSON text component") String title,
            @Schema(description = "Description as vanilla JSON text component") String description,
            @Schema(description = "Background texture of the tab, only used by root advancements") String background,
            @Schema(description = "ID of the parent advancement, null for a root advancement") UUID parentId,
            @Schema(description = "X position in the advancement tree") float x,
            @Schema(description = "Y position in the advancement tree") float y,
            @Schema(description = "Whether a toast is shown when the advancement is achieved") boolean showToast,
            @Schema(description = "Whether the advancement is announced in the chat") boolean announceToChat,
            @Schema(description = "Whether the advancement is hidden until it is achieved") boolean hidden,
            @Schema(description = "ID of the project this advancement belongs to") UUID projectId,
            @Schema(description = "The point in time at which the advancement was created") Instant creationDate,
            @Schema(description = "The point in time at which the advancement was last modified") Instant modificationDate
    ) implements AdvancementModelResponseDTO {

        /**
         * Creates a DTO from an AdvancementEntity.
         *
         * @param advancement the AdvancementEntity to convert
         * @return a new AdvancementModelDTO instance
         */
        public static AdvancementModelDTO createDTO(AdvancementEntity advancement) {
            AdvancementEntity parent = advancement.getParent();
            return new AdvancementModelDTO(
                    advancement.getId(),
                    advancement.getUiName(),
                    advancement.getKey(),
                    advancement.getComment(),
                    advancement.getMaterial(),
                    advancement.getFrameType(),
                    advancement.getTitle(),
                    advancement.getDescription(),
                    advancement.getBackground(),
                    parent == null ? null : parent.getId(),
                    advancement.getX(),
                    advancement.getY(),
                    advancement.isShowToast(),
                    advancement.isAnnounceToChat(),
                    advancement.isHidden(),
                    advancement.getProject().getId(),
                    advancement.getCreationDate(),
                    advancement.getModificationDate()
            );
        }
    }
}
