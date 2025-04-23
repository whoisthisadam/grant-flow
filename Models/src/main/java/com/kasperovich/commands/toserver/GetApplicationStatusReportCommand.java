package com.kasperovich.commands.toserver;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * Command to request application status report
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetApplicationStatusReportCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    private Long programId; // Can be null for all programs
    private Long periodId;  // Can be null for all periods
}
