package com.kasperovich.commands.fromserver;

import com.kasperovich.dto.scholarship.FundAllocationDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Response containing a single fund allocation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FundAllocationResponse implements Serializable {
    private FundAllocationDTO allocation;
    private String errorMessage;
    private boolean success;
    
    /**
     * Creates a successful response with a fund allocation.
     *
     * @param allocation the fund allocation
     */
    public FundAllocationResponse(FundAllocationDTO allocation) {
        this.allocation = allocation;
        this.success = true;
    }
    
    /**
     * Creates an error response with an error message.
     *
     * @param errorMessage the error message
     */
    public FundAllocationResponse(String errorMessage) {
        this.errorMessage = errorMessage;
        this.success = false;
    }
}
