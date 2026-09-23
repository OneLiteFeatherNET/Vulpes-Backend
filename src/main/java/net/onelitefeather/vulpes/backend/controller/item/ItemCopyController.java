package net.onelitefeather.vulpes.backend.controller.item;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import net.onelitefeather.vulpes.api.model.ItemEntity;
import net.onelitefeather.vulpes.backend.copier.EntityCopier;
import net.onelitefeather.vulpes.backend.domain.copy.CopyRequest;
import net.onelitefeather.vulpes.backend.domain.copy.RelationalCopyDTO;
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail;
import net.onelitefeather.vulpes.backend.domain.item.ItemModelResponseDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemRelation;

import java.util.Set;
import java.util.UUID;

/**
 * REST controller for copying item resources, within the same project or into another one.
 */
@Controller("/project/{projectId}/item")
public class ItemCopyController {

    private final EntityCopier<ItemEntity, ItemRelation> itemCopier;

    @Inject
    public ItemCopyController(@Named("item") EntityCopier<ItemEntity, ItemRelation> itemCopier) {
        this.itemCopier = itemCopier;
    }

    @Operation(
            summary = "Copy an item",
            operationId = "copyItem",
            description = "Copies an item owned by the given project into the same project or another one, under a new or the same key, optionally including its lore, flags, and enchantments.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The item was successfully copied.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemModelResponseDTO.ItemModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The item or the target project was not found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "An item with the resolved key already exists in the target project.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Post("/{itemId}/copy")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<ItemModelResponseDTO.ItemModelDTO> copy(
            @PathVariable UUID projectId,
            @PathVariable("itemId") UUID itemId,
            @Nullable @Body RelationalCopyDTO<ItemRelation> copyDTO
    ) {
        Set<ItemRelation> relations = copyDTO != null ? copyDTO.relationsOrEmpty() : Set.of();
        var copied = itemCopier.copy(
                projectId,
                itemId,
                CopyRequest.targetProjectId(copyDTO),
                CopyRequest.targetKey(copyDTO),
                CopyRequest.targetName(copyDTO),
                relations
        );
        return HttpResponse.ok(ItemModelResponseDTO.ItemModelDTO.createDTO(copied));
    }
}
