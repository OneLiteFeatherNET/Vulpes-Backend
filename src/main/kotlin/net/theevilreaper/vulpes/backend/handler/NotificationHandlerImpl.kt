package net.theevilreaper.vulpes.backend.handler

import net.theevilreaper.vulpes.backend.spec.database.NotificationDatabaseHandler
import net.theevilreaper.vulpes.model.NotificationModel
import net.theevilreaper.vulpes.repository.NotificationRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since
 **/
@Service
class NotificationHandlerImpl(val notificationRepository: NotificationRepository) : NotificationDatabaseHandler {

    override fun getAll(): ResponseEntity<List<NotificationModel>> {
        notificationRepository.findAll().let {
            return ResponseEntity.ok(it)
        }
    }

    override fun deleteAll(): ResponseEntity<List<NotificationModel>> {
        notificationRepository.deleteAll()
        return ResponseEntity.ok().build()
    }

    override fun delete(id: String): ResponseEntity<NotificationModel> {
        if (id.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "The id value can't be empty")
        }
        val model = notificationRepository.findById(id).orElseThrow()
        notificationRepository.deleteById(model.id ?: id)
        return ResponseEntity.ok(model)
    }

    override fun add(model: NotificationModel): ResponseEntity<NotificationModel> {
        return ResponseEntity.ok(notificationRepository.save(model))
    }

    override fun update(model: NotificationModel): ResponseEntity<NotificationModel> {
        return ResponseEntity.ok(notificationRepository.save(model))
    }

    override fun getByID(id: String): ResponseEntity<NotificationModel> {
        if (id.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "The id value can't be empty")
        }
        val value = notificationRepository.findById(id)

        return if (value.isEmpty) throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "The requested object does not exists"
        ) else ResponseEntity.ok(value.get())
    }
}