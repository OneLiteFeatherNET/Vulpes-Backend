package net.theevilreaper.vulpes.backend.spec.handler

import net.theevilreaper.vulpes.backend.spec.database.BlockDatabaseHandler
import net.theevilreaper.vulpes.model.BlockModel
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
class BlockHandler {

    @Autowired
    lateinit var blockDatabaseHandler: BlockDatabaseHandler

    /**
     * Add a new [BlockModel] entry to the database.
     * @param model the model which should be added
     */
    @RequestMapping("/block", method = [RequestMethod.POST], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun add(@RequestBody model: BlockModel): ResponseEntity<BlockModel> {
        return blockDatabaseHandler.add(model)
    }

    @RequestMapping("/block/{id}", method = [RequestMethod.GET], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getByID(@PathVariable("id") id: String): ResponseEntity<BlockModel> {
        return blockDatabaseHandler.getByID(id);
    }

    /**
     * Deletes a [BlockModel] by a given id.
     * @param id the id from the model string
     * @return the deleted [BlockModel] in a [ResponseEntity] object.
     */
    @RequestMapping("/block/remove/{id}", method = [RequestMethod.DELETE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun remove(@PathVariable("id") id: String): ResponseEntity<BlockModel> {
        return blockDatabaseHandler.delete(id)
    }

    /**
     * Get all [BlockModel] entries which are currently in the database
     * @return a [ResponseEntity] which contains a list with a [BlockModel] entries
     */
    @RequestMapping("/block/getAll", method = [RequestMethod.GET], produces = [MediaType.APPLICATION_JSON_VALUE])
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

    @RequestMapping("/block/update", method = [RequestMethod.POST], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun update(@RequestBody model: BlockModel): ResponseEntity<BlockModel> {
        return blockDatabaseHandler.update(model)
    }
}
