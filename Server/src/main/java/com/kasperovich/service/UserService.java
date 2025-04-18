package com.kasperovich.service;

import com.kasperovich.dao.UserDao;
import com.kasperovich.dao.impl.UserDaoImpl;
import com.kasperovich.dto.auth.UserDTO;
import com.kasperovich.entities.User;
import com.kasperovich.entities.UserRole;
import com.kasperovich.utils.DTOConverter;
import com.kasperovich.utils.LoggerUtil;
import com.kasperovich.utils.PasswordUtils;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Service class for user-related operations.
 */
public class UserService {
    
    private static final Logger logger = LoggerUtil.getLogger(UserService.class);
    
    private final UserDao userDao;
    private final Map<String, Long> authTokens; // token -> userId
    private final DTOConverter dtoConverter;
    
    private static UserService instance;
    
    /**
     * Private constructor for singleton pattern.
     */
    public UserService() {
        this.userDao = new UserDaoImpl();
        this.authTokens = new HashMap<>();
        this.dtoConverter = new DTOConverter();
    }
    
    /**
     * Gets the singleton instance of the UserService.
     *
     * @return the UserService instance
     */
    public static synchronized UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }
    
    /**
     * Registers a new user.
     *
     * @param username the username
     * @param password the password
     * @param firstName the first name
     * @param lastName the last name
     * @param email the email
     * @return the created user, or null if registration failed
     */
    public User registerUser(String username, String password, String firstName, String lastName, String email) {
        logger.info("Registering new user with username: {}", username);
        
        // Check if username already exists
        if (userDao.findByUsername(username).isPresent()) {
            logger.warn("Username already exists: {}", username);
            return null;
        }
        
        // Hash the password
        String hashedPassword = PasswordUtils.hashPassword(password);
        
        // Create and save the user
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(hashedPassword);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setRole(UserRole.STUDENT);
        user.setActive(true);
        
        return userDao.save(user);
    }
    
    /**
     * Authenticates a user.
     *
     * @param username the username
     * @param password the password
     * @return an authentication token if successful, or null if authentication failed
     */
    public String authenticateUser(String username, String password) {
        logger.info("Authenticating user: {}", username);
        
        Optional<User> userOpt = userDao.findByUsername(username);
        if (userOpt.isEmpty()) {
            logger.warn("User not found: {}", username);
            return null;
        }
        
        User user = userOpt.get();
        
        // Check if the user is active
        if (!user.isActive()) {
            logger.warn("User is not active: {}", username);
            return null;
        }
        
        // Verify the password
        if (!PasswordUtils.verifyPassword(password, user.getPasswordHash())) {
            logger.warn("Invalid password for user: {}", username);
            return null;
        }
        
        // Generate and store an authentication token
        String token = generateAuthToken();
        authTokens.put(token, user.getId());
        
        logger.info("User authenticated successfully: {}", username);
        return token;
    }
    
    /**
     * Logs out a user.
     *
     * @param authToken the authentication token
     * @return true if logout was successful, false otherwise
     */
    public boolean logoutUser(String authToken) {
        logger.info("Logging out user with token: {}", authToken);
        
        if (authToken == null || !authTokens.containsKey(authToken)) {
            logger.warn("Invalid authentication token: {}", authToken);
            return false;
        }
        
        authTokens.remove(authToken);
        logger.info("User logged out successfully");
        return true;
    }
    
    /**
     * Gets a user by their authentication token.
     *
     * @param authToken the authentication token
     * @return the user, or null if the token is invalid
     */
    public User getUserByToken(String authToken) {
        logger.debug("Getting user by token: {}", authToken);
        
        if (authToken == null || !authTokens.containsKey(authToken)) {
            logger.warn("Invalid authentication token: {}", authToken);
            return null;
        }
        
        Long userId = authTokens.get(authToken);
        Optional<User> userOpt = userDao.findById(userId);
        
        if (userOpt.isEmpty()) {
            logger.warn("User not found for token: {}", authToken);
            authTokens.remove(authToken); // Clean up invalid token
            return null;
        }
        
        return userOpt.get();
    }
    
    /**
     * Gets a user by their ID.
     *
     * @param userId the user ID
     * @return the user, or null if not found
     */
    public User getUserById(Long userId) {
        logger.debug("Getting user by ID: {}", userId);
        
        Optional<User> userOpt = userDao.findById(userId);
        return userOpt.orElse(null);
    }
    
    /**
     * Gets a user by their username.
     *
     * @param username the username
     * @return the user, or null if not found
     */
    public User getUserByUsername(String username) {
        logger.debug("Getting user by username: {}", username);
        
        Optional<User> userOpt = userDao.findByUsername(username);
        return userOpt.orElse(null);
    }
    
    /**
     * Gets all users.
     *
     * @return a list of all users
     */
    public List<User> getAllUsers() {
        logger.debug("Getting all users");
        return userDao.findAll();
    }
    
    /**
     * Updates a user's profile with the provided information.
     * Only updates the fields that are not null.
     *
     * @param userId the user ID
     * @param username the new username, or null to keep current
     * @param firstName the new first name, or null to keep current
     * @param lastName the new last name, or null to keep current
     * @param email the new email, or null to keep current
     * @return the updated user DTO, or null if update failed
     */
    public UserDTO updateUserProfile(Long userId, String username, String firstName, String lastName, String email) {
        logger.info("Updating profile for user ID: {}", userId);
        
        Optional<User> userOpt = userDao.findById(userId);
        if (userOpt.isEmpty()) {
            logger.warn("User not found: {}", userId);
            return null;
        }
        
        User user = userOpt.get();
        
        // Only update fields that are not null
        if (username != null && !username.trim().isEmpty()) {
            // Check if the new username is already taken by another user
            Optional<User> existingUser = userDao.findByUsername(username);
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                logger.warn("Username already exists: {}", username);
                throw new IllegalArgumentException("Username already exists");
            }
            user.setUsername(username);
        }
        
        if (firstName != null) {
            user.setFirstName(firstName);
        }
        
        if (lastName != null) {
            user.setLastName(lastName);
        }
        
        if (email != null && !email.trim().isEmpty()) {
            user.setEmail(email);
        }
        
        User updatedUser = userDao.save(user);
        return dtoConverter.convertToDTO(updatedUser);
    }
    
    /**
     * Changes a user's password.
     *
     * @param userId the user ID
     * @param currentPassword the current password
     * @param newPassword the new password
     * @return true if the password was changed, false otherwise
     */
    public boolean changePassword(Long userId, String currentPassword, String newPassword) {
        logger.info("Changing password for user ID: {}", userId);
        
        Optional<User> userOpt = userDao.findById(userId);
        if (userOpt.isEmpty()) {
            logger.warn("User not found: {}", userId);
            return false;
        }
        
        User user = userOpt.get();
        
        // Verify the current password
        if (!PasswordUtils.verifyPassword(currentPassword, user.getPasswordHash())) {
            logger.warn("Current password is incorrect for user ID: {}", userId);
            return false;
        }
        
        // Hash the new password
        String hashedPassword = PasswordUtils.hashPassword(newPassword);
        user.setPasswordHash(hashedPassword);
        
        userDao.save(user);
        logger.info("Password changed successfully for user ID: {}", userId);
        return true;
    }
    
    /**
     * Checks if a user is an administrator.
     *
     * @param user the user to check
     * @return true if the user is an administrator, false otherwise
     */
    public boolean isAdmin(User user) {
        return user != null && UserRole.ADMIN == user.getRole();
    }
    
    /**
     * Generates a unique authentication token.
     *
     * @return a unique authentication token
     */
    private String generateAuthToken() {
        return UUID.randomUUID().toString();
    }
}
