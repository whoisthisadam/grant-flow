package com.kasperovich.commands.fromserver;

import com.kasperovich.dto.auth.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Response for updating a user's status.
 * Contains the updated user information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserStatusResponse implements Serializable {
    private boolean success;
    private String message;
    private UserDTO user;
}
