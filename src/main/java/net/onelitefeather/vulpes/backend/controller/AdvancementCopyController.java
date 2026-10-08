package net.onelitefeather.vulpes.backend.controller;

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
import net.onelitefeather.vulpes.api.model.advancement.AdvancementEntity;
import net.onelitefeather.vulpes.backend.copier.EntityCopier;
import net.onelitefeather.vulpes.backend.domain.copy.CopyDTO;
import net.onelitefeather.vulpes.backend.domain.copy.CopyRequest;
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail;
import net.onelitefeather.vulpes.backend.domain.advancement.AdvancementModelResponseDTO;

import java.util.UUID;

/**
 * REST controller for copying advancement resources, within the same project or into another one.
 */
@Controller("/project/{projectId}/advancement")
public class AdvancementCopyController {

    private final EntityCopier<AdvancementEntity, Void> advancementCopier;

    @Inject
    public AdvancementCopyController(@Named("advancement") EntityCopier<AdvancementEntity, Void> advancementCopier) {
        this.advancementCopier = advancementCopier;
    }

    @Operation(
            summary = "Copy an advancement",
            operationId = "copyAdvancement",
            description = "Copies an advancement owned by the given project into the same project or another one, under a new or the same key.",
            tags = {"Advancement"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The advancement was successfully copied.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = AdvancementModelResponseDTO.AdvancementModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The advancement or the target project was not found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "An advancement with the resolved key already exists in the target project.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Post("/{id}/copy")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<AdvancementModelResponseDTO.AdvancementModelDTO> copy(
            @PathVariable UUID projectId,
            @PathVariable UUID id,
            @Nullable @Body CopyDTO copyDTO
    ) {
        var copied = advancementCopier.copy(
                projectId,
                id,
                CopyRequest.targetProjectId(copyDTO),
                CopyRequest.targetKey(copyDTO),
                CopyRequest.targetName(copyDTO)
        );
        return HttpResponse.ok(AdvancementModelResponseDTO.AdvancementModelDTO.createDTO(copied));
    }
}
