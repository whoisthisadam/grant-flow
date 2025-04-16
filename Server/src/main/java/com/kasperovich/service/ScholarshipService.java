package com.kasperovich.service;

import com.kasperovich.dao.ScholarshipProgramDao;
import com.kasperovich.dao.impl.ScholarshipProgramDaoImpl;
import com.kasperovich.dto.scholarship.ScholarshipProgramDTO;
import com.kasperovich.entities.ScholarshipProgram;
import com.kasperovich.utils.DTOConverter;
import com.kasperovich.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for scholarship-related operations.
 */
public class ScholarshipService {
    private static final Logger logger = LoggerUtil.getLogger(ScholarshipService.class);
    
    private final ScholarshipProgramDao scholarshipProgramDao;
    private final DTOConverter dtoConverter;
    
    /**
     * Constructs a new ScholarshipService with default implementations.
     */
    public ScholarshipService() {
        this.scholarshipProgramDao = new ScholarshipProgramDaoImpl();
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
}
