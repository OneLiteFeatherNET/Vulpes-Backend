package net.theevilreaper.vulpes.backend.spec.database

import org.springframework.http.ResponseEntity

/**
 * This interface defines all basic actions.
 * @author theEvilReaper
 * @version 1.0.0
 * @since
 **/
interface DatabaseBaseHandler<T> {

    /**
     * Get an entry from the database by a given id.
     * @param id the id as string
     * @return the fetched value
     */
    fun getByID(id: String): ResponseEntity<T>

    /**
     * Add a new entry to the database.
     * @param model the model to add
     * @return the model to add
     */
    fun add(model: T): ResponseEntity<T>

    /**
     * Update a model in the database.
     * @param model the model to update
     * @return the updated model
     */
    fun update(model: T): ResponseEntity<T>

    /**
     * Delete a model by his id.
     * @param id the id from the model as string which should be deleted
     * @return the deleted model
     */
    fun delete(id: String): ResponseEntity<T>

    /**
     * Delete all entries from a database.
     * @return a list which contains all deleted entries
     */
    fun deleteAll(): ResponseEntity<List<T>>

    /**
     * Get all entries from the database.
     * @return a list which contains all entries
     */
    fun getAll(): ResponseEntity<List<T>>
}
