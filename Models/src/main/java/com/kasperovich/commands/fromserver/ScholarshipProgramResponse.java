package com.kasperovich.commands.fromserver;

import com.kasperovich.dto.scholarship.ScholarshipProgramDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Response containing a single scholarship program.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScholarshipProgramResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private boolean success;
    private String message;
    private ScholarshipProgramDTO program;
}
