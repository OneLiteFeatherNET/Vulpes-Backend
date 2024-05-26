package net.theevilreaper.vulpes.backend.controller

import net.theevilreaper.vulpes.api.model.NotificationModel
import net.theevilreaper.vulpes.backend.dao.DatabaseAccessObject
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since
 **/
@CrossOrigin(
    origins = ["*"],
    maxAge = 4800,
    allowCredentials = "false",
    methods = [RequestMethod.POST, RequestMethod.DELETE, RequestMethod.GET, RequestMethod.HEAD, RequestMethod.OPTIONS]
)
@RestController
class NotificationController(
    private val notificationHandler: DatabaseAccessObject<NotificationModel>
) {

    @PostMapping("/notification", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun add(@RequestBody model: NotificationModel): ResponseEntity<NotificationModel> {
        // Validation
        return notificationHandler.add(model)
    }

    @GetMapping("/notification/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getByID(@PathVariable("id") id: String): ResponseEntity<NotificationModel> {
        return notificationHandler.getByID(id);
    }

    @DeleteMapping(
        "/notification/remove/{id}",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun remove(@PathVariable("id") id: String): ResponseEntity<NotificationModel> {
        return notificationHandler.delete(id)
    }

    @GetMapping("/notification/getAll", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAll(): ResponseEntity<List<NotificationModel>> {
        return notificationHandler.getAll()
    }

    @DeleteMapping("/notification/deleteAll")
    fun deleteAll(): ResponseEntity<List<NotificationModel>> {
        return notificationHandler.deleteAll()
    }

    @PostMapping(
        "/notification/update",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun update(@RequestBody model: NotificationModel): ResponseEntity<NotificationModel> {
        return notificationHandler.update(model)
    }
}
