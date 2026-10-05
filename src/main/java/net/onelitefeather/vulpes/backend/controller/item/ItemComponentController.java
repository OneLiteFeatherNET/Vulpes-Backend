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
import jakarta.validation.Valid;
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail;
import net.onelitefeather.vulpes.backend.domain.item.ItemComponentDTO;
import net.onelitefeather.vulpes.backend.domain.item.ItemComponentResponseDTO;
import net.onelitefeather.vulpes.backend.service.item.ItemComponentService;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup;

import java.util.List;
import java.util.UUID;

/**
 * Endpoints for the data components of an item, e.g. {@code minecraft:food}.
 */
@Controller("/item")
public class ItemComponentController {

    private final ItemComponentService componentService;

    @Inject
    public ItemComponentController(ItemComponentService componentService) {
        this.componentService = componentService;
    }

    @Operation(
            summary = "Add a component to an item",
            operationId = "createComponent",
            description = "Adds a data component to the item identified by itemId. An item can have each component once.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Component successfully added.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemComponentResponseDTO.ItemComponentDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "The request body failed validation, or the component has a dedicated field on the item.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
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
            responseCode = "409",
            description = "The item already has the component.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Put("/{itemId}/component")
    @Validated(groups = ValidationGroup.Create.class)
    public HttpResponse<ItemComponentResponseDTO.ItemComponentDTO> createComponent(
            @PathVariable("itemId") UUID itemId,
            @Valid @Body ItemComponentDTO component
    ) {
        return HttpResponse.ok(componentService.createComponent(itemId, component));
    }

    @Operation(
            summary = "Get the components of an item",
            operationId = "getComponents",
            description = "Retrieves a pageable list of the data components of the item identified by itemId.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Components successfully retrieved.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(
                            schema = @Schema(implementation = ItemComponentResponseDTO.class),
                            arraySchema = @Schema(implementation = Page.class)
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
    @Get("/{itemId}/components")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Page<ItemComponentResponseDTO.ItemComponentDTO>> getComponents(
            @PathVariable("itemId") UUID itemId,
            Pageable pageable
    ) {
        return HttpResponse.ok(componentService.findComponents(itemId, pageable));
    }

    @Operation(
            summary = "Update a component of an item",
            operationId = "updateComponent",
            description = "Updates a data component of the item identified by itemId.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Component successfully updated.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemComponentResponseDTO.ItemComponentDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "The request body failed validation, or the component has a dedicated field on the item.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Item or component for the given ID was not found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "The item already has the component the entry should be changed to.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Post("/{itemId}/component")
    @Validated(groups = ValidationGroup.Update.class)
    public HttpResponse<ItemComponentResponseDTO.ItemComponentDTO> updateComponent(
            @PathVariable("itemId") UUID itemId,
            @Valid @Body ItemComponentDTO component
    ) {
        return HttpResponse.ok(componentService.updateComponent(itemId, component));
    }

    @Operation(
            summary = "Remove a component from an item",
            operationId = "deleteComponent",
            description = "Removes a data component (componentId) from the item identified by itemId.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Component successfully removed.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ItemComponentResponseDTO.ItemComponentDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Item or component for the given ID was not found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Delete("/{itemId}/component/{componentId}")
    public HttpResponse<ItemComponentResponseDTO.ItemComponentDTO> deleteComponent(
            @PathVariable("itemId") UUID itemId,
            @PathVariable("componentId") UUID componentId
    ) {
        return HttpResponse.ok(componentService.deleteComponent(itemId, componentId));
    }

    @Operation(
            summary = "Remove all components from an item",
            operationId = "deleteComponents",
            description = "Removes all data components from the item identified by itemId.",
            tags = {"Item"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Components successfully removed.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(
                            schema = @Schema(implementation = ItemComponentResponseDTO.ItemComponentDTO.class)
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
    @Delete("/{itemId}/component")
    public HttpResponse<List<ItemComponentResponseDTO.ItemComponentDTO>> deleteComponents(@PathVariable("itemId") UUID itemId) {
        return HttpResponse.ok(componentService.deleteAllComponents(itemId));
    }
}
