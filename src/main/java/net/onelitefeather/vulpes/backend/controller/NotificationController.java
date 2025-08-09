package net.onelitefeather.vulpes.backend.controller;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.inject.Inject;
import net.onelitefeather.vulpes.api.model.NotificationEntity;
import net.onelitefeather.vulpes.backend.domain.notification.NotificationModelDTO;
import net.onelitefeather.vulpes.backend.domain.notification.NotificationModelResponseDTO;
import net.onelitefeather.vulpes.backend.service.NotificationService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller for managing notifications.
 * Provides endpoints to add, retrieve, update, and delete notifications.
 *
 * @author theEvilReaper
 * @version 1.0.0
 * @since 1.0.0
 */
@Controller("/notification")
public class NotificationController {

    private final NotificationService notificationService;

    @Inject
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Adds a new notification.
     *
     * @param model the notification model to be added
     * @return HttpResponse containing the added notification
     */
    @Operation(
            summary = "Add a new notification",
            operationId = "addNotification",
            description = "Adds a new notification to the database.",
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
            responseCode = "500",
            description = "The notification could not be added to the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = NotificationModelResponseDTO.NotificationModelErrorDTO.class)
            )
    )
    @Post
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<NotificationModelResponseDTO> add(
            @Body @Valid NotificationModelDTO model
    ) {
        NotificationModelResponseDTO.NotificationModelDTO result = notificationService.createNotification(model);
        return HttpResponse.ok(result);
    }

    /**
     * Retrieves a notification by its ID.
     *
     * @param id the ID of the notification to retrieve
     * @return HttpResponse containing the notification if found, or not found response
     */
    @Operation(
            summary = "Get a notification by ID",
            operationId = "getNotificationById",
            description = "Retrieves a notification from the database by its ID.",
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
            description = "The notification was not found in the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = NotificationModelResponseDTO.NotificationModelErrorDTO.class)
            )
    )
    @Get("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<NotificationModelResponseDTO> getById(@PathVariable UUID id) {
        Optional<NotificationEntity> model = notificationService.findNotificationById(id);
        if (model.isPresent()) {
            return HttpResponse.ok(NotificationModelResponseDTO.NotificationModelDTO.createDTO(model.get()));
        }
        return HttpResponse.notFound(new NotificationModelResponseDTO.NotificationModelErrorDTO("Notification not found"));
    }

    /**
     * Removes a notification by its ID.
     *
     * @param id the ID of the notification to remove
     * @return HttpResponse containing the removed notification if found, or not found response
     */
    @Operation(
            summary = "Remove a notification by ID",
            operationId = "removeNotificationById",
            description = "Removes a notification from the database by its ID.",
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
            description = "The notification was not found in the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = NotificationModelResponseDTO.NotificationModelErrorDTO.class)
            )
    )
    @Delete("/delete/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<NotificationModelResponseDTO> remove(@PathVariable UUID id) {
        NotificationModelResponseDTO result = notificationService.deleteNotification(id);
        if (result instanceof NotificationModelResponseDTO.NotificationModelErrorDTO) {
            return HttpResponse.notFound(result);
        }
        return HttpResponse.ok(result);
    }

    /**
     * Retrieves all notifications.
     *
     * @return HttpResponse containing a list of all notifications
     */
    @Operation(
            summary = "Get all notifications",
            operationId = "getAllNotifications",
            description = "Retrieves all notifications from the database.",
            tags = {"Notification"}
    )
    @ApiResponse(
            responseCode = "200",
            description = "The notifications were successfully retrieved from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = NotificationModelResponseDTO.NotificationModelDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "No notifications were found in the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = NotificationModelResponseDTO.NotificationModelErrorDTO.class)
            )
    )
    @Get(uris = {"/all"})
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Page<NotificationModelResponseDTO.NotificationModelDTO>> getAll(Pageable pageable) {
        Page<NotificationModelResponseDTO.NotificationModelDTO> list = notificationService.getAllNotifications(pageable);
        return HttpResponse.ok(list);
    }

    /**
     * Deletes all notifications.
     *
     * @return HttpResponse containing an empty list
     */
    @Operation(
            summary = "Delete all notifications",
            operationId = "deleteAllNotifications",
            description = "Deletes all notifications from the database.",
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
    @Delete("/delete/all")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<List<NotificationModelResponseDTO>> deleteAll() {
        List<NotificationModelResponseDTO> result = notificationService.deleteAllNotifications();
        return HttpResponse.ok(result);
    }

    /**
     * Updates an existing notification.
     *
     * @param model the notification model to update
     * @return HttpResponse containing the updated notification
     */
    @Operation(
            summary = "Update a notification",
            operationId = "updateNotification",
            description = "Updates an existing notification in the database.",
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
            description = "The notification was not found in the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = NotificationModelResponseDTO.NotificationModelErrorDTO.class)
            )
    )
    @Post("/update")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<NotificationModelResponseDTO> update(@Body @Valid NotificationModelDTO model) {
        NotificationModelResponseDTO result = notificationService.updateNotification(model);
        if (result instanceof NotificationModelResponseDTO.NotificationModelErrorDTO) {
            return HttpResponse.notFound(result);
        }
        return HttpResponse.ok(result);
    }
}
