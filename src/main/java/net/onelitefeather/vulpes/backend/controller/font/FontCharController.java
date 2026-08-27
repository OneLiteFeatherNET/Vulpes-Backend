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
import io.micronaut.http.annotation.Put;
import io.micronaut.validation.Validated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail;
import net.onelitefeather.vulpes.backend.domain.font.FontStringDTO;
import net.onelitefeather.vulpes.backend.domain.font.FontStringResponseDTO;
import net.onelitefeather.vulpes.backend.service.FontService;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup;

import java.util.List;
import java.util.UUID;

@Controller("/font/chars")
public class FontCharController {

    private final FontService fontService;

    @Inject
    public FontCharController(FontService fontService) {
        this.fontService = fontService;
    }

    @Operation(
            summary = "Create character of a font",
            operationId = "createChar",
            description = "Create the character of a font in the database.",
            tags = {"Font"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The characters of the font were successfully created in the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = FontStringResponseDTO.FontStringDTO.class)
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
    @Put("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Validated(groups = ValidationGroup.Create.class)
    public HttpResponse<FontStringResponseDTO.FontStringDTO> createChar(@PathVariable UUID id, @Body FontStringDTO charModel) {
        return HttpResponse.ok(fontService.createCharByFontId(id, charModel));
    }

    @Operation(
            summary = "Get characters by font ID",
            operationId = "getCharsById",
            description = "Gets the characters of a font by its ID from the database.",
            tags = {"Font"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The characters of the font were successfully retrieved from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = FontStringResponseDTO.FontStringDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The font was not found in the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Get("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Page<FontStringResponseDTO.FontStringDTO>> readCharsById(@PathVariable UUID id, Pageable pageable) {
        Page<FontStringResponseDTO.FontStringDTO> models = fontService.findCharsByFontId(id, pageable);
        return HttpResponse.ok(models);
    }

    @Operation(
            summary = "Update character of a font",
            operationId = "updateChar",
            description = "Updates the character of a font in the database.",
            tags = {"Font"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The characters of the font were successfully updated in the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(
                            schema = @Schema(implementation = FontStringResponseDTO.FontStringDTO.class)
                    )
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
    @Post("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Validated(groups = ValidationGroup.Update.class)
    public HttpResponse<FontStringResponseDTO.FontStringDTO> updateChar(@PathVariable UUID id, @Body FontStringDTO charModel) {
        return HttpResponse.ok(fontService.updateCharByFontId(id, charModel));
    }

    @Operation(
            summary = "Delete character of a font",
            operationId = "deleteChar",
            description = "Delete the character of a font in the database.",
            tags = {"Font"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The characters of the font were successfully updated in the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = FontStringResponseDTO.FontStringDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The character was not found in the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Delete("/{fontId}/{charId}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<FontStringResponseDTO.FontStringDTO> deleteChar(@PathVariable UUID fontId, @PathVariable UUID charId) {
        return HttpResponse.ok(fontService.deleteCharByFontId(fontId, charId));
    }

    @Operation(
            summary = "Delete character of a font",
            operationId = "deleteChar",
            description = "Delete the character of a font in the database.",
            tags = {"Font"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The characters of the font were successfully deleted in the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(
                            schema = @Schema(implementation = FontStringResponseDTO.FontStringDTO.class)
                    )
            )
    )
    @Delete("/{fontId}/")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<List<FontStringResponseDTO.FontStringDTO>> deleteAllChars(@PathVariable UUID fontId) {
        return HttpResponse.ok(fontService.deleteAllCharByFontId(fontId));
    }
}
