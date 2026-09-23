package net.onelitefeather.vulpes.backend.controller.font;

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
import net.onelitefeather.vulpes.api.model.FontEntity;
import net.onelitefeather.vulpes.backend.copier.EntityCopier;
import net.onelitefeather.vulpes.backend.domain.copy.CopyRequest;
import net.onelitefeather.vulpes.backend.domain.copy.RelationalCopyDTO;
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail;
import net.onelitefeather.vulpes.backend.domain.font.FontModelResponseDTO;
import net.onelitefeather.vulpes.backend.domain.font.FontRelation;

import java.util.Set;
import java.util.UUID;

/**
 * REST controller for copying font resources, within the same project or into another one.
 */
@Controller("/project/{projectId}/font")
public class FontCopyController {

    private final EntityCopier<FontEntity, FontRelation> fontCopier;

    @Inject
    public FontCopyController(@Named("font") EntityCopier<FontEntity, FontRelation> fontCopier) {
        this.fontCopier = fontCopier;
    }

    @Operation(
            summary = "Copy a font",
            operationId = "copyFont",
            description = "Copies a font owned by the given project into the same project or another one, under a new or the same key, optionally including its characters.",
            tags = {"Font"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The font was successfully copied.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = FontModelResponseDTO.FontModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The font or the target project was not found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "A font with the resolved key already exists in the target project.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Post("/{id}/copy")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<FontModelResponseDTO.FontModelDTO> copy(
            @PathVariable UUID projectId,
            @PathVariable UUID id,
            @Nullable @Body RelationalCopyDTO<FontRelation> copyDTO
    ) {
        Set<FontRelation> relations = copyDTO != null ? copyDTO.relationsOrEmpty() : Set.of();
        var copied = fontCopier.copy(
                projectId,
                id,
                CopyRequest.targetProjectId(copyDTO),
                CopyRequest.targetKey(copyDTO),
                CopyRequest.targetName(copyDTO),
                relations
        );
        return HttpResponse.ok(FontModelResponseDTO.FontModelDTO.createDTO(copied));
    }
}
