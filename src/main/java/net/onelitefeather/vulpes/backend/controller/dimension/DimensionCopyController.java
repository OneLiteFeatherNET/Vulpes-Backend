package net.onelitefeather.vulpes.backend.controller.dimension;

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
import net.onelitefeather.vulpes.api.model.dimension.DimensionTypeEntity;
import net.onelitefeather.vulpes.backend.copier.EntityCopier;
import net.onelitefeather.vulpes.backend.domain.copy.CopyRequest;
import net.onelitefeather.vulpes.backend.domain.copy.RelationalCopyDTO;
import net.onelitefeather.vulpes.backend.domain.dimension.DimensionModelResponseDTO;
import net.onelitefeather.vulpes.backend.domain.dimension.DimensionRelation;
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail;

import java.util.Set;
import java.util.UUID;

/**
 * REST controller for copying dimension type resources, within the same project or into another one.
 */
@Controller("/project/{projectId}/dimension")
public class DimensionCopyController {

    private final EntityCopier<DimensionTypeEntity, DimensionRelation> dimensionCopier;

    @Inject
    public DimensionCopyController(@Named("dimension") EntityCopier<DimensionTypeEntity, DimensionRelation> dimensionCopier) {
        this.dimensionCopier = dimensionCopier;
    }

    @Operation(
            summary = "Copy a dimension type",
            operationId = "copyDimension",
            description = "Copies a dimension type owned by the given project into the same project or another one, under a new or the same key, optionally including its environment attributes and timeline references.",
            tags = {"Dimension"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The dimension type was successfully copied.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = DimensionModelResponseDTO.DimensionModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The dimension type or the target project was not found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "A dimension type with the resolved key already exists in the target project.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Post("/{dimensionId}/copy")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<DimensionModelResponseDTO.DimensionModelDTO> copy(
            @PathVariable UUID projectId,
            @PathVariable("dimensionId") UUID dimensionId,
            @Nullable @Body RelationalCopyDTO<DimensionRelation> copyDTO
    ) {
        Set<DimensionRelation> relations = copyDTO != null ? copyDTO.relationsOrEmpty() : Set.of();
        var copied = dimensionCopier.copy(
                projectId,
                dimensionId,
                CopyRequest.targetProjectId(copyDTO),
                CopyRequest.targetKey(copyDTO),
                CopyRequest.targetName(copyDTO),
                relations
        );
        return HttpResponse.ok(DimensionModelResponseDTO.DimensionModelDTO.createDTO(copied));
    }
}
