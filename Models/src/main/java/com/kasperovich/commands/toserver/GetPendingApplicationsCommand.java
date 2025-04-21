package com.kasperovich.commands.toserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Command to request all pending scholarship applications.
 */
@Data
@AllArgsConstructor
public class GetPendingApplicationsCommand implements Serializable {
    // No fields needed as authentication is handled by CommandWrapper
}
