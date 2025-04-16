package com.kasperovich.dto.scholarship;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Data Transfer Object for AcademicPeriod entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AcademicPeriodDTO implements Serializable {
    
    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String type;
    private boolean active;
}
