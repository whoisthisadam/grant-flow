package com.kasperovich.commands.fromserver;

import com.kasperovich.dto.report.AcademicPerformanceReportDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Response containing academic performance report data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcademicPerformanceReportResponse implements Serializable {
    private AcademicPerformanceReportDTO report;
}
