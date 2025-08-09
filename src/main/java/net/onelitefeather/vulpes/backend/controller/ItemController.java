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
import java.util.stream.Collectors;

import static net.onelitefeather.vulpes.backend.domain.item.ItemModelResponseDTO.*;

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
                    schema = @Schema(implementation = ItemModelEnchantmentResponseDTO.class)
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
    @Get("/enchantments/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<ItemModelResponseDTO> getEnchantmentsById(@PathVariable UUID id) {
        Map<String, Short> enchantments = itemService.findEnchantmentsById(id);
        if (enchantments.isEmpty()) {
            return HttpResponse.notFound(new ItemModelErrorDTO("Item not found or has no enchantments"));
        }
        return HttpResponse.ok(ItemModelEnchantmentResponseDTO.create(id, enchantments));
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
                    schema = @Schema(implementation = ItemModelFlagResponseDTO.class)
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
    @Get("/flags/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<ItemModelResponseDTO> getFlagsById(@PathVariable UUID id) {
        List<String> flags = itemService.findFlagsById(id);
        if (flags.isEmpty()) {
            return HttpResponse.notFound(new ItemModelErrorDTO("Item not found or has no flags"));
        }
        return HttpResponse.ok(ItemModelFlagResponseDTO.createDTO(id, flags));
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
                    schema = @Schema(implementation = ItemModelLoreResponseDTO.class)
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
    @Get("/lore/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<ItemModelResponseDTO> getLoreById(@PathVariable UUID id) {
        List<String> lore = itemService.findLoreById(id);
        if (lore.isEmpty()) {
            return HttpResponse.notFound(new ItemModelErrorDTO("Item not found or has no lore"));
        }
        return HttpResponse.ok(ItemModelLoreResponseDTO.createDTO(id, lore));
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
                    schema = @Schema(implementation = ItemModelResponseDTO.ItemModelDTO.class)
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
