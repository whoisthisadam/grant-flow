package com.kasperovich.commands.toserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Command for allocating funds to a scholarship program.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AllocateFundsCommand implements Serializable {
    private Long budgetId;
    private Long programId;
    private BigDecimal amount;
    private String notes;
}
