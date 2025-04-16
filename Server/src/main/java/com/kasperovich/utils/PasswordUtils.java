package com.kasperovich.utils;

import org.apache.logging.log4j.Logger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for password hashing and verification.
 */
public class PasswordUtils {
    
    private static final Logger logger = LoggerUtil.getLogger(PasswordUtils.class);
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16;
    private static final String DELIMITER = ":";
    
    /**
     * Hashes a password using SHA-256 with a random salt.
     *
     * @param password the password to hash
     * @return the hashed password with salt
     */
    public static String hashPassword(String password) {
        try {
            // Generate a random salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            
            // Hash the password with the salt
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());
            
            // Encode the salt and hashed password as Base64
            String encodedSalt = Base64.getEncoder().encodeToString(salt);
            String encodedHash = Base64.getEncoder().encodeToString(hashedPassword);
            
            // Combine the salt and hash with a delimiter
            return encodedSalt + DELIMITER + encodedHash;
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error hashing password", e);
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    /**
     * Verifies a password against a hashed password.
     *
     * @param password the password to verify
     * @param hashedPassword the hashed password to verify against
     * @return true if the password matches, false otherwise
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        try {
            // Split the hashed password into salt and hash
            String[] parts = hashedPassword.split(DELIMITER);
            if (parts.length != 2) {
                logger.warn("Invalid hashed password format");
                return false;
            }
            
            String encodedSalt = parts[0];
            String encodedHash = parts[1];
            
            // Decode the salt and hash
            byte[] salt = Base64.getDecoder().decode(encodedSalt);
            
            // Hash the password with the same salt
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            md.update(salt);
            byte[] hashedInput = md.digest(password.getBytes());
            String encodedInput = Base64.getEncoder().encodeToString(hashedInput);
            
            // Compare the hashes
            return encodedInput.equals(encodedHash);
        } catch (Exception e) {
            logger.error("Error verifying password", e);
            return false;
        }
    }
}
