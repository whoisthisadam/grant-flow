package com.kasperovich.commands.toserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Command to update a user's active status.
 * Used by administrators to activate or deactivate user accounts.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserStatusCommand implements Serializable {
    private Long userId;
    private boolean active;
}
