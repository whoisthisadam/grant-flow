package com.kasperovich.commands.fromserver;

import com.kasperovich.dto.auth.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Response containing all users in the system.
 * Used for the user management feature.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetAllUsersResponse implements Serializable {
    private List<UserDTO> users;
}
