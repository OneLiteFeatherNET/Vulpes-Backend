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
import net.onelitefeather.vulpes.backend.domain.sound.SoundEventDTO;
import net.onelitefeather.vulpes.backend.domain.sound.SoundFileSourceDTO;
import net.onelitefeather.vulpes.backend.domain.sound.SoundResponseDTO;
import net.onelitefeather.vulpes.backend.service.SoundService;

import java.util.List;
import java.util.UUID;

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
    private final SoundService soundService;

    /**
     * Constructs a new {@link SoundController} with the specified {@link SoundService}.
     *
     * @param soundService the service to manage sound events
     */
    @Inject
    public SoundController(SoundService soundService) {
        this.soundService = soundService;
    }

    @Operation(
            summary = "Add a new sound event",
            operationId = "addSoundEvent",
            description = "Adds a new sound event to the database. The sound event is created with the given properties.",
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
            responseCode = "500",
            description = "The sound event could not be added to the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SoundResponseDTO.SoundErrorDTO.class)
            )
    )
    @Post
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<SoundResponseDTO> add(
            @Body @Valid SoundEventDTO dtoModel
    ) {
        SoundResponseDTO result = soundService.createSoundEvent(dtoModel);
        if (result instanceof SoundResponseDTO.SoundErrorDTO) {
            return HttpResponse.badRequest(result);
        }
        return HttpResponse.ok(result);
    }

    @Operation(
            summary = "Get a sound by its ID",
            operationId = "getSoundById",
            description = "Retrieves a sound from the database by its ID.",
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
            description = "The sound was not found in the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SoundResponseDTO.SoundErrorDTO.class)
            )
    )
    @Get("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<SoundResponseDTO> getById(@PathVariable UUID id) {
        var soundEvent = soundService.findSoundEventById(id);
        if (soundEvent.isPresent()) {
            return HttpResponse.ok(SoundResponseDTO.SoundModelDTO.createDTO(soundEvent.get()));
        }
        return HttpResponse.notFound(new SoundResponseDTO.SoundErrorDTO(GENERIC_ERROR));
    }

    @Operation(
            summary = "Remove a sound event by ID",
            operationId = "removeSoundEventById",
            description = "Removes a sound event from the database by its ID.",
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
            description = "The sound event was not found in the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SoundResponseDTO.SoundErrorDTO.class)
            )
    )
    @Delete("/delete/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<SoundResponseDTO> remove(@PathVariable UUID id) {
        SoundResponseDTO result = soundService.deleteSoundEvent(id);
        if (result instanceof SoundResponseDTO.SoundErrorDTO) {
            return HttpResponse.notFound(result);
        }
        return HttpResponse.ok(result);
    }

    @Operation(
            summary = "Get all sound events",
            operationId = "getAllSoundEvents",
            description = "Retrieves all sound events from the database.",
            tags = {"Sound"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The sound events were successfully retrieved from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SoundResponseDTO.SoundModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "No sound events were found in the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SoundResponseDTO.SoundErrorDTO.class)
            )
    )
    @Get("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<List<SoundResponseDTO>> getAll() {
        List<SoundResponseDTO> results = soundService.getAllSoundEvents();
        return HttpResponse.ok(results);
    }

    @Operation(
            summary = "Delete all sound events",
            operationId = "deleteAllSoundEvents",
            description = "Deletes all sound events from the database.",
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
    @Delete("/delete/all")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<List<SoundResponseDTO>> deleteAll() {
        List<SoundResponseDTO> results = soundService.deleteAllSoundEvents();
        return HttpResponse.ok(results);
    }

    @Operation(
            summary = "Update a sound event",
            operationId = "updateSoundEvent",
            description = "Updates an existing sound event in the database.",
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
            description = "The sound event was not found in the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SoundResponseDTO.SoundErrorDTO.class)
            )
    )
    @Post("/update")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<SoundResponseDTO> update(@Body @Valid SoundEventDTO model) {
        SoundResponseDTO result = soundService.updateSoundEvent(model);
        if (result instanceof SoundResponseDTO.SoundErrorDTO) {
            return HttpResponse.notFound(result);
        }
        return HttpResponse.ok(result);
    }

    @Operation(
            summary = "Get all sound file sources by an id",
            operationId = "getSoundSourcesById",
            description = "Get all sound file sources by a given sound event ID.",
            tags = {"Sound"},
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "The sound file sources were successfully retrieved.",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = SoundResponseDTO.SoundFileSourceDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "No sound file sources were found for the given sound event ID.",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON,
                                    schema = @Schema(implementation = SoundResponseDTO.SoundErrorDTO.class)
                            )
                    )
            }
    )
    @Get("{id}/sources")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Page<SoundResponseDTO>> get(@Valid @PathVariable UUID id, Pageable pageable) {
        Page<SoundResponseDTO> result = soundService.getSoundSourcesById(id, pageable);
        return HttpResponse.ok(result);
    }

    /**
     * Creates a new SoundFileSource and links it to a SoundEventEntity by ID.
     *
     * @param soundEventId the ID of the SoundEventEntity
     * @param sourceDTO the DTO containing source data
     * @return the created SoundFileSourceDTO
     */
    @Operation(
        summary = "Create and link a SoundFileSource",
        operationId = "createAndLinkSoundFileSource",
        description = "Creates a new SoundFileSource and links it to a SoundEventEntity by its ID.",
        tags = {"Sound"}
    )
    @ApiResponse(
        responseCode = "200",
        description = "The SoundFileSource was successfully created and linked.",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = SoundResponseDTO.SoundFileSourceDTO.class)
        )
    )
    @ApiResponse(
        responseCode = "404",
        description = "The SoundEventEntity was not found.",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = SoundResponseDTO.SoundFileSourceDTO.class)
        )
    )
    @Post("{id}/sources")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<SoundResponseDTO> createSource(
            @Valid @PathVariable("id") UUID soundEventId,
            @Body SoundFileSourceDTO sourceDTO
    ) {
        SoundResponseDTO result = soundService.createAndLinkSource(soundEventId, sourceDTO);
        return HttpResponse.ok(result);
    }

    /**
     * Updates an existing SoundFileSource linked to a SoundEventEntity by ID.
     *
     * @param soundEventId the ID of the SoundEventEntity
     * @param sourceDTO the DTO containing updated source data
     * @return the updated SoundFileSourceDTO
     */
    @Operation(
            summary = "Update a linked SoundFileSource",
            operationId = "updateLinkedSoundFileSource",
            description = "Updates an existing SoundFileSource linked to a SoundEventEntity by its ID.",
            tags = {"Sound"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The SoundFileSource was successfully updated.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SoundResponseDTO.SoundFileSourceDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The SoundFileSource or SoundEventEntity was not found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SoundResponseDTO.SoundFileSourceDTO.class)
            )
    )
    @Post("{id}/sources/update")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<SoundResponseDTO> updateSource(
            @Valid @PathVariable("id") UUID soundEventId,
            @Body SoundFileSourceDTO sourceDTO
    ) {
        SoundResponseDTO result = soundService.updateLinkedSource(soundEventId, sourceDTO);
        return HttpResponse.ok(result);
    }


    /**
     * Deletes an existing SoundFileSource linked to a SoundEventEntity by ID.
     *
     * @param soundEventId the ID of the SoundEventEntity
     * @param sourceDTO the DTO containing source data to delete
     * @return the deleted SoundFileSourceDTO
     */
    @Operation(
            summary = "Delete a linked SoundFileSource",
            operationId = "deleteLinkedSoundFileSource",
            description = "Deletes an existing SoundFileSource linked to a SoundEventEntity by its ID.",
            tags = {"Sound"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The SoundFileSource was successfully deleted.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SoundResponseDTO.SoundFileSourceDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The SoundFileSource or SoundEventEntity was not found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SoundResponseDTO.SoundFileSourceDTO.class)
            )
    )
    @Delete("{id}/sources/delete")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<SoundResponseDTO> deleteSource(
            @Valid @PathVariable("id") UUID soundEventId,
            @Body SoundFileSourceDTO sourceDTO
    ) {
        SoundResponseDTO result = soundService.deleteLinkedSource(soundEventId, sourceDTO);
        return HttpResponse.ok(result);
    }






}
