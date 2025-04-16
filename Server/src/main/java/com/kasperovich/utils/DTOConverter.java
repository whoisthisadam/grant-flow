package com.kasperovich.utils;

import com.kasperovich.dto.auth.UserDTO;
import com.kasperovich.dto.scholarship.AcademicPeriodDTO;
import com.kasperovich.dto.scholarship.ScholarshipApplicationDTO;
import com.kasperovich.dto.scholarship.ScholarshipProgramDTO;
import com.kasperovich.entities.AcademicPeriod;
import com.kasperovich.entities.ScholarshipApplication;
import com.kasperovich.entities.ScholarshipProgram;
import com.kasperovich.entities.User;
import org.apache.logging.log4j.Logger;

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
            try {
                User creator = program.getCreatedBy();
                dto.setCreatedById(creator.getId());
                dto.setCreatedByUsername(creator.getUsername());
            } catch (Exception e) {
                logger.warn("Error accessing creator information for program: {}", program.getId(), e);
                dto.setCreatedById(program.getCreatedBy().getId());
                dto.setCreatedByUsername("User #" + program.getCreatedBy().getId());
            }
        }
        
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
            try {
                dto.setApplicantId(applicant.getId());
                dto.setApplicantUsername(applicant.getUsername());
                dto.setApplicantFullName(applicant.getFirstName() + " " + applicant.getLastName());
            } catch (Exception e) {
                logger.warn("Error accessing applicant information for application: {}", application.getId(), e);
                dto.setApplicantId(applicant.getId());
                dto.setApplicantUsername("User #" + applicant.getId());
                dto.setApplicantFullName("User #" + applicant.getId());
            }
        }
        
        // Set program information
        ScholarshipProgram program = application.getProgram();
        if (program != null) {
            try {
                dto.setProgramId(program.getId());
                dto.setProgramName(program.getName());
            } catch (Exception e) {
                logger.warn("Error accessing program information for application: {}", application.getId(), e);
                dto.setProgramId(program.getId());
                dto.setProgramName("Program #" + program.getId());
            }
        }
        
        // Set period information
        AcademicPeriod period = application.getPeriod();
        if (period != null) {
            try {
                dto.setPeriodId(period.getId());
                dto.setPeriodName(period.getName());
            } catch (Exception e) {
                logger.warn("Error accessing period information for application: {}", application.getId(), e);
                dto.setPeriodId(period.getId());
                dto.setPeriodName("Period #" + period.getId());
            }
        }
        
        // Set reviewer information if available
        User reviewer = application.getReviewer();
        if (reviewer != null) {
            try {
                dto.setReviewerId(reviewer.getId());
                dto.setReviewerUsername(reviewer.getUsername());
            } catch (Exception e) {
                logger.warn("Error accessing reviewer information for application: {}", application.getId(), e);
                dto.setReviewerId(reviewer.getId());
                dto.setReviewerUsername("User #" + reviewer.getId());
            }
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
