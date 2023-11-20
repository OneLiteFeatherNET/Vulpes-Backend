package net.theevilreaper.vulpes.backend.spec.handler

import net.theevilreaper.vulpes.backend.spec.database.FontDatabaseHandler
import net.theevilreaper.vulpes.model.FontModel
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
class FontHandler {

    @Autowired
    lateinit var fontDatabaseHandler: FontDatabaseHandler


    @RequestMapping("/font", method = [RequestMethod.POST], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun add(@RequestBody item: FontModel): ResponseEntity<FontModel> {
        // Validation
        return fontDatabaseHandler.add(item)
    }

    @RequestMapping("/font/{id}", method = [RequestMethod.GET], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getByID(@PathVariable("id") id: String): ResponseEntity<FontModel> {
        return fontDatabaseHandler.getByID(id)
    }

    @RequestMapping("/font/remove/{id}", method = [RequestMethod.DELETE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun remove(@PathVariable("id") id: String): ResponseEntity<FontModel> {
        return fontDatabaseHandler.delete(id)
    }

    @RequestMapping("/font/getAll", method = [RequestMethod.GET], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAll(): ResponseEntity<List<FontModel>> {
        return fontDatabaseHandler.getAll()
    }

    @DeleteMapping("/font/deleteAll")
    fun deleteAll(): ResponseEntity<List<FontModel>> {
        return fontDatabaseHandler.deleteAll()
    }

    @RequestMapping("/font/update", method = [RequestMethod.POST], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun update(@RequestBody model: FontModel): ResponseEntity<FontModel> {
        return fontDatabaseHandler.update(model)
    }
}
    