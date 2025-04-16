package com.kasperovich.commands.toserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Data object for requesting a list of scholarship programs from the server.
 * To be used with CommandWrapper and Command.GET_AVAILABLE_SCHOLARSHIPS.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetScholarshipProgramsCommand implements Serializable {
    
    private boolean activeOnly = true;
    private boolean acceptingApplicationsOnly = true;
    
    public GetScholarshipProgramsCommand(boolean activeOnly) {
        this.activeOnly = activeOnly;
    }
}
