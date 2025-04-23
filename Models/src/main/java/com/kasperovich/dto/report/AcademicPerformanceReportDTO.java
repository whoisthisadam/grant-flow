package com.kasperovich.dto.report;

import com.kasperovich.dto.auth.UserDTO;
import com.kasperovich.dto.scholarship.ScholarshipApplicationDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for academic performance report data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcademicPerformanceReportDTO implements Serializable {
    // Student information
    private UserDTO user;
    private String studentId;
    private String major;
    private String department;
    private Integer academicYear;
    private LocalDate enrollmentDate;
    private LocalDate expectedGraduationDate;
    private Double currentGpa;
    
    // Course performance
    private List<CourseGradeDTO> courseGrades;
    
    // Scholarship history
    private List<ScholarshipApplicationDTO> scholarshipApplications;
    
    // Payment information
    private List<PaymentDTO> payments;
    
    // Summary statistics
    private Integer totalCreditsCompleted;
    private Integer totalCreditsInProgress;
    private Double averageGpa;
    private Integer scholarshipsApplied;
    private Integer scholarshipsApproved;
    private BigDecimal totalScholarshipAmount;
}
