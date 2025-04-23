package com.kasperovich.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for application status report data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationStatusDTO implements Serializable {
    private String programName;
    private String periodName;
    private Integer pendingCount;
    private Integer approvedCount;
    private Integer rejectedCount;
    private BigDecimal totalAmount; // Total amount for approved applications
}
