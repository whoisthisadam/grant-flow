package com.kasperovich.dao;

import com.kasperovich.entities.User;

import java.util.Optional;

/**
 * Data Access Object interface for User entities.
 */
public interface UserDao extends BaseDao<User, Long> {
    
    /**
     * Finds a user by username.
     *
     * @param username the username to search for
     * @return an Optional containing the found user, or empty if not found
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Finds a user by email.
     *
     * @param email the email to search for
     * @return an Optional containing the found user, or empty if not found
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Checks if a username already exists.
     *
     * @param username the username to check
     * @return true if the username exists, false otherwise
     */
    boolean existsByUsername(String username);
    
    /**
     * Checks if an email already exists.
     *
     * @param email the email to check
     * @return true if the email exists, false otherwise
     */
    boolean existsByEmail(String email);
    
    /**
     * Updates the last login time for a user.
     *
     * @param userId the ID of the user to update
     */
    void updateLastLogin(Long userId);
}
