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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import net.onelitefeather.vulpes.api.model.ItemEntity;
import net.onelitefeather.vulpes.backend.domain.item.ItemModelDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemModelResponseDTO;
import net.onelitefeather.vulpes.backend.service.ItemService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static net.onelitefeather.vulpes.backend.domain.item.ItemModelResponseDTO.ItemModelEnchantmentResponseDTO;
import static net.onelitefeather.vulpes.backend.domain.item.ItemModelResponseDTO.ItemModelErrorDTO;

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
                    schema = @Schema(implementation = ItemModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "500",
            description = "The item could not be added to the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemModelErrorDTO.class)
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
                    schema = @Schema(implementation = ItemModelErrorDTO.class)
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
        return HttpResponse.notFound(new ItemModelErrorDTO("Item not found"));
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
                    schema = @Schema(implementation = ItemModelErrorDTO.class)
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
            summary = "Get enchantments of an item",
            description = "Retrieves the enchantments of an item by its ID.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The enchantments of the item were successfully retrieved.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(
                            schema = @Schema(implementation = ItemModelEnchantmentResponseDTO.class),
                            arraySchema = @Schema(implementation = Page.class)
                    )
            )
    )
    @Get(uris = {
            "/enchantments/{id}",
            "/{id}/enchantments"
    })
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Page<ItemModelEnchantmentResponseDTO>> getEnchantmentsById(@PathVariable UUID id, Pageable pageable) {
        Map<String, Short> enchantments = itemService.findEnchantmentsById(id, pageable);
        return HttpResponse.ok(Page.of(enchantments.entrySet().stream().map(ItemModelResponseDTO.ItemModelEnchantmentResponseDTO::createDTO).toList(), pageable, (long) enchantments.size()));
    }

    @Operation(
            summary = "Update enchantments of an item",
            description = "Updates the enchantments of an item by its ID.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The enchantments of the item were successfully updated.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(
                            schema = @Schema(implementation = ItemModelEnchantmentResponseDTO.class),
                            arraySchema = @Schema(implementation = List.class)
                    )
            )
    )
    @Post(uris = {
            "/enchantments/{id}",
            "/{id}/enchantments"
    })
    public HttpResponse<List<ItemModelEnchantmentResponseDTO>> updateEnchantments(@PathVariable UUID id, @Body Map<String, Short> enchantments) {
        var enchantmentResult = itemService.updateEnchantmentsById(id, enchantments);
        return HttpResponse.ok(enchantmentResult.entrySet().stream().map(ItemModelResponseDTO.ItemModelEnchantmentResponseDTO::createDTO).toList());
    }

    @Operation(
            summary = "Get all flags of an item",
            description = "Retrieves all flags of an item by its ID.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The flags of the item were successfully retrieved.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(
                            schema = @Schema(implementation = String.class),
                            arraySchema = @Schema(implementation = Page.class)
                    )
            )
    )
    @Get(uris = {
            "/flags/{id}",
            "/{id}/flags"
    })
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Page<String>> getFlagsById(@PathVariable UUID id, Pageable pageable) {
        List<String> flags = itemService.findFlagsById(id, pageable);
        return HttpResponse.ok(Page.of(flags, pageable, (long) flags.size()));
    }

    @Operation(
            summary = "Update flags of an item",
            description = "Updates the flags of an item by its ID.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The flags of the item were successfully updated.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(
                            schema = @Schema(implementation = String.class),
                            arraySchema = @Schema(implementation = List.class)
                    )
            )
    )
    @Post(uris = {
            "/flags/{id}",
            "/{id}/flags"
    })
    public HttpResponse<List<String>> updateFlags(@PathVariable UUID id,@Body List<String> flags) {
        List<String> result = itemService.updateFlagsById(id, flags);
        return HttpResponse.ok(result);
    }

    @Operation(
            summary = "Get all lore of an item",
            description = "Retrieves all lore of an item by its ID.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The lore of the item was successfully retrieved.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(
                            schema = @Schema(implementation = String.class),
                            arraySchema = @Schema(implementation = Page.class)
                    )
            )
    )
    @Get(uris = {
            "/lore/{id}",
            "/{id}/lore"
    })
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Page<String>> getLoreById(@PathVariable UUID id, Pageable pageable) {
        List<String> lore = itemService.findLoreById(id, pageable);
        return HttpResponse.ok(Page.of(lore, pageable, (long) lore.size()));
    }

    @Operation(
            summary = "Update lore of an item",
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
    public HttpResponse<List<String>> updateLore(@PathVariable UUID id,@Body List<String> lore) {
        List<String> result = itemService.updateLoreById(id, lore);
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
    @Get(uris = {"/all"})
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
                    schema = @Schema(implementation = ItemModelErrorDTO.class)
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
}
