package com.kasperovich.commands.toserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Command to delete an academic period.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteAcademicPeriodCommand implements Serializable {
    private Long periodId;
}
