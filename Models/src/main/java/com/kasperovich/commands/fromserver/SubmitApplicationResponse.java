package com.kasperovich.commands.fromserver;

import com.kasperovich.dto.scholarship.ScholarshipApplicationDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Response for a scholarship application submission.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitApplicationResponse implements Serializable {
    
    private ResponseFromServer responseType;
    private ScholarshipApplicationDTO application;
    private String message;
    
    public SubmitApplicationResponse(ScholarshipApplicationDTO application) {
        this.responseType = ResponseFromServer.APPLICATION_SUBMITTED;
        this.application = application;
        this.message = "Application submitted successfully";
    }
    
    public SubmitApplicationResponse(String errorMessage) {
        this.responseType = ResponseFromServer.ERROR;
        this.message = errorMessage;
    }
    
    /**
     * Gets the error message if there was an error.
     * 
     * @return the error message, or null if there was no error
     */
    public String getErrorMessage() {
        return message;
    }
}
