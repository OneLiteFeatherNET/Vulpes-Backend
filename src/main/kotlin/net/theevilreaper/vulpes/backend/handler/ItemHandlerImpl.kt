package net.theevilreaper.vulpes.backend.handler

import net.theevilreaper.vulpes.backend.spec.database.ItemDatabaseHandler
import net.theevilreaper.vulpes.backend.util.INVALID_ID_MESSAGE
import net.theevilreaper.vulpes.model.ItemModel
import net.theevilreaper.vulpes.repository.ItemRepository
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
class ItemHandlerImpl(val itemRepository: ItemRepository) : ItemDatabaseHandler {
    override fun getAll(): ResponseEntity<List<ItemModel>> {
        itemRepository.findAll().let {
            return ResponseEntity.ok(it)
        }
    }

    override fun deleteAll(): ResponseEntity<List<ItemModel>> {
        itemRepository.deleteAll()
        return ResponseEntity.ok().build()
    }

    override fun delete(id: String): ResponseEntity<ItemModel> {
        if (id.trim().isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_ID_MESSAGE)
        }
        val model = itemRepository.findById(id).orElseThrow()
        itemRepository.deleteById(id)
        return ResponseEntity.ok(model)
    }

    override fun add(model: ItemModel): ResponseEntity<ItemModel> {
       /* if (model.id.orEmpty().trim().isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_ID_MESSAGE)
        }*/
        return ResponseEntity.ok(itemRepository.save(model))
    }

    override fun update(model: ItemModel): ResponseEntity<ItemModel> {
        return ResponseEntity.ok(itemRepository.save(model))
    }

    override fun getByID(id: String): ResponseEntity<ItemModel> {
        if (id.trim().isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, INVALID_ID_MESSAGE)
        }
        val value = itemRepository.findById(id);

        return if (value.isEmpty) throw ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "The requested object does not exists"
        ) else ResponseEntity.ok(value.get())
    }
}
