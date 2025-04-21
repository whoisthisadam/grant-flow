package com.kasperovich.service;

import com.kasperovich.dao.AcademicPeriodDao;
import com.kasperovich.dao.ScholarshipApplicationDao;
import com.kasperovich.dao.ScholarshipProgramDao;
import com.kasperovich.dao.UserDao;
import com.kasperovich.dao.impl.AcademicPeriodDaoImpl;
import com.kasperovich.dao.impl.ScholarshipApplicationDaoImpl;
import com.kasperovich.dao.impl.ScholarshipProgramDaoImpl;
import com.kasperovich.dao.impl.UserDaoImpl;
import com.kasperovich.dto.scholarship.ScholarshipApplicationDTO;
import com.kasperovich.entities.*;
import com.kasperovich.utils.DTOConverter;
import com.kasperovich.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for scholarship application processing.
 */
public class ScholarshipApplicationService {
    private static final Logger logger = LoggerUtil.getLogger(ScholarshipApplicationService.class);
    
    private final ScholarshipApplicationDao applicationDao;
    private final ScholarshipProgramDao programDao;
    private final AcademicPeriodDao periodDao;
    private final UserDao userDao;
    private final DTOConverter dtoConverter;
    
    /**
     * Constructs a new ScholarshipApplicationService with default DAOs.
     */
    public ScholarshipApplicationService() {
        this.applicationDao = new ScholarshipApplicationDaoImpl();
        this.programDao = new ScholarshipProgramDaoImpl();
        this.periodDao = new AcademicPeriodDaoImpl();
        this.userDao = new UserDaoImpl();
        this.dtoConverter = new DTOConverter();
    }
    
    /**
     * Submits a new scholarship application.
     *
     * @param userId the ID of the user submitting the application
     * @param programId the ID of the scholarship program
     * @param periodId the ID of the academic period
     * @param additionalInfo additional information provided by the applicant
     * @return the created application DTO
     * @throws Exception if any error occurs during application submission
     */
    public ScholarshipApplicationDTO submitApplication(Long userId, Long programId, Long periodId, String additionalInfo) 
            throws Exception {
        logger.debug("Submitting scholarship application for user: {}, program: {}, period: {}", userId, programId, periodId);
        
        try {
            // Validate user
            User user = userDao.findById(userId)
                    .orElseThrow(() -> new Exception("User not found with ID: " + userId));
            
            // Validate program
            ScholarshipProgram program = programDao.findById(programId);
            if (program == null) {
                throw new Exception("Scholarship program not found with ID: " + programId);
            }
            
            // Validate period
            Optional<AcademicPeriod> period = periodDao.findById(periodId);
            if (period.isEmpty()) {
                throw new Exception("Academic period not found with ID: " + periodId);
            }
            
            // Check if application deadline has passed
            if (program.getApplicationDeadline() != null && 
                    program.getApplicationDeadline().isBefore(LocalDateTime.now().toLocalDate())) {
                throw new Exception("Application deadline has passed for this program");
            }
            
            // Check if period is active
            if (!period.get().isActive()) {
                throw new Exception("The selected academic period is not active");
            }
            
            // Check if user has already applied for this program in this period
            List<ScholarshipApplication> existingApplications = applicationDao.findByApplicant(user);
            boolean alreadyApplied = existingApplications.stream()
                    .anyMatch(app -> app.getProgram().getId().equals(programId) && 
                                    app.getPeriod().getId().equals(periodId));
            
            if (alreadyApplied) {
                throw new Exception("You have already applied for this program in the selected period");
            }
            
            // Create and save the application
            ScholarshipApplication application = new ScholarshipApplication();
            application.setApplicant(user);
            application.setProgram(program);
            application.setPeriod(period.get());
            application.setSubmissionDate(LocalDateTime.now());
            application.setStatus("PENDING");
            application.setAdditionalInfo(additionalInfo);
            
            ScholarshipApplication savedApplication = applicationDao.save(application);
            logger.info("Scholarship application submitted successfully. ID: {}", savedApplication.getId());
            
            return dtoConverter.convertToDTO(savedApplication);
        } catch (Exception e) {
            logger.error("Error submitting scholarship application", e);
            throw e;
        }
    }
    
    /**
     * Gets all applications submitted by a specific user.
     *
     * @param userId the ID of the user
     * @return a list of application DTOs
     * @throws Exception if any error occurs while retrieving applications
     */
    public List<ScholarshipApplicationDTO> getUserApplications(Long userId) throws Exception {
        logger.debug("Getting scholarship applications for user: {}", userId);
        
        try {
            User user = userDao.findById(userId)
                    .orElseThrow(() -> new Exception("User not found with ID: " + userId));
            
            List<ScholarshipApplication> applications = applicationDao.findByApplicant(user);
            
            List<ScholarshipApplicationDTO> dtos = applications.stream()
                    .map(dtoConverter::convertToDTO)
                    .collect(Collectors.toList());
            
            logger.info("Retrieved {} scholarship applications for user: {}", dtos.size(), userId);
            return dtos;
        } catch (Exception e) {
            logger.error("Error getting user applications", e);
            throw e;
        }
    }
    
