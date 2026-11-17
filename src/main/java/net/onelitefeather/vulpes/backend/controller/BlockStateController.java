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
import net.onelitefeather.vulpes.backend.domain.blockstate.BlockStateDTO;
import net.onelitefeather.vulpes.backend.domain.blockstate.BlockStateResponseDTO;
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.service.BlockStateService;

import java.util.UUID;

@Controller("/project/{projectId}/blockstate")
public class BlockStateController {

    private final BlockStateService blockStateService;

    @Inject
    public BlockStateController(BlockStateService blockStateService) {
        this.blockStateService = blockStateService;
    }

    @Operation(
            summary = "Add a new blockstate",
            operationId = "addBlockState",
            description = "Adds a new blockstate to the given project.",
            tags = {"Blockstate"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The blockstate was successfully added to the database.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BlockStateResponseDTO.class)
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
    public HttpResponse<BlockStateResponseDTO> add(@PathVariable UUID projectId, @Body BlockStateDTO model) {
        return HttpResponse.ok(blockStateService.create(projectId, model));
    }

    @Operation(
            summary = "Update a blockstate",
            operationId = "updateBlockState",
            description = "Updates a blockstate owned by the given project.",
            tags = {"Blockstate"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The blockstate was successfully found.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BlockStateResponseDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The blockstate was not found, or does not belong to the given project.",
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
    public HttpResponse<BlockStateResponseDTO> update(@PathVariable UUID projectId, @Body BlockStateDTO model) {
        return HttpResponse.ok(blockStateService.update(projectId, model));
    }

    @Operation(
            summary = "Get a blockstate by ID",
            operationId = "getBlockStateById",
            description = "Retrieves a blockstate owned by the given project by its ID.",
            tags = {"Blockstate"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The blockstate was successfully retrieved from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = BlockStateResponseDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The blockstate was not found, or does not belong to the given project.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Get("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<BlockStateResponseDTO> getById(@PathVariable UUID projectId, @PathVariable UUID id) {
        return HttpResponse.ok(blockStateService.findById(projectId, id)
                .map(blockStateService::toResponse)
                .orElseThrow(() -> ApiException.notFound("Blockstate")));
    }

    @Operation(
            summary = "Delete a blockstate by ID",
            operationId = "deleteBlockStateById",
            description = "Deletes a blockstate owned by the given project.",
            tags = {"Blockstate"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The blockstate was successfully deleted.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BlockStateResponseDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The blockstate was not found, or does not belong to the given project.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Delete("/delete/{id}")
    public HttpResponse<BlockStateResponseDTO> delete(@PathVariable UUID projectId, @PathVariable UUID id) {
        return HttpResponse.ok(blockStateService.delete(projectId, id));
    }

    /**
     * Deletes all blockstates belonging to the given project.
     *
     * @return an empty 204 response
     */
    @Operation(
            summary = "Delete all blockstates",
            operationId = "deleteAllBlockStates",
            description = "Deletes all blockstates belonging to the given project.",
            tags = {"Blockstate"}
    )
    @ApiResponse(
            responseCode = "204",
            description = "All blockstates were successfully deleted."
    )
    @Delete("/delete")
    public HttpResponse<Void> deleteAll(@PathVariable UUID projectId) {
        blockStateService.deleteAll(projectId);
        return HttpResponse.noContent();
    }

    /**
     * Returns all blockstates belonging to the given project.
     *
     * @return a list with all blockstates mapped in a [HttpResponse]
     */
    @Operation(
            summary = "Get all blockstates",
            operationId = "getAllBlockStates",
            description = "Gets all blockstates belonging to the given project.",
            tags = {"Blockstate"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The blockstates were successfully retrieved.",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(
                            schema = @Schema(implementation = BlockStateResponseDTO.class),
                            arraySchema = @Schema(implementation = Page.class)
                    )
            )
    )
    @Produces(MediaType.APPLICATION_JSON)
    @Get(uris = {"/"})
    public HttpResponse<Page<BlockStateResponseDTO>> getAll(@PathVariable UUID projectId, Pageable pageable) {
        Page<BlockStateResponseDTO> models = blockStateService.getAll(projectId, pageable);
        return HttpResponse.ok(models);
    }
}
