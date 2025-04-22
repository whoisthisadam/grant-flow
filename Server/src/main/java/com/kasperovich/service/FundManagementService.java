package com.kasperovich.service;

import com.kasperovich.dao.BudgetDao;
import com.kasperovich.dao.FundAllocationDao;
import com.kasperovich.dao.ScholarshipProgramDao;
import com.kasperovich.dao.UserDao;
import com.kasperovich.dao.impl.BudgetDaoImpl;
import com.kasperovich.dao.impl.FundAllocationDaoImpl;
import com.kasperovich.dao.impl.ScholarshipProgramDaoImpl;
import com.kasperovich.dao.impl.UserDaoImpl;
import com.kasperovich.dto.scholarship.BudgetDTO;
import com.kasperovich.dto.scholarship.FundAllocationDTO;
import com.kasperovich.entities.AllocationStatus;
import com.kasperovich.entities.Budget;
import com.kasperovich.entities.BudgetStatus;
import com.kasperovich.entities.FundAllocation;
import com.kasperovich.entities.ScholarshipProgram;
import com.kasperovich.entities.User;
import com.kasperovich.entities.UserRole;
import com.kasperovich.utils.DTOConverter;
import com.kasperovich.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for fund management operations.
 */
public class FundManagementService {
    private static final Logger logger = LoggerUtil.getLogger(FundManagementService.class);
    
    private final BudgetDao budgetDao;
    private final FundAllocationDao fundAllocationDao;
    private final ScholarshipProgramDao scholarshipProgramDao;
    private final UserDao userDao;
    private final DTOConverter dtoConverter;
    
    /**
     * Constructs a new FundManagementService with default implementations.
     */
    public FundManagementService() {
        this.budgetDao = new BudgetDaoImpl();
        this.fundAllocationDao = new FundAllocationDaoImpl();
        this.scholarshipProgramDao = new ScholarshipProgramDaoImpl();
        this.userDao = new UserDaoImpl();
        this.dtoConverter = new DTOConverter();
    }
    
    // Budget Management Methods
    
    /**
     * Gets all budgets.
     *
     * @return a list of all budgets as DTOs
     */
    public List<BudgetDTO> getAllBudgets() {
        logger.debug("Getting all budgets");
        List<Budget> budgets = budgetDao.findAll();
        return convertToBudgetDTOs(budgets);
    }
    
    /**
     * Gets all budgets with the specified status.
     *
     * @param status the status to filter by
     * @return a list of budgets with the specified status as DTOs
     */
    public List<BudgetDTO> getBudgetsByStatus(BudgetStatus status) {
        logger.debug("Getting budgets with status: {}", status);
        List<Budget> budgets = budgetDao.findByStatus(status);
        return convertToBudgetDTOs(budgets);
    }
    
    /**
     * Gets the active budget.
     *
     * @return the active budget as a DTO, or null if not found
     */
    public BudgetDTO getActiveBudget() {
        logger.debug("Getting active budget");
        Optional<Budget> budget = budgetDao.findActiveBudget();
        return budget.map(dtoConverter::convertToDTO).orElse(null);
    }
    
    /**
     * Gets a budget by its ID.
     *
     * @param id the ID of the budget to get
     * @return the budget as a DTO, or null if not found
     */
    public BudgetDTO getBudgetById(Long id) {
        logger.debug("Getting budget with ID: {}", id);
        Optional<Budget> budget = budgetDao.findById(id);
        return budget.map(dtoConverter::convertToDTO).orElse(null);
    }
    
    /**
     * Creates a new budget.
     *
     * @param fiscalYear the fiscal year
     * @param fiscalPeriod the fiscal period
     * @param totalAmount the total amount
     * @param startDate the start date
     * @param endDate the end date
     * @param description the description
     * @param userId the ID of the user creating the budget
     * @return the created budget as a DTO
     * @throws Exception if the user is not an admin or if required data is missing
     */
    public BudgetDTO createBudget(Integer fiscalYear, String fiscalPeriod, BigDecimal totalAmount,
                                 LocalDate startDate, LocalDate endDate, String description, Long userId) throws Exception {
        logger.debug("Creating new budget for fiscal year: {}", fiscalYear);
        
        // Validate user is admin
        Optional<User> user = userDao.findById(userId);
        if (user.isEmpty() || user.get().getRole() != UserRole.ADMIN) {
            logger.warn("Non-admin user attempted to create budget. User ID: {}", userId);
            throw new Exception("Only administrators can create budgets");
        }
        
        // Validate required fields
        if (fiscalYear == null) {
            throw new Exception("Fiscal year is required");
        }
        
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("Total amount must be greater than zero");
        }
        
