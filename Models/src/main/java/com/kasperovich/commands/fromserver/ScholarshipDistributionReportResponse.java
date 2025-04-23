package com.kasperovich.commands.fromserver;

import com.kasperovich.dto.report.ScholarshipDistributionDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Response containing scholarship distribution report data
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScholarshipDistributionReportResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    private List<ScholarshipDistributionDTO> reportData;
}
