package net.onelitefeather.vulpes.backend.controller;

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
import io.micronaut.http.annotation.Put;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import net.onelitefeather.vulpes.api.model.ItemEntity;
import net.onelitefeather.vulpes.backend.domain.item.ItemEnchantmentDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemEnchantmentResponseDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemFlagDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemFlagResponseDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemLoreDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemLoreResponseDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemModelDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemModelResponseDTO;
import net.onelitefeather.vulpes.backend.service.ItemService;

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
    public HttpResponse<ItemModelResponseDTO> add(
           @Valid @Body ItemModelDTO itemModel
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
            @Valid @PathVariable("itemId") UUID itemId
    ) {
        Optional<ItemEntity> foundItemOpt = itemService.findItemById(itemId);
        if (foundItemOpt.isPresent()) {
            var foundItem = foundItemOpt.get();
            return HttpResponse.ok(ItemModelResponseDTO.ItemModelDTO.createDTO(foundItem));
        }
        return HttpResponse.notFound(new ItemModelResponseDTO.ItemModelErrorDTO("Item not found"));
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
    public HttpResponse<ItemModelResponseDTO> remove(@PathVariable("itemId") UUID itemId) {
        ItemModelResponseDTO deleteResult = itemService.deleteItem(itemId);
        if (deleteResult instanceof ItemModelResponseDTO.ItemModelErrorDTO) {
            return HttpResponse.notFound(deleteResult);
        }
        return HttpResponse.ok(deleteResult);
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
    public HttpResponse<ItemModelResponseDTO> update(
            @Valid @Body ItemModelDTO itemModel
    ) {
        ItemModelResponseDTO updateResult = itemService.updateItem(itemModel);
        if (updateResult instanceof ItemModelResponseDTO.ItemModelErrorDTO) {
            return HttpResponse.notFound(updateResult);
        }
        return HttpResponse.ok(updateResult);
    }

    @Operation(
            summary = "Get enchantments of an item",
            operationId = "getEnchantments",
            description = "Retrieves a pageable list of enchantments for the item identified by itemId.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Enchantments successfully retrieved.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(
                            schema = @Schema(implementation = ItemEnchantmentResponseDTO.class),
                            arraySchema = @Schema(implementation = Page.class)
                    )
            )
    )
    @Get(uris = {
            "/enchantments/{itemId}",
            "/{itemId}/enchantments"
    })
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Page<ItemEnchantmentResponseDTO>> getEnchantmentsById(@PathVariable("itemId") UUID itemId, Pageable pageable) {
        Page<ItemEnchantmentResponseDTO> enchantmentsPage = itemService.findEnchantmentsById(itemId, pageable);
        return HttpResponse.ok(enchantmentsPage);
    }

    @Operation(
            summary = "Update an enchantment of an item",
            operationId = "updateEnchantment",
            description = "Updates a specific enchantment of the item identified by itemId.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Enchantment successfully updated.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemEnchantmentResponseDTO.ItemEnchantmentDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Item for the given ID was not found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemEnchantmentResponseDTO.ItemEnchantmentErrorDTO.class)
            )
    )
    @Post(uris = {
            "/enchantment/{itemId}",
            "/{itemId}/enchantment"
    })
    public HttpResponse<ItemEnchantmentResponseDTO> updateEnchantment(@PathVariable("itemId") UUID itemId, @Body ItemEnchantmentDTO enchantment) {
        var updateResult = itemService.updateEnchantmentById(itemId, enchantment);
        if (updateResult instanceof ItemEnchantmentResponseDTO.ItemEnchantmentErrorDTO) {
            return HttpResponse.notFound(updateResult);
        }
        return HttpResponse.ok(updateResult);
    }

    @Operation(
            summary = "Create an enchantment for an item",
            operationId = "createEnchantment",
            description = "Creates a new enchantment entry for the item identified by itemId.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Enchantment successfully created.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemEnchantmentResponseDTO.ItemEnchantmentDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Item for the given ID was not found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemEnchantmentResponseDTO.ItemEnchantmentErrorDTO.class)
            )
    )
    @Put(uris = {
            "/enchantment/{itemId}",
            "/{itemId}/enchantment"
    })
    public HttpResponse<ItemEnchantmentResponseDTO> createEnchantment(@PathVariable("itemId") UUID itemId, @Body ItemEnchantmentDTO enchantment) {
        var createResult = itemService.createEnchantmentById(itemId, enchantment);
        return HttpResponse.ok(createResult);
    }

    @Operation(
            summary = "Delete an enchantment of an item",
            operationId = "deleteEnchantment",
            description = "Deletes a specific enchantment (enchantmentId) of the item identified by itemId.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Enchantment successfully deleted.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemEnchantmentResponseDTO.ItemEnchantmentDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Item for the given ID was not found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemEnchantmentResponseDTO.ItemEnchantmentErrorDTO.class)
            )
    )
    @Delete(uris = {
            "/enchantment/{itemId}/{enchantmentId}",
            "/{itemId}/enchantment/{enchantmentId}"
    })
    public HttpResponse<ItemEnchantmentResponseDTO> deleteEnchantment(@PathVariable("itemId") UUID itemId, @PathVariable("enchantmentId") UUID enchantmentId) {
        var deleteResult = itemService.deleteEnchantmentById(itemId, enchantmentId);
        if (deleteResult instanceof ItemEnchantmentResponseDTO.ItemEnchantmentErrorDTO) {
            return HttpResponse.notFound(deleteResult);
        }
        return HttpResponse.ok(deleteResult);
    }

    @Operation(
            summary = "Delete all enchantments of an item",
            operationId = "deleteEnchantments",
            description = "Deletes all enchantments of the item identified by itemId.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Enchantments successfully deleted.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(
                            schema = @Schema(implementation = ItemEnchantmentResponseDTO.ItemEnchantmentDTO.class)
                    )
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Item for the given ID was not found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemEnchantmentResponseDTO.ItemEnchantmentErrorDTO.class)
            )
    )
    @Delete(uris = {
            "/enchantment/{itemId}/",
            "/{itemId}/enchantment/"
    })
    public HttpResponse<List<ItemEnchantmentResponseDTO>> deleteEnchantments(@PathVariable("itemId") UUID itemId) {
        var deletedEnchantments = itemService.deleteAllEnchantmentsById(itemId);
        return HttpResponse.ok(deletedEnchantments);
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
    public HttpResponse<Page<ItemLoreResponseDTO>> getLoreById(@PathVariable("itemId") UUID itemId, Pageable pageable) {
        Page<ItemLoreResponseDTO> lorePage = itemService.findLoreById(itemId, pageable);
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
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemLoreResponseDTO.ItemLoreErrorDTO.class)
            )
    )
    @Post(uris = {
            "/lore/{itemId}",
            "/{itemId}/lore"
    })
    public HttpResponse<ItemLoreResponseDTO> updateLore(@PathVariable("itemId") UUID itemId, @Body ItemLoreDTO lore) {
        ItemLoreResponseDTO updateResult = itemService.updateLoreById(itemId, lore);
        if (updateResult instanceof ItemLoreResponseDTO.ItemLoreErrorDTO) {
            return HttpResponse.notFound(updateResult);
        }
        return HttpResponse.ok(updateResult);
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
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemLoreResponseDTO.ItemLoreErrorDTO.class)
            )
    )
    @Put(uris = {
            "/lore/{itemId}",
            "/{itemId}/lore"
    })
    public HttpResponse<ItemLoreResponseDTO> createLore(@PathVariable("itemId") UUID itemId, @Body ItemLoreDTO lore) {
        ItemLoreResponseDTO createResult = itemService.createLoreById(itemId, lore);
        if (createResult instanceof ItemLoreResponseDTO.ItemLoreErrorDTO) {
            return HttpResponse.notFound(createResult);
        }
        return HttpResponse.ok(createResult);
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
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemLoreResponseDTO.ItemLoreErrorDTO.class)
            )
    )
    @Delete(uris = {
            "/lore/{itemId}/{loreId}",
            "/{itemId}/lore/{loreId}"
    })
    public HttpResponse<ItemLoreResponseDTO> deleteLore(@PathVariable("itemId") UUID itemId, @PathVariable("loreId") UUID loreId) {
        var deleteResult = itemService.deleteLoreById(itemId, loreId);
        if (deleteResult instanceof ItemLoreResponseDTO.ItemLoreErrorDTO) {
            return HttpResponse.notFound(deleteResult);
        }
        return HttpResponse.ok(deleteResult);
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
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemLoreResponseDTO.ItemLoreErrorDTO.class)
            )
    )
    @Delete(uris = {
            "/lore/{itemId}/",
            "/{itemId}/lore/"
    })
    public HttpResponse<List<ItemLoreResponseDTO>> deleteLores(@PathVariable("itemId") UUID itemId) {
        var deletedLores = itemService.deleteAllLoreById(itemId);
        return HttpResponse.ok(deletedLores);
    }


    @Operation(
            summary = "Get all flags of an item",
            operationId = "getFlags",
            description = "Retrieves a pageable list of flags for the item identified by itemId.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Flags successfully retrieved.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(
                            schema = @Schema(implementation = ItemFlagResponseDTO.ItemFlagDTO.class),
                            arraySchema = @Schema(implementation = Page.class)
                    )
            )
    )
    @Get(uris = {
            "/flags/{itemId}",
            "/{itemId}/flags"
    })
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Page<ItemFlagResponseDTO>> getFlagsById(@PathVariable("itemId") UUID itemId, Pageable pageable) {
        Page<ItemFlagResponseDTO> flagsPage = itemService.findFlagsById(itemId, pageable);
        return HttpResponse.ok(flagsPage);
    }

    @Operation(
            summary = "Update flag of an item",
            operationId = "updateFlag",
            description = "Updates a specific flag of the item identified by itemId.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Flag successfully updated.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemFlagResponseDTO.ItemFlagDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Item for the given ID was not found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemFlagResponseDTO.ItemFlagErrorDTO.class)
            )
    )
    @Post(uris = {
            "/flag/{itemId}",
            "/{itemId}/flag"
    })
    public HttpResponse<ItemFlagResponseDTO> updateFlags(@PathVariable("itemId") UUID itemId, @Body ItemFlagDTO flag) {
        ItemFlagResponseDTO updateResult = itemService.updateFlagById(itemId, flag);
        if (updateResult instanceof ItemFlagResponseDTO.ItemFlagErrorDTO) {
            return HttpResponse.notFound(updateResult);
        }
        return HttpResponse.ok(updateResult);
    }

    @Operation(
            summary = "Create flag of an item",
            operationId = "createFlag",
            description = "Creates a new flag entry for the item identified by itemId.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Flag successfully created.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemFlagResponseDTO.ItemFlagDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Item for the given ID was not found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemFlagResponseDTO.ItemFlagErrorDTO.class)
            )
    )
    @Put(uris = {
            "/flag/{itemId}",
            "/{itemId}/flag"
    })
    public HttpResponse<ItemFlagResponseDTO> createFlag(@PathVariable("itemId") UUID itemId, @Body ItemFlagDTO flag) {
        ItemFlagResponseDTO createResult = itemService.createFlagById(itemId, flag);
        if (createResult instanceof ItemFlagResponseDTO.ItemFlagErrorDTO) {
            return HttpResponse.notFound(createResult);
        }
        return HttpResponse.ok(createResult);
    }


    @Operation(
            summary = "Delete flag of an item",
            operationId = "deleteFlag",
            description = "Deletes a specific flag (flagId) of the item identified by itemId.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Flag successfully deleted.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemFlagResponseDTO.ItemFlagDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Item for the given ID was not found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemFlagResponseDTO.ItemFlagErrorDTO.class)
            )
    )
    @Delete(uris = {
            "/flag/{itemId}/{flagId}",
            "/{itemId}/flag/{flagId}"
    })
    public HttpResponse<ItemFlagResponseDTO> deleteFlag(@PathVariable("itemId") UUID itemId, @PathVariable("flagId") UUID flagId) {
        var deleteResult = itemService.deleteFlagById(itemId, flagId);
        if (deleteResult instanceof ItemFlagResponseDTO.ItemFlagErrorDTO) {
            return HttpResponse.notFound(deleteResult);
        }
        return HttpResponse.ok(deleteResult);
    }

    @Operation(
            summary = "Delete all flags of an item",
            operationId = "deleteFlags",
            description = "Deletes all flags of the item identified by itemId.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "All flags were successfully deleted.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(
                            schema = @Schema(implementation = ItemFlagResponseDTO.ItemFlagDTO.class)
                    )
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Item for the given ID was not found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemFlagResponseDTO.ItemFlagErrorDTO.class)
            )
    )
    @Delete(uris = {
            "/flag/{itemId}/",
            "/{itemId}/flag/"
    })
    public HttpResponse<List<ItemFlagResponseDTO>> deleteFlags(@PathVariable("itemId") UUID itemId) {
        var deletedFlags = itemService.deleteAllFlagsById(itemId);
        return HttpResponse.ok(deletedFlags);
    }

}