        if (startDate == null) {
            throw new Exception("Start date is required");
        }
        
        if (endDate == null) {
            throw new Exception("End date is required");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new Exception("Start date cannot be after end date");
        }
        
        // Check for overlapping active budgets
        if (startDate.isBefore(LocalDate.now().plusDays(1)) && endDate.isAfter(LocalDate.now().minusDays(1))) {
            Optional<Budget> existingActiveBudget = budgetDao.findActiveBudget();
            if (existingActiveBudget.isPresent()) {
                throw new Exception("An active budget already exists for the current period");
            }
        }
        
        // Create new budget entity
        Budget budget = new Budget();
        budget.setFiscalYear(fiscalYear);
        budget.setFiscalPeriod(fiscalPeriod);
        budget.setTotalAmount(totalAmount);
        budget.setAllocatedAmount(BigDecimal.ZERO);
        budget.setStartDate(startDate);
        budget.setEndDate(endDate);
        budget.setDescription(description);
        budget.setStatus(BudgetStatus.DRAFT);
        budget.setCreatedBy(user.get());
        budget.setCreatedAt(LocalDateTime.now());
        
        // Calculate remaining amount
        budget.calculateRemainingAmount();
        
        // Save the budget
        Budget savedBudget = budgetDao.save(budget);
        logger.info("Created new budget with ID: {}", savedBudget.getId());
        
