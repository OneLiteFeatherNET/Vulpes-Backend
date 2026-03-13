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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import net.onelitefeather.vulpes.backend.domain.sound.SoundFileSourceDTO;
import net.onelitefeather.vulpes.backend.domain.sound.SoundResponseDTO;
import net.onelitefeather.vulpes.backend.service.SoundService;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup;

import java.util.UUID;

@Controller("/sound")
public class SoundSourceController {

    private final SoundService soundService;

    @Inject
    public SoundSourceController(SoundService soundService) {
        this.soundService = soundService;
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
    @Get("/{id}/sources")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Page<SoundResponseDTO>> get(@PathVariable UUID id, Pageable pageable) {
        Page<SoundResponseDTO> result = soundService.getSoundSourcesById(id, pageable);
        return HttpResponse.ok(result);
    }

    /**
     * Creates a new SoundFileSource and links it to a SoundEventEntity by ID.
     *
     * @param soundEventId the ID of the SoundEventEntity
     * @param sourceDTO    the DTO containing source data
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
    @Post("/{id}/sources")
    @Produces(MediaType.APPLICATION_JSON)
    @Validated(groups = ValidationGroup.Create.class)
    public HttpResponse<SoundResponseDTO> createSource(
            @PathVariable("id") UUID soundEventId,
            @Body SoundFileSourceDTO sourceDTO
    ) {
        SoundResponseDTO result = soundService.createAndLinkSource(soundEventId, sourceDTO);
        return HttpResponse.ok(result);
    }

    /**
     * Updates an existing SoundFileSource linked to a SoundEventEntity by ID.
     *
     * @param soundEventId the ID of the SoundEventEntity
     * @param sourceDTO    the DTO containing updated source data
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
    @Post("/{id}/sources/update")
    @Produces(MediaType.APPLICATION_JSON)
    @Validated(groups = ValidationGroup.Update.class)
    public HttpResponse<SoundResponseDTO> updateSource(
            @PathVariable("id") UUID soundEventId,
            @Body SoundFileSourceDTO sourceDTO
    ) {
        SoundResponseDTO result = soundService.updateLinkedSource(soundEventId, sourceDTO);
        return HttpResponse.ok(result);
    }


    /**
     * Deletes an existing SoundFileSource linked to a SoundEventEntity by ID.
     *
     * @param soundEventId the ID of the SoundEventEntity
     * @param soundId      the DTO containing source data to delete
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
    @Delete("/{id}/sources/delete/{soundId}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<SoundResponseDTO> deleteSource(
            @Parameter(name = "id", description = "The id of the sound event", in = ParameterIn.PATH, required = true)
            @PathVariable("id") UUID soundEventId,
            @Parameter(name = "soundID", description = "The id of the sound file", in = ParameterIn.PATH, required = true)
            @PathVariable("soundId") UUID soundId
    ) {
        SoundResponseDTO result = soundService.deleteLinkedSource(soundEventId, soundId);
        return HttpResponse.ok(result);
    }
}
