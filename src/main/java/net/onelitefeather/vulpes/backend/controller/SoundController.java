package net.onelitefeather.vulpes.backend.controller;

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
import net.onelitefeather.vulpes.api.model.sound.SoundEventEntity;
import net.onelitefeather.vulpes.api.repository.SoundRepository;
import net.onelitefeather.vulpes.backend.domain.sound.SoundEventDTO;
import net.onelitefeather.vulpes.backend.domain.sound.SoundResponseDTO;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.onelitefeather.vulpes.backend.domain.sound.SoundResponseDTO.*;

/**
 * The {@link SoundController} contains endpoints for managing requests related to sound events.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 0.1.0
 */
@Controller("/sound")
public class SoundController {

    private static final String GENERIC_ERROR = "Sound event not found";
    private final SoundRepository soundRepository;

    /**
     * Constructs a new {@link SoundController} with the specified {@link SoundRepository}.
     *
     * @param soundRepository the repository to manage sound events
     */
    @Inject
    public SoundController(SoundRepository soundRepository) {
        this.soundRepository = soundRepository;
    }

    @Operation(
            summary = "Add a new sound event",
            description = "Adds a new sound event to the database. The sound event is created with the given properties.",
            tags = {"Sound"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The sound event was successfully added to the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SoundModelDTO.class)
            )
    )
    @Post
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<SoundResponseDTO> add(
            @Body @Valid SoundEventDTO dtoModel
    ) {
        SoundEventEntity event = dtoModel.toEntity();
        event = soundRepository.save(event);
        return HttpResponse.ok(SoundModelDTO.createDTO(event));
    }

    @Operation(
            summary = "Get a sound by its ID",
            description = "Retrieves a sound from the database by its ID.",
            tags = {"Sound"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The sound was successfully retrieved from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SoundModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The sound was not found in the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SoundErrorDTO.class)
            )
    )
    @Get("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<SoundResponseDTO> getById(@PathVariable UUID id) {
        Optional<SoundEventEntity> model = soundRepository.findById(id);
        if (model.isPresent()) {
            return HttpResponse.ok(SoundModelDTO.createDTO(model.get()));
        }
        return HttpResponse.notFound(new SoundErrorDTO(GENERIC_ERROR));
    }

    @Operation(
            summary = "Remove a sound event by ID",
            description = "Removes a sound event from the database by its ID.",
            tags = {"Sound"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The sound event was successfully removed from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SoundModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The sound event was not found in the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SoundErrorDTO.class)
            )
    )
    @Delete("/remove/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<SoundResponseDTO> remove(@PathVariable UUID id) {
        Optional<SoundEventEntity> model = soundRepository.findById(id);
        if (model.isPresent()) {
            soundRepository.deleteById(id);
            return HttpResponse.ok(SoundModelDTO.createDTO(model.get()));
        }
        return HttpResponse.notFound(new SoundErrorDTO(GENERIC_ERROR));
    }

    @Operation(
            summary = "Get all sound events",
            description = "Retrieves all sound events from the database.",
            tags = {"Sound"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The sound events were successfully retrieved from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SoundModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "No sound events were found in the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SoundErrorDTO.class)
            )
    )
    @Get("/getAll")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<List<SoundResponseDTO>> getAll() {
        List<SoundEventEntity> list = soundRepository.findAll();

        if (list.isEmpty()) {
            return HttpResponse.ok(Collections.emptyList());
        }

        List<SoundResponseDTO> mapped = list.stream()
                .map(SoundModelDTO::createDTO)
                .collect(Collectors.toList());
        return HttpResponse.ok(mapped);
    }

    @Operation(
            summary = "Delete all sound events",
            description = "Deletes all sound events from the database.",
            tags = {"Sound"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "All sound events were successfully deleted from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SoundModelDTO.class)
            )
    )
    @Delete("/deleteAll")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<List<SoundResponseDTO>> deleteAll() {
        soundRepository.deleteAll();
        return HttpResponse.ok(List.of());
    }


    @Operation(
            summary = "Update a sound event",
            description = "Updates an existing sound event in the database.",
            tags = {"Sound"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The sound event was successfully updated in the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SoundModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The sound event was not found in the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SoundErrorDTO.class)
            )
    )
    @Post("/update")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<SoundResponseDTO> update(@Body @Valid SoundEventDTO model) {
        Optional<SoundEventEntity> existingModel = soundRepository.findById(model.getId());
        if (existingModel.isEmpty()) {
            return HttpResponse.notFound(new SoundErrorDTO(GENERIC_ERROR));
        }
        SoundEventEntity soundModel = model.toEntity();
        soundModel = soundRepository.update(soundModel);
        return HttpResponse.ok(SoundModelDTO.createDTO(soundModel));
    }

    @Operation(
            summary = "Get all sound file sources by an id",
            description = "",
            tags = {"Sound"}
    )
    @Get("{id}/sources")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<SoundResponseDTO> get(@Valid @PathVariable UUID id) {
        return HttpResponse.ok();
    }
}
