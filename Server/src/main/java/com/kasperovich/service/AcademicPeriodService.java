package com.kasperovich.service;

import com.kasperovich.dao.AcademicPeriodDao;
import com.kasperovich.dao.impl.AcademicPeriodDaoImpl;
import com.kasperovich.database.HibernateUtil;
import com.kasperovich.dto.scholarship.AcademicPeriodDTO;
import com.kasperovich.entities.AcademicPeriod;
import com.kasperovich.utils.DTOConverter;
import com.kasperovich.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import jakarta.persistence.Query;

import java.time.LocalDate;
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

    /**
     * Creates a new academic period.
     *
     * @param periodDTO the academic period DTO to create
     * @return the created academic period DTO with ID, or null if creation failed
     * @throws IllegalArgumentException if the period data is invalid
     */
    public AcademicPeriodDTO createAcademicPeriod(AcademicPeriodDTO periodDTO) {
        validateAcademicPeriod(periodDTO);

        try {
            // Check if a period with the same name already exists
            AcademicPeriod existingPeriod = academicPeriodDao.findByName(periodDTO.getName());
            if (existingPeriod != null) {
                throw new IllegalArgumentException("Academic period with this name already exists");
            }

            AcademicPeriod period = new AcademicPeriod();
            period.setName(periodDTO.getName());
            period.setType(periodDTO.getType());
            period.setStartDate(periodDTO.getStartDate());
            period.setEndDate(periodDTO.getEndDate());
            // The active status is derived from start and end dates, not set directly

            AcademicPeriod savedPeriod = academicPeriodDao.save(period);
            logger.info("Created new academic period with ID: {}", savedPeriod.getId());

            return dtoConverter.convertToDTO(savedPeriod);
        } catch (IllegalArgumentException e) {
            logger.error("Error creating academic period: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error creating academic period", e);
            return null;
        }
    }

    /**
     * Updates an existing academic period.
     *
     * @param periodDTO the academic period DTO to update
     * @return the updated academic period DTO, or null if update failed
     * @throws IllegalArgumentException if the period data is invalid or the period doesn't exist
     */
    public AcademicPeriodDTO updateAcademicPeriod(AcademicPeriodDTO periodDTO) {
        if (periodDTO.getId() == null) {
            throw new IllegalArgumentException("Academic period ID cannot be null for update");
        }

        validateAcademicPeriod(periodDTO);

        try {
            // Check if the period exists
            AcademicPeriod existingPeriod = academicPeriodDao.findById(periodDTO.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Academic period not found"));

            // Check if the name is being changed and if it conflicts with another period
            if (!existingPeriod.getName().equals(periodDTO.getName())) {
                AcademicPeriod periodWithSameName = academicPeriodDao.findByName(periodDTO.getName());
                if (periodWithSameName != null && !periodWithSameName.getId().equals(periodDTO.getId())) {
                    throw new IllegalArgumentException("Another academic period with this name already exists");
                }
            }

            existingPeriod.setName(periodDTO.getName());
            existingPeriod.setType(periodDTO.getType());
            existingPeriod.setStartDate(periodDTO.getStartDate());
            existingPeriod.setEndDate(periodDTO.getEndDate());
            // The active status is derived from start and end dates, not set directly

            AcademicPeriod updatedPeriod = academicPeriodDao.save(existingPeriod);
            logger.info("Updated academic period with ID: {}", updatedPeriod.getId());

            return dtoConverter.convertToDTO(updatedPeriod);
        } catch (IllegalArgumentException e) {
            logger.error("Error updating academic period: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error updating academic period", e);
            return null;
        }
    }

    /**
     * Updates the status of an academic period by adjusting its dates.
     * Since active status is derived from dates, this method will:
     * - If activating: Set dates to include current date
     * - If deactivating: Set dates to a past period
     *
     * @param id the ID of the academic period
     * @param active whether the period should be active
     * @return the updated academic period DTO
     * @throws IllegalArgumentException if the period doesn't exist
     */
    public AcademicPeriodDTO updateAcademicPeriodStatus(Long id, boolean active) {
        try {
            // Check if the period exists
            AcademicPeriod existingPeriod = academicPeriodDao.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Academic period not found"));

            LocalDate now = LocalDate.now();
            
            if (active && !existingPeriod.isActive()) {
                // To activate: Adjust dates to include current date
                // If end date is in the past, set it to 6 months from now
                if (existingPeriod.getEndDate().isBefore(now)) {
                    existingPeriod.setEndDate(now.plusMonths(6));
                }
                
                // If start date is in the future, set it to today
                if (existingPeriod.getStartDate().isAfter(now)) {
                    existingPeriod.setStartDate(now);
                }
            } else if (!active && existingPeriod.isActive()) {
                // To deactivate: Set end date to yesterday
                existingPeriod.setEndDate(now.minusDays(1));
            }

            AcademicPeriod updatedPeriod = academicPeriodDao.save(existingPeriod);
            logger.info("Updated academic period dates to change active status: ID={}, active={}", id, updatedPeriod.isActive());

            return dtoConverter.convertToDTO(updatedPeriod);
        } catch (IllegalArgumentException e) {
            logger.error("Error updating academic period status: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error updating academic period status", e);
            return null;
        }
    }

    /**
     * Deletes an academic period.
     *
     * @param id the ID of the academic period to delete
     * @return true if deleted successfully, false otherwise
     * @throws IllegalArgumentException if the period doesn't exist or cannot be deleted
     */
    public boolean deleteAcademicPeriod(Long id) {
        try {
            // Check if the period exists
            AcademicPeriod existingPeriod = academicPeriodDao.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Academic period not found"));

            // Check if the period is used in any scholarship applications
            if (isAcademicPeriodInUse(id)) {
                throw new IllegalArgumentException("Cannot delete academic period because it is referenced by scholarship applications");
            }

            try {
                academicPeriodDao.delete(existingPeriod);
                logger.info("Deleted academic period with ID: {}", id);
                return true;
            } catch (Exception e) {
                logger.warn("Failed to delete academic period with ID: {}", id, e);
                return false;
            }
        } catch (IllegalArgumentException e) {
            logger.error("Error deleting academic period: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting academic period", e);
            return false;
        }
    }

    /**
     * Checks if an academic period is in use by any scholarship applications.
     *
     * @param periodId the ID of the academic period to check
     * @return true if the period is in use, false otherwise
     */
    private boolean isAcademicPeriodInUse(Long periodId) {
        try {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Query query = session.createQuery(
                "SELECT COUNT(a) FROM ScholarshipApplication a WHERE a.period.id = :periodId"
            );
            query.setParameter("periodId", periodId);
            Long count = (Long) query.getSingleResult();
            session.close();
            
            return count > 0;
        } catch (Exception e) {
            logger.error("Error checking if academic period is in use", e);
            // If we can't determine if it's in use, assume it is to be safe
            return true;
        }
    }

    /**
     * Validates academic period data.
     *
     * @param periodDTO the academic period DTO to validate
     * @throws IllegalArgumentException if the period data is invalid
     */
    private void validateAcademicPeriod(AcademicPeriodDTO periodDTO) {
        if (periodDTO == null) {
            throw new IllegalArgumentException("Academic period cannot be null");
        }

        if (periodDTO.getName() == null || periodDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Academic period name cannot be empty");
        }

        if (periodDTO.getType() == null || periodDTO.getType().trim().isEmpty()) {
            throw new IllegalArgumentException("Academic period type cannot be empty");
        }

        if (periodDTO.getStartDate() == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }

        if (periodDTO.getEndDate() == null) {
            throw new IllegalArgumentException("End date cannot be null");
        }

        if (periodDTO.getEndDate().isBefore(periodDTO.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
    }
}
