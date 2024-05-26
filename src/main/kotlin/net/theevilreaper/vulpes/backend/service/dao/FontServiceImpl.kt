package net.theevilreaper.vulpes.backend.service.dao

import net.theevilreaper.vulpes.api.model.FontModel
import net.theevilreaper.vulpes.api.repository.FontRepository
import net.theevilreaper.vulpes.backend.dao.DatabaseAccessObject
import net.theevilreaper.vulpes.backend.exception.ResourceNotFoundException
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since
 **/
@Service
class FontServiceImpl(val fontRepository: FontRepository) : DatabaseAccessObject<FontModel> {
    override fun getAll(): ResponseEntity<List<FontModel>> {
        fontRepository.findAll().let {
            return ResponseEntity.ok(it)
        }
    }

    override fun deleteAll(): ResponseEntity<List<FontModel>> {
        fontRepository.deleteAll()
        return ResponseEntity.ok().build()
    }

    override fun delete(id: String): ResponseEntity<FontModel> {
        val fontModel = this.fontRepository.findById(id)
        if (fontModel.isEmpty) {
            throw ResourceNotFoundException(HttpMethod.DELETE, id)
        }
        fontRepository.delete(fontModel.get())
        return ResponseEntity.ok(fontModel.get())
    }

    override fun add(model: FontModel): ResponseEntity<FontModel> {
        return ResponseEntity.ok(fontRepository.save(model))
    }

    override fun update(model: FontModel): ResponseEntity<FontModel> {
        return ResponseEntity.ok(fontRepository.save(model))
    }

    override fun getByID(id: String): ResponseEntity<FontModel> {
        val value = fontRepository.findById(id);

        return if (value.isEmpty) throw ResourceNotFoundException(
            HttpMethod.GET,
            id,
        ) else ResponseEntity.ok(value.get())
    }
}