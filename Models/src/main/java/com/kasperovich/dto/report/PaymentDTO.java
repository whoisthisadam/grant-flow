package com.kasperovich.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for payment data in academic performance report
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO implements Serializable {
    private Long id;
    private String programName;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private String status;
    private String referenceNumber;
}
