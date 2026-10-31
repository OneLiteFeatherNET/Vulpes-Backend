package net.onelitefeather.vulpes.backend.controller.session;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Delete;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.sse.Event;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import net.onelitefeather.vulpes.backend.domain.error.ProblemDetail;
import net.onelitefeather.vulpes.backend.domain.session.LiveSessionRequestDTO;
import net.onelitefeather.vulpes.backend.domain.session.LiveSessionResponseDTO;
import net.onelitefeather.vulpes.backend.service.LiveSessionService;
import net.onelitefeather.vulpes.backend.service.session.AssetEvent;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.UUID;

/**
 * Live edit sessions of project assets, and the event stream that announces saves and session changes.
 *
 * <p>A session is held in memory of this backend instance and expires 60 seconds after its last {@code PUT}.
 * The event stream carries {@code saved} events after committed saves, {@code session} events when a session
 * starts, ends or expires, and a comment every 15 seconds. Events are not replayed to a client that reconnects.
 * Live sessions require a single backend replica until a shared store exists.
 */
@Controller("/project/{projectId}/asset/{id}")
public class LiveSessionController {

    private static final Duration KEEP_ALIVE = Duration.ofSeconds(15);

    private final LiveSessionService liveSessionService;

    /**
     * Constructs the controller.
     *
     * @param liveSessionService the service for live sessions and asset events
     */
    @Inject
    public LiveSessionController(LiveSessionService liveSessionService) {
        this.liveSessionService = liveSessionService;
    }

    @Operation(
            summary = "Get the live session of an asset",
            operationId = "getAssetLiveSession",
            description = "Returns the active live session of an asset, if a host started one and has refreshed it "
                    + "within the last 60 seconds.",
            tags = {"Asset"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The live session is active.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = LiveSessionResponseDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The asset was not found, or it has no active live session.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Get("/live-session")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<LiveSessionResponseDTO> getLiveSession(@PathVariable UUID projectId, @PathVariable UUID id) {
        return HttpResponse.ok(liveSessionService.getLiveSession(projectId, id));
    }

    @Operation(
            summary = "Start or refresh the live session of an asset",
            operationId = "putAssetLiveSession",
            description = "Creates the live session of an asset, or refreshes its expiry when the token is the one of "
                    + "the active session. The session expires 60 seconds after the last request; clients refresh "
                    + "it about every 20 seconds.",
            tags = {"Asset"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The session was created or refreshed.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = LiveSessionResponseDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "The token or the host is invalid. 'errors' names the rejected fields.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The asset was not found, or does not belong to the given project.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "Another token holds an active live session of the asset.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Put("/live-session")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<LiveSessionResponseDTO> putLiveSession(
            @PathVariable UUID projectId,
            @PathVariable UUID id,
            @Valid @Body LiveSessionRequestDTO request
    ) {
        return HttpResponse.ok(liveSessionService.startLiveSession(projectId, id, request));
    }

    @Operation(
            summary = "End the live session of an asset",
            operationId = "deleteAssetLiveSession",
            description = "Ends the live session when the token is the one of the active session. Answers 204 as "
                    + "well when no session is active.",
            tags = {"Asset"}
    )
    @ApiResponse(
            responseCode = "204",
            description = "The session was ended, or no session was active."
    )
    @ApiResponse(
            responseCode = "404",
            description = "The asset was not found, or does not belong to the given project.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "Another token holds the active live session of the asset.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Delete("/live-session")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Void> deleteLiveSession(
            @PathVariable UUID projectId,
            @PathVariable UUID id,
            @QueryValue String token
    ) {
        liveSessionService.endLiveSession(projectId, id, token);
        return HttpResponse.noContent();
    }

    @Operation(
            summary = "Stream the events of an asset",
            operationId = "streamAssetEvents",
            description = "Server-sent event stream of one asset. 'saved' carries the new ETag, SHA-1 and revision "
                    + "after every committed create, replacement or restore. 'session' carries state started or "
                    + "ended with the token and host of a live session. The stream begins with a comment and "
                    + "carries a comment every 15 seconds.",
            tags = {"Asset"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "Stream of asset events.",
            content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM)
    )
    @ApiResponse(
            responseCode = "404",
            description = "The asset was not found, or does not belong to the given project. No stream is started.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_PROBLEM,
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @Get("/events")
    @Produces(MediaType.TEXT_EVENT_STREAM)
    @ExecuteOn(TaskExecutors.IO)
    public Publisher<Event<Object>> events(@PathVariable UUID projectId, @PathVariable UUID id) {
        Flux<Event<Object>> frames = liveSessionService.events(projectId, id).map(LiveSessionController::toFrame);
        Flux<Event<Object>> keepAlive = Flux.interval(KEEP_ALIVE).map(tick -> comment("keep-alive"));
        // The connected comment is written after the subscription is registered, so every later event reaches the stream.
        return Flux.concat(Flux.just(comment("connected")), Flux.merge(frames, keepAlive));
    }

    /**
     * Maps an asset event to its SSE frame. The payload is the DTO of the event.
     */
    private static Event<Object> toFrame(AssetEvent event) {
        return switch (event) {
            case AssetEvent.Saved saved -> Event.of((Object) saved.data()).name("saved");
            case AssetEvent.Session session -> Event.of((Object) session.data()).name("session");
        };
    }

    /**
     * A frame that carries no data, only a comment line.
     */
    private static Event<Object> comment(String text) {
        return Event.of((Object) "").comment(text);
    }
}
