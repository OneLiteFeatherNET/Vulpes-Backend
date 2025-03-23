package net.theevilreaper.vulpes.backend.controller;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import net.theevilreaper.vulpes.api.model.NotificationModel;
import net.theevilreaper.vulpes.api.repository.NotificationRepository;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

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
    @Post
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<NotificationModel> add(@Body NotificationModel model) {
        return HttpResponse.ok(notificationRepository.save(model));
    }

    /**
     * Retrieves a notification by its ID.
     *
     * @param id the ID of the notification to retrieve
     * @return HttpResponse containing the notification if found, or not found response
     */
    @Get("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<NotificationModel> getById(@PathVariable String id) {
        Optional<NotificationModel> model = notificationRepository.findById(id);
        if (model.isPresent()) {
            return HttpResponse.ok(model.get());
        }
        return HttpResponse.notFound();
    }

    /**
     * Removes a notification by its ID.
     *
     * @param id the ID of the notification to remove
     * @return HttpResponse containing the removed notification if found, or not found response
     */
    @Delete("/remove/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<NotificationModel> remove(@PathVariable String id) {
        Optional<NotificationModel> model = notificationRepository.findById(id);
        if (model.isPresent()) {
            notificationRepository.deleteById(id);
            return HttpResponse.ok(model.get());
        }
        return HttpResponse.notFound();
    }

    /**
     * Retrieves all notifications.
     *
     * @return HttpResponse containing a list of all notifications
     */
    @Get("/getAll")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<List<NotificationModel>> getAll() {
        return HttpResponse.ok(notificationRepository.findAll());
    }

    /**
     * Deletes all notifications.
     *
     * @return HttpResponse containing an empty list
     */
    @Delete("/deleteAll")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<List<NotificationModel>> deleteAll() {
        notificationRepository.deleteAll();
        return HttpResponse.ok(List.of());
    }

    /**
     * Updates an existing notification.
     *
     * @param model the notification model to update
     * @return HttpResponse containing the updated notification
     */
    @Post("/update")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<NotificationModel> update(@Body NotificationModel model) {
        return HttpResponse.ok(notificationRepository.update(model));
    }
}