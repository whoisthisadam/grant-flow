package com.kasperovich.dto.scholarship;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Object for ScholarshipProgram entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ScholarshipProgramDTO implements Serializable {
    
    private Long id;
    private String name;
    private String description;
    private BigDecimal fundingAmount;
    private BigDecimal minGpa;
    private Long createdById;
    private String createdByUsername;
    private boolean active;
    private LocalDate applicationDeadline;
    private boolean acceptingApplications;
    private BigDecimal allocatedAmount;
    private BigDecimal usedAmount;
    private BigDecimal remainingAmount;
    private boolean hasFundsAvailable;
}
