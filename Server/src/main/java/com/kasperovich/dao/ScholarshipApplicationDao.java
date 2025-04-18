package com.kasperovich.dao;

import com.kasperovich.entities.ScholarshipApplication;
import com.kasperovich.entities.User;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for ScholarshipApplication entity.
 */
public interface ScholarshipApplicationDao {
    
    /**
     * Saves a scholarship application.
     *
     * @param application the application to save
     * @return the saved application
     */
    ScholarshipApplication save(ScholarshipApplication application);
    
    /**
     * Finds a scholarship application by its ID.
     *
     * @param id the ID of the application
     * @return an Optional containing the application if found, empty otherwise
     */
    Optional<ScholarshipApplication> findById(Long id);
    
    /**
     * Finds all scholarship applications submitted by a specific user.
     *
     * @param user the user who submitted the applications
     * @return a list of applications submitted by the user
     */
    List<ScholarshipApplication> findByApplicant(User user);
    
    /**
     * Finds all scholarship applications for a specific program.
     *
     * @param programId the ID of the program
     * @return a list of applications for the program
     */
    List<ScholarshipApplication> findByProgramId(Long programId);
    
    /**
     * Finds all scholarship applications for a specific academic period.
     *
     * @param periodId the ID of the academic period
     * @return a list of applications for the period
     */
    List<ScholarshipApplication> findByPeriodId(Long periodId);
    
    /**
     * Finds all scholarship applications with a specific status.
     *
     * @param status the status to filter by
     * @return a list of applications with the given status
     */
    List<ScholarshipApplication> findByStatus(String status);
    
    /**
     * Finds all scholarship applications.
     *
     * @return a list of all applications
     */
    List<ScholarshipApplication> findAll();
    
    /**
     * Updates a scholarship application.
     *
     * @param application the application to update
     * @return the updated application
     */
    ScholarshipApplication update(ScholarshipApplication application);
    
    /**
     * Deletes a scholarship application.
     *
     * @param application the application to delete
     */
    void delete(ScholarshipApplication application);
}
