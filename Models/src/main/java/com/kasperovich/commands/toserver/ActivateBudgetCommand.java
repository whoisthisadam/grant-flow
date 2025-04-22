package com.kasperovich.commands.toserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Command for activating a budget.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivateBudgetCommand implements Serializable {
    private Long budgetId;
}
