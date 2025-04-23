package com.kasperovich.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for user activity report data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserActivityDTO implements Serializable {
    private String month; // Format: YYYY-MM
    private Integer newUserCount;
    private Integer applicationCount;
}
