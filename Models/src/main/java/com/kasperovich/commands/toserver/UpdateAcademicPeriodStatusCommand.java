package com.kasperovich.commands.toserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Command to update the active status of an academic period.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAcademicPeriodStatusCommand implements Serializable {
    private Long periodId;
    private boolean active;
}
