package com.kasperovich.dao;

import java.util.List;
import java.util.Optional;

/**
 * Generic Base DAO interface that defines common CRUD operations.
 *
 * @param <T> The entity type
 * @param <ID> The type of the entity's primary key
 */
public interface BaseDao<T, ID> {
    
    /**
     * Saves a new entity or updates an existing one.
     *
     * @param entity The entity to save or update
     * @return The saved or updated entity
     */
    T save(T entity);
    
    /**
     * Finds an entity by its ID.
     *
     * @param id The ID of the entity to find
     * @return An Optional containing the found entity, or empty if not found
     */
    Optional<T> findById(ID id);
    
    /**
     * Retrieves all entities.
     *
     * @return A list of all entities
     */
    List<T> findAll();
    
    /**
     * Deletes an entity.
     *
     * @param entity The entity to delete
     */
    void delete(T entity);
    
    /**
     * Deletes an entity by its ID.
     *
     * @param id The ID of the entity to delete
     * @return true if the entity was deleted, false if not found
     */
    boolean deleteById(ID id);
    
    /**
     * Checks if an entity with the given ID exists.
     *
     * @param id The ID to check
     * @return true if an entity with the given ID exists, false otherwise
     */
    boolean existsById(ID id);
    
    /**
     * Counts the total number of entities.
     *
     * @return The count of entities
     */
    long count();
}
