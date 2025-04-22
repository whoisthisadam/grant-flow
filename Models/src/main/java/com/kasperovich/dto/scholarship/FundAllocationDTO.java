package com.kasperovich.dto.scholarship;

import com.kasperovich.entities.AllocationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for FundAllocation entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundAllocationDTO implements Serializable {
    private Long id;
    private Long budgetId;
    private String budgetFiscalYear;
    private String budgetFiscalPeriod;
    private Long programId;
    private String programName;
    private BigDecimal amount;
    private BigDecimal previousAmount;
    private LocalDateTime allocationDate;
    private Long allocatedById;
    private String allocatedByName;
    private AllocationStatus status;
    private String notes;
}
