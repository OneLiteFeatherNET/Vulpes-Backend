package net.theevilreaper.vulpes.backend.spec.handler

import net.theevilreaper.vulpes.backend.spec.database.NotificationDatabaseHandler
import net.theevilreaper.vulpes.model.NotificationModel
import org.springframework.beans.factory.annotation.Autowired
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
class NotificationHandler {

    @Autowired
    lateinit var notificationHandler: NotificationDatabaseHandler

    @RequestMapping("/notification", method = [RequestMethod.POST], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun add(@RequestBody model: NotificationModel): ResponseEntity<NotificationModel> {
        // Validation
        return notificationHandler.add(model)
    }

    @RequestMapping("/notification/{id}", method = [RequestMethod.GET], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getByID(@PathVariable("id") id: String): ResponseEntity<NotificationModel> {
        return notificationHandler.getByID(id);
    }

    @RequestMapping(
        "/notification/remove/{id}",
        method = [RequestMethod.DELETE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun remove(@PathVariable("id") id: String): ResponseEntity<NotificationModel> {
        return notificationHandler.delete(id)
    }

    @RequestMapping("/notification/getAll", method = [RequestMethod.GET], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAll(): ResponseEntity<List<NotificationModel>> {
        return notificationHandler.getAll()
    }

    @DeleteMapping("/notification/deleteAll")
    fun deleteAll(): ResponseEntity<List<NotificationModel>> {
        return notificationHandler.deleteAll()
    }

    @RequestMapping(
        "/notification/update",
        method = [RequestMethod.POST],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun update(@RequestBody model: NotificationModel): ResponseEntity<NotificationModel> {
        return notificationHandler.update(model)
    }
}
