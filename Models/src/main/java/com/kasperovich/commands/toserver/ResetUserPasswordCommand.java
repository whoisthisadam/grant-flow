package com.kasperovich.commands.toserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Command to reset a user's password.
 * Used by administrators to reset passwords for users.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetUserPasswordCommand implements Serializable {
    private String authToken;
    private Long userId;
    private String newPassword;
}
