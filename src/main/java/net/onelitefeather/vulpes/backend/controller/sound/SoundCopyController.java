package net.onelitefeather.vulpes.backend.controller.sound;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import net.onelitefeather.vulpes.api.model.sound.SoundEventEntity;
import net.onelitefeather.vulpes.backend.copier.EntityCopier;
import net.onelitefeather.vulpes.backend.domain.copy.CopyRequest;
import net.onelitefeather.vulpes.backend.domain.copy.RelationalCopyDTO;
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail;
import net.onelitefeather.vulpes.backend.domain.sound.SoundRelation;
import net.onelitefeather.vulpes.backend.domain.sound.SoundResponseDTO;

import java.util.Set;
import java.util.UUID;

/**
 * REST controller for copying sound event resources, within the same project or into another one.
 */
@Controller("/project/{projectId}/sound")
public class SoundCopyController {

    private final EntityCopier<SoundEventEntity, SoundRelation> soundCopier;

    @Inject
    public SoundCopyController(@Named("sound") EntityCopier<SoundEventEntity, SoundRelation> soundCopier) {
        this.soundCopier = soundCopier;
    }

    @Operation(
            summary = "Copy a sound event",
            operationId = "copySoundEvent",
            description = "Copies a sound event owned by the given project into the same project or another one, under a new or the same key, optionally including its sound file sources.",
            tags = {"Sound"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The sound event was successfully copied.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = SoundResponseDTO.SoundModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The sound event or the target project was not found.",
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
    @ApiResponse(
            responseCode = "409",
            description = "A sound event with the resolved key already exists in the target project.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Post("/{id}/copy")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<SoundResponseDTO.SoundModelDTO> copy(
            @PathVariable UUID projectId,
            @PathVariable UUID id,
            @Nullable @Body RelationalCopyDTO<SoundRelation> copyDTO
    ) {
        Set<SoundRelation> relations = copyDTO != null ? copyDTO.relationsOrEmpty() : Set.of();
        var copied = soundCopier.copy(
                projectId,
                id,
                CopyRequest.targetProjectId(copyDTO),
                CopyRequest.targetKey(copyDTO),
                CopyRequest.targetName(copyDTO),
                relations
        );
        return HttpResponse.ok(SoundResponseDTO.SoundModelDTO.createDTO(copied));
    }
}
