package net.theevilreaper.vulpes.backend.handler

import net.theevilreaper.vulpes.backend.spec.database.BlockDatabaseHandler
import net.theevilreaper.vulpes.model.BlockModel
import net.theevilreaper.vulpes.repository.BlocKRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

/**
 * @author theEvilReaper
 * @version 1.0.0
 * @since
 **/

const val ENTRY_NOT_FOUND = "The request entry not found"

@Service
class BlockHandlerImpl(val blocKRepository: BlocKRepository) : BlockDatabaseHandler {
    override fun getAll(): ResponseEntity<List<BlockModel>> {
        blocKRepository.findAll().let {
            return ResponseEntity.ok(it)
        }
    }

    override fun deleteAll(): ResponseEntity<List<BlockModel>> {
        blocKRepository.deleteAll()
        return ResponseEntity.ok().build()
    }

    override fun delete(id: String): ResponseEntity<BlockModel> {
        if (id.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "The id value can't be empty")
        }
        blocKRepository.deleteById(id)
        return ResponseEntity.ok().build()
    }

    override fun add(model: BlockModel): ResponseEntity<BlockModel> {
        return ResponseEntity.ok(blocKRepository.save(model))
    }

    override fun update(model: BlockModel): ResponseEntity<BlockModel> {
        return ResponseEntity.ok(blocKRepository.save(model))
    }

    override fun getByID(id: String): ResponseEntity<BlockModel> {
        if (id.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "The id value can't be empty")
        }

        blocKRepository.findById(id).let {
            return if (it.isPresent) {
                ResponseEntity.ok(it.get());
            } else {
                throw ResponseStatusException(HttpStatus.NOT_FOUND, ENTRY_NOT_FOUND)
            }
        }
    }
}