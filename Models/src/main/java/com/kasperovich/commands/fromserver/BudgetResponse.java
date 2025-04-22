package com.kasperovich.commands.fromserver;

import com.kasperovich.dto.scholarship.BudgetDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Response containing a single budget.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetResponse implements Serializable {
    private BudgetDTO budget;
    private String errorMessage;
    private boolean success;
    
    /**
     * Creates a successful response with a budget.
     *
     * @param budget the budget
     */
    public BudgetResponse(BudgetDTO budget) {
        this.budget = budget;
        this.success = true;
    }
    
    /**
     * Creates an error response with an error message.
     *
     * @param errorMessage the error message
     */
    public BudgetResponse(String errorMessage) {
        this.errorMessage = errorMessage;
        this.success = false;
    }
}
