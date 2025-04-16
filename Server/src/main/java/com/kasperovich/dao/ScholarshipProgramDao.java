package com.kasperovich.dao;

import com.kasperovich.entities.ScholarshipProgram;

import java.util.List;

/**
 * Data Access Object interface for ScholarshipProgram entity.
 */
public interface ScholarshipProgramDao {
    
    /**
     * Finds all scholarship programs.
     *
     * @return a list of all scholarship programs
     */
    List<ScholarshipProgram> findAll();
    
    /**
     * Finds all active scholarship programs.
     *
     * @return a list of all active scholarship programs
     */
    List<ScholarshipProgram> findAllActive();
    
    /**
     * Finds a scholarship program by its ID.
     *
     * @param id the ID of the scholarship program to find
     * @return the scholarship program with the specified ID, or null if not found
     */
    ScholarshipProgram findById(Long id);
    
    /**
     * Saves a scholarship program.
     *
     * @param program the scholarship program to save
     * @return the saved scholarship program
     */
    ScholarshipProgram save(ScholarshipProgram program);
    
    /**
     * Updates a scholarship program.
     *
     * @param program the scholarship program to update
     * @return the updated scholarship program
     */
    ScholarshipProgram update(ScholarshipProgram program);
    
    /**
     * Deletes a scholarship program.
     *
     * @param program the scholarship program to delete
     */
    void delete(ScholarshipProgram program);
}
