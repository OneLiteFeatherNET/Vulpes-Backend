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
import net.onelitefeather.vulpes.backend.domain.item.ItemFlagDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemFlagResponseDTO;
import net.onelitefeather.vulpes.backend.service.ItemService;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup;

import java.util.List;
import java.util.UUID;

@Controller("/item/")
public class ItemFlagController {

    private final ItemService itemService;

    @Inject
    public ItemFlagController(ItemService itemService) {
        this.itemService = itemService;
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
    @Validated(groups = ValidationGroup.Update.class)
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
    @Validated(groups = ValidationGroup.Create.class)
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
            "/flag/{itemId}",
            "/{itemId}/flag"
    })
    public HttpResponse<List<ItemFlagResponseDTO>> deleteFlags(@PathVariable("itemId") UUID itemId) {
        var deletedFlags = itemService.deleteAllFlagsById(itemId);
        return HttpResponse.ok(deletedFlags);
    }
}
