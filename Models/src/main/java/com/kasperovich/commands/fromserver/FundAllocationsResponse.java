package com.kasperovich.commands.fromserver;

import com.kasperovich.dto.scholarship.FundAllocationDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Response containing a list of fund allocations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FundAllocationsResponse implements Serializable {
    private List<FundAllocationDTO> allocations;
    private String errorMessage;
    private boolean success;
    
    /**
     * Creates a successful response with a list of fund allocations.
     *
     * @param allocations the list of fund allocations
     */
    public FundAllocationsResponse(List<FundAllocationDTO> allocations) {
        this.allocations = allocations;
        this.success = true;
    }
    
    /**
     * Creates an error response with an error message.
     *
     * @param errorMessage the error message
     */
    public FundAllocationsResponse(String errorMessage) {
        this.errorMessage = errorMessage;
        this.success = false;
    }
}
