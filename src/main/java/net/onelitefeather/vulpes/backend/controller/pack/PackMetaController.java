package net.onelitefeather.vulpes.backend.controller.pack;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.Put;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import net.onelitefeather.vulpes.api.model.pack.PackType;
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail;
import net.onelitefeather.vulpes.backend.domain.pack.PackMetaDTO;
import net.onelitefeather.vulpes.backend.domain.pack.PackResponse;
import net.onelitefeather.vulpes.backend.service.PackMetaService;

import java.util.List;
import java.util.UUID;

/**
 * Manages the {@code pack.mcmeta} content of a project, one entry per {@link PackType}.
 */
@Controller("/project/{projectId}/pack-meta")
public class PackMetaController {

    private final PackMetaService packMetaService;

    @Inject
    public PackMetaController(PackMetaService packMetaService) {
        this.packMetaService = packMetaService;
    }

    @Operation(
            summary = "Get all pack metas",
            operationId = "getAllPackMetas",
            description = "Gets the pack metas of the given project, at most one per pack type.",
            tags = {"PackMeta"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The pack metas were successfully retrieved from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(schema = @Schema(implementation = PackResponse.PackMetaDTO.class))
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
    @Get(uris = {"/"})
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<List<PackResponse.PackMetaDTO>> getAll(@PathVariable UUID projectId) {
        return HttpResponse.ok(packMetaService.getAll(projectId));
    }

    @Operation(
            summary = "Get a pack meta by pack type",
            operationId = "getPackMeta",
            description = "Gets the pack meta of the given project for one pack type.",
            tags = {"PackMeta"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The pack meta was successfully retrieved from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = PackResponse.PackMetaDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The project has no pack meta of that type.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Get("/{packType}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<PackResponse.PackMetaDTO> get(@PathVariable UUID projectId, @PathVariable PackType packType) {
        return HttpResponse.ok(packMetaService.get(projectId, packType));
    }

    @Operation(
            summary = "Create or replace a pack meta",
            operationId = "putPackMeta",
            description = "Creates the pack meta of the given project for one pack type, or replaces the existing one.",
            tags = {"PackMeta"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The pack meta was successfully stored.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = PackResponse.PackMetaDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "The request body failed validation, the format range is inverted or the description is no JSON text component.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
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
    @Put("/{packType}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<PackResponse.PackMetaDTO> put(
            @PathVariable UUID projectId,
            @PathVariable PackType packType,
            @Body PackMetaDTO item
    ) {
        return HttpResponse.ok(packMetaService.put(projectId, packType, item));
    }

    @Operation(
            summary = "Remove a pack meta by pack type",
            operationId = "deletePackMeta",
            description = "Removes the pack meta of the given project for one pack type.",
            tags = {"PackMeta"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The pack meta was successfully removed from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = PackResponse.PackMetaDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The project has no pack meta of that type.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Delete("/{packType}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<PackResponse.PackMetaDTO> remove(@PathVariable UUID projectId, @PathVariable PackType packType) {
        return HttpResponse.ok(packMetaService.delete(projectId, packType));
    }
}
