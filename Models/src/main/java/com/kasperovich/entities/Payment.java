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
 * Entity representing a scholarship payment to a student.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"application", "approvedBy"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "payments")
public class Payment implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private ScholarshipApplication application;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @Column(nullable = false, length = 20)
    private String status; // PENDING, PROCESSED, FAILED

    @Column(name = "reference_number", unique = true, length = 50)
    private String referenceNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    @Column(name = "receipt_acknowledged")
    private boolean receiptAcknowledged = false;

    /**
     * Processes the payment with the given approver.
     *
     * @param approver the user who processed the payment
     */
    public void process(User approver) {
        this.status = "PROCESSED";
        this.approvedBy = approver;
        this.approvedDate = LocalDateTime.now();
    }

    /**
     * Marks the payment as failed.
     */
    public void markAsFailed() {
        this.status = "FAILED";
    }

    /**
     * Acknowledges receipt of the payment.
     */
    public void acknowledgeReceipt() {
        this.receiptAcknowledged = true;
    }

    /**
     * Generates a unique reference number for the payment.
     *
     * @return a unique reference number
     */
    private String generateReferenceNumber() {
        // Format: SCH-YYYYMMDD-RANDOM
        String datePart = LocalDateTime.now().toString().replaceAll("[^0-9]", "").substring(0, 8);
        String randomPart = String.valueOf((int) (Math.random() * 10000));
        return "SCH-" + datePart + "-" + randomPart;
    }
}
