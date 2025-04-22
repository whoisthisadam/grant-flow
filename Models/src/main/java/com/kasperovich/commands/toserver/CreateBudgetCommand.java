package com.kasperovich.commands.toserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Command for creating a new budget.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBudgetCommand implements Serializable {
    private Integer fiscalYear;
    private String fiscalPeriod;
    private BigDecimal totalAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
}
