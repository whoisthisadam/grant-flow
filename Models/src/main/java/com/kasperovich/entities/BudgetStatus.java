package com.kasperovich.entities;

/**
 * Enum representing the status of a budget.
 */
public enum BudgetStatus {
    DRAFT,      // Initial state, budget is being prepared
    ACTIVE,     // Budget is currently active and can be used for allocations
    CLOSED,     // Budget is closed and no longer accepting allocations
    ARCHIVED    // Budget is archived for historical purposes
}
