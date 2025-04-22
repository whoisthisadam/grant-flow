package com.kasperovich.dto.scholarship;

import com.kasperovich.entities.BudgetStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Budget entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetDTO implements Serializable {
    private Long id;
    private Integer fiscalYear;
    private String fiscalPeriod;
    private BigDecimal totalAmount;
    private BigDecimal allocatedAmount;
    private BigDecimal remainingAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private BudgetStatus status;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
