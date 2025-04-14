package com.kasperovich.service;

import com.kasperovich.dao.UserDao;
import com.kasperovich.dao.impl.UserDaoImpl;
import com.kasperovich.dto.auth.LoginRequest;
import com.kasperovich.dto.auth.RegistrationRequest;
import com.kasperovich.dto.auth.UserDTO;
import com.kasperovich.entities.User;
import com.kasperovich.entities.UserRole;
import com.kasperovich.security.PasswordUtils;
import com.kasperovich.security.TokenManager;
import com.kasperovich.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service for handling user authentication, registration, and session management.
 */
public class AuthenticationService {
    private static final Logger logger = LoggerUtil.getLogger(AuthenticationService.class);
    private static final AuthenticationService instance = new AuthenticationService();
    
    private final UserDao userDao;
    private final TokenManager tokenManager;
    
    /**
     * Private constructor to enforce singleton pattern.
     */
    private AuthenticationService() {
        this.userDao = new UserDaoImpl();
        this.tokenManager = TokenManager.getInstance();
    }
    
    /**
     * Gets the singleton instance.
     *
     * @return the authentication service instance
     */
    public static AuthenticationService getInstance() {
        return instance;
    }
    
    /**
     * Registers a new user.
     *
     * @param request the registration request
     * @return the registered user DTO and auth token if successful, null if registration failed
     */
    public UserDTO register(RegistrationRequest request) {
        logger.info("Processing registration request for username: {}", request.getUsername());
        
        // Check if username or email already exists
        if (userDao.existsByUsername(request.getUsername())) {
            logger.warn("Registration failed: Username already exists: {}", request.getUsername());
            return null;
        }
        
        if (userDao.existsByEmail(request.getEmail())) {
            logger.warn("Registration failed: Email already exists: {}", request.getEmail());
            return null;
        }
        
        try {
            // Create new user
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPasswordHash(PasswordUtils.hashPassword(request.getPassword()));
            user.setEmail(request.getEmail());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            
            // Set role
            try {
                user.setRole(UserRole.valueOf(request.getRole().toUpperCase()));
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid role: {}. Defaulting to STUDENT.", request.getRole());
                user.setRole(UserRole.STUDENT);
            }
            
            user.setCreatedAt(LocalDateTime.now());
            user.setLastLogin(LocalDateTime.now());
            user.setActive(true);
            
            // Save user
            User savedUser = userDao.save(user);
            logger.info("User registered successfully: {}", savedUser.getUsername());
            
            // Create and return user DTO
            return convertToDTO(savedUser);
        } catch (Exception e) {
            logger.error("Error registering user", e);
            return null;
        }
    }
    
    /**
     * Authenticates a user and returns a token.
     *
     * @param request the login request
     * @return the authenticated user DTO and auth token if successful, null if authentication failed
     */
    public UserDTO login(LoginRequest request) {
        logger.info("Processing login request for username: {}", request.getUsername());
        
        try {
            // Find user by username
            Optional<User> userOpt = userDao.findByUsername(request.getUsername());
            
            if (userOpt.isEmpty()) {
                logger.warn("Login failed: User not found: {}", request.getUsername());
                return null;
            }
            
            User user = userOpt.get();
            
            // Verify password
            if (!PasswordUtils.verifyPassword(request.getPassword(), user.getPasswordHash())) {
                logger.warn("Login failed: Invalid password for user: {}", request.getUsername());
                return null;
            }
            
            // Check if user is active
            if (!user.isActive()) {
                logger.warn("Login failed: User is not active: {}", request.getUsername());
                return null;
            }
            
            // Update last login time
            userDao.updateLastLogin(user.getId());
            
            logger.info("User logged in successfully: {}", user.getUsername());
            
            // Create and return user DTO
            return convertToDTO(user);
        } catch (Exception e) {
            logger.error("Error logging in user", e);
            return null;
        }
    }
    
    /**
     * Validates an authentication token.
     *
     * @param token the token to validate
     * @return the user ID associated with the token, or null if the token is invalid
     */
    public Long validateToken(String token) {
        return tokenManager.validateToken(token);
    }
    
    /**
     * Generates a new authentication token for the specified user.
     *
     * @param userId the user ID
     * @return the generated token
     */
    public String generateToken(Long userId) {
        return tokenManager.generateToken(userId);
    }
    
    /**
     * Logs out a user by invalidating their token.
     *
     * @param token the token to invalidate
     */
    public void logout(String token) {
        tokenManager.invalidateToken(token);
        logger.info("User logged out successfully");
    }
    
    /**
     * Gets a user by ID.
     *
     * @param userId the user ID
     * @return the user DTO if found, null otherwise
     */
    public UserDTO getUserById(Long userId) {
        logger.debug("Getting user by ID: {}", userId);
        
        Optional<User> userOpt = userDao.findById(userId);
        return userOpt.map(this::convertToDTO).orElse(null);
    }
    
    /**
     * Converts a User entity to a UserDTO.
     *
     * @param user the user entity
     * @return the user DTO
     */
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setRole(user.getRole().name());
        return dto;
    }
}
