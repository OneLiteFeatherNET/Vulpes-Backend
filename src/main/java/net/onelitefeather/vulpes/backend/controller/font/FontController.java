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
import net.onelitefeather.vulpes.api.model.FontEntity;
import net.onelitefeather.vulpes.backend.domain.font.FontModelDTO;
import net.onelitefeather.vulpes.backend.domain.font.FontModelResponseDTO;
import net.onelitefeather.vulpes.backend.service.FontService;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    @Validated(groups = ValidationGroup.Create.class)
    public HttpResponse<FontModelResponseDTO> add(
            @Body FontModelDTO item
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
                    array = @ArraySchema(
                            schema = @Schema(implementation = FontModelResponseDTO.FontModelDTO.class),
                            arraySchema = @Schema(implementation = Page.class)
                    )
            )
    )
    @Get(uris = {"/"})
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
    @Delete("delete")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<List<FontModelResponseDTO>> deleteAll() {
        List<FontModelResponseDTO> result = fontService.deleteAllFonts();
        return HttpResponse.ok(result);
    }
}
