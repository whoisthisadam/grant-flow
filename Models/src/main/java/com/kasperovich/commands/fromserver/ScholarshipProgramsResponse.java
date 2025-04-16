package com.kasperovich.commands.fromserver;

import com.kasperovich.dto.scholarship.ScholarshipProgramDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Response containing a list of scholarship programs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScholarshipProgramsResponse implements Serializable {
    
    private ResponseFromServer responseType;
    private List<ScholarshipProgramDTO> programs;
    private String message;
    
    public ScholarshipProgramsResponse(List<ScholarshipProgramDTO> programs) {
        this.responseType = ResponseFromServer.DATA_FOUND;
        this.programs = programs;
    }
    
    public ScholarshipProgramsResponse(String errorMessage) {
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
