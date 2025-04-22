package com.kasperovich.commands.fromserver;

import com.kasperovich.dto.scholarship.AcademicPeriodDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Response containing a single academic period.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcademicPeriodResponse implements Serializable {
    private AcademicPeriodDTO period;
}
