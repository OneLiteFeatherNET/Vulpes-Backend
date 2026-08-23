package net.onelitefeather.vulpes.backend.controller;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.validation.Validated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.inject.Inject;
import net.onelitefeather.vulpes.api.model.NotificationEntity;
import net.onelitefeather.vulpes.backend.domain.notification.NotificationModelDTO;
import net.onelitefeather.vulpes.backend.domain.notification.NotificationModelResponseDTO;
import net.onelitefeather.vulpes.backend.service.NotificationService;
import net.onelitefeather.vulpes.backend.validation.ValidationGroup;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Controller for managing notifications.
 * Provides endpoints to add, retrieve, update, and delete notifications.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
@Controller("/project/{projectId}/notification")
public class NotificationController {

    private final NotificationService notificationService;

    @Inject
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(
            summary = "Add a new notification",
            operationId = "addNotification",
            description = "Adds a new notification to the given project.",
            tags = {"Notification"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The notification was successfully added to the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = NotificationModelResponseDTO.NotificationModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The project was not found.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = NotificationModelResponseDTO.NotificationModelErrorDTO.class)
            )
    )
    @Post
    @Produces(MediaType.APPLICATION_JSON)
    @Validated(groups = ValidationGroup.Create.class)
    public HttpResponse<NotificationModelResponseDTO> add(@PathVariable UUID projectId, @Body NotificationModelDTO model) {
        NotificationModelResponseDTO result = notificationService.create(projectId, model);
        if (result instanceof NotificationModelResponseDTO.NotificationModelErrorDTO) {
            return HttpResponse.notFound(result);
        }
        return HttpResponse.ok(result);
    }

    @Operation(
            summary = "Get a notification by ID",
            operationId = "getNotificationById",
            description = "Retrieves a notification owned by the given project by its ID.",
            tags = {"Notification"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The notification was successfully retrieved from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = NotificationModelResponseDTO.NotificationModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The notification was not found, or does not belong to the given project.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = NotificationModelResponseDTO.NotificationModelErrorDTO.class)
            )
    )
    @Get("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<NotificationModelResponseDTO> getById(@PathVariable UUID projectId, @PathVariable UUID id) {
        Optional<NotificationEntity> model = notificationService.findById(projectId, id);
        if (model.isPresent()) {
            return HttpResponse.ok(NotificationModelResponseDTO.NotificationModelDTO.createDTO(model.get()));
        }
        return HttpResponse.notFound(new NotificationModelResponseDTO.NotificationModelErrorDTO("Notification not found"));
    }

    @Operation(
            summary = "Remove a notification by ID",
            operationId = "removeNotificationById",
            description = "Removes a notification owned by the given project by its ID.",
            tags = {"Notification"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The notification was successfully removed from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = NotificationModelResponseDTO.NotificationModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The notification was not found, or does not belong to the given project.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = NotificationModelResponseDTO.NotificationModelErrorDTO.class)
            )
    )
    @Delete("/delete/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<NotificationModelResponseDTO> remove(@PathVariable UUID projectId, @PathVariable UUID id) {
        NotificationModelResponseDTO result = notificationService.delete(projectId, id);
        if (result instanceof NotificationModelResponseDTO.NotificationModelErrorDTO) {
            return HttpResponse.notFound(result);
        }
        return HttpResponse.ok(result);
    }

    @Operation(
            summary = "Get all notifications",
            operationId = "getAllNotifications",
            description = "Retrieves all notifications belonging to the given project.",
            tags = {"Notification"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The notifications were successfully retrieved from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    array = @ArraySchema(
                            schema = @Schema(implementation = NotificationModelResponseDTO.NotificationModelDTO.class),
                            arraySchema = @Schema(implementation = Page.class)
                    )
            )
    )
    @Get(uris = {"/"})
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Page<NotificationModelResponseDTO.NotificationModelDTO>> getAll(@PathVariable UUID projectId, Pageable pageable) {
        Page<NotificationModelResponseDTO.NotificationModelDTO> list = notificationService.getAll(projectId, pageable);
        return HttpResponse.ok(list);
    }

    @Operation(
            summary = "Delete all notifications",
            operationId = "deleteAllNotifications",
            description = "Deletes all notifications belonging to the given project.",
            tags = {"Notification"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "All notifications were successfully deleted from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = NotificationModelResponseDTO.NotificationModelDTO.class)
            )
    )
    @Delete("/delete/")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<List<NotificationModelResponseDTO>> deleteAll(@PathVariable UUID projectId) {
        List<NotificationModelResponseDTO> result = notificationService.deleteAll(projectId);
        return HttpResponse.ok(result);
    }

    @Operation(
            summary = "Update a notification",
            operationId = "updateNotification",
            description = "Updates a notification owned by the given project.",
            tags = {"Notification"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The notification was successfully updated in the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = NotificationModelResponseDTO.NotificationModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The notification was not found, or does not belong to the given project.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = NotificationModelResponseDTO.NotificationModelErrorDTO.class)
            )
    )
    @Post("/update")
    @Produces(MediaType.APPLICATION_JSON)
    @Validated(groups = ValidationGroup.Update.class)
    public HttpResponse<NotificationModelResponseDTO> update(@PathVariable UUID projectId, @Body NotificationModelDTO model) {
        NotificationModelResponseDTO result = notificationService.update(projectId, model);
        if (result instanceof NotificationModelResponseDTO.NotificationModelErrorDTO) {
            return HttpResponse.notFound(result);
        }
        return HttpResponse.ok(result);
    }
}