    /**
     * Gets an application by its ID.
     *
     * @param applicationId the ID of the application
     * @return the application DTO
     * @throws Exception if the application is not found or any other error occurs
     */
    public ScholarshipApplicationDTO getApplicationById(Long applicationId) throws Exception {
        logger.debug("Getting scholarship application with ID: {}", applicationId);
        
        try {
            ScholarshipApplication application = applicationDao.findById(applicationId)
                    .orElseThrow(() -> new Exception("Application not found with ID: " + applicationId));
            
            ScholarshipApplicationDTO dto = dtoConverter.convertToDTO(application);
            logger.info("Retrieved scholarship application with ID: {}", applicationId);
            return dto;
        } catch (Exception e) {
            logger.error("Error getting application by ID", e);
            throw e;
        }
    }
    
    /**
     * Gets all pending scholarship applications.
     *
     * @return a list of pending application DTOs
     * @throws Exception if any error occurs while retrieving applications
     */
    public List<ScholarshipApplicationDTO> getPendingApplications() throws Exception {
        logger.debug("Getting all pending scholarship applications");
        
        try {
            List<ScholarshipApplication> applications = applicationDao.findByStatus("PENDING");
            
            List<ScholarshipApplicationDTO> dtos = applications.stream()
                    .map(dtoConverter::convertToDTO)
                    .collect(Collectors.toList());
            
            logger.info("Retrieved {} pending scholarship applications", dtos.size());
            return dtos;
        } catch (Exception e) {
            logger.error("Error getting pending applications", e);
            throw e;
        }
    }
    
    /**
     * Gets all scholarship applications.
     *
     * @return a list of all application DTOs
     * @throws Exception if any error occurs while retrieving applications
     */
    public List<ScholarshipApplicationDTO> getAllApplications() throws Exception {
        logger.debug("Getting all scholarship applications");
        
        try {
            List<ScholarshipApplication> applications = applicationDao.findAll();
            
            List<ScholarshipApplicationDTO> dtos = applications.stream()
                    .map(dtoConverter::convertToDTO)
                    .collect(Collectors.toList());
            
            logger.info("Retrieved {} scholarship applications", dtos.size());
            return dtos;
        } catch (Exception e) {
            logger.error("Error getting all applications", e);
            throw e;
        }
    }
    
    /**
     * Gets all pending scholarship applications for an admin user.
     *
     * @param userId the ID of the user requesting the applications
     * @return a list of pending application DTOs
     * @throws Exception if the user is not an admin or any error occurs while retrieving applications
     */
    public List<ScholarshipApplicationDTO> getPendingApplicationsForAdmin(Long userId) throws Exception {
        logger.debug("Getting pending scholarship applications for admin user: {}", userId);
        
        try {
            // Validate user is admin
            User user = userDao.findById(userId)
                    .orElseThrow(() -> new Exception("User not found with ID: " + userId));
            
            if (!UserRole.ADMIN.equals(user.getRole())) {
                logger.warn("Non-admin user attempted to get pending applications: {}", userId);
                throw new Exception("Only administrators can view pending applications");
            }
            
            // Get pending applications
            return getPendingApplications();
        } catch (Exception e) {
            logger.error("Error getting pending applications for admin", e);
            throw e;
        }
    }
    
    /**
     * Gets all scholarship applications for an admin user.
     *
     * @param userId the ID of the user requesting the applications
     * @return a list of all application DTOs
     * @throws Exception if the user is not an admin or any error occurs while retrieving applications
     */
    public List<ScholarshipApplicationDTO> getAllApplicationsForAdmin(Long userId) throws Exception {
        logger.debug("Getting all scholarship applications for admin user: {}", userId);
        
        try {
            // Validate user is admin
            User user = userDao.findById(userId)
                    .orElseThrow(() -> new Exception("User not found with ID: " + userId));

            if (!UserRole.ADMIN.equals(user.getRole())) {
                logger.warn("Non-admin user attempted to get all applications: {}", userId);
                throw new Exception("Only administrators can view all applications");
            }

            // Get all applications
            return getAllApplications();
        } catch (Exception e) {
            logger.error("Error getting all applications for admin", e);
            throw e;
        }
    }
    
