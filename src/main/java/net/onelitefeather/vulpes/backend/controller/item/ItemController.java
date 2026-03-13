package net.onelitefeather.vulpes.backend.controller.item;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.validation.Validated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import net.onelitefeather.vulpes.api.model.ItemEntity;
import net.onelitefeather.vulpes.backend.domain.item.ItemModelDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemModelResponseDTO;
import net.onelitefeather.vulpes.backend.service.ItemService;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller for item resources.
 * Provides CRUD operations and nested resource management (enchantments, lore, flags).
 */
@Controller("/item")
public class ItemController {

    private final ItemService itemService;

    @Inject
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @Operation(
            summary = "Create a new item",
            operationId = "addItem",
            description = "Creates a new item with the provided properties and stores it in the database.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Item successfully created.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemModelResponseDTO.ItemModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "500",
            description = "Item could not be created due to an internal error.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemModelResponseDTO.ItemModelErrorDTO.class)
            )
    )
    @Post
    @Produces(MediaType.APPLICATION_JSON)
    @Validated(groups = ValidationGroup.Create.class)
    public HttpResponse<ItemModelResponseDTO> add(
           @Body ItemModelDTO itemModel
    ) {
        ItemModelResponseDTO.ItemModelDTO createdItem = itemService.createItem(itemModel);
        return HttpResponse.ok(createdItem);
    }

    @Operation(
            summary = "Get an item by ID",
            operationId = "getItemById",
            description = "Retrieves a single item from the database by its unique ID (itemId).",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Item successfully retrieved.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemModelResponseDTO.ItemModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Item with the given ID was not found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemModelResponseDTO.ItemModelErrorDTO.class)
            )
    )
    @Get("/{itemId}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<ItemModelResponseDTO> getById(
            @PathVariable("itemId") UUID itemId
    ) {
        Optional<ItemEntity> foundItemOpt = itemService.findItemById(itemId);
        if (foundItemOpt.isPresent()) {
            var foundItem = foundItemOpt.get();
            return HttpResponse.ok(ItemModelResponseDTO.ItemModelDTO.createDTO(foundItem));
        }
        return HttpResponse.notFound(new ItemModelResponseDTO.ItemModelErrorDTO("Item not found"));
    }

    @Operation(
            summary = "Get all items",
            operationId = "getAllItems",
            description = "Retrieves a pageable list of all items. Supports standard Micronaut pagination (page, size, sort).",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Items successfully retrieved.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(
                            schema = @Schema(implementation = ItemModelResponseDTO.ItemModelDTO.class),
                            arraySchema = @Schema(implementation = Page.class)
                    )
            )
    )
    @Get
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Page<ItemModelResponseDTO.ItemModelDTO>> getAll(Pageable pageable) {
        Page<ItemModelResponseDTO.ItemModelDTO> itemsPage = itemService.getAllItems(pageable);
        return HttpResponse.ok(itemsPage);
    }

    @Operation(
            summary = "Update an item",
            operationId = "updateItem",
            description = "Updates an existing item in the database.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Item successfully updated.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemModelResponseDTO.ItemModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Item was not found and could not be updated.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemModelResponseDTO.ItemModelErrorDTO.class)
            )
    )
    @Post("/update")
    @Produces(MediaType.APPLICATION_JSON)
    @Validated(groups = ValidationGroup.Update.class)
    public HttpResponse<ItemModelResponseDTO> update(
            @Body ItemModelDTO itemModel
    ) {
        ItemModelResponseDTO updateResult = itemService.updateItem(itemModel);
        if (updateResult instanceof ItemModelResponseDTO.ItemModelErrorDTO) {
            return HttpResponse.notFound(updateResult);
        }
        return HttpResponse.ok(updateResult);
    }

    @Operation(
            summary = "Remove an item by ID",
            operationId = "removeItemById",
            description = "Deletes an item from the database by its unique ID (itemId).",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Item successfully deleted.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemModelResponseDTO.ItemModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Item with the given ID was not found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemModelResponseDTO.ItemModelErrorDTO.class)
            )
    )
    @Delete("/delete/{itemId}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<ItemModelResponseDTO> delete(@PathVariable("itemId") UUID itemId) {
        ItemModelResponseDTO deleteResult = itemService.deleteItem(itemId);
        if (deleteResult instanceof ItemModelResponseDTO.ItemModelErrorDTO) {
            return HttpResponse.notFound(deleteResult);
        }
        return HttpResponse.ok(deleteResult);
    }

    @Operation(
            summary = "Delete all items",
            description = "Deletes all items from the database.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "All items were successfully deleted.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(
                            schema = @Schema(implementation = ItemModelResponseDTO.ItemModelDTO.class)
                    )
            )
    )
    @Delete("/deleteAll")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<List<ItemModelResponseDTO>> deleteAll() {
        List<ItemModelResponseDTO> deleteResults = itemService.deleteAllItems();
        return HttpResponse.ok(deleteResults);
    }
}
