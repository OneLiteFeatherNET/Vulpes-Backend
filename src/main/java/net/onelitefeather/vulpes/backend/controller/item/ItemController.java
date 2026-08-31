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
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail;
import net.onelitefeather.vulpes.backend.domain.item.ItemModelDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemModelResponseDTO;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.service.ItemService;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup;

import java.util.UUID;

/**
 * REST controller for item resources.
 * Provides CRUD operations and nested resource management (enchantments, lore, flags).
 */
@Controller("/project/{projectId}/item")
public class ItemController {

    private final ItemService itemService;

    @Inject
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @Operation(
            summary = "Create a new item",
            operationId = "addItem",
            description = "Creates a new item in the given project and stores it in the database.",
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
            responseCode = "404",
            description = "The project was not found.",
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
    @Post
    @Produces(MediaType.APPLICATION_JSON)
    @Validated(groups = ValidationGroup.Create.class)
    public HttpResponse<ItemModelResponseDTO.ItemModelDTO> add(
            @PathVariable UUID projectId,
            @Body ItemModelDTO itemModel
    ) {
        return HttpResponse.ok(itemService.create(projectId, itemModel));
    }

    @Operation(
            summary = "Get an item by ID",
            operationId = "getItemById",
            description = "Retrieves a single item owned by the given project by its unique ID (itemId).",
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
            description = "Item with the given ID was not found, or does not belong to the given project.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Get("/{itemId}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<ItemModelResponseDTO.ItemModelDTO> getById(
            @PathVariable UUID projectId,
            @PathVariable("itemId") UUID itemId
    ) {
        return HttpResponse.ok(itemService.findById(projectId, itemId)
                .map(ItemModelResponseDTO.ItemModelDTO::createDTO)
                .orElseThrow(() -> ApiException.notFound("Item")));
    }

    @Operation(
            summary = "Get all items",
            operationId = "getAllItems",
            description = "Retrieves a pageable list of all items belonging to the given project. Supports standard Micronaut pagination (page, size, sort).",
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
    public HttpResponse<Page<ItemModelResponseDTO.ItemModelDTO>> getAll(@PathVariable UUID projectId, Pageable pageable) {
        Page<ItemModelResponseDTO.ItemModelDTO> itemsPage = itemService.getAll(projectId, pageable);
        return HttpResponse.ok(itemsPage);
    }

    @Operation(
            summary = "Update an item",
            operationId = "updateItem",
            description = "Updates an existing item owned by the given project.",
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
            description = "Item was not found, or does not belong to the given project.",
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
    @Post("/update")
    @Produces(MediaType.APPLICATION_JSON)
    @Validated(groups = ValidationGroup.Update.class)
    public HttpResponse<ItemModelResponseDTO.ItemModelDTO> update(
            @PathVariable UUID projectId,
            @Body ItemModelDTO itemModel
    ) {
        return HttpResponse.ok(itemService.update(projectId, itemModel));
    }

    @Operation(
            summary = "Remove an item by ID",
            operationId = "removeItemById",
            description = "Deletes an item owned by the given project by its unique ID (itemId).",
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
            description = "Item with the given ID was not found, or does not belong to the given project.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Delete("/delete/{itemId}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<ItemModelResponseDTO.ItemModelDTO> delete(@PathVariable UUID projectId, @PathVariable("itemId") UUID itemId) {
        return HttpResponse.ok(itemService.delete(projectId, itemId));
    }

    @Operation(
            summary = "Delete all items",
            description = "Deletes all items belonging to the given project.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "204",
            description = "All items were successfully deleted."
    )
    @Delete("/deleteAll")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Void> deleteAll(@PathVariable UUID projectId) {
        itemService.deleteAll(projectId);
        return HttpResponse.noContent();
    }
}
