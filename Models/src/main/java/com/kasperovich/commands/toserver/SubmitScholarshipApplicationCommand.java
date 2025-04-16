package com.kasperovich.commands.toserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Data object for submitting a scholarship application to the server.
 * To be used with CommandWrapper and Command.APPLY_FOR_SCHOLARSHIP.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitScholarshipApplicationCommand implements Serializable {
    
    private Long programId;
    private Long periodId;
    private String additionalInfo;
}
