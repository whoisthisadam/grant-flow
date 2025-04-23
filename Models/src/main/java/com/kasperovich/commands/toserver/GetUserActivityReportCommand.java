package com.kasperovich.commands.toserver;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * Command to request user activity report
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetUserActivityReportCommand implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    private LocalDate startDate;
    private LocalDate endDate;
}
