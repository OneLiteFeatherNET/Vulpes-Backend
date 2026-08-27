package net.onelitefeather.vulpes.backend.controller.item;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Patch;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.Put;
import io.micronaut.validation.Validated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail;
import net.onelitefeather.vulpes.backend.domain.item.ItemLoreDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemLoreReorderDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemLoreResponseDTO;
import net.onelitefeather.vulpes.backend.service.ItemService;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup;

import java.util.List;
import java.util.UUID;

@Controller("/item")
public class ItemLoreController {

    private final ItemService itemService;

    @Inject
    public ItemLoreController(ItemService itemService) {
        this.itemService = itemService;
    }

    @Operation(
            summary = "Get all lore of an item",
            operationId = "getLore",
            description = "Retrieves a pageable list of lore entries for the item identified by itemId.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Lore successfully retrieved.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(
                            schema = @Schema(implementation = ItemLoreResponseDTO.class),
                            arraySchema = @Schema(implementation = Page.class)
                    )
            )
    )
    @Get(uris = {
            "/lore/{itemId}",
            "/{itemId}/lore"
    })
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Page<ItemLoreResponseDTO.ItemLoreDTO>> getLoreById(@PathVariable("itemId") UUID itemId, Pageable pageable) {
        Page<ItemLoreResponseDTO.ItemLoreDTO> lorePage = itemService.findLoreById(itemId, pageable);
        return HttpResponse.ok(lorePage);
    }

    @Operation(
            summary = "Update lore of an item",
            operationId = "updateLore",
            description = "Updates a specific lore entry of the item identified by itemId.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Lore successfully updated.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemLoreResponseDTO.ItemLoreDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Item for the given ID was not found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "The request body failed validation. 'errors' names the rejected fields.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Post(uris = {
            "/lore/{itemId}",
            "/{itemId}/lore"
    })
    @Validated(groups = ValidationGroup.Update.class)
    public HttpResponse<ItemLoreResponseDTO.ItemLoreDTO> updateLore(@PathVariable("itemId") UUID itemId, @Body ItemLoreDTO lore) {
        return HttpResponse.ok(itemService.updateLoreById(itemId, lore));
    }

    @Operation(
            summary = "Create lore of an item",
            operationId = "createLore",
            description = "Creates a new lore entry for the item identified by itemId.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Lore successfully created.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemLoreResponseDTO.ItemLoreDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Item for the given ID was not found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "The request body failed validation. 'errors' names the rejected fields.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Put(uris = {
            "/lore/{itemId}",
            "/{itemId}/lore"
    })
    @Validated(groups = ValidationGroup.Create.class)
    public HttpResponse<ItemLoreResponseDTO.ItemLoreDTO> createLore(@PathVariable("itemId") UUID itemId, @Body ItemLoreDTO lore) {
        return HttpResponse.ok(itemService.createLoreById(itemId, lore));
    }

    @Operation(
            summary = "Reorder a lore entry of an item",
            operationId = "reorderLore",
            description = "Moves the lore entry identified by entryId to a new position within the lore list of the item identified by itemId.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "204",
            description = "Lore successfully reordered."
    )
    @ApiResponse(
            responseCode = "400",
            description = "Item or lore entry for the given ID was not found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Patch("/{itemId}/lore/reorder")
    @Validated
    public HttpResponse<Void> reorderLore(@PathVariable("itemId") UUID itemId, @Body ItemLoreReorderDTO reorder) {
        itemService.reorderLoreById(itemId, reorder.entryId(), reorder.newIndex());
        return HttpResponse.noContent();
    }

    @Operation(
            summary = "Delete lore of an item",
            operationId = "deleteLore",
            description = "Deletes a specific lore entry (loreId) of the item identified by itemId.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Lore successfully deleted.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemLoreResponseDTO.ItemLoreDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Item for the given ID was not found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Delete(uris = {
            "/lore/{itemId}/{loreId}",
            "/{itemId}/lore/{loreId}"
    })
    public HttpResponse<ItemLoreResponseDTO.ItemLoreDTO> deleteLore(@PathVariable("itemId") UUID itemId, @PathVariable("loreId") UUID loreId) {
        return HttpResponse.ok(itemService.deleteLoreById(itemId, loreId));
    }

    @Operation(
            summary = "Delete all lore of an item",
            operationId = "deleteAllLore",
            description = "Deletes all lore entries of the item identified by itemId.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "All lore entries were successfully deleted.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(
                            schema = @Schema(implementation = ItemLoreResponseDTO.ItemLoreDTO.class)
                    )
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Item for the given ID was not found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Delete(uris = {
            "/lore/{itemId}",
            "/{itemId}/lore"
    })
    public HttpResponse<List<ItemLoreResponseDTO.ItemLoreDTO>> deleteLores(@PathVariable("itemId") UUID itemId) {
        var deletedLores = itemService.deleteAllLoreById(itemId);
        return HttpResponse.ok(deletedLores);
    }
}
