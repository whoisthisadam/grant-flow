package com.kasperovich.commands.toserver;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Command to update an existing scholarship program.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateScholarshipProgramCommand implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String name;
    private String description;
    private BigDecimal fundingAmount;
    private BigDecimal minGpa;
    private LocalDate applicationDeadline;
    private boolean active;
}
