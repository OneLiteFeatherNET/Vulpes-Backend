package net.theevilreaper.vulpes.backend.spec.handler

import net.theevilreaper.vulpes.backend.spec.database.ItemDatabaseHandler
import net.theevilreaper.vulpes.model.ItemModel
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
class ItemHandler {

    @Autowired
    lateinit var itemDatabaseHandler: ItemDatabaseHandler

    @RequestMapping("/item", method = [RequestMethod.POST], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun add(@RequestBody item: ItemModel): ResponseEntity<ItemModel> {
        // Validation
        return itemDatabaseHandler.add(item)
    }

    @RequestMapping("/item/{id}", method = [RequestMethod.GET], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getByID(@PathVariable("id") id: String): ResponseEntity<ItemModel> {
        return itemDatabaseHandler.getByID(id)
    }

    @RequestMapping("/item/remove/{id}", method = [RequestMethod.DELETE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun remove(@PathVariable("id") id: String): ResponseEntity<ItemModel> {
        return itemDatabaseHandler.delete(id)
    }

    @RequestMapping("/item/getAll", method = [RequestMethod.GET], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAll(): ResponseEntity<List<ItemModel>> {
        return itemDatabaseHandler.getAll()
    }

    @DeleteMapping("/item/deleteAll")
    fun deleteAll(): ResponseEntity<List<ItemModel>> {
        return itemDatabaseHandler.deleteAll()
    }

    @RequestMapping("/item/update", method = [RequestMethod.POST], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun update(@RequestBody model: ItemModel): ResponseEntity<ItemModel> {
        return itemDatabaseHandler.update(model)
    }
}
