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
import com.kasperovich.entities.AcademicPeriod;
import com.kasperovich.entities.ScholarshipApplication;
import com.kasperovich.entities.ScholarshipProgram;
import com.kasperovich.entities.User;
import com.kasperovich.utils.DTOConverter;
import com.kasperovich.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
}
