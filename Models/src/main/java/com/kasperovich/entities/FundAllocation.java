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
import java.time.LocalDateTime;

/**
 * Entity representing a fund allocation from a budget to a scholarship program.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"budget", "program", "allocatedBy"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "fund_allocations")
public class FundAllocation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_id", nullable = false)
    private Budget budget;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private ScholarshipProgram program;
    
    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "previous_amount", precision = 12, scale = 2)
    private BigDecimal previousAmount = BigDecimal.ZERO;
    
    @Column(name = "allocation_date", nullable = false)
    private LocalDateTime allocationDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "allocated_by", nullable = false)
    private User allocatedBy;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AllocationStatus status = AllocationStatus.APPROVED;
    
    @Column(length = 500)
    private String notes;
}
