package net.onelitefeather.vulpes.backend.controller.dimension;

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
import net.onelitefeather.vulpes.backend.domain.dimension.DimensionModelDTO;
import net.onelitefeather.vulpes.backend.domain.dimension.DimensionModelResponseDTO;
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.service.DimensionService;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup;

import java.util.UUID;

/**
 * REST controller for dimension type resources.
 * Provides CRUD operations for dimension types scoped to a project.
 */
@Controller("/project/{projectId}/dimension")
public class DimensionController {

    private final DimensionService dimensionService;

    @Inject
    public DimensionController(DimensionService dimensionService) {
        this.dimensionService = dimensionService;
    }

    @Operation(
            summary = "Create a new dimension type",
            operationId = "addDimension",
            description = "Creates a new dimension type in the given project and stores it in the database.",
            tags = {"Dimension"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Dimension type successfully created.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = DimensionModelResponseDTO.DimensionModelDTO.class)
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
    public HttpResponse<DimensionModelResponseDTO.DimensionModelDTO> add(
            @PathVariable UUID projectId,
            @Body DimensionModelDTO dimensionModel
    ) {
        return HttpResponse.ok(dimensionService.create(projectId, dimensionModel));
    }

    @Operation(
            summary = "Get a dimension type by ID",
            operationId = "getDimensionById",
            description = "Retrieves a single dimension type owned by the given project by its unique ID (dimensionId).",
            tags = {"Dimension"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Dimension type successfully retrieved.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = DimensionModelResponseDTO.DimensionModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Dimension type with the given ID was not found, or does not belong to the given project.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Get("/{dimensionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<DimensionModelResponseDTO.DimensionModelDTO> getById(
            @PathVariable UUID projectId,
            @PathVariable("dimensionId") UUID dimensionId
    ) {
        return HttpResponse.ok(dimensionService.findById(projectId, dimensionId)
                .map(DimensionModelResponseDTO.DimensionModelDTO::createDTO)
                .orElseThrow(() -> ApiException.notFound("Dimension")));
    }

    @Operation(
            summary = "Get all dimension types",
            operationId = "getAllDimensions",
            description = "Retrieves a pageable list of all dimension types belonging to the given project. Supports standard Micronaut pagination (page, size, sort).",
            tags = {"Dimension"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Dimension types successfully retrieved.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(
                            schema = @Schema(implementation = DimensionModelResponseDTO.DimensionModelDTO.class),
                            arraySchema = @Schema(implementation = Page.class)
                    )
            )
    )
    @Get
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Page<DimensionModelResponseDTO.DimensionModelDTO>> getAll(@PathVariable UUID projectId, Pageable pageable) {
        Page<DimensionModelResponseDTO.DimensionModelDTO> dimensionsPage = dimensionService.getAll(projectId, pageable);
        return HttpResponse.ok(dimensionsPage);
    }

    @Operation(
            summary = "Update a dimension type",
            operationId = "updateDimension",
            description = "Updates an existing dimension type owned by the given project.",
            tags = {"Dimension"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Dimension type successfully updated.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = DimensionModelResponseDTO.DimensionModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Dimension type was not found, or does not belong to the given project.",
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
    public HttpResponse<DimensionModelResponseDTO.DimensionModelDTO> update(
            @PathVariable UUID projectId,
            @Body DimensionModelDTO dimensionModel
    ) {
        return HttpResponse.ok(dimensionService.update(projectId, dimensionModel));
    }

    @Operation(
            summary = "Remove a dimension type by ID",
            operationId = "removeDimensionById",
            description = "Deletes a dimension type owned by the given project by its unique ID (dimensionId).",
            tags = {"Dimension"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Dimension type successfully deleted.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = DimensionModelResponseDTO.DimensionModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Dimension type with the given ID was not found, or does not belong to the given project.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Delete("/delete/{dimensionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<DimensionModelResponseDTO.DimensionModelDTO> delete(
            @PathVariable UUID projectId,
            @PathVariable("dimensionId") UUID dimensionId
    ) {
        return HttpResponse.ok(dimensionService.delete(projectId, dimensionId));
    }

    @Operation(
            summary = "Delete all dimension types",
            description = "Deletes all dimension types belonging to the given project.",
            tags = {"Dimension"}
    )
    @ApiResponse(
            responseCode = "204",
            description = "All dimension types were successfully deleted."
    )
    @Delete("/deleteAll")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Void> deleteAll(@PathVariable UUID projectId) {
        dimensionService.deleteAll(projectId);
        return HttpResponse.noContent();
    }
}
