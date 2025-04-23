package com.kasperovich.commands.toserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Command to get an academic performance report.
 */
@Data
@AllArgsConstructor
public class GetAcademicPerformanceReportCommand implements Serializable {
    // For this simplified implementation, we don't need any parameters
    // The report will be generated for the authenticated user
}
