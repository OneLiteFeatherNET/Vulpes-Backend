package net.theevilreaper.vulpes.backend.service.dao

import net.theevilreaper.vulpes.api.model.BlockModel
import net.theevilreaper.vulpes.api.repository.BlocKRepository
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
class BlockServiceImpl(val blocKRepository: BlocKRepository) : DatabaseAccessObject<BlockModel> {
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
        blocKRepository.findById(id).let {
            return if (it.isPresent) {
                ResponseEntity.ok(it.get());
            } else {
                throw ResourceNotFoundException(HttpMethod.GET, id)
            }
        }
    }
}