package com.kasperovich.service;

import com.kasperovich.dao.ScholarshipProgramDao;
import com.kasperovich.dao.impl.ScholarshipProgramDaoImpl;
import com.kasperovich.dao.UserDao;
import com.kasperovich.dao.impl.UserDaoImpl;
import com.kasperovich.dto.scholarship.ScholarshipProgramDTO;
import com.kasperovich.entities.ScholarshipProgram;
import com.kasperovich.entities.User;
import com.kasperovich.entities.UserRole;
import com.kasperovich.utils.DTOConverter;
import com.kasperovich.utils.LoggerUtil;
import com.kasperovich.commands.toserver.CreateScholarshipProgramCommand;
import com.kasperovich.commands.toserver.UpdateScholarshipProgramCommand;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for scholarship-related operations.
 */
public class ScholarshipService {
    private static final Logger logger = LoggerUtil.getLogger(ScholarshipService.class);
    
    private final ScholarshipProgramDao scholarshipProgramDao;
    private final UserDao userDao;
    private final DTOConverter dtoConverter;
    
    /**
     * Constructs a new ScholarshipService with default implementations.
     */
    public ScholarshipService() {
        this.scholarshipProgramDao = new ScholarshipProgramDaoImpl();
        this.userDao = new UserDaoImpl();
        this.dtoConverter = new DTOConverter();
    }
    
    /**
     * Gets all scholarship programs.
     *
     * @return a list of all scholarship programs as DTOs
     */
    public List<ScholarshipProgramDTO> getAllScholarshipPrograms() {
        logger.debug("Getting all scholarship programs");
        List<ScholarshipProgram> programs = scholarshipProgramDao.findAll();
        return convertToDTOs(programs);
    }
    
    /**
     * Gets all active scholarship programs.
     *
     * @return a list of all active scholarship programs as DTOs
     */
    public List<ScholarshipProgramDTO> getActiveScholarshipPrograms() {
        logger.debug("Getting active scholarship programs");
        List<ScholarshipProgram> programs = scholarshipProgramDao.findAllActive();
        return convertToDTOs(programs);
    }
    
    /**
     * Gets a scholarship program by its ID.
     *
     * @param id the ID of the scholarship program to get
     * @return the scholarship program as a DTO, or null if not found
     */
    public ScholarshipProgramDTO getScholarshipProgramById(Long id) {
        logger.debug("Getting scholarship program with ID: {}", id);
        ScholarshipProgram program = scholarshipProgramDao.findById(id);
        return program != null ? dtoConverter.convertToDTO(program) : null;
    }
    
