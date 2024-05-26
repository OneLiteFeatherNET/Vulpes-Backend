package net.theevilreaper.vulpes.backend.service.dao

import net.theevilreaper.vulpes.api.model.NotificationModel
import net.theevilreaper.vulpes.api.repository.NotificationRepository
import net.theevilreaper.vulpes.backend.dao.DatabaseAccessObject
import net.theevilreaper.vulpes.backend.exception.ResourceNotFoundException
import org.springframework.http.HttpMethod
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
class NotificationServiceImpl(val notificationRepository: NotificationRepository) :
    DatabaseAccessObject<NotificationModel> {

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
        val notificationModel = notificationRepository.findById(id)
        if (notificationModel.isEmpty) {
            throw ResourceNotFoundException(HttpMethod.DELETE, id)
        }
        val model = notificationModel.get()
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
        val value = notificationRepository.findById(id)
        return if (value.isEmpty) throw ResourceNotFoundException(
            HttpMethod.GET, id
        ) else ResponseEntity.ok(value.get())
    }
}