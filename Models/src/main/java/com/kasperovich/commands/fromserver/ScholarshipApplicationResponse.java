package com.kasperovich.commands.fromserver;

import com.kasperovich.dto.scholarship.ScholarshipApplicationDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Response for scholarship application submission.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScholarshipApplicationResponse implements Serializable {
    private boolean success;
    private String message;
    private ScholarshipApplicationDTO application;
}
