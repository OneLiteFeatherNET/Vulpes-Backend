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
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail;
import net.onelitefeather.vulpes.backend.service.AttributeService;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup;

import java.util.UUID;

@Controller("/project/{projectId}/attribute")
public class AttributeController {

    private final AttributeService attributeService;

    @Inject
    public AttributeController(AttributeService attributeService) {
        this.attributeService = attributeService;
    }

    @Operation(
            summary = "Add a new attribute",
            operationId = "addAttribute",
            description = "Adds a new attribute to the given project.",
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
    @Validated(groups = ValidationGroup.Create.class)
    public HttpResponse<AttributeModelResponseDTO.AttributeModelDTO> add(@PathVariable UUID projectId, @Body AttributeModelDTO model) {
        return HttpResponse.ok(attributeService.create(projectId, model));
    }

    @Operation(
            summary = "Update an attribute",
            operationId = "updateAttribute",
            description = "Updates an attribute owned by the given project.",
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
            description = "The attribute was not found, or does not belong to the given project.",
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
    @Validated(groups = ValidationGroup.Update.class)
    public HttpResponse<AttributeModelResponseDTO.AttributeModelDTO> update(@PathVariable UUID projectId, @Body AttributeModelDTO model) {
        return HttpResponse.ok(attributeService.update(projectId, model));
    }

    @Operation(
            summary = "Delete an attribute by ID",
            operationId = "deleteAttributeById",
            description = "Deletes an attribute owned by the given project.",
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
            description = "The attribute was not found, or does not belong to the given project.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Delete("/delete/{id}")
    public HttpResponse<AttributeModelResponseDTO.AttributeModelDTO> delete(@PathVariable UUID projectId, @PathVariable UUID id) {
        return HttpResponse.ok(attributeService.delete(projectId, id));
    }

    /**
     * Deletes all [AttributeModel] belonging to the given project.
     *
     * @return an empty 204 response
     */
    @Operation(
            summary = "Delete all attributes",
            operationId = "deleteAllAttributes",
            description = "Deletes all attributes belonging to the given project.",
            tags = {"Attribute"}
    )
    @ApiResponse(
            responseCode = "204",
            description = "All attributes were successfully deleted."
    )
    @Delete("/delete")
    public HttpResponse<Void> deleteAll(@PathVariable UUID projectId) {
        attributeService.deleteAll(projectId);
        return HttpResponse.noContent();
    }

    /**
     * Returns all [AttributeModel] belonging to the given project.
     *
     * @return a list with all [AttributeModel] mapped in a [HttpResponse]
     */
    @Operation(
            summary = "Get all attributes",
            operationId = "getAllAttributes",
            description = "Gets all attributes belonging to the given project.",
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
    public HttpResponse<Page<AttributeModelResponseDTO.AttributeModelDTO>> getAll(@PathVariable UUID projectId, Pageable pageable) {
        Page<AttributeModelResponseDTO.AttributeModelDTO> models = attributeService.getAll(projectId, pageable);
        return HttpResponse.ok(models);
    }
}
