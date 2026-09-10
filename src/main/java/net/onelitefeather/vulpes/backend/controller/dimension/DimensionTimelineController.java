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
import net.onelitefeather.vulpes.backend.domain.dimension.DimensionTimelineDTO;
import net.onelitefeather.vulpes.backend.domain.dimension.DimensionTimelineResponseDTO;
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail;
import net.onelitefeather.vulpes.backend.service.DimensionService;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup;

import java.util.List;
import java.util.UUID;

@Controller("/dimension")
public class DimensionTimelineController {

    private final DimensionService dimensionService;

    @Inject
    public DimensionTimelineController(DimensionService dimensionService) {
        this.dimensionService = dimensionService;
    }

    @Operation(
            summary = "Get all timelines of a dimension type",
            operationId = "getTimelines",
            description = "Retrieves a pageable list of timeline references for the dimension type identified by dimensionId.",
            tags = {"Dimension"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Timelines successfully retrieved.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(
                            schema = @Schema(implementation = DimensionTimelineResponseDTO.DimensionTimelineDTO.class),
                            arraySchema = @Schema(implementation = Page.class)
                    )
            )
    )
    @Get(uris = {
            "/timeline/{dimensionId}",
            "/{dimensionId}/timeline"
    })
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Page<DimensionTimelineResponseDTO.DimensionTimelineDTO>> getTimelinesById(
            @PathVariable("dimensionId") UUID dimensionId, Pageable pageable
    ) {
        Page<DimensionTimelineResponseDTO.DimensionTimelineDTO> timelinePage = dimensionService.findTimelinesById(dimensionId, pageable);
        return HttpResponse.ok(timelinePage);
    }

    @Operation(
            summary = "Update a timeline of a dimension type",
            operationId = "updateTimeline",
            description = "Updates a specific timeline reference of the dimension type identified by dimensionId.",
            tags = {"Dimension"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Timeline successfully updated.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = DimensionTimelineResponseDTO.DimensionTimelineDTO.class)
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
            "/timeline/{dimensionId}",
            "/{dimensionId}/timeline"
    })
    @Validated(groups = ValidationGroup.Update.class)
    public HttpResponse<DimensionTimelineResponseDTO.DimensionTimelineDTO> updateTimeline(
            @PathVariable("dimensionId") UUID dimensionId, @Body DimensionTimelineDTO timeline
    ) {
        return HttpResponse.ok(dimensionService.updateTimelineById(dimensionId, timeline));
    }

    @Operation(
            summary = "Create a timeline of a dimension type",
            operationId = "createTimeline",
            description = "Creates a new timeline reference for the dimension type identified by dimensionId.",
            tags = {"Dimension"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Timeline successfully created.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = DimensionTimelineResponseDTO.DimensionTimelineDTO.class)
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
            "/timeline/{dimensionId}",
            "/{dimensionId}/timeline"
    })
    @Validated(groups = ValidationGroup.Create.class)
    public HttpResponse<DimensionTimelineResponseDTO.DimensionTimelineDTO> createTimeline(
            @PathVariable("dimensionId") UUID dimensionId, @Body DimensionTimelineDTO timeline
    ) {
        return HttpResponse.ok(dimensionService.createTimelineById(dimensionId, timeline));
    }

    @Operation(
            summary = "Delete a timeline of a dimension type",
            operationId = "deleteTimeline",
            description = "Deletes a specific timeline reference (timelineId) of the dimension type identified by dimensionId.",
            tags = {"Dimension"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Timeline successfully deleted.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = DimensionTimelineResponseDTO.DimensionTimelineDTO.class)
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
            "/timeline/{dimensionId}/{timelineId}",
            "/{dimensionId}/timeline/{timelineId}"
    })
    public HttpResponse<DimensionTimelineResponseDTO.DimensionTimelineDTO> deleteTimeline(
            @PathVariable("dimensionId") UUID dimensionId, @PathVariable("timelineId") UUID timelineId
    ) {
        return HttpResponse.ok(dimensionService.deleteTimelineById(dimensionId, timelineId));
    }

    @Operation(
            summary = "Delete all timelines of a dimension type",
            operationId = "deleteTimelines",
            description = "Deletes all timeline references of the dimension type identified by dimensionId.",
            tags = {"Dimension"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "All timelines were successfully deleted.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(
                            schema = @Schema(implementation = DimensionTimelineResponseDTO.DimensionTimelineDTO.class)
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
            "/timeline/{dimensionId}",
            "/{dimensionId}/timeline"
    })
    public HttpResponse<List<DimensionTimelineResponseDTO.DimensionTimelineDTO>> deleteTimelines(
            @PathVariable("dimensionId") UUID dimensionId
    ) {
        var deletedTimelines = dimensionService.deleteAllTimelinesById(dimensionId);
        return HttpResponse.ok(deletedTimelines);
    }
}
