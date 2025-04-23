package com.kasperovich.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for scholarship distribution report data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScholarshipDistributionDTO implements Serializable {
    private String programName;
    private BigDecimal totalAmount;
    private Integer applicationsCount;
    private Integer approvedCount;
    private Double approvalRate; // Percentage of approved applications
}
