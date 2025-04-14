package com.kasperovich.security;

import com.kasperovich.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages authentication tokens for user sessions.
 */
public class TokenManager {
    private static final Logger logger = LoggerUtil.getLogger(TokenManager.class);
    private static final int TOKEN_LENGTH = 32;
    private static final long TOKEN_EXPIRATION_MINUTES = 60; // 1 hour
    
    private static final TokenManager instance = new TokenManager();
    
    // Map of token to user ID and expiration time
    private final Map<String, TokenInfo> tokenMap = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    
    /**
     * Private constructor to enforce singleton pattern.
     */
    private TokenManager() {
    }
    
    /**
     * Gets the singleton instance.
     *
     * @return the token manager instance
     */
    public static TokenManager getInstance() {
        return instance;
    }
    
    /**
     * Generates a new token for the specified user.
     *
     * @param userId the user ID
     * @return the generated token
     */
    public String generateToken(Long userId) {
        byte[] tokenBytes = new byte[TOKEN_LENGTH];
        random.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        
        // Store token with expiration time
        LocalDateTime expiration = LocalDateTime.now().plusMinutes(TOKEN_EXPIRATION_MINUTES);
        tokenMap.put(token, new TokenInfo(userId, expiration));
        
        logger.debug("Generated token for user ID {}: {}", userId, token);
        return token;
    }
    
    /**
     * Validates a token and returns the associated user ID if valid.
     *
     * @param token the token to validate
     * @return the user ID associated with the token, or null if the token is invalid
     */
    public Long validateToken(String token) {
        TokenInfo info = tokenMap.get(token);
        
        if (info == null) {
            logger.debug("Token not found: {}", token);
            return null;
        }
        
        if (info.isExpired()) {
            logger.debug("Token expired: {}", token);
            tokenMap.remove(token);
            return null;
        }
        
        // Extend token expiration on successful validation
        info.extendExpiration();
        
        logger.debug("Token validated for user ID {}: {}", info.getUserId(), token);
        return info.getUserId();
    }
    
    /**
     * Invalidates a token.
     *
     * @param token the token to invalidate
     */
    public void invalidateToken(String token) {
        TokenInfo removed = tokenMap.remove(token);
        if (removed != null) {
            logger.debug("Token invalidated for user ID {}: {}", removed.getUserId(), token);
        }
    }
    
    /**
     * Invalidates all tokens for the specified user.
     *
     * @param userId the user ID
     */
    public void invalidateUserTokens(Long userId) {
        tokenMap.entrySet().removeIf(entry -> {
            boolean matches = entry.getValue().getUserId().equals(userId);
            if (matches) {
                logger.debug("Token invalidated for user ID {}: {}", userId, entry.getKey());
            }
            return matches;
        });
    }
    
    /**
     * Removes expired tokens.
     */
    public void cleanupExpiredTokens() {
        int count = 0;
        for (Map.Entry<String, TokenInfo> entry : tokenMap.entrySet()) {
            if (entry.getValue().isExpired()) {
                tokenMap.remove(entry.getKey());
                count++;
            }
        }
        if (count > 0) {
            logger.debug("Cleaned up {} expired tokens", count);
        }
    }
    
    /**
     * Inner class to store token information.
     */
    private static class TokenInfo {
        private final Long userId;
        private LocalDateTime expiration;
        
        /**
         * Creates a new token info with the specified user ID and expiration time.
         *
         * @param userId the user ID
         * @param expiration the expiration time
         */
        public TokenInfo(Long userId, LocalDateTime expiration) {
            this.userId = userId;
            this.expiration = expiration;
        }
        
        /**
         * Gets the user ID.
         *
         * @return the user ID
         */
        public Long getUserId() {
            return userId;
        }
        
        /**
         * Checks if the token is expired.
         *
         * @return true if the token is expired, false otherwise
         */
        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiration);
        }
        
        /**
         * Extends the expiration time.
         */
        public void extendExpiration() {
            this.expiration = LocalDateTime.now().plusMinutes(TOKEN_EXPIRATION_MINUTES);
        }
    }
}
