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
 * Entity representing a scholarship budget.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"createdBy"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "budgets")
public class Budget implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "fiscal_year", nullable = false)
    private Integer fiscalYear;
    
    @Column(name = "fiscal_period")
    private String fiscalPeriod;
    
    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;
    
    @Column(name = "allocated_amount")
    private BigDecimal allocatedAmount = BigDecimal.ZERO;
    
    @Column(name = "remaining_amount")
    private BigDecimal remainingAmount;
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    
    @Column(length = 500)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BudgetStatus status = BudgetStatus.DRAFT;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Checks if the budget is active.
     *
     * @return true if the budget is active
     */
    public boolean isActive() {
        return status == BudgetStatus.ACTIVE;
    }
    
    /**
     * Checks if the budget has sufficient funds for the specified amount.
     *
     * @param amount the amount to check
     * @return true if the budget has sufficient funds
     */
    public boolean hasSufficientFunds(BigDecimal amount) {
        if (amount == null || remainingAmount == null) {
            return false;
        }
        return remainingAmount.compareTo(amount) >= 0;
    }
    
    /**
     * Calculates the remaining amount based on total and allocated amounts.
     */
    public void calculateRemainingAmount() {
        if (totalAmount == null) {
            totalAmount = BigDecimal.ZERO;
        }
        
        if (allocatedAmount == null) {
            allocatedAmount = BigDecimal.ZERO;
        }
        
        remainingAmount = totalAmount.subtract(allocatedAmount);
    }
}
