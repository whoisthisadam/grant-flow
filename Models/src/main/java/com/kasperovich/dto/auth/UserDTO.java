package com.kasperovich.dto.auth;

import com.kasperovich.entities.User;
import com.kasperovich.entities.UserRole;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * Data transfer object for user information.
 */
@Setter
@Getter
public class UserDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * -- GETTER --
     *  Gets the user ID.
     *
     *
     * -- SETTER --
     *  Sets the user ID.
     *
     @return the user ID
      * @param id the user ID
     */
    private Long id;
    /**
     * -- GETTER --
     *  Gets the username.
     *
     *
     * -- SETTER --
     *  Sets the username.
     *
     @return the username
      * @param username the username
     */
    private String username;
    /**
     * -- GETTER --
     *  Gets the email address.
     *
     *
     * -- SETTER --
     *  Sets the email address.
     *
     @return the email address
      * @param email the email address
     */
    private String email;
    /**
     * -- GETTER --
     *  Gets the first name.
     *
     *
     * -- SETTER --
     *  Sets the first name.
     *
     @return the first name
      * @param firstName the first name
     */
    private String firstName;
    /**
     * -- GETTER --
     *  Gets the last name.
     *
     *
     * -- SETTER --
     *  Sets the last name.
     *
     @return the last name
      * @param lastName the last name
     */
    private String lastName;
    /**
     * -- GETTER --
     *  Gets the user role.
     *
     *
     * -- SETTER --
     *  Sets the user role.
     *
     @return the user role
      * @param role the user role
     */
    private String role;
    
    /**
     * Default constructor for serialization.
     */
    public UserDTO() {
    }
    
    /**
     * Creates a new user DTO with the specified details.
     *
     * @param id the user ID
     * @param username the username
     * @param email the email address
     * @param firstName the first name
     * @param lastName the last name
     * @param role the user role
     */
    public UserDTO(Long id, String username, String email, 
                   String firstName, String lastName, String role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }

    /**
     * Converts this DTO to a User entity.
     *
     * @return the User entity
     */
    public User toEntity() {
        User user = new User();
        user.setId(this.id);
        user.setUsername(this.username);
        user.setEmail(this.email);
        user.setFirstName(this.firstName);
        user.setLastName(this.lastName);
        
        try {
            user.setRole(UserRole.valueOf(this.role));
        } catch (IllegalArgumentException e) {
            // Default to STUDENT if role is invalid
            user.setRole(UserRole.STUDENT);
        }
        
        return user;
    }
}
