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
public class ScholarshipApplicationsResponse implements Serializable {
    
    private ResponseFromServer responseType;
    private List<ScholarshipApplicationDTO> applications;
    private String message;
    
    public ScholarshipApplicationsResponse(List<ScholarshipApplicationDTO> applications) {
        this.responseType = ResponseFromServer.DATA_FOUND;
        this.applications = applications;
    }
    
    public ScholarshipApplicationsResponse(String errorMessage) {
        this.responseType = ResponseFromServer.ERROR;
        this.message = errorMessage;
    }
}
