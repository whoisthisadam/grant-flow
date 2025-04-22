package com.kasperovich.commands.fromserver;

import com.kasperovich.dto.scholarship.AcademicPeriodDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Response containing a list of academic periods.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcademicPeriodsResponse implements Serializable {
    private ResponseFromServer responseType;
    private List<AcademicPeriodDTO> periods;
    private String message;

    public AcademicPeriodsResponse(List<AcademicPeriodDTO> periods) {
        this.responseType = ResponseFromServer.DATA_FOUND;
        this.periods = periods;
    }

    public AcademicPeriodsResponse(String errorMessage) {
        this.responseType = ResponseFromServer.ERROR;
        this.message = errorMessage;
    }
}
