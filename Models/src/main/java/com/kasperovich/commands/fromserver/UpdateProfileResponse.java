package com.kasperovich.commands.fromserver;

import com.kasperovich.dto.auth.UserDTO;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Response for profile update operations.
 */
@Setter
@Getter
public class UpdateProfileResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean success;
    private String message;
    private UserDTO user;

    public UpdateProfileResponse() {
    }

    public UpdateProfileResponse(boolean success, String message, UserDTO user) {
        this.success = success;
        this.message = message;
        this.user = user;
    }

    @Override
    public String toString() {
        return "UpdateProfileResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", user=" + user +
                '}';
    }
}
