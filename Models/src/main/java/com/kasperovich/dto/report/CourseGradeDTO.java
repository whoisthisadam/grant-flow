package com.kasperovich.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO for course grade data in academic performance report
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseGradeDTO implements Serializable {
    private String courseCode;
    private String courseName;
    private Integer credits;
    private Double gradeValue;
    private String gradeLetter;
    private String academicPeriod;
    private LocalDate completionDate;
    private boolean includedInGpa;
}
