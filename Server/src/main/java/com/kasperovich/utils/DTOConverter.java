package com.kasperovich.utils;

import com.kasperovich.dto.auth.UserDTO;
import com.kasperovich.dto.scholarship.AcademicPeriodDTO;
import com.kasperovich.dto.scholarship.BudgetDTO;
import com.kasperovich.dto.scholarship.FundAllocationDTO;
import com.kasperovich.dto.scholarship.ScholarshipApplicationDTO;
import com.kasperovich.dto.scholarship.ScholarshipProgramDTO;
import com.kasperovich.entities.AcademicPeriod;
import com.kasperovich.entities.Budget;
import com.kasperovich.entities.FundAllocation;
import com.kasperovich.entities.ScholarshipApplication;
import com.kasperovich.entities.ScholarshipProgram;
import com.kasperovich.entities.User;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;

/**
 * Utility class for converting between entity objects and DTOs.
 */
public class DTOConverter {
    
    private static final Logger logger = LoggerUtil.getLogger(DTOConverter.class);
    
    /**
     * Constructor for DTOConverter.
     */
    public DTOConverter() {
        // Empty constructor
    }
    
    /**
     * Converts a User entity to a UserDTO.
     *
     * @param user the User entity
     * @return the UserDTO
     */
    public UserDTO convertToDTO(User user) {
        if (user == null) {
            return null;
        }
        
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setRole(user.getRole().toString());
        
        return dto;
    }
    
    /**
     * Converts a ScholarshipProgram entity to a ScholarshipProgramDTO.
     *
     * @param program the ScholarshipProgram entity
     * @return the ScholarshipProgramDTO
     */
    public ScholarshipProgramDTO convertToDTO(ScholarshipProgram program) {
        if (program == null) {
            return null;
        }
        
        ScholarshipProgramDTO dto = new ScholarshipProgramDTO();
        dto.setId(program.getId());
        dto.setName(program.getName());
        dto.setDescription(program.getDescription());
        dto.setFundingAmount(program.getFundingAmount());
        dto.setMinGpa(program.getMinGpa());
        dto.setActive(program.isActive());
        dto.setApplicationDeadline(program.getApplicationDeadline());
        dto.setAcceptingApplications(isAcceptingApplications(program));
        
        // Set creator information if available
        if (program.getCreatedBy() != null) {
            dto.setCreatedById(program.getCreatedBy().getId());
            dto.setCreatedByUsername(program.getCreatedBy().getUsername());
        }
        
        // Set fund tracking information
        dto.setAllocatedAmount(program.getAllocatedAmount());
        dto.setUsedAmount(program.getUsedAmount());
        dto.setRemainingAmount(program.getRemainingAmount());
        dto.setHasFundsAvailable(program.getRemainingAmount() != null && 
                                program.getRemainingAmount().compareTo(BigDecimal.ZERO) > 0);
        
        return dto;
    }
    
    /**
     * Converts a ScholarshipApplication entity to a ScholarshipApplicationDTO.
     *
     * @param application the ScholarshipApplication entity
     * @return the ScholarshipApplicationDTO
     */
    public ScholarshipApplicationDTO convertToDTO(ScholarshipApplication application) {
        if (application == null) {
            return null;
        }
        
        ScholarshipApplicationDTO dto = new ScholarshipApplicationDTO();
        dto.setId(application.getId());
        dto.setSubmissionDate(application.getSubmissionDate());
        dto.setStatus(application.getStatus());
        dto.setDecisionDate(application.getDecisionDate());
        dto.setDecisionComments(application.getDecisionComments());
        
        // Set applicant information
        User applicant = application.getApplicant();
        if (applicant != null) {
            dto.setApplicantId(applicant.getId());
            dto.setApplicantUsername(applicant.getUsername());
            dto.setApplicantFullName(applicant.getFirstName() + " " + applicant.getLastName());
        }
        
        // Set program information
        ScholarshipProgram program = application.getProgram();
        if (program != null) {
            dto.setProgramId(program.getId());
            dto.setProgramName(program.getName());
        }
        
        // Set period information
        AcademicPeriod period = application.getPeriod();
        if (period != null) {
            dto.setPeriodId(period.getId());
            dto.setPeriodName(period.getName());
        }
        
        // Set reviewer information if available
        User reviewer = application.getReviewer();
        if (reviewer != null) {
            dto.setReviewerId(reviewer.getId());
            dto.setReviewerUsername(reviewer.getUsername());
        }
        
        return dto;
    }
    
