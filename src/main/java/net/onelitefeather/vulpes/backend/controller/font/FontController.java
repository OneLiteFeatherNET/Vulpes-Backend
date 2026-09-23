package net.onelitefeather.vulpes.backend.controller.font;

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
import net.onelitefeather.vulpes.backend.domain.font.FontModelDTO;
import net.onelitefeather.vulpes.backend.domain.font.FontModelResponseDTO;
import net.onelitefeather.vulpes.backend.exception.ApiException;
import net.onelitefeather.vulpes.backend.service.FontService;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup;

import java.util.UUID;

import static net.onelitefeather.vulpes.backend.domain.font.FontModelResponseDTO.*;

@Controller("/project/{projectId}/font")
public class FontController {

    private final FontService fontService;

    @Inject
    public FontController(FontService fontService) {
        this.fontService = fontService;
    }

    @Operation(
            summary = "Add a new font",
            operationId = "addFont",
            description = "Adds a new font to the given project.",
            tags = {"Font"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The font was successfully added to the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = FontModelResponseDTO.FontModelDTO.class)
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
    public HttpResponse<FontModelResponseDTO.FontModelDTO> add(@PathVariable UUID projectId, @Body FontModelDTO item) {
        return HttpResponse.ok(fontService.create(projectId, item));
    }

    @Operation(
            summary = "Get a font by ID",
            operationId = "getFontById",
            description = "Gets a font owned by the given project by ID.",
            tags = {"Font"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The font was successfully retrieved from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = FontModelResponseDTO.FontModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The font was not found, or does not belong to the given project.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Get("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<FontModelResponseDTO.FontModelDTO> getById(@PathVariable UUID projectId, @PathVariable UUID id) {
        return HttpResponse.ok(fontService.findById(projectId, id)
                .map(FontModelResponseDTO.FontModelDTO::createDTO)
                .orElseThrow(() -> ApiException.notFound("Font")));
    }

    @Operation(
            summary = "Update an existing font",
            operationId = "updateFont",
            description = "Updates a font in the given project.",
            tags = {"Font"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The font was successfully updated.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = FontModelResponseDTO.FontModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The font or project was not found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Post("/update")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<FontModelResponseDTO.FontModelDTO> update(@PathVariable UUID projectId, @Body FontModelDTO item) {
        return HttpResponse.ok(fontService.update(projectId, item));
    }

    @Operation(
            summary = "Remove a font by ID",
            operationId = "deleteFont",
            description = "Removes a font owned by the given project by ID.",
            tags = {"Font"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The font was successfully removed from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = FontModelResponseDTO.FontModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The font was not found, or does not belong to the given project.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Delete("/delete/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<FontModelResponseDTO.FontModelDTO> remove(@PathVariable UUID projectId, @PathVariable UUID id) {
        return HttpResponse.ok(fontService.delete(projectId, id));
    }

    @Operation(
            summary = "Get all fonts",
            operationId = "getAllFonts",
            description = "Gets all fonts belonging to the given project.",
            tags = {"Font"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The fonts were successfully retrieved from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(
                            schema = @Schema(implementation = FontModelResponseDTO.FontModelDTO.class),
                            arraySchema = @Schema(implementation = Page.class)
                    )
            )
    )
    @Get(uris = {"/"})
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Page<FontModelResponseDTO.FontModelDTO>> getAll(@PathVariable UUID projectId, Pageable pageable) {
        Page<FontModelResponseDTO.FontModelDTO> models = fontService.getAll(projectId, pageable);
        return HttpResponse.ok(models);
    }

    @Operation(
            summary = "Delete all fonts",
            operationId = "deleteAllFonts",
            description = "Deletes all fonts belonging to the given project.",
            tags = {"Font"}
    )
    @ApiResponse(
            responseCode = "204",
            description = "All fonts were successfully deleted from the database."
    )
    @Delete("delete")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Void> deleteAll(@PathVariable UUID projectId) {
        fontService.deleteAll(projectId);
        return HttpResponse.noContent();
    }
}
