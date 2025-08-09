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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import net.onelitefeather.vulpes.api.model.FontEntity;
import net.onelitefeather.vulpes.backend.domain.font.FontModelDTO;
import net.onelitefeather.vulpes.backend.domain.font.FontModelResponseDTO;
import net.onelitefeather.vulpes.backend.service.FontService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.onelitefeather.vulpes.backend.domain.font.FontModelResponseDTO.*;

@Controller("/font")
public class FontController {

    private final FontService fontService;

    @Inject
    public FontController(FontService fontService) {
        this.fontService = fontService;
    }

    @Operation(
            summary = "Add a new font",
            operationId = "addFont",
            description = "Adds a new font to the database. The font is created with the given properties.",
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
            responseCode = "500",
            description = "The font could not be added to the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = FontModelErrorDTO.class)
            )
    )
    @Post
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<FontModelResponseDTO> add(
            @Valid @Body FontModelDTO item
    ) {
        FontModelResponseDTO.FontModelDTO result = fontService.createFont(item);
        return HttpResponse.ok(result);
    }

    @Operation(
            summary = "Get a font by ID",
            operationId = "getFontById",
            description = "Gets a font by ID from the database.",
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
            description = "The font was not found in the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = FontModelErrorDTO.class)
            )
    )
    @Get("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<FontModelResponseDTO> getById(@PathVariable UUID id) {
        Optional<FontEntity> model = fontService.findFontById(id);
        if (model.isPresent()) {
            FontEntity fontModel = model.get();
            FontModelResponseDTO.FontModelDTO dto = FontModelResponseDTO.FontModelDTO.createDTO(fontModel);
            return HttpResponse.ok(dto);
        }
        return HttpResponse.notFound(new FontModelErrorDTO("Font not found"));
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
                    schema = @Schema(implementation = FontModelCharsResponseDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The font was not found in the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = FontModelErrorDTO.class)
            )
    )
    @Get("/chars/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<FontModelResponseDTO> getCharsById(@PathVariable UUID id) {
        List<String> model = fontService.findCharsByFontId(id);
        if (!model.isEmpty()) {
            FontModelCharsResponseDTO dto = FontModelCharsResponseDTO.createDTO(id, model);
            return HttpResponse.ok(dto);
        }
        return HttpResponse.notFound(new FontModelErrorDTO("Font not found"));
    }

    @Operation(
            summary = "Remove a font by ID",
            operationId = "deleteFont",
            description = "Removes a font by ID from the database.",
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
            description = "The font was not found in the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = FontModelErrorDTO.class)
            )
    )
    @Delete("/delete/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<FontModelResponseDTO> remove(@PathVariable UUID id) {
        FontModelResponseDTO result = fontService.deleteFont(id);
        if (result instanceof FontModelResponseDTO.FontModelErrorDTO) {
            return HttpResponse.notFound(result);
        }
        return HttpResponse.ok(result);
    }

    @Operation(
            summary = "Get all fonts",
            operationId = "getAllFonts",
            description = "Gets all fonts from the database.",
            tags = {"Font"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The fonts were successfully retrieved from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = FontModelResponseDTO.FontModelDTO.class)
            )
    )
    @Get(uris = {"/all"})
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Page<FontModelResponseDTO.FontModelDTO>> getAll(Pageable pageable) {
        Page<FontModelResponseDTO.FontModelDTO> models = fontService.getAllFonts(pageable);
        return HttpResponse.ok(models);
    }

    @Operation(
            summary = "Delete all fonts",
            operationId = "deleteAllFonts",
            description = "Deletes all fonts from the database.",
            tags = {"Font"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "All fonts were successfully deleted from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = FontModelResponseDTO.FontModelDTO.class)
            )
    )
    @Delete("delete/all")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<List<FontModelResponseDTO>> deleteAll() {
        List<FontModelResponseDTO> result = fontService.deleteAllFonts();
        return HttpResponse.ok(result);
    }

    @Operation(
            summary = "Update a font",
            operationId = "updateFont",
            description = "Updates a font in the database.",
            tags = {"Font"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The font was successfully updated in the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = FontModelResponseDTO.FontModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The font was not found in the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = FontModelErrorDTO.class)
            )
    )
    @Post("/update")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<FontModelResponseDTO> update(
            @Valid @Body FontModelDTO model
    ) {
        FontModelResponseDTO result = fontService.updateFont(model);
        if (result instanceof FontModelResponseDTO.FontModelErrorDTO) {
            return HttpResponse.notFound(result);
        }
        return HttpResponse.ok(result);
    }
}
