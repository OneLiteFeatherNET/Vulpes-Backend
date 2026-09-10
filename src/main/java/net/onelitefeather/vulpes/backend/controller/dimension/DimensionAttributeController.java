package net.onelitefeather.vulpes.backend.controller.dimension;

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
import io.micronaut.http.annotation.Put;
import io.micronaut.validation.Validated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import net.onelitefeather.vulpes.backend.domain.dimension.DimensionAttributeDTO;
import net.onelitefeather.vulpes.backend.domain.dimension.DimensionAttributeResponseDTO;
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail;
import net.onelitefeather.vulpes.backend.service.DimensionService;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup;

import java.util.List;
import java.util.UUID;

@Controller("/dimension")
public class DimensionAttributeController {

    private final DimensionService dimensionService;

    @Inject
    public DimensionAttributeController(DimensionService dimensionService) {
        this.dimensionService = dimensionService;
    }

    @Operation(
            summary = "Get all attributes of a dimension type",
            operationId = "getAttributes",
            description = "Retrieves a pageable list of environment attributes for the dimension type identified by dimensionId.",
            tags = {"Dimension"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Attributes successfully retrieved.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(
                            schema = @Schema(implementation = DimensionAttributeResponseDTO.DimensionAttributeDTO.class),
                            arraySchema = @Schema(implementation = Page.class)
                    )
            )
    )
    @Get(uris = {
            "/attribute/{dimensionId}",
            "/{dimensionId}/attribute"
    })
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Page<DimensionAttributeResponseDTO.DimensionAttributeDTO>> getAttributesById(
            @PathVariable("dimensionId") UUID dimensionId, Pageable pageable
    ) {
        Page<DimensionAttributeResponseDTO.DimensionAttributeDTO> attributePage = dimensionService.findAttributesById(dimensionId, pageable);
        return HttpResponse.ok(attributePage);
    }

    @Operation(
            summary = "Update an attribute of a dimension type",
            operationId = "updateAttribute",
            description = "Updates a specific environment attribute of the dimension type identified by dimensionId.",
            tags = {"Dimension"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Attribute successfully updated.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = DimensionAttributeResponseDTO.DimensionAttributeDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Dimension type for the given ID was not found.",
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
    @Post(uris = {
            "/attribute/{dimensionId}",
            "/{dimensionId}/attribute"
    })
    @Validated(groups = ValidationGroup.Update.class)
    public HttpResponse<DimensionAttributeResponseDTO.DimensionAttributeDTO> updateAttribute(
            @PathVariable("dimensionId") UUID dimensionId,
            @Body DimensionAttributeDTO attribute
    ) {
        return HttpResponse.ok(dimensionService.updateAttributeById(dimensionId, attribute));
    }

    @Operation(
            summary = "Create an attribute of a dimension type",
            operationId = "createAttribute",
            description = "Creates a new environment attribute for the dimension type identified by dimensionId.",
            tags = {"Dimension"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Attribute successfully created.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = DimensionAttributeResponseDTO.DimensionAttributeDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Dimension type for the given ID was not found.",
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
    @Put(uris = {
            "/attribute/{dimensionId}",
            "/{dimensionId}/attribute"
    })
    @Validated(groups = ValidationGroup.Create.class)
    public HttpResponse<DimensionAttributeResponseDTO.DimensionAttributeDTO> createAttribute(
            @PathVariable("dimensionId") UUID dimensionId,
            @Body DimensionAttributeDTO attribute
    ) {
        return HttpResponse.ok(dimensionService.createAttributeById(dimensionId, attribute));
    }

    @Operation(
            summary = "Delete an attribute of a dimension type",
            operationId = "deleteAttribute",
            description = "Deletes a specific attribute (attributeId) of the dimension type identified by dimensionId.",
            tags = {"Dimension"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Attribute successfully deleted.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = DimensionAttributeResponseDTO.DimensionAttributeDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Dimension type for the given ID was not found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Delete(uris = {
            "/attribute/{dimensionId}/{attributeId}",
            "/{dimensionId}/attribute/{attributeId}"
    })
    public HttpResponse<DimensionAttributeResponseDTO.DimensionAttributeDTO> deleteAttribute(
            @PathVariable("dimensionId") UUID dimensionId,
            @PathVariable("attributeId") UUID attributeId
    ) {
        return HttpResponse.ok(dimensionService.deleteAttributeById(dimensionId, attributeId));
    }

    @Operation(
            summary = "Delete all attributes of a dimension type",
            operationId = "deleteAttributes",
            description = "Deletes all environment attributes of the dimension type identified by dimensionId.",
            tags = {"Dimension"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "All attributes were successfully deleted.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(
                            schema = @Schema(implementation = DimensionAttributeResponseDTO.DimensionAttributeDTO.class)
                    )
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Dimension type for the given ID was not found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Delete(uris = {
            "/attribute/{dimensionId}",
            "/{dimensionId}/attribute"
    })
    public HttpResponse<List<DimensionAttributeResponseDTO.DimensionAttributeDTO>> deleteAttributes(
            @PathVariable("dimensionId") UUID dimensionId
    ) {
        var deletedAttributes = dimensionService.deleteAllAttributesById(dimensionId);
        return HttpResponse.ok(deletedAttributes);
    }
}
