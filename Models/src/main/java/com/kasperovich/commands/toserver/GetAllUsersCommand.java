package com.kasperovich.commands.toserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Command to request all users in the system.
 * Used by administrators for user management.
 */
@Data
@AllArgsConstructor
public class GetAllUsersCommand implements Serializable {
    // No fields needed as this is a simple request
    // Authentication is handled by the CommandWrapper
}