        // Return as DTO
        return dtoConverter.convertToDTO(savedBudget);
    }
    
    /**
     * Updates an existing budget.
     *
     * @param id the ID of the budget to update
     * @param fiscalYear the fiscal year
     * @param fiscalPeriod the fiscal period
     * @param totalAmount the total amount
     * @param startDate the start date
     * @param endDate the end date
     * @param description the description
     * @param status the status
     * @param userId the ID of the user updating the budget
     * @return the updated budget as a DTO
     * @throws Exception if the user is not an admin, the budget is not found, or if required data is missing
     */
    public BudgetDTO updateBudget(Long id, Integer fiscalYear, String fiscalPeriod, BigDecimal totalAmount,
                                 LocalDate startDate, LocalDate endDate, String description, 
                                 BudgetStatus status, Long userId) throws Exception {
        logger.debug("Updating budget with ID: {}", id);
        
        // Validate user is admin
        Optional<User> user = userDao.findById(userId);
        if (user.isEmpty() || user.get().getRole() != UserRole.ADMIN) {
            logger.warn("Non-admin user attempted to update budget. User ID: {}", userId);
            throw new Exception("Only administrators can update budgets");
        }
        
        // Find the budget
        Optional<Budget> optionalBudget = budgetDao.findById(id);
        if (optionalBudget.isEmpty()) {
            logger.warn("Budget not found with ID: {}", id);
            throw new Exception("Budget not found with ID: " + id);
        }
        
        Budget budget = optionalBudget.get();
        
        // Validate required fields
        if (fiscalYear == null) {
            throw new Exception("Fiscal year is required");
        }
        
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("Total amount must be greater than zero");
        }
        
        if (startDate == null) {
            throw new Exception("Start date is required");
        }
        
        if (endDate == null) {
            throw new Exception("End date is required");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new Exception("Start date cannot be after end date");
        }
        
        // Check if total amount is being reduced below allocated amount
        if (totalAmount.compareTo(budget.getAllocatedAmount()) < 0) {
            throw new Exception("Total amount cannot be less than the already allocated amount: " + budget.getAllocatedAmount());
        }
        
        // Update budget entity
        budget.setFiscalYear(fiscalYear);
        budget.setFiscalPeriod(fiscalPeriod);
        budget.setTotalAmount(totalAmount);
        budget.setStartDate(startDate);
        budget.setEndDate(endDate);
        budget.setDescription(description);
        budget.setStatus(status);
        budget.setUpdatedAt(LocalDateTime.now());
        
        // Calculate remaining amount
        budget.calculateRemainingAmount();
        
        // Save the budget
        Budget updatedBudget = budgetDao.update(budget);
        logger.info("Updated budget with ID: {}", updatedBudget.getId());
        
        // Return as DTO
        return dtoConverter.convertToDTO(updatedBudget);
    }
    
    /**
     * Activates a budget.
     *
     * @param id the ID of the budget to activate
     * @param userId the ID of the user activating the budget
     * @return the activated budget as a DTO
     * @throws Exception if the user is not an admin, the budget is not found, or if another active budget exists
     */
    public BudgetDTO activateBudget(Long id, Long userId) throws Exception {
        logger.debug("Activating budget with ID: {}", id);
        
        // Validate user is admin
        Optional<User> user = userDao.findById(userId);
        if (user.isEmpty() || user.get().getRole() != UserRole.ADMIN) {
            logger.warn("Non-admin user attempted to activate budget. User ID: {}", userId);
            throw new Exception("Only administrators can activate budgets");
        }
        
        // Find the budget
        Optional<Budget> optionalBudget = budgetDao.findById(id);
        if (optionalBudget.isEmpty()) {
            logger.warn("Budget not found with ID: {}", id);
            throw new Exception("Budget not found with ID: " + id);
        }
        
        Budget budget = optionalBudget.get();
        
        // Check if the budget is already active
        if (budget.getStatus() == BudgetStatus.ACTIVE) {
            logger.info("Budget is already active: {}", id);
            return dtoConverter.convertToDTO(budget);
        }
        
        // Check for other active budgets
        Optional<Budget> existingActiveBudget = budgetDao.findActiveBudget();
        if (existingActiveBudget.isPresent() && !existingActiveBudget.get().getId().equals(id)) {
            throw new Exception("Another active budget already exists. Please close it before activating this one.");
        }
        
        // Activate the budget
        budget.setStatus(BudgetStatus.ACTIVE);
        budget.setUpdatedAt(LocalDateTime.now());
        
        // Save the budget
        Budget updatedBudget = budgetDao.update(budget);
        logger.info("Activated budget with ID: {}", updatedBudget.getId());
        
        // Return as DTO
        return dtoConverter.convertToDTO(updatedBudget);
    }
    
    /**
     * Closes a budget.
     *
     * @param id the ID of the budget to close
     * @param userId the ID of the user closing the budget
     * @return the closed budget as a DTO
     * @throws Exception if the user is not an admin or the budget is not found
     */
    public BudgetDTO closeBudget(Long id, Long userId) throws Exception {
        logger.debug("Closing budget with ID: {}", id);
        
        // Validate user is admin
        Optional<User> user = userDao.findById(userId);
        if (user.isEmpty() || user.get().getRole() != UserRole.ADMIN) {
            logger.warn("Non-admin user attempted to close budget. User ID: {}", userId);
            throw new Exception("Only administrators can close budgets");
        }
        
        // Find the budget
        Optional<Budget> optionalBudget = budgetDao.findById(id);
        if (optionalBudget.isEmpty()) {
            logger.warn("Budget not found with ID: {}", id);
            throw new Exception("Budget not found with ID: " + id);
        }
        
        Budget budget = optionalBudget.get();
        
        // Check if the budget is already closed
        if (budget.getStatus() == BudgetStatus.CLOSED) {
            logger.info("Budget is already closed: {}", id);
            return dtoConverter.convertToDTO(budget);
        }
        
        // Close the budget
        budget.setStatus(BudgetStatus.CLOSED);
        budget.setUpdatedAt(LocalDateTime.now());
        
        // Save the budget
        Budget updatedBudget = budgetDao.update(budget);
        logger.info("Closed budget with ID: {}", updatedBudget.getId());
        
        // Return as DTO
        return dtoConverter.convertToDTO(updatedBudget);
    }
    
    // Fund Allocation Methods
    
    /**
     * Gets all fund allocations.
     *
     * @return a list of all fund allocations as DTOs
     */
    public List<FundAllocationDTO> getAllAllocations() {
        logger.debug("Getting all fund allocations");
        List<FundAllocation> allocations = fundAllocationDao.findAll();
        return convertToAllocationDTOs(allocations);
    }
    
    /**
     * Gets all fund allocations for a specific budget.
     *
     * @param budgetId the ID of the budget
     * @return a list of fund allocations for the specified budget as DTOs
     */
    public List<FundAllocationDTO> getAllocationsByBudget(Long budgetId) {
        logger.debug("Getting fund allocations for budget ID: {}", budgetId);
        List<FundAllocation> allocations = fundAllocationDao.findByBudgetId(budgetId);
        return convertToAllocationDTOs(allocations);
    }
    
    /**
     * Gets all fund allocations for a specific scholarship program.
     *
     * @param programId the ID of the scholarship program
     * @return a list of fund allocations for the specified program as DTOs
     */
    public List<FundAllocationDTO> getAllocationsByProgram(Long programId) {
        logger.debug("Getting fund allocations for program ID: {}", programId);
        List<FundAllocation> allocations = fundAllocationDao.findByProgramId(programId);
        return convertToAllocationDTOs(allocations);
    }
    
    /**
     * Allocates funds to a scholarship program.
     *
     * @param budgetId the ID of the budget
     * @param programId the ID of the scholarship program
     * @param amount the amount to allocate
     * @param notes notes about the allocation
     * @param userId the ID of the user making the allocation
     * @return the created fund allocation as a DTO
     * @throws Exception if the user is not an admin, the budget or program is not found, or if there are insufficient funds
     */
    public FundAllocationDTO allocateFunds(Long budgetId, Long programId, BigDecimal amount, String notes, Long userId) throws Exception {
        logger.debug("Allocating {} to program ID: {} from budget ID: {}", amount, programId, budgetId);
        
        // Validate user is admin
        Optional<User> user = userDao.findById(userId);
        if (user.isEmpty() || user.get().getRole() != UserRole.ADMIN) {
            logger.warn("Non-admin user attempted to allocate funds. User ID: {}", userId);
            throw new Exception("Only administrators can allocate funds");
        }
        
        // Validate amount
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("Allocation amount must be greater than zero");
        }
        
        // Find the budget
        Optional<Budget> optionalBudget = budgetDao.findById(budgetId);
        if (optionalBudget.isEmpty()) {
            logger.warn("Budget not found with ID: {}", budgetId);
            throw new Exception("Budget not found with ID: " + budgetId);
        }
        
        Budget budget = optionalBudget.get();
        
        // Check if budget is active
        if (budget.getStatus() != BudgetStatus.ACTIVE) {
            throw new Exception("Cannot allocate funds from a budget that is not active");
        }
        
        // Check if there are sufficient funds
        if (!budget.hasSufficientFunds(amount)) {
            throw new Exception("Insufficient funds in the budget. Available: " + budget.getRemainingAmount());
        }
        
        // Find the program
        ScholarshipProgram program = scholarshipProgramDao.findById(programId);
        if (program == null) {
            logger.warn("Scholarship program not found with ID: {}", programId);
            throw new Exception("Scholarship program not found with ID: " + programId);
        }
        
        // Get previous allocation amount
        BigDecimal previousAmount = program.getAllocatedAmount();
        
        // Create new fund allocation entity
        FundAllocation allocation = new FundAllocation();
        allocation.setBudget(budget);
        allocation.setProgram(program);
        allocation.setAmount(amount);
        allocation.setPreviousAmount(previousAmount);
        allocation.setAllocationDate(LocalDateTime.now());
        allocation.setAllocatedBy(user.get());
        allocation.setStatus(AllocationStatus.APPROVED); // Auto-approve for now
        allocation.setNotes(notes);
        
        // Update budget
        budget.setAllocatedAmount(budget.getAllocatedAmount().add(amount));
        budget.calculateRemainingAmount();
        budgetDao.update(budget);
        
        // Update program
        if (program.getAllocatedAmount() == null) {
            program.setAllocatedAmount(amount);
        } else {
            program.setAllocatedAmount(program.getAllocatedAmount().add(amount));
        }
        program.calculateRemainingAmount();
        scholarshipProgramDao.update(program);
        
        // Save the allocation
        FundAllocation savedAllocation = fundAllocationDao.save(allocation);
        logger.info("Created new fund allocation with ID: {}", savedAllocation.getId());
        
        // Return as DTO
        return dtoConverter.convertToDTO(savedAllocation);
    }
    
    /**
     * Records fund usage for a scholarship program.
     *
     * @param programId the ID of the scholarship program
     * @param amount the amount of funds used
     * @param userId the ID of the user recording the usage
     * @throws Exception if the user is not an admin, the program is not found, or if there are insufficient funds
     */
    public void recordFundUsage(Long programId, BigDecimal amount, Long userId) throws Exception {
        logger.debug("Recording fund usage of {} for program ID: {}", amount, programId);
        
        // Validate user is admin
        Optional<User> user = userDao.findById(userId);
        if (user.isEmpty() || user.get().getRole() != UserRole.ADMIN) {
            logger.warn("Non-admin user attempted to record fund usage. User ID: {}", userId);
            throw new Exception("Only administrators can record fund usage");
        }
        
        // Validate amount
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("Usage amount must be greater than zero");
        }
        
        // Find the program
        ScholarshipProgram program = scholarshipProgramDao.findById(programId);
        if (program == null) {
            logger.warn("Scholarship program not found with ID: {}", programId);
            throw new Exception("Scholarship program not found with ID: " + programId);
        }
        
        // Check if program is active
        if (!program.isActive()) {
            throw new Exception("Cannot record fund usage for an inactive program");
        }
        
        // Check if there are sufficient funds
        if (!program.hasSufficientFunds(amount)) {
            throw new Exception("Insufficient funds in the program. Available: " + program.getRemainingAmount());
        }
        
        // Update program
        program.setUsedAmount(program.getUsedAmount().add(amount));
        program.calculateRemainingAmount();
        
        // Save the program
        scholarshipProgramDao.update(program);
        logger.info("Recorded fund usage of {} for program ID: {}", amount, programId);
    }
    
    /**
     * Checks if a scholarship program has sufficient funds.
     *
     * @param programId the ID of the scholarship program
     * @param amount the amount to check
     * @return true if the program has sufficient funds
     */
    public boolean hasSufficientFunds(Long programId, BigDecimal amount) {
        logger.debug("Checking if program ID: {} has sufficient funds: {}", programId, amount);
        
        // Find the program
        ScholarshipProgram program = scholarshipProgramDao.findById(programId);
        if (program == null) {
            logger.warn("Scholarship program not found with ID: {}", programId);
            return false;
        }
        
        // Check if program is active
        if (!program.isActive()) {
            logger.debug("Program ID: {} is not active", programId);
            return false;
        }
        
        // Check if there are sufficient funds
        boolean hasFunds = program.hasSufficientFunds(amount);
        logger.debug("Program ID: {} has sufficient funds: {}", programId, hasFunds);
        return hasFunds;
    }
    
    // Helper Methods
    
    /**
     * Converts a list of Budget entities to DTOs.
     *
     * @param budgets the list of Budget entities
     * @return a list of BudgetDTO objects
     */
    private List<BudgetDTO> convertToBudgetDTOs(List<Budget> budgets) {
        if (budgets == null || budgets.isEmpty()) {
            return new ArrayList<>();
        }
        
        return budgets.stream()
                .map(dtoConverter::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Converts a list of FundAllocation entities to DTOs.
     *
     * @param allocations the list of FundAllocation entities
     * @return a list of FundAllocationDTO objects
     */
    private List<FundAllocationDTO> convertToAllocationDTOs(List<FundAllocation> allocations) {
        if (allocations == null || allocations.isEmpty()) {
            return new ArrayList<>();
        }
        
        return allocations.stream()
                .map(dtoConverter::convertToDTO)
                .collect(Collectors.toList());
    }
}
