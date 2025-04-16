package com.kasperovich.commands.toserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Data object for requesting a list of academic periods from the server.
 * To be used with CommandWrapper and Command.GET_AVAILABLE_SCHOLARSHIPS.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetAcademicPeriodsCommand implements Serializable {
    
    private boolean activeOnly = true;
    private String type;
    
    public GetAcademicPeriodsCommand(boolean activeOnly) {
        this.activeOnly = activeOnly;
    }
}
