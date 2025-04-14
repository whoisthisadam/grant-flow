package com.kasperovich.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Entity representing a grade received by a student for a course.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"student", "course", "academicPeriod"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "course_grades", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "course_id", "period_id"}))
public class CourseGrade implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "period_id", nullable = false)
    private AcademicPeriod academicPeriod;

    @Column(name = "grade_value", nullable = false)
    private Double gradeValue;

    @Column(name = "grade_letter", length = 2)
    private String gradeLetter;

    @Column(name = "completion_date")
    private LocalDate completionDate;

    @Column(name = "included_in_gpa")
    private boolean includedInGpa = true;

    @Column(length = 500)
    private String comments;

    /**
     * Calculates the letter grade based on the numeric grade value.
     * 
     * @param gradeValue the numeric grade value
     * @return the letter grade
     */
    private String calculateGradeLetter(Double gradeValue) {
        if (gradeValue == null) {
            return null;
        }
        
        if (gradeValue >= 4.0) return "A";
        if (gradeValue >= 3.7) return "A-";
        if (gradeValue >= 3.3) return "B+";
        if (gradeValue >= 3.0) return "B";
        if (gradeValue >= 2.7) return "B-";
        if (gradeValue >= 2.3) return "C+";
        if (gradeValue >= 2.0) return "C";
        if (gradeValue >= 1.7) return "C-";
        if (gradeValue >= 1.3) return "D+";
        if (gradeValue >= 1.0) return "D";
        return "F";
    }
    
    /**
     * Sets the grade value and automatically calculates the corresponding letter grade.
     *
     * @param gradeValue the numeric grade value
     */
    public void setGradeValue(Double gradeValue) {
        this.gradeValue = gradeValue;
        this.gradeLetter = calculateGradeLetter(gradeValue);
    }
}
