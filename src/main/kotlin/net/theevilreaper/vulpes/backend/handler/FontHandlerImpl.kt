package net.theevilreaper.vulpes.backend.handler

import net.theevilreaper.vulpes.backend.spec.database.FontDatabaseHandler
import net.theevilreaper.vulpes.model.FontModel
import net.theevilreaper.vulpes.repository.FontRepository
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
class FontHandlerImpl(val fontRepository: FontRepository) : FontDatabaseHandler {
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
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "The given id doesn't match any entry")
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
        if (id.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "The id value can't be empty")
        }
        val value = fontRepository.findById(id);

        return if (value.isEmpty) throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "The requested object does not exists"
        ) else ResponseEntity.ok(value.get())
    }
}