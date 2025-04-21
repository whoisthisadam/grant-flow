package com.kasperovich.commands.toserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Command to reject a scholarship application.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RejectApplicationCommand implements Serializable {
    private Long applicationId;
    private String comments;
}
