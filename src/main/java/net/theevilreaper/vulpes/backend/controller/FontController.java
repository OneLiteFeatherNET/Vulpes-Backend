package net.theevilreaper.vulpes.backend.controller;

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
import net.theevilreaper.vulpes.api.model.FontModel;
import net.theevilreaper.vulpes.api.repository.FontRepository;
import net.theevilreaper.vulpes.backend.domain.font.FontModelDTO;
import net.theevilreaper.vulpes.backend.domain.font.FontModelResponseDTO;
import net.theevilreaper.vulpes.backend.domain.item.ItemModelDTO;
import net.theevilreaper.vulpes.backend.domain.item.ItemModelResponseDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller("/font")
public class FontController {

    private final FontRepository fontRepository;

    @Inject
    public FontController(FontRepository fontRepository) {
        this.fontRepository = fontRepository;
    }

    @Operation(
            summary = "Add a new font",
            description = "Adds a new font to the database. The font is created with the given properties."
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
                    schema = @Schema(implementation = FontModelResponseDTO.FontModelErrorDTO.class)
            )
    )
    @Post
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<FontModelResponseDTO> add(
            @Valid @Body FontModelDTO item
    ) {
        FontModel fontModel = item.toFontModel();
        fontModel = fontRepository.save(fontModel);
        return HttpResponse.ok(FontModelResponseDTO.FontModelDTO.createDTO(fontModel));
    }

    @Operation(
            summary = "Get a font by ID",
            description = "Gets a font by ID from the database."
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
                    schema = @Schema(implementation = FontModelResponseDTO.FontModelErrorDTO.class)
            )
    )
    @Get("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<FontModelResponseDTO> getById(@PathVariable UUID id) {
        Optional<FontModel> model = fontRepository.findById(id);
        if (model.isPresent()) {
            FontModel fontModel = model.get();
            FontModelResponseDTO.FontModelDTO dto = FontModelResponseDTO.FontModelDTO.createDTO(fontModel);
            return HttpResponse.ok(dto);
        }
        return HttpResponse.notFound(new FontModelResponseDTO.FontModelErrorDTO("Font not found"));
    }

    @Operation(
            summary = "Remove a font by ID",
            description = "Removes a font by ID from the database."
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
                    schema = @Schema(implementation = FontModelResponseDTO.FontModelErrorDTO.class)
            )
    )
    @Delete("/remove/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<FontModelResponseDTO> remove(@PathVariable UUID id) {
        Optional<FontModel> model = fontRepository.findById(id);
        if (model.isPresent()) {
            fontRepository.deleteById(id);
            FontModel fontModel = model.get();
            FontModelResponseDTO.FontModelDTO dto = FontModelResponseDTO.FontModelDTO.createDTO(fontModel);
            return HttpResponse.ok(dto);
        }
        return HttpResponse.notFound(new FontModelResponseDTO.FontModelErrorDTO("Font not found"));
    }

    @Operation(
            summary = "Get all fonts",
            description = "Gets all fonts from the database."
    )
    @ApiResponse(
            responseCode = "200",
            description = "The fonts were successfully retrieved from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = FontModelResponseDTO.FontModelDTO.class)
            )
    )
    @Get("/getAll")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<List<FontModelResponseDTO>> getAll() {
        List<FontModelResponseDTO> models = fontRepository.findAll().stream().map(FontModelResponseDTO.FontModelDTO::createDTO).collect(Collectors.toList());
        return HttpResponse.ok(models);
    }

    @Operation(
            summary = "Delete all fonts",
            description = "Deletes all fonts from the database."
    )
    @ApiResponse(
            responseCode = "200",
            description = "All fonts were successfully deleted from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = FontModelResponseDTO.FontModelDTO.class)
            )
    )
    @Delete("/deleteAll")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<List<FontModelResponseDTO>> deleteAll() {
        fontRepository.deleteAll();
        return HttpResponse.ok(List.of());
    }

    @Operation(
            summary = "Update a font",
            description = "Updates a font in the database."
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
                    schema = @Schema(implementation = FontModelResponseDTO.FontModelErrorDTO.class)
            )
    )
    @Post("/update")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<FontModelResponseDTO> update(
            @Valid @Body FontModelDTO model
    ) {
        Optional<FontModel> modelOptional = fontRepository.findById(model.getId());
        if (modelOptional.isEmpty()) {
            return HttpResponse.notFound(new FontModelResponseDTO.FontModelErrorDTO("Font not found"));
        }
        var updatedFontModel = fontRepository.update(model.toFontModel());
        return HttpResponse.ok(FontModelResponseDTO.FontModelDTO.createDTO(updatedFontModel));
    }
}
