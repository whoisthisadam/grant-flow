package com.kasperovich.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entity representing a scholarship application submitted by a student.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"applicant", "program", "period", "reviewer"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "scholarship_applications")
public class ScholarshipApplication implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    private User applicant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private ScholarshipProgram program;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "period_id", nullable = false)
    private AcademicPeriod period;

    @Column(name = "submission_date", nullable = false)
    private LocalDateTime submissionDate;

    @Column(nullable = false, length = 20)
    private String status; // PENDING, APPROVED, REJECTED

    @Column(name = "decision_date")
    private LocalDateTime decisionDate;

    @Column(name = "decision_comments", length = 1000)
    private String decisionComments;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;
    
    @Column(name = "additional_info", length = 2000)
    private String additionalInfo;
    
    /**
     * Approves the application with the given reviewer and comments.
     *
     * @param reviewer the user who approved the application
     * @param comments comments about the decision
     */
    public void approve(User reviewer, String comments) {
        this.status = "APPROVED";
        this.reviewer = reviewer;
        this.decisionComments = comments;
        this.decisionDate = LocalDateTime.now();
    }

    /**
     * Rejects the application with the given reviewer and comments.
     *
     * @param reviewer the user who rejected the application
     * @param comments comments about the decision
     */
    public void reject(User reviewer, String comments) {
        this.status = "REJECTED";
        this.reviewer = reviewer;
        this.decisionComments = comments;
        this.decisionDate = LocalDateTime.now();
    }

    /**
     * Checks if the application is pending.
     *
     * @return true if the application is pending, false otherwise
     */
    public boolean isPending() {
        return "PENDING".equals(status);
    }

    /**
     * Checks if the application is approved.
     *
     * @return true if the application is approved, false otherwise
     */
    public boolean isApproved() {
        return "APPROVED".equals(status);
    }

    /**
     * Checks if the application is rejected.
     *
     * @return true if the application is rejected, false otherwise
     */
    public boolean isRejected() {
        return "REJECTED".equals(status);
    }
}
