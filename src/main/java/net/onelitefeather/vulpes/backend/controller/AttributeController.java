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
import io.micronaut.validation.Validated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import net.onelitefeather.vulpes.backend.domain.attribute.AttributeModelDTO;
import net.onelitefeather.vulpes.backend.domain.attribute.AttributeModelResponseDTO;
import net.onelitefeather.vulpes.backend.service.AttributeService;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup;

import java.util.List;
import java.util.UUID;

@Controller("/attribute")
public class AttributeController {

    private final AttributeService attributeService;

    @Inject
    public AttributeController(AttributeService attributeService) {
        this.attributeService = attributeService;
    }

    @Operation(
            summary = "Add a new attribute",
            operationId = "addAttribute",
            description = "Adds a new attribute to the database. The attribute is created with the given properties.",
            tags = {"Attribute"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The attribute was successfully added to the database.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AttributeModelResponseDTO.AttributeModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "500",
            description = "The attribute could not be added to the database.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AttributeModelResponseDTO.AttributeModelErrorDTO.class)
            )
    )
    @Post
    @Validated(groups = ValidationGroup.Create.class)
    public HttpResponse<AttributeModelResponseDTO> add(@Body AttributeModelDTO model) {
        AttributeModelResponseDTO.AttributeModelDTO createdAttribute = attributeService.createAttribute(model);
        return HttpResponse.ok(createdAttribute);
    }

    @Operation(
            summary = "Update an attribute",
            operationId = "updateAttribute",
            description = "Returns the attribute with the given ID.",
            tags = {"Attribute"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The attribute was successfully found.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AttributeModelResponseDTO.AttributeModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The attribute was not found.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AttributeModelResponseDTO.AttributeModelErrorDTO.class)
            )
    )
    @Post("/update")
    @Validated(groups = ValidationGroup.Update.class)
    public HttpResponse<AttributeModelResponseDTO> update(@Body AttributeModelDTO model) {
        AttributeModelResponseDTO result = attributeService.updateAttribute(model);
        if (result instanceof AttributeModelResponseDTO.AttributeModelErrorDTO) {
            return HttpResponse.notFound(result);
        }
        return HttpResponse.ok(result);
    }

    @Operation(
            summary = "Delete an attribute by ID",
            operationId = "deleteAttributeById",
            description = "Deletes the attribute with the given ID.",
            tags = {"Attribute"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The attribute was successfully deleted.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AttributeModelResponseDTO.AttributeModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The attribute was not found.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AttributeModelResponseDTO.AttributeModelErrorDTO.class)
            )
    )
    @Delete("/delete/{id}")
    public HttpResponse<AttributeModelResponseDTO> delete(@PathVariable UUID id) {
        AttributeModelResponseDTO result = attributeService.deleteAttribute(id);
        if (result instanceof AttributeModelResponseDTO.AttributeModelErrorDTO) {
            return HttpResponse.notFound(result);
        }
        return HttpResponse.ok(result);
    }

    /**
     * Deletes all [AttributeModel] from the database.
     *
     * @return a list with all [AttributeModel] mapped in a [HttpResponse]
     */
    @Operation(
            summary = "Delete all attributes",
            operationId = "deleteAllAttributes",
            description = "Deletes all attributes from the database.",
            tags = {"Attribute"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "All attributes were successfully deleted.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AttributeModelResponseDTO.AttributeModelDTO.class)
            )
    )
    @Delete("/delete")
    public HttpResponse<List<AttributeModelResponseDTO>> deleteAll() {
        List<AttributeModelResponseDTO> result = attributeService.deleteAllAttributes();
        return HttpResponse.ok(result);
    }

    /**
     * Returns all [AttributeModel] which are currently persists in the database.
     *
     * @return a list with all [AttributeModel] mapped in a [HttpResponse]
     */
    @Operation(
            summary = "Get all attributes",
            operationId = "getAllAttributes",
            description = "Gets all attributes from the database.",
            tags = {"Attribute"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The attributes were successfully retrieved.",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(
                            schema = @Schema(implementation = AttributeModelResponseDTO.AttributeModelDTO.class),
                            arraySchema = @Schema(implementation = Page.class)
                    )
            )
    )
    @Produces(MediaType.APPLICATION_JSON)
    @Get(uris = {"/"})
    public HttpResponse<Page<AttributeModelResponseDTO.AttributeModelDTO>> getAll(Pageable pageable) {
        Page<AttributeModelResponseDTO.AttributeModelDTO> models = attributeService.getAllAttributes(pageable);
        return HttpResponse.ok(models);
    }
}
