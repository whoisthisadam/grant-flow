package com.kasperovich.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing a scholarship program.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"createdBy"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "scholarship_programs")
public class ScholarshipProgram implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "funding_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal fundingAmount;

    @Column(name = "min_gpa", precision = 3, scale = 2)
    private BigDecimal minGpa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "application_deadline")
    private LocalDate applicationDeadline;
    
    @Column(name = "allocated_amount", precision = 12, scale = 2)
    private BigDecimal allocatedAmount = BigDecimal.ZERO;
    
    @Column(name = "used_amount", precision = 12, scale = 2)
    private BigDecimal usedAmount = BigDecimal.ZERO;
    
    @Column(name = "remaining_amount", precision = 12, scale = 2)
    private BigDecimal remainingAmount = BigDecimal.ZERO;

    /**
     * Checks if applications are currently being accepted for this program.
     *
     * @return true if the program is active and the deadline has not passed
     */
    public boolean isAcceptingApplications() {
        if (!active) {
            return false;
        }
        
        if (applicationDeadline == null) {
            return true;
        }
        
        return !LocalDate.now().isAfter(applicationDeadline);
    }
    
    /**
     * Checks if the program has sufficient funds available.
     *
     * @param amount the amount to check
     * @return true if the program has sufficient funds
     */
    public boolean hasSufficientFunds(BigDecimal amount) {
        if (amount == null || remainingAmount == null) {
            return false;
        }
        return remainingAmount.compareTo(amount) >= 0;
    }
    
    /**
     * Recalculates the remaining amount based on allocated and used amounts.
     */
    public void calculateRemainingAmount() {
        if (allocatedAmount == null) {
            allocatedAmount = BigDecimal.ZERO;
        }
        
        if (usedAmount == null) {
            usedAmount = BigDecimal.ZERO;
        }
        
        remainingAmount = allocatedAmount.subtract(usedAmount);
    }
}
