package com.kasperovich.commands.fromserver;

import com.kasperovich.dto.scholarship.ScholarshipApplicationDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Response for application review operations (approve/reject).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationReviewResponse implements Serializable {
    private ScholarshipApplicationDTO application;
    private boolean success;
    private String errorMessage;
    
    /**
     * Constructs a successful response with the updated application.
     *
     * @param application the updated application
     */
    public ApplicationReviewResponse(ScholarshipApplicationDTO application) {
        this.application = application;
        this.success = true;
    }
    
    /**
     * Constructs a failed response with an error message.
     *
     * @param errorMessage the error message
     */
    public ApplicationReviewResponse(String errorMessage) {
        this.success = false;
        this.errorMessage = errorMessage;
    }
}