    /**
     * Converts a list of ScholarshipProgram entities to DTOs.
     *
     * @param programs the list of ScholarshipProgram entities
     * @return a list of ScholarshipProgramDTO objects
     */
    private List<ScholarshipProgramDTO> convertToDTOs(List<ScholarshipProgram> programs) {
        if (programs == null || programs.isEmpty()) {
            return new ArrayList<>();
        }
        
        return programs.stream()
                .map(dtoConverter::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Creates a new scholarship program.
     *
     * @param command the command containing the scholarship program data
     * @param userId the ID of the user creating the program
     * @return the created scholarship program as a DTO
     * @throws IllegalArgumentException if the user is not an admin or if required data is missing
     */
    public ScholarshipProgramDTO createScholarshipProgram(CreateScholarshipProgramCommand command, Long userId) {
        logger.debug("Creating new scholarship program: {}", command.getName());
        
        // Validate user is admin
        Optional<User> user = userDao.findById(userId);
        if (user.isEmpty() || user.get().getRole() != UserRole.ADMIN) {
            logger.warn("Non-admin user attempted to create scholarship program. User ID: {}", userId);
            throw new IllegalArgumentException("Only administrators can create scholarship programs");
        }
        
        // Validate required fields
        if (command.getName() == null || command.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Program name is required");
        }
        
        if (command.getDescription() == null || command.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Program description is required");
        }
        
        if (command.getFundingAmount() == null) {
            throw new IllegalArgumentException("Funding amount is required");
        }
        
        if (command.getMinGpa() == null) {
            throw new IllegalArgumentException("Minimum GPA is required");
        }
        
        if (command.getApplicationDeadline() == null) {
            throw new IllegalArgumentException("Application deadline is required");
        }
        
        // Create new program entity
        ScholarshipProgram program = new ScholarshipProgram();
        program.setName(command.getName());
        program.setDescription(command.getDescription());
        program.setFundingAmount(command.getFundingAmount());
        program.setMinGpa(command.getMinGpa());
        program.setCreatedBy(user.get());
        program.setActive(command.isActive());
        program.setApplicationDeadline(command.getApplicationDeadline());
        program.setCreatedAt(LocalDateTime.now());
        
        // Save the program
        ScholarshipProgram savedProgram = scholarshipProgramDao.save(program);
        logger.info("Created new scholarship program with ID: {}", savedProgram.getId());
        
        // Return as DTO
        return dtoConverter.convertToDTO(savedProgram);
    }
    
    /**
     * Updates an existing scholarship program.
     *
     * @param command the command containing the updated scholarship program data
     * @param userId the ID of the user updating the program
     * @return the updated scholarship program as a DTO
     * @throws IllegalArgumentException if the user is not an admin, if the program doesn't exist, or if required data is missing
     */
    public ScholarshipProgramDTO updateScholarshipProgram(UpdateScholarshipProgramCommand command, Long userId) {
        logger.debug("Updating scholarship program with ID: {}", command.getId());
        
        // Validate user is admin
        Optional<User> user = userDao.findById(userId);
        if (user.isEmpty() || user.get().getRole() != UserRole.ADMIN) {
            logger.warn("Non-admin user attempted to update scholarship program. User ID: {}", userId);
            throw new IllegalArgumentException("Only administrators can update scholarship programs");
        }
        
        // Validate program exists
        ScholarshipProgram program = scholarshipProgramDao.findById(command.getId());
        if (program == null) {
            logger.warn("Attempted to update non-existent scholarship program with ID: {}", command.getId());
            throw new IllegalArgumentException("Scholarship program not found");
        }
        
        // Validate required fields
        if (command.getName() == null || command.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Program name is required");
        }
        
        if (command.getDescription() == null || command.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Program description is required");
        }
        
        if (command.getFundingAmount() == null) {
            throw new IllegalArgumentException("Funding amount is required");
        }
        
        if (command.getMinGpa() == null) {
            throw new IllegalArgumentException("Minimum GPA is required");
        }
        
        if (command.getApplicationDeadline() == null) {
            throw new IllegalArgumentException("Application deadline is required");
        }
        
        // Update program fields
        program.setName(command.getName());
        program.setDescription(command.getDescription());
        program.setFundingAmount(command.getFundingAmount());
        program.setMinGpa(command.getMinGpa());
        program.setActive(command.isActive());
        program.setApplicationDeadline(command.getApplicationDeadline());
        
        // Save the updated program
        ScholarshipProgram updatedProgram = scholarshipProgramDao.update(program);
        logger.info("Updated scholarship program with ID: {}", updatedProgram.getId());
        
        // Return as DTO
        return dtoConverter.convertToDTO(updatedProgram);
    }
    
    /**
     * Deletes a scholarship program.
     *
     * @param programId the ID of the scholarship program to delete
     * @param userId the ID of the user deleting the program
     * @return true if the program was deleted, false otherwise
     * @throws IllegalArgumentException if the user is not an admin or if the program doesn't exist
     */
    public boolean deleteScholarshipProgram(Long programId, Long userId) {
        logger.debug("Deleting scholarship program with ID: {}", programId);
        
        // Validate user is admin
        Optional<User> user = userDao.findById(userId);
        if (user.isEmpty() || user.get().getRole() != UserRole.ADMIN) {
            logger.warn("Non-admin user attempted to delete scholarship program. User ID: {}", userId);
            throw new IllegalArgumentException("Only administrators can delete scholarship programs");
        }
        
        // Validate program exists
        ScholarshipProgram program = scholarshipProgramDao.findById(programId);
        if (program == null) {
            logger.warn("Attempted to delete non-existent scholarship program with ID: {}", programId);
            throw new IllegalArgumentException("Scholarship program not found");
        }
        
        // Delete the program
        boolean deleted = scholarshipProgramDao.delete(program);
        
        if (deleted) {
            logger.info("Deleted scholarship program with ID: {}", programId);
        } else {
            logger.warn("Failed to delete scholarship program with ID: {}", programId);
        }
        
        return deleted;
    }
}
