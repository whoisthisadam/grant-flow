package com.kasperovich.dao;

import com.kasperovich.entities.AcademicPeriod;

import java.time.LocalDate;
import java.util.List;

/**
 * Data Access Object interface for AcademicPeriod entity.
 */
public interface AcademicPeriodDao extends BaseDao<AcademicPeriod, Long> {
    
    /**
     * Finds an academic period by its name.
     *
     * @param name the name of the academic period
     * @return the academic period, or null if not found
     */
    AcademicPeriod findByName(String name);
    
    /**
     * Finds all academic periods that are currently active.
     *
     * @return a list of active academic periods
     */
    List<AcademicPeriod> findActiveAcademicPeriods();
    
    /**
     * Finds all academic periods that include the specified date.
     *
     * @param date the date to check
     * @return a list of academic periods that include the specified date
     */
    List<AcademicPeriod> findByDate(LocalDate date);
    
    /**
     * Finds all academic periods of a specific type.
     *
     * @param type the type of academic period (e.g., "SEMESTER", "YEAR")
     * @return a list of academic periods of the specified type
     */
    List<AcademicPeriod> findByType(String type);
}
