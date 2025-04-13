package net.theevilreaper.vulpes.backend.controller;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import net.theevilreaper.vulpes.api.model.NotificationModel;
import net.theevilreaper.vulpes.api.repository.NotificationRepository;
import jakarta.inject.Inject;
import net.theevilreaper.vulpes.backend.domain.notification.NotificationModelDTO;
import net.theevilreaper.vulpes.backend.domain.notification.NotificationModelResponseDTO;

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

    private final NotificationRepository notificationRepository;

    @Inject
    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Adds a new notification.
     *
     * @param model the notification model to be added
     * @return HttpResponse containing the added notification
     */
    @Operation(
            summary = "Add a new notification",
            description = "Adds a new notification to the database."
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
        NotificationModel notificationModel = model.toNotificationModel();
        notificationModel = notificationRepository.save(notificationModel);
        return HttpResponse.ok(NotificationModelResponseDTO.NotificationModelDTO.createDTO(notificationModel));
    }

    /**
     * Retrieves a notification by its ID.
     *
     * @param id the ID of the notification to retrieve
     * @return HttpResponse containing the notification if found, or not found response
     */
    @Operation(
            summary = "Get a notification by ID",
            description = "Retrieves a notification from the database by its ID."
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
        Optional<NotificationModel> model = notificationRepository.findById(id);
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
            description = "Removes a notification from the database by its ID."
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
    @Delete("/remove/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<NotificationModelResponseDTO> remove(@PathVariable UUID id) {
        Optional<NotificationModel> model = notificationRepository.findById(id);
        if (model.isPresent()) {
            notificationRepository.deleteById(id);
            return HttpResponse.ok(NotificationModelResponseDTO.NotificationModelDTO.createDTO(model.get()));
        }
        return HttpResponse.notFound(new NotificationModelResponseDTO.NotificationModelErrorDTO("Notification not found"));
    }

    /**
     * Retrieves all notifications.
     *
     * @return HttpResponse containing a list of all notifications
     */
    @Operation(
            summary = "Get all notifications",
            description = "Retrieves all notifications from the database."
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
    @Get("/getAll")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<List<NotificationModelResponseDTO>> getAll() {
        List<NotificationModelResponseDTO> list = notificationRepository.findAll().stream().map(NotificationModelResponseDTO.NotificationModelDTO::createDTO).collect(Collectors.toList());
        return HttpResponse.ok(list);
    }

    /**
     * Deletes all notifications.
     *
     * @return HttpResponse containing an empty list
     */
    @Operation(
            summary = "Delete all notifications",
            description = "Deletes all notifications from the database."
    )
    @ApiResponse(
            responseCode = "200",
            description = "All notifications were successfully deleted from the database.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = NotificationModelResponseDTO.NotificationModelDTO.class)
            )
    )
    @Delete("/deleteAll")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<List<NotificationModelResponseDTO>> deleteAll() {
        notificationRepository.deleteAll();
        return HttpResponse.ok(List.of());
    }

    /**
     * Updates an existing notification.
     *
     * @param model the notification model to update
     * @return HttpResponse containing the updated notification
     */
    @Operation(
            summary = "Update a notification",
            description = "Updates an existing notification in the database."
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
        Optional<NotificationModel> existingModel = notificationRepository.findById(model.getId());
        if (existingModel.isEmpty()) {
            return HttpResponse.notFound(new NotificationModelResponseDTO.NotificationModelErrorDTO("Notification not found"));
        }
        NotificationModel notificationModel = model.toNotificationModel();
        notificationModel = notificationRepository.update(notificationModel);
        return HttpResponse.ok(NotificationModelResponseDTO.NotificationModelDTO.createDTO(notificationModel));
    }
}