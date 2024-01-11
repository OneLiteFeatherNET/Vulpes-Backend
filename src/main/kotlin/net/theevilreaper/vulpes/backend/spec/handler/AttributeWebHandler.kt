package net.theevilreaper.vulpes.backend.spec.handler

import net.theevilreaper.vulpes.api.model.AttributeModel
import net.theevilreaper.vulpes.api.repository.AttributeRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

/**
 * The [AttributeWebHandler] contains the endpoints to handle requests to perform operations on [AttributeModel].
 * @since 1.0.0
 * @author theEvilReaper
 */
@CrossOrigin(
    origins = ["*"],
    maxAge = 4800,
    allowCredentials = "false",
    methods = [RequestMethod.POST, RequestMethod.DELETE, RequestMethod.GET, RequestMethod.HEAD, RequestMethod.OPTIONS]
)
@RestController
class AttributeWebHandler {

    @Autowired
    private lateinit var attributeRepository: AttributeRepository

    /**
     * Add a new [AttributeModel] to the database.
     * @return the added [AttributeModel] mapped in a [ResponseEntity]
     */
    @PostMapping("/attribute")
    fun add(@RequestBody model: AttributeModel): ResponseEntity<AttributeModel> {
        return ResponseEntity.ok(attributeRepository.save(model))
    }

    @PostMapping("/attribute/update")
    fun update(@RequestBody model: AttributeModel): ResponseEntity<AttributeModel> {
        return ResponseEntity.ok(attributeRepository.save(model))
    }

    @DeleteMapping("/attribute/delete/{id}")
    fun delete(@PathVariable("id") id: String): ResponseEntity<AttributeModel> {
        val attributeModel = attributeRepository.findById(id)
        if (attributeModel.isPresent) {
            attributeRepository.deleteById(id)
            return ResponseEntity.ok(attributeModel.get())
        }

        return ResponseEntity.notFound().build()
    }

    @DeleteMapping("/attribute/deleteAll")
    fun deleteAll(): ResponseEntity<List<AttributeModel>> {
        attributeRepository.deleteAll()
        return ResponseEntity.ok(listOf())
    }

    /**
     * Returns all [AttributeModel] which are currently persists in the database.
     * @return a list with all [AttributeModel] mapped in a [ResponseEntity]
     */
    @GetMapping("/attribute/getAll")
    fun getAll(): ResponseEntity<List<AttributeModel>> {
        return ResponseEntity.ok(attributeRepository.findAll())
    }
}