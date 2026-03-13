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
import io.micronaut.http.annotation.Put;
import io.micronaut.validation.Validated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import net.onelitefeather.vulpes.backend.domain.item.ItemEnchantmentDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemEnchantmentResponseDTO;
import net.onelitefeather.vulpes.backend.service.ItemService;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup;

import java.util.List;
import java.util.UUID;

@Controller("/item")
public class ItemEnchantmentController {

    private final ItemService itemService;

    @Inject
    public ItemEnchantmentController(ItemService itemService) {
        this.itemService = itemService;
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
    @Validated(groups = ValidationGroup.Create.class)
    public HttpResponse<ItemEnchantmentResponseDTO> createEnchantment(@PathVariable("itemId") UUID itemId, @Body ItemEnchantmentDTO enchantment) {
        var createResult = itemService.createEnchantmentById(itemId, enchantment);
        return HttpResponse.ok(createResult);
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
    @Validated(groups = ValidationGroup.Update.class)
    public HttpResponse<ItemEnchantmentResponseDTO> updateEnchantment(@PathVariable("itemId") UUID itemId, @Body ItemEnchantmentDTO enchantment) {
        var updateResult = itemService.updateEnchantmentById(itemId, enchantment);
        if (updateResult instanceof ItemEnchantmentResponseDTO.ItemEnchantmentErrorDTO) {
            return HttpResponse.notFound(updateResult);
        }
        return HttpResponse.ok(updateResult);
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
            "/enchantment/{itemId}",
            "/{itemId}/enchantment"
    })
    public HttpResponse<List<ItemEnchantmentResponseDTO>> deleteEnchantments(@PathVariable("itemId") UUID itemId) {
        var deletedEnchantments = itemService.deleteAllEnchantmentsById(itemId);
        return HttpResponse.ok(deletedEnchantments);
    }
}

