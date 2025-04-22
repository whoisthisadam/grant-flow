package com.kasperovich.dao;

import com.kasperovich.entities.Budget;
import com.kasperovich.entities.BudgetStatus;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Budget entity.
 */
public interface BudgetDao {
    
    /**
     * Finds all budgets.
     *
     * @return a list of all budgets
     */
    List<Budget> findAll();
    
    /**
     * Finds all budgets with the specified status.
     *
     * @param status the status to filter by
     * @return a list of budgets with the specified status
     */
    List<Budget> findByStatus(BudgetStatus status);
    
    /**
     * Finds the currently active budget.
     *
     * @return an Optional containing the active budget, or empty if none is active
     */
    Optional<Budget> findActiveBudget();
    
    /**
     * Finds a budget by its ID.
     *
     * @param id the ID of the budget to find
     * @return an Optional containing the budget with the specified ID, or empty if not found
     */
    Optional<Budget> findById(Long id);
    
    /**
     * Saves a budget.
     *
     * @param budget the budget to save
     * @return the saved budget
     */
    Budget save(Budget budget);
    
    /**
     * Updates a budget.
     *
     * @param budget the budget to update
     * @return the updated budget
     */
    Budget update(Budget budget);
    
    /**
     * Deletes a budget.
     *
     * @param budget the budget to delete
     * @return true if the budget was deleted, false otherwise
     */
    boolean delete(Budget budget);
}
