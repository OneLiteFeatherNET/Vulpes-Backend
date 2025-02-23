package net.theevilreaper.vulpes.backend.controller;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.annotation.Controller;
import net.theevilreaper.vulpes.api.model.NotificationModel;
import net.theevilreaper.vulpes.api.repository.NotificationRepository;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

/**
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

    @Post
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<NotificationModel> add(@Body NotificationModel model) {
        return HttpResponse.ok(notificationRepository.save(model));
    }

    @Get("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<NotificationModel> getById(@PathVariable String id) {
        Optional<NotificationModel> model = notificationRepository.findById(id);
        if (model.isPresent()) {
            return HttpResponse.ok(model.get());
        }
        return HttpResponse.notFound();
    }

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

    @Get("/getAll")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<List<NotificationModel>> getAll() {
        return HttpResponse.ok(notificationRepository.findAll());
    }

    @Delete("/deleteAll")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<List<NotificationModel>> deleteAll() {
        notificationRepository.deleteAll();
        return HttpResponse.ok(List.of());
    }

    @Post("/update")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<NotificationModel> update(@Body NotificationModel model) {
        return HttpResponse.ok(notificationRepository.update(model));
    }
}