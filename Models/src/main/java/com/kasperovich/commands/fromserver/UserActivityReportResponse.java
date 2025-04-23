package com.kasperovich.commands.fromserver;

import com.kasperovich.dto.report.UserActivityDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Response containing user activity report data
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityReportResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    private List<UserActivityDTO> reportData;
}
