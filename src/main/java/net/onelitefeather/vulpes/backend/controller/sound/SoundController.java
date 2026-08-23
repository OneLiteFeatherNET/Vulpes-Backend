package net.onelitefeather.vulpes.backend.controller.sound;

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
import net.onelitefeather.vulpes.backend.domain.sound.SoundEventDTO;
import net.onelitefeather.vulpes.backend.domain.sound.SoundResponseDTO;
import net.onelitefeather.vulpes.backend.service.SoundService;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup;

import java.util.List;
import java.util.UUID;

/**
 * The {@link SoundController} contains endpoints for managing requests related to sound events.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 0.1.0
 */
@Controller("/project/{projectId}/sound")
public class SoundController {

    private static final String GENERIC_ERROR = "Sound event not found";
    private final SoundService soundService;

    @Inject
    public SoundController(SoundService soundService) {
        this.soundService = soundService;
    }

    @Operation(
            summary = "Add a new sound event",
            operationId = "addSoundEvent",
            description = "Adds a new sound event to the given project.",
            tags = {"Sound"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The sound event was successfully added to the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SoundResponseDTO.SoundModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The project was not found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SoundResponseDTO.SoundErrorDTO.class)
            )
    )
    @Post
    @Produces(MediaType.APPLICATION_JSON)
    @Validated(groups = ValidationGroup.Create.class)
    public HttpResponse<SoundResponseDTO> add(@PathVariable UUID projectId, @Body SoundEventDTO dtoModel) {
        SoundResponseDTO result = soundService.create(projectId, dtoModel);
        if (result instanceof SoundResponseDTO.SoundErrorDTO) {
            return HttpResponse.badRequest(result);
        }
        return HttpResponse.ok(result);
    }

    @Operation(
            summary = "Get a sound by its ID",
            operationId = "getSoundById",
            description = "Retrieves a sound owned by the given project by its ID.",
            tags = {"Sound"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The sound was successfully retrieved from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SoundResponseDTO.SoundModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The sound was not found, or does not belong to the given project.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SoundResponseDTO.SoundErrorDTO.class)
            )
    )
    @Get("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<SoundResponseDTO> getById(@PathVariable UUID projectId, @PathVariable UUID id) {
        var soundEvent = soundService.findById(projectId, id);
        if (soundEvent.isPresent()) {
            return HttpResponse.ok(SoundResponseDTO.SoundModelDTO.createDTO(soundEvent.get()));
        }
        return HttpResponse.notFound(new SoundResponseDTO.SoundErrorDTO(GENERIC_ERROR));
    }

    @Operation(
            summary = "Remove a sound event by ID",
            operationId = "removeSoundEventById",
            description = "Removes a sound event owned by the given project by its ID.",
            tags = {"Sound"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The sound event was successfully removed from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SoundResponseDTO.SoundModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The sound event was not found, or does not belong to the given project.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SoundResponseDTO.SoundErrorDTO.class)
            )
    )
    @Delete("/delete/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<SoundResponseDTO> remove(@PathVariable UUID projectId, @PathVariable UUID id) {
        SoundResponseDTO result = soundService.delete(projectId, id);
        if (result instanceof SoundResponseDTO.SoundErrorDTO) {
            return HttpResponse.notFound(result);
        }
        return HttpResponse.ok(result);
    }

    @Operation(
            summary = "Get all sound events",
            operationId = "getAllSoundEvents",
            description = "Retrieves all sound events belonging to the given project.",
            tags = {"Sound"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The sound events were successfully retrieved from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(
                            schema = @Schema(implementation = SoundResponseDTO.class),
                            arraySchema = @Schema(implementation = Page.class)
                    )
            )
    )
    @Get("/")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Page<SoundResponseDTO.SoundModelDTO>> getAll(@PathVariable UUID projectId, Pageable pageable) {
        Page<SoundResponseDTO.SoundModelDTO> returnValues = soundService.getAll(projectId, pageable);
        return HttpResponse.ok(returnValues);
    }

    @Operation(
            summary = "Delete all sound events",
            operationId = "deleteAllSoundEvents",
            description = "Deletes all sound events belonging to the given project.",
            tags = {"Sound"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "All sound events were successfully deleted from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SoundResponseDTO.SoundModelDTO.class)
            )
    )
    @Delete("/delete/")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<List<SoundResponseDTO>> deleteAll(@PathVariable UUID projectId) {
        List<SoundResponseDTO> results = soundService.deleteAll(projectId);
        return HttpResponse.ok(results);
    }

    @Operation(
            summary = "Update a sound event",
            operationId = "updateSoundEvent",
            description = "Updates a sound event owned by the given project.",
            tags = {"Sound"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The sound event was successfully updated in the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SoundResponseDTO.SoundModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The sound event was not found, or does not belong to the given project.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SoundResponseDTO.SoundErrorDTO.class)
            )
    )
    @Post("/update")
    @Produces(MediaType.APPLICATION_JSON)
    @Validated(groups = ValidationGroup.Update.class)
    public HttpResponse<SoundResponseDTO> update(@PathVariable UUID projectId, @Body SoundEventDTO model) {
        SoundResponseDTO result = soundService.update(projectId, model);
        if (result instanceof SoundResponseDTO.SoundErrorDTO) {
            return HttpResponse.notFound(result);
        }
        return HttpResponse.ok(result);
    }
}
