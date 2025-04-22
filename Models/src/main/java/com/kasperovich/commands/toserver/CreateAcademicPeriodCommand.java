package com.kasperovich.commands.toserver;

import com.kasperovich.dto.scholarship.AcademicPeriodDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Command to create a new academic period.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAcademicPeriodCommand implements Serializable {
    private AcademicPeriodDTO period;
}
