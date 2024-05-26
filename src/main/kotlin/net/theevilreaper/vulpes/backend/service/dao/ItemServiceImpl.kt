package net.theevilreaper.vulpes.backend.service.dao

import net.theevilreaper.vulpes.api.model.ItemModel
import net.theevilreaper.vulpes.api.repository.ItemRepository
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
class ItemServiceImpl(val itemRepository: ItemRepository) : DatabaseAccessObject<ItemModel> {
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
        itemRepository.findById(id).let {
            if (it.isEmpty) {
                throw ResourceNotFoundException(HttpMethod.DELETE, id)
            }
            itemRepository.deleteById(id)
            return ResponseEntity.ok(it.get())
        }
    }

    override fun add(model: ItemModel): ResponseEntity<ItemModel> {
        return ResponseEntity.ok(itemRepository.save(model))
    }

    override fun update(model: ItemModel): ResponseEntity<ItemModel> {
        return ResponseEntity.ok(itemRepository.save(model))
    }

    override fun getByID(id: String): ResponseEntity<ItemModel> {
        val value = itemRepository.findById(id);

        return if (value.isEmpty) throw ResourceNotFoundException(
            HttpMethod.GET,
            id
        ) else ResponseEntity.ok(value.get())
    }
}
