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
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
@Controller("/item")
public class ItemController {

    private final ItemService itemService;

    @Inject
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @Operation(
            summary = "Add a new item",
            operationId = "addItem",
            description = "Adds a new item to the database. The item is created with the given properties.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The item was successfully added to the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemModelResponseDTO.ItemModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "500",
            description = "The item could not be added to the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemModelResponseDTO.ItemModelErrorDTO.class)
            )
    )
    @Post
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<ItemModelResponseDTO> add(
           @Valid @Body ItemModelDTO itemModelDto
    ) {
        ItemModelResponseDTO.ItemModelDTO result = itemService.createItem(itemModelDto);
        return HttpResponse.ok(result);
    }

    @Operation(
            summary = "Get an item by ID",
            operationId = "getItemById",
            description = "Retrieves an item from the database by its ID.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The item was successfully retrieved from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemModelResponseDTO.ItemModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The item was not found in the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemModelResponseDTO.ItemModelErrorDTO.class)
            )
    )
    @Get("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<ItemModelResponseDTO> getById(
            @Valid @PathVariable UUID id
    ) {
        Optional<ItemEntity> model = itemService.findItemById(id);
        if (model.isPresent()) {
            var itemModel = model.get();
            return HttpResponse.ok(ItemModelResponseDTO.ItemModelDTO.createDTO(itemModel));
        }
        return HttpResponse.notFound(new ItemModelResponseDTO.ItemModelErrorDTO("Item not found"));
    }

    @Operation(
            summary = "Remove an item by ID",
            operationId = "removeItemById",
            description = "Removes an item from the database by its ID.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The item was successfully removed from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemModelResponseDTO.ItemModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The item was not found in the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemModelResponseDTO.ItemModelErrorDTO.class)
            )
    )
    @Delete("/delete/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<ItemModelResponseDTO> remove(@PathVariable UUID id) {
        ItemModelResponseDTO result = itemService.deleteItem(id);
        if (result instanceof ItemModelResponseDTO.ItemModelErrorDTO) {
            return HttpResponse.notFound(result);
        }
        return HttpResponse.ok(result);
    }

    @Operation(
            summary = "Get all items",
            operationId = "getAllItems",
            description = "Retrieves all items from the database.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "All items were successfully retrieved from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(
                            schema = @Schema(implementation = ItemModelResponseDTO.ItemModelDTO.class),
                            arraySchema = @Schema(implementation = Page.class)
                    )
            )
    )
    @Get(uris = {"/"})
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Page<ItemModelResponseDTO.ItemModelDTO>> getAll(Pageable pageable) {
        Page<ItemModelResponseDTO.ItemModelDTO> list = itemService.getAllItems(pageable);
        return HttpResponse.ok(list);
    }

    @Operation(
            summary = "Delete all items",
            description = "Deletes all items from the database.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "All items were successfully deleted from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemModelResponseDTO.ItemModelDTO.class)
            )
    )
    @Delete("/deleteAll")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<List<ItemModelResponseDTO>> deleteAll() {
        List<ItemModelResponseDTO> result = itemService.deleteAllItems();
        return HttpResponse.ok(result);
    }

    @Operation(
            summary = "Update an item",
            operationId = "updateItem",
            description = "Updates an item in the database.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The item was successfully updated in the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemModelResponseDTO.ItemModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The item was not found in the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemModelResponseDTO.ItemModelErrorDTO.class)
            )
    )
    @Post("/update")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<ItemModelResponseDTO> update(
            @Valid @Body ItemModelDTO model
    ) {
        ItemModelResponseDTO result = itemService.updateItem(model);
        if (result instanceof ItemModelResponseDTO.ItemModelErrorDTO) {
            return HttpResponse.notFound(result);
        }
        return HttpResponse.ok(result);
    }

    @Operation(
            summary = "Get enchantments of an item",
            operationId = "getEnchantments",
            description = "Retrieves the enchantments of an item by its ID.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The enchantments of the item were successfully retrieved.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(
                            schema = @Schema(implementation = ItemEnchantmentResponseDTO.class),
                            arraySchema = @Schema(implementation = Page.class)
                    )
            )
    )
    @Get(uris = {
            "/enchantments/{id}",
            "/{id}/enchantments"
    })
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Page<ItemEnchantmentResponseDTO>> getEnchantmentsById(@PathVariable UUID id, Pageable pageable) {
        Page<ItemEnchantmentResponseDTO> enchantments = itemService.findEnchantmentsById(id, pageable);
        return HttpResponse.ok(enchantments);
    }

    @Operation(
            summary = "Update enchantment of an item",
            operationId = "updateEnchantment",
            description = "Updates the enchantment of an item by its ID.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The enchantment of the item were successfully updated.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemEnchantmentResponseDTO.ItemEnchantmentDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "No item were found for the given item ID.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemEnchantmentResponseDTO.ItemEnchantmentErrorDTO.class)
            )
    )
    @Post(uris = {
            "/enchantment/{id}",
            "/{id}/enchantment"
    })
    public HttpResponse<ItemEnchantmentResponseDTO> updateEnchantment(@PathVariable UUID id, @Body ItemEnchantmentDTO enchantment) {
        var enchantmentResult = itemService.updateEnchantmentById(id, enchantment);
        if (enchantmentResult instanceof ItemEnchantmentResponseDTO.ItemEnchantmentErrorDTO) {
            return HttpResponse.notFound(enchantmentResult);
        }
        return HttpResponse.ok(enchantmentResult);
    }

    @Operation(
            summary = "Create enchantment of an item",
            operationId = "createEnchantment",
            description = "Create the enchantment of an item by its ID.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The enchantment of the item were successfully created.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemEnchantmentResponseDTO.ItemEnchantmentDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "No item were found for the given item ID.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemEnchantmentResponseDTO.ItemEnchantmentErrorDTO.class)
            )
    )
    @Put(uris = {
            "/enchantment/{id}",
            "/{id}/enchantment"
    })
    public HttpResponse<ItemEnchantmentResponseDTO> createEnchantment(@PathVariable UUID id, @Body ItemEnchantmentDTO enchantment) {
        var enchantmentResult = itemService.createEnchantmentById(id, enchantment);
        return HttpResponse.ok(enchantmentResult);
    }

    @Operation(
            summary = "Delete enchantment of an item",
            operationId = "deleteEnchantment",
            description = "delete the enchantment of an item by its ID.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The enchantment of the item were successfully deleted.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemEnchantmentResponseDTO.ItemEnchantmentDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "No item were found for the given item ID.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemEnchantmentResponseDTO.ItemEnchantmentErrorDTO.class)
            )
    )
    @Delete(uris = {
            "/enchantment/{id}/{enchantmentId}",
            "/{id}/enchantment/{enchantmentId}"
    })
    public HttpResponse<ItemEnchantmentResponseDTO> deleteEnchantment(@PathVariable UUID id, @PathVariable UUID enchantmentId) {
        var enchantmentResult = itemService.deleteEnchantmentById(id, enchantmentId);
        if (enchantmentResult instanceof ItemEnchantmentResponseDTO.ItemEnchantmentErrorDTO) {
            return HttpResponse.notFound(enchantmentResult);
        }
        return HttpResponse.ok(enchantmentResult);
    }

    @Operation(
            summary = "Delete enchantments of an item",
            operationId = "deleteEnchantments",
            description = "delete the enchantments of an item by its ID.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The enchantment of the item were successfully deleted.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(
                            schema = @Schema(implementation = ItemEnchantmentResponseDTO.ItemEnchantmentDTO.class)
                    )
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "No item were found for the given item ID.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemEnchantmentResponseDTO.ItemEnchantmentErrorDTO.class)
            )
    )
    @Delete(uris = {
            "/enchantment/{id}/",
            "/{id}/enchantment/"
    })
    public HttpResponse<List<ItemEnchantmentResponseDTO>> deleteEnchantments(@PathVariable UUID id) {
        var enchantmentResult = itemService.deleteAllEnchantmentsById(id);
        return HttpResponse.ok(enchantmentResult);
    }

    @Operation(
            summary = "Get all lore of an item",
            operationId = "getLore",
            description = "Retrieves all lore of an item by its ID.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The lore of the item was successfully retrieved.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(
                            schema = @Schema(implementation = ItemLoreResponseDTO.class),
                            arraySchema = @Schema(implementation = Page.class)
                    )
            )
    )
    @Get(uris = {
            "/lore/{id}",
            "/{id}/lore"
    })
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Page<ItemLoreResponseDTO>> getLoreById(@PathVariable UUID id, Pageable pageable) {
        Page<ItemLoreResponseDTO> lore = itemService.findLoreById(id, pageable);
        return HttpResponse.ok(lore);
    }

    @Operation(
            summary = "Update lore of an item",
            operationId = "updateLore",
            description = "Updates the lore of an item by its ID.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The lore of the item was successfully updated.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(
                            schema = @Schema(implementation = String.class),
                            arraySchema = @Schema(implementation = List.class)
                    )
            )
    )
    @Post(uris = {
            "/lore/{id}",
            "/{id}/lore"
    })
    public HttpResponse<ItemLoreResponseDTO> updateLore(@PathVariable UUID id,@Body ItemLoreDTO lore) {
        ItemLoreResponseDTO result = itemService.updateLoreById(id, lore);
        if (result instanceof ItemLoreResponseDTO.ItemLoreErrorDTO) {
            return HttpResponse.notFound(result);
        }
        return HttpResponse.ok(result);
    }

    @Operation(
            summary = "Create lore of an item",
            operationId = "createdLore",
            description = "Updates the lore of an item by its ID.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The lore of the item was successfully created.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemLoreResponseDTO.ItemLoreDTO.class)
            )
    )
    @Put(uris = {
            "/lore/{id}",
            "/{id}/lore"
    })
    public HttpResponse<ItemLoreResponseDTO> createLore(@PathVariable UUID id,@Body ItemLoreDTO lore) {
        ItemLoreResponseDTO result = itemService.createLoreById(id, lore);
        if (result instanceof ItemLoreResponseDTO.ItemLoreErrorDTO) {
            return HttpResponse.notFound(result);
        }
        return HttpResponse.ok(result);
    }


    @Operation(
            summary = "Delete lore of an item",
            operationId = "deleteLore",
            description = "delete the lore of an item by its ID.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The lore of the item were successfully deleted.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemLoreResponseDTO.ItemLoreDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "No item were found for the given item ID.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemLoreResponseDTO.ItemLoreErrorDTO.class)
            )
    )
    @Delete(uris = {
            "/lore/{id}/{loreId}",
            "/{id}/lore/{loreId}"
    })
    public HttpResponse<ItemLoreResponseDTO> deleteLore(@PathVariable UUID id, @PathVariable UUID loreId) {
        var deleteResponse = itemService.deleteLoreById(id, loreId);
        if (deleteResponse instanceof ItemLoreResponseDTO.ItemLoreErrorDTO) {
            return HttpResponse.notFound(deleteResponse);
        }
        return HttpResponse.ok(deleteResponse);
    }

    @Operation(
            summary = "Delete all lore of an item",
            operationId = "deleteAllLore",
            description = "delete all the lores of an item by its ID.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The lores of the item were successfully deleted.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(
                            schema = @Schema(implementation = ItemLoreResponseDTO.ItemLoreDTO.class)
                    )
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "No item were found for the given item ID.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemLoreResponseDTO.ItemLoreErrorDTO.class)
            )
    )
    @Delete(uris = {
            "/lore/{id}/",
            "/{id}/lore/"
    })
    public HttpResponse<List<ItemLoreResponseDTO>> deleteLores(@PathVariable UUID id) {
        var enchantmentResult = itemService.deleteAllLoreById(id);
        return HttpResponse.ok(enchantmentResult);
    }


    @Operation(
            summary = "Get all flags of an item",
            operationId = "getFlags",
            description = "Retrieves all flags of an item by its ID.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The flags of the item were successfully retrieved.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(
                            schema = @Schema(implementation = ItemFlagResponseDTO.ItemFlagDTO.class),
                            arraySchema = @Schema(implementation = Page.class)
                    )
            )
    )
    @Get(uris = {
            "/flags/{id}",
            "/{id}/flags"
    })
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Page<ItemFlagResponseDTO>> getFlagsById(@PathVariable UUID id, Pageable pageable) {
        Page<ItemFlagResponseDTO> flags = itemService.findFlagsById(id, pageable);
        return HttpResponse.ok(flags);
    }

    @Operation(
            summary = "Update flag of an item",
            operationId = "updateFlag",
            description = "Updates the flag of an item by its ID.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The flags of the item were successfully updated.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemLoreResponseDTO.ItemLoreDTO.class)
            )
    )
    @Post(uris = {
            "/flag/{id}",
            "/{id}/flag"
    })
    public HttpResponse<ItemFlagResponseDTO> updateFlags(@PathVariable UUID id, @Body ItemFlagDTO flag) {
        ItemFlagResponseDTO result = itemService.updateFlagById(id, flag);
        if (result instanceof ItemFlagResponseDTO.ItemFlagErrorDTO) {
            return HttpResponse.notFound(result);
        }
        return HttpResponse.ok(result);
    }

    @Operation(
            summary = "Create flag of an item",
            operationId = "createdFlag",
            description = "Create the flag of an item by its ID.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The flag of the item was successfully created.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemFlagResponseDTO.ItemFlagDTO.class)
            )
    )
    @Put(uris = {
            "/lore/{id}",
            "/{id}/lore"
    })
    public HttpResponse<ItemFlagResponseDTO> createFlag(@PathVariable UUID id,@Body ItemFlagDTO dto) {
        ItemFlagResponseDTO result = itemService.createFlagById(id, dto);
        if (result instanceof ItemFlagResponseDTO.ItemFlagErrorDTO) {
            return HttpResponse.notFound(result);
        }
        return HttpResponse.ok(result);
    }


    @Operation(
            summary = "Delete flag of an item",
            operationId = "deleteFlag",
            description = "delete the flag of an item by its ID.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The flag of the item were successfully deleted.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemFlagResponseDTO.ItemFlagDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "No item were found for the given item ID.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemFlagResponseDTO.ItemFlagErrorDTO.class)
            )
    )
    @Delete(uris = {
            "/flag/{id}/{flagId}",
            "/{id}/flag/{flagId}"
    })
    public HttpResponse<ItemFlagResponseDTO> deleteFlag(@PathVariable UUID id, @PathVariable UUID flagId) {
        var deleteResponse = itemService.deleteFlagById(id, flagId);
        if (deleteResponse instanceof ItemFlagResponseDTO.ItemFlagErrorDTO) {
            return HttpResponse.notFound(deleteResponse);
        }
        return HttpResponse.ok(deleteResponse);
    }

    @Operation(
            summary = "Delete all flags of an item",
            operationId = "deleteFlags",
            description = "delete all the flags of an item by its ID.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The flags of the item were successfully deleted.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(
                            schema = @Schema(implementation = ItemFlagResponseDTO.ItemFlagDTO.class)
                    )
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "No item were found for the given item ID.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemFlagResponseDTO.ItemFlagErrorDTO.class)
            )
    )
    @Delete(uris = {
            "/flag/{id}/",
            "/{id}/flag/"
    })
    public HttpResponse<List<ItemFlagResponseDTO>> deleteFlags(@PathVariable UUID id) {
        var dtos = itemService.deleteAllFlagsById(id);
        return HttpResponse.ok(dtos);
    }

}
