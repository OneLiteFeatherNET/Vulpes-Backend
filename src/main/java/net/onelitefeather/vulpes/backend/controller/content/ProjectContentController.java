package net.onelitefeather.vulpes.backend.controller.content;

import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Produces;
import io.micronaut.json.JsonMapper;
import io.micronaut.json.tree.JsonNode;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.UUID;
import net.onelitefeather.vulpes.backend.domain.content.ProjectContentDTO;
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail;
import net.onelitefeather.vulpes.backend.service.content.ContentRevision;
import net.onelitefeather.vulpes.backend.service.content.ProjectContentService;
import org.jspecify.annotations.Nullable;

/**
 * Serves the complete content of a project in one document, for the generator of the resource pack.
 *
 * <p>The body carries its {@code revision}, which is also sent as the strong {@code ETag}. A client that
 * sends the ETag of its last export as {@code If-None-Match} receives {@code 304 Not Modified} while the
 * content is unchanged.
 *
 * @author TheMeinerLP
 * @version 1.0.0
 * @since 3.3.0
 */
@Controller("/project/{projectId}")
public class ProjectContentController {

    private static final String ETAG_QUOTE = "\"";

    private final ProjectContentService contentService;
    private final JsonMapper jsonMapper;

    /**
     * Constructs the controller.
     *
     * @param contentService the service that exports the content of a project
     * @param jsonMapper     the mapper that turns the export into its canonical tree
     */
    @Inject
    public ProjectContentController(ProjectContentService contentService, JsonMapper jsonMapper) {
        this.contentService = contentService;
        this.jsonMapper = jsonMapper;
    }

    /**
     * Exports the content of a project. The revision is computed over the serialized tree of the export, so it
     * covers exactly the fields the body delivers.
     *
     * @param projectId   the id of the project
     * @param ifNoneMatch the {@code If-None-Match} header, or {@code null} when absent
     * @return the export with its revision and ETag, or {@code 304} without a body when the ETag matches
     */
    @Operation(
            summary = "Get the complete content of a project",
            operationId = "getProjectContent",
            description = "Gets every content type of the given project from one consistent read, with a revision "
                    + "that identifies the exported state. Answers 304 when If-None-Match carries the current ETag.",
            tags = {"ProjectContent"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The content of the project was exported.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ProjectContentDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "304",
            description = "The If-None-Match header matches the current revision. The body is empty."
    )
    @ApiResponse(
            responseCode = "404",
            description = "The project was not found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Get("/content")
    @Produces(MediaType.APPLICATION_JSON)
    @ExecuteOn(TaskExecutors.IO)
    public HttpResponse<ProjectContentDTO> getContent(
            @PathVariable UUID projectId,
            @Nullable @Header(HttpHeaders.IF_NONE_MATCH) String ifNoneMatch) {
        ProjectContentDTO content = contentService.export(projectId);
        String revision = ContentRevision.of(toTree(content));
        String etag = ETAG_QUOTE + revision + ETAG_QUOTE;
        if (EntityTagMatcher.matches(ifNoneMatch, revision)) {
            return HttpResponse.<ProjectContentDTO>notModified().header(HttpHeaders.ETAG, etag);
        }
        return HttpResponse.ok(content.withRevision(revision)).header(HttpHeaders.ETAG, etag);
    }

    private JsonNode toTree(ProjectContentDTO content) {
        try {
            return jsonMapper.writeValueToTree(content);
        } catch (IOException exception) {
            throw new UncheckedIOException("The project content could not be serialized", exception);
        }
    }
}
