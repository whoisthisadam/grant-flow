package com.kasperovich.dao;

import com.kasperovich.entities.FundAllocation;
import com.kasperovich.entities.AllocationStatus;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for FundAllocation entity.
 */
public interface FundAllocationDao {
    
    /**
     * Finds all fund allocations.
     *
     * @return a list of all fund allocations
     */
    List<FundAllocation> findAll();
    
    /**
     * Finds all fund allocations for a specific budget.
     *
     * @param budgetId the ID of the budget
     * @return a list of fund allocations for the specified budget
     */
    List<FundAllocation> findByBudgetId(Long budgetId);
    
    /**
     * Finds all fund allocations for a specific scholarship program.
     *
     * @param programId the ID of the scholarship program
     * @return a list of fund allocations for the specified program
     */
    List<FundAllocation> findByProgramId(Long programId);
    
    /**
     * Finds all fund allocations with the specified status.
     *
     * @param status the status to filter by
     * @return a list of fund allocations with the specified status
     */
    List<FundAllocation> findByStatus(AllocationStatus status);
    
    /**
     * Finds a fund allocation by its ID.
     *
     * @param id the ID of the fund allocation to find
     * @return an Optional containing the fund allocation with the specified ID, or empty if not found
     */
    Optional<FundAllocation> findById(Long id);
    
    /**
     * Saves a fund allocation.
     *
     * @param allocation the fund allocation to save
     * @return the saved fund allocation
     */
    FundAllocation save(FundAllocation allocation);
    
    /**
     * Updates a fund allocation.
     *
     * @param allocation the fund allocation to update
     * @return the updated fund allocation
     */
    FundAllocation update(FundAllocation allocation);
    
    /**
     * Deletes a fund allocation.
     *
     * @param allocation the fund allocation to delete
     * @return true if the fund allocation was deleted, false otherwise
     */
    boolean delete(FundAllocation allocation);
}
