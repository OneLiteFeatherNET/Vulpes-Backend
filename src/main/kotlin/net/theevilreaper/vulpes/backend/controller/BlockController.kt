package net.theevilreaper.vulpes.backend.controller

import net.theevilreaper.vulpes.api.model.BlockModel
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
class BlockController(
    private val blockDatabaseHandler: DatabaseAccessObject<BlockModel>
) {

    /**
     * Add a new [BlockModel] entry to the database.
     * @param model the model which should be added
     */
    @PostMapping("/block", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun add(@RequestBody model: BlockModel): ResponseEntity<BlockModel> {
        return blockDatabaseHandler.add(model)
    }

    @GetMapping("/block/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getByID(@PathVariable("id") id: String): ResponseEntity<BlockModel> {
        return blockDatabaseHandler.getByID(id);
    }

    /**
     * Deletes a [BlockModel] by a given id.
     * @param id the id from the model string
     * @return the deleted [BlockModel] in a [ResponseEntity] object.
     */
    @DeleteMapping(
        "/block/remove/{id}",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun remove(@PathVariable("id") id: String): ResponseEntity<BlockModel> {
        return blockDatabaseHandler.delete(id)
    }

    /**
     * Get all [BlockModel] entries which are currently in the database
     * @return a [ResponseEntity] which contains a list with a [BlockModel] entries
     */
    @GetMapping("/block/getAll", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAll(): ResponseEntity<List<BlockModel>> {
        return blockDatabaseHandler.getAll()
    }

    /**
     * Deletes all [BlockModel] entries from the database.
     */
    @DeleteMapping("/block/deleteAll")
    fun deleteAll(): ResponseEntity<List<BlockModel>> {
        return blockDatabaseHandler.deleteAll()
    }

    @PostMapping("/block/update", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun update(@RequestBody model: BlockModel): ResponseEntity<BlockModel> {
        return blockDatabaseHandler.update(model)
    }
}
