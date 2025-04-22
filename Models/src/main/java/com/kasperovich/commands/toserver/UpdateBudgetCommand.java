package com.kasperovich.commands.toserver;

import com.kasperovich.entities.BudgetStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Command for updating an existing budget.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBudgetCommand implements Serializable {
    private Long id;
    private Integer fiscalYear;
    private String fiscalPeriod;
    private BigDecimal totalAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private BudgetStatus status;
}
