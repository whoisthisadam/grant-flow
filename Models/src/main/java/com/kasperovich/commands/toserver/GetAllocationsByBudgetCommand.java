package com.kasperovich.commands.toserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Command for getting fund allocations by budget.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetAllocationsByBudgetCommand implements Serializable {
    private Long budgetId;
}
