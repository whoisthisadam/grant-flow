package com.kasperovich.service;

import com.kasperovich.dao.AcademicPeriodDao;
import com.kasperovich.dao.impl.AcademicPeriodDaoImpl;
import com.kasperovich.dto.scholarship.AcademicPeriodDTO;
import com.kasperovich.entities.AcademicPeriod;
import com.kasperovich.utils.DTOConverter;
import com.kasperovich.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for academic period operations.
 */
public class AcademicPeriodService {
    private static final Logger logger = LoggerUtil.getLogger(AcademicPeriodService.class);
    private final AcademicPeriodDao academicPeriodDao;
    private final DTOConverter dtoConverter;

    /**
     * Constructs a new AcademicPeriodService
     */
    public AcademicPeriodService() {
        this.academicPeriodDao = new AcademicPeriodDaoImpl();
        this.dtoConverter = new DTOConverter();
    }

    /**
     * Gets all academic periods.
     *
     * @return a list of academic period DTOs
     */
    public List<AcademicPeriodDTO> getAllAcademicPeriods() {
        try {
            List<AcademicPeriod> periods = academicPeriodDao.findAll();
            return periods.stream()
                    .map(dtoConverter::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting all academic periods", e);
            return new ArrayList<>();
        }
    }

    /**
     * Gets all active academic periods.
     *
     * @return a list of active academic period DTOs
     */
    public List<AcademicPeriodDTO> getActiveAcademicPeriods() {
        try {
            List<AcademicPeriod> periods = academicPeriodDao.findByActive(true);
            return periods.stream()
                    .map(dtoConverter::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting active academic periods", e);
            return new ArrayList<>();
        }
    }

    /**
     * Gets an academic period by its ID.
     *
     * @param id the ID of the academic period
     * @return the academic period DTO, or null if not found
     */
    public AcademicPeriodDTO getAcademicPeriodById(Long id) {
        try {
            return academicPeriodDao.findById(id)
                    .map(dtoConverter::convertToDTO)
                    .orElse(null);
        } catch (Exception e) {
            logger.error("Error getting academic period by ID: {}", id, e);
            return null;
        }
    }
}
