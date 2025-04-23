package com.kasperovich.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Data Transfer Object for admin dashboard activity entries.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityDTO {
    private LocalDateTime timestamp;
    private String type;
    private String details;
    
    /**
     * Gets the formatted date string.
     * 
     * @return the formatted date
     */
    public String getDate() {
        return timestamp != null ? timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "";
    }
}
