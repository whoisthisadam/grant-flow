package com.kasperovich.dto.scholarship;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for ScholarshipApplication entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ScholarshipApplicationDTO implements Serializable {
    
    private Long id;
    private Long applicantId;
    private String applicantUsername;
    private String applicantFullName;
    private Long programId;
    private String programName;
    private Long periodId;
    private String periodName;
    private LocalDateTime submissionDate;
    private String status;
    private LocalDateTime decisionDate;
    private String decisionComments;
    private Long reviewerId;
    private String reviewerUsername;
}
