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
import net.onelitefeather.vulpes.api.model.AttributeEntity;
import net.onelitefeather.vulpes.backend.copier.EntityCopier;
import net.onelitefeather.vulpes.backend.domain.attribute.AttributeModelResponseDTO;
import net.onelitefeather.vulpes.backend.domain.copy.CopyDTO;
import net.onelitefeather.vulpes.backend.domain.copy.CopyRequest;
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail;

import java.util.UUID;

/**
 * REST controller for copying attribute resources, within the same project or into another one.
 */
@Controller("/project/{projectId}/attribute")
public class AttributeCopyController {

    private final EntityCopier<AttributeEntity, Void> attributeCopier;

    @Inject
    public AttributeCopyController(@Named("attribute") EntityCopier<AttributeEntity, Void> attributeCopier) {
        this.attributeCopier = attributeCopier;
    }

    @Operation(
            summary = "Copy an attribute",
            operationId = "copyAttribute",
            description = "Copies an attribute owned by the given project into the same project or another one, under a new or the same key.",
            tags = {"Attribute"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The attribute was successfully copied.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AttributeModelResponseDTO.AttributeModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The attribute or the target project was not found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "An attribute with the resolved key already exists in the target project.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Post("/{id}/copy")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<AttributeModelResponseDTO.AttributeModelDTO> copy(
            @PathVariable UUID projectId,
            @PathVariable UUID id,
            @Nullable @Body CopyDTO copyDTO
    ) {
        var copied = attributeCopier.copy(
                projectId,
                id,
                CopyRequest.targetProjectId(copyDTO),
                CopyRequest.targetKey(copyDTO),
                CopyRequest.targetName(copyDTO)
        );
        return HttpResponse.ok(AttributeModelResponseDTO.AttributeModelDTO.create(copied));
    }
}
