package com.kasperovich.commands.fromserver;

import com.kasperovich.dto.report.ApplicationStatusDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Response containing application status report data
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationStatusReportResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    private List<ApplicationStatusDTO> reportData;
}
