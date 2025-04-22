package com.kasperovich.commands.toserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Command for closing a budget.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CloseBudgetCommand implements Serializable {
    private Long budgetId;
}