    /**
     * Approves a scholarship application.
     *
     * @param applicationId the ID of the application to approve
     * @param reviewerId the ID of the reviewer (admin)
     * @param comments comments about the approval decision
     * @return the updated application DTO
     * @throws Exception if the application is not found, the reviewer is not an admin, or any other error occurs
     */
    public ScholarshipApplicationDTO approveApplication(Long applicationId, Long reviewerId, String comments) throws Exception {
        logger.debug("Approving scholarship application with ID: {}", applicationId);
        
        try {
            // Validate application
            ScholarshipApplication application = applicationDao.findById(applicationId)
                    .orElseThrow(() -> new Exception("Application not found with ID: " + applicationId));
            
            // Validate reviewer
            User reviewer = userDao.findById(reviewerId)
                    .orElseThrow(() -> new Exception("Reviewer not found with ID: " + reviewerId));
            
            // Check if reviewer is an admin
            if (!UserRole.ADMIN.equals(reviewer.getRole())) {
                throw new Exception("Only administrators can approve applications");
            }
            
            // Check if application is already processed
            if (!application.isPending()) {
                throw new Exception("This application has already been " + application.getStatus().toLowerCase());
            }
            
            // Approve the application
            application.approve(reviewer, comments);
            
            // Update the application
            ScholarshipApplication updatedApplication = applicationDao.update(application);
            logger.info("Scholarship application with ID: {} has been approved", applicationId);
            
            return dtoConverter.convertToDTO(updatedApplication);
        } catch (Exception e) {
            logger.error("Error approving application", e);
            throw e;
        }
    }
    
    /**
     * Rejects a scholarship application.
     *
     * @param applicationId the ID of the application to reject
     * @param reviewerId the ID of the reviewer (admin)
     * @param comments comments about the rejection decision
     * @return the updated application DTO
     * @throws Exception if the application is not found, the reviewer is not an admin, or any other error occurs
     */
    public ScholarshipApplicationDTO rejectApplication(Long applicationId, Long reviewerId, String comments) throws Exception {
        logger.debug("Rejecting scholarship application with ID: {}", applicationId);
        
        try {
            // Validate application
            ScholarshipApplication application = applicationDao.findById(applicationId)
                    .orElseThrow(() -> new Exception("Application not found with ID: " + applicationId));
            
            // Validate reviewer
            User reviewer = userDao.findById(reviewerId)
                    .orElseThrow(() -> new Exception("Reviewer not found with ID: " + reviewerId));
            
            // Check if reviewer is an admin
            if (!UserRole.ADMIN.equals(reviewer.getRole())) {
                throw new Exception("Only administrators can reject applications");
            }
            
            // Check if application is already processed
            if (!application.isPending()) {
                throw new Exception("This application has already been " + application.getStatus().toLowerCase());
            }
            
            // Reject the application
            application.reject(reviewer, comments);
            
            // Update the application
            ScholarshipApplication updatedApplication = applicationDao.update(application);
            logger.info("Scholarship application with ID: {} has been rejected", applicationId);
            
            return dtoConverter.convertToDTO(updatedApplication);
        } catch (Exception e) {
            logger.error("Error rejecting application", e);
            throw e;
        }
    }
    
    /**
     * Approves a scholarship application with validation for the authenticated user.
     *
     * @param applicationId the ID of the application to approve
     * @param userId the ID of the authenticated user
     * @param comments comments about the approval decision
     * @return the updated application DTO
     * @throws Exception if the user is not authorized, the application is not found, or any other error occurs
     */
    public ScholarshipApplicationDTO approveApplicationWithAuth(Long applicationId, Long userId, String comments) throws Exception {
        logger.debug("Approving scholarship application with ID: {} by user: {}", applicationId, userId);
        
        try {
            // Validate user is admin
            User user = userDao.findById(userId)
                    .orElseThrow(() -> new Exception("User not found with ID: " + userId));
            
            if (!UserRole.ADMIN.equals(user.getRole())) {
                logger.warn("Non-admin user attempted to approve application: {}", userId);
                throw new Exception("Only administrators can approve applications");
            }
            
            // Approve the application
            return approveApplication(applicationId, userId, comments);
        } catch (Exception e) {
            logger.error("Error approving application with authentication", e);
            throw e;
        }
    }
    
    /**
     * Rejects a scholarship application with validation for the authenticated user.
     *
     * @param applicationId the ID of the application to reject
     * @param userId the ID of the authenticated user
     * @param comments comments about the rejection decision
     * @return the updated application DTO
     * @throws Exception if the user is not authorized, the application is not found, or any other error occurs
     */
    public ScholarshipApplicationDTO rejectApplicationWithAuth(Long applicationId, Long userId, String comments) throws Exception {
        logger.debug("Rejecting scholarship application with ID: {} by user: {}", applicationId, userId);
        
        try {
            // Validate user is admin
            User user = userDao.findById(userId)
                    .orElseThrow(() -> new Exception("User not found with ID: " + userId));
            
            if (!UserRole.ADMIN.equals(user.getRole())) {
                logger.warn("Non-admin user attempted to reject application: {}", userId);
                throw new Exception("Only administrators can reject applications");
            }
            
            // Reject the application
            return rejectApplication(applicationId, userId, comments);
        } catch (Exception e) {
            logger.error("Error rejecting application with authentication", e);
            throw e;
        }
    }
}
