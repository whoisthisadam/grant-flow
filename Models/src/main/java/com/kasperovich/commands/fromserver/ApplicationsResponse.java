package com.kasperovich.commands.fromserver;

import com.kasperovich.dto.scholarship.ScholarshipApplicationDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Response containing a list of scholarship applications.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationsResponse implements Serializable {
    private List<ScholarshipApplicationDTO> applications;
    private boolean success;
    private String errorMessage;
    
    /**
     * Constructs a successful response with applications.
     *
     * @param applications the list of applications
     */
    public ApplicationsResponse(List<ScholarshipApplicationDTO> applications) {
        this.applications = applications;
        this.success = true;
    }
    
    /**
     * Constructs a failed response with an error message.
     *
     * @param errorMessage the error message
     */
    public ApplicationsResponse(String errorMessage) {
        this.success = false;
        this.errorMessage = errorMessage;
    }
}