    /**
     * Converts an AcademicPeriod entity to an AcademicPeriodDTO.
     *
     * @param period the AcademicPeriod entity
     * @return the AcademicPeriodDTO
     */
    public AcademicPeriodDTO convertToDTO(AcademicPeriod period) {
        if (period == null) {
            return null;
        }
        
        AcademicPeriodDTO dto = new AcademicPeriodDTO();
        dto.setId(period.getId());
        dto.setName(period.getName());
        dto.setStartDate(period.getStartDate());
        dto.setEndDate(period.getEndDate());
        dto.setType(period.getType());
        dto.setActive(period.isActive());
        
        return dto;
    }
    
    /**
     * Converts a Budget entity to a BudgetDTO.
     *
     * @param budget the Budget entity
     * @return the BudgetDTO
     */
    public BudgetDTO convertToDTO(Budget budget) {
        if (budget == null) {
            return null;
        }
        
        BudgetDTO dto = new BudgetDTO();
        dto.setId(budget.getId());
        dto.setFiscalYear(budget.getFiscalYear());
        dto.setFiscalPeriod(budget.getFiscalPeriod());
        dto.setTotalAmount(budget.getTotalAmount());
        dto.setAllocatedAmount(budget.getAllocatedAmount());
        dto.setRemainingAmount(budget.getRemainingAmount());
        dto.setStartDate(budget.getStartDate());
        dto.setEndDate(budget.getEndDate());
        dto.setDescription(budget.getDescription());
        dto.setStatus(budget.getStatus());
        dto.setCreatedAt(budget.getCreatedAt());
        dto.setUpdatedAt(budget.getUpdatedAt());
        
        // Set creator information if available
        if (budget.getCreatedBy() != null) {
            dto.setCreatedById(budget.getCreatedBy().getId());
            dto.setCreatedByName(budget.getCreatedBy().getFirstName() + " " + budget.getCreatedBy().getLastName());
        }
        
        return dto;
    }
    
    /**
     * Converts a FundAllocation entity to a FundAllocationDTO.
     *
     * @param allocation the FundAllocation entity
     * @return the FundAllocationDTO
     */
    public FundAllocationDTO convertToDTO(FundAllocation allocation) {
        if (allocation == null) {
            return null;
        }
        
        FundAllocationDTO dto = new FundAllocationDTO();
        dto.setId(allocation.getId());
        dto.setAmount(allocation.getAmount());
        dto.setPreviousAmount(allocation.getPreviousAmount());
        dto.setAllocationDate(allocation.getAllocationDate());
        dto.setStatus(allocation.getStatus());
        dto.setNotes(allocation.getNotes());
        
        // Set budget information
        Budget budget = allocation.getBudget();
        if (budget != null) {
            dto.setBudgetId(budget.getId());
            dto.setBudgetFiscalYear(budget.getFiscalYear().toString());
            if (budget.getFiscalPeriod() != null) {
                dto.setBudgetFiscalPeriod(budget.getFiscalPeriod());
            }
        }
        
        // Set program information
        ScholarshipProgram program = allocation.getProgram();
        if (program != null) {
            dto.setProgramId(program.getId());
            dto.setProgramName(program.getName());
        }
        
        // Set allocator information
        User allocator = allocation.getAllocatedBy();
        if (allocator != null) {
            dto.setAllocatedById(allocator.getId());
            dto.setAllocatedByName(allocator.getFirstName() + " " + allocator.getLastName());
        }
        
        return dto;
    }
    
    /**
     * Checks if a scholarship program is currently accepting applications.
     *
     * @param program the ScholarshipProgram entity
     * @return true if the program is accepting applications, false otherwise
     */
    private boolean isAcceptingApplications(ScholarshipProgram program) {
        if (!program.isActive()) {
            return false;
        }
        
        if (program.getApplicationDeadline() == null) {
            return true;
        }
        
        return !program.getApplicationDeadline().isBefore(java.time.LocalDate.now());
    }
}
