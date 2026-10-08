package net.onelitefeather.vulpes.backend.controller;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.validation.Validated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail;
import net.onelitefeather.vulpes.backend.domain.advancement.AdvancementModelDTO;
import net.onelitefeather.vulpes.backend.domain.advancement.AdvancementModelResponseDTO;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.service.AdvancementService;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup;

import java.util.UUID;

/**
 * Controller for managing advancements.
 * Provides endpoints to add, retrieve, update, and delete advancements.
 *
 * @author theEvilReaper
 * @version 2.0.0
 * @since 1.0.0
 */
@Controller("/project/{projectId}/advancement")
public class AdvancementController {

    private final AdvancementService advancementService;

    @Inject
    public AdvancementController(AdvancementService advancementService) {
        this.advancementService = advancementService;
    }

    @Operation(
            summary = "Add a new advancement",
            operationId = "addAdvancement",
            description = "Adds a new advancement to the given project.",
            tags = {"Advancement"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The advancement was successfully added to the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = AdvancementModelResponseDTO.AdvancementModelDTO.class)
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
    public HttpResponse<AdvancementModelResponseDTO.AdvancementModelDTO> add(@PathVariable UUID projectId, @Body AdvancementModelDTO model) {
        return HttpResponse.ok(advancementService.create(projectId, model));
    }

    @Operation(
            summary = "Get an advancement by ID",
            operationId = "getAdvancementById",
            description = "Retrieves an advancement owned by the given project by its ID.",
            tags = {"Advancement"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The advancement was successfully retrieved from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = AdvancementModelResponseDTO.AdvancementModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The advancement was not found, or does not belong to the given project.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Get("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<AdvancementModelResponseDTO.AdvancementModelDTO> getById(@PathVariable UUID projectId, @PathVariable UUID id) {
        return HttpResponse.ok(advancementService.findById(projectId, id)
                .map(AdvancementModelResponseDTO.AdvancementModelDTO::createDTO)
                .orElseThrow(() -> ApiException.notFound("Advancement")));
    }

    @Operation(
            summary = "Remove an advancement by ID",
            operationId = "removeAdvancementById",
            description = "Removes an advancement owned by the given project by its ID.",
            tags = {"Advancement"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The advancement was successfully removed from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = AdvancementModelResponseDTO.AdvancementModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The advancement was not found, or does not belong to the given project.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Delete("/delete/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<AdvancementModelResponseDTO.AdvancementModelDTO> remove(@PathVariable UUID projectId, @PathVariable UUID id) {
        return HttpResponse.ok(advancementService.delete(projectId, id));
    }

    @Operation(
            summary = "Get all advancements",
            operationId = "getAllAdvancements",
            description = "Retrieves all advancements belonging to the given project.",
            tags = {"Advancement"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The advancements were successfully retrieved from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(
                            schema = @Schema(implementation = AdvancementModelResponseDTO.AdvancementModelDTO.class),
                            arraySchema = @Schema(implementation = Page.class)
                    )
            )
    )
    @Get(uris = {"/"})
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Page<AdvancementModelResponseDTO.AdvancementModelDTO>> getAll(@PathVariable UUID projectId, Pageable pageable) {
        Page<AdvancementModelResponseDTO.AdvancementModelDTO> list = advancementService.getAll(projectId, pageable);
        return HttpResponse.ok(list);
    }

    @Operation(
            summary = "Delete all advancements",
            operationId = "deleteAllAdvancements",
            description = "Deletes all advancements belonging to the given project.",
            tags = {"Advancement"}
    )
    @ApiResponse(
            responseCode = "204",
            description = "All advancements were successfully deleted from the database."
    )
    @Delete("/delete/")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Void> deleteAll(@PathVariable UUID projectId) {
        advancementService.deleteAll(projectId);
        return HttpResponse.noContent();
    }

    @Operation(
            summary = "Update an advancement",
            operationId = "updateAdvancement",
            description = "Updates an advancement owned by the given project.",
            tags = {"Advancement"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The advancement was successfully updated in the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = AdvancementModelResponseDTO.AdvancementModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The advancement was not found, or does not belong to the given project.",
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
    public HttpResponse<AdvancementModelResponseDTO.AdvancementModelDTO> update(@PathVariable UUID projectId, @Body AdvancementModelDTO model) {
        return HttpResponse.ok(advancementService.update(projectId, model));
    }
}
