package com.kasperovich.commands.toserver;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Command to update user profile information.
 */
@Setter
@Getter
public class UpdateProfileCommand implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId; // ID of the user to update (used for admin updates)
    private String username;
    private String firstName;
    private String lastName;
    private String email;

    public UpdateProfileCommand() {
    }

    public UpdateProfileCommand(String username, String firstName, String lastName, String email) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }
    
    public UpdateProfileCommand(Long userId, String username, String firstName, String lastName, String email) {
        this.userId = userId;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    @Override
    public String toString() {
        return "UpdateProfileCommand{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
