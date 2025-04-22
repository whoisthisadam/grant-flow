package com.kasperovich.commands.fromserver;

import com.kasperovich.dto.scholarship.BudgetDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Response containing a list of budgets.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetsResponse implements Serializable {
    private List<BudgetDTO> budgets;
    private String errorMessage;
    private boolean success;
    
    /**
     * Creates a successful response with a list of budgets.
     *
     * @param budgets the list of budgets
     */
    public BudgetsResponse(List<BudgetDTO> budgets) {
        this.budgets = budgets;
        this.success = true;
    }
    
    /**
     * Creates an error response with an error message.
     *
     * @param errorMessage the error message
     */
    public BudgetsResponse(String errorMessage) {
        this.errorMessage = errorMessage;
        this.success = false;
    }
}
