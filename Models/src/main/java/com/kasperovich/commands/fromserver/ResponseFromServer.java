package com.kasperovich.commands.fromserver;

import java.io.Serializable;

/**
 * Enum representing responses that can be sent from the server to the client.
 */
public enum ResponseFromServer implements Serializable {
    // General responses
    SUCCESS,
    ERROR,
    UNKNOWN_COMMAND,
    
    // Authentication responses
    LOGIN_SUCCESS,
    LOGIN_FAILED,
    REGISTRATION_SUCCESS,
    REGISTRATION_FAILED_USERNAME_EXISTS,
    REGISTRATION_FAILED_INVALID_DATA,
    LOGOUT_SUCCESS,
    AUTHENTICATION_REQUIRED,
    
    // Data access responses
    DATA_FOUND,
    DATA_NOT_FOUND,
    DATA_UPDATED,
    DATA_UPDATE_FAILED,
    
    // Permission responses
    PERMISSION_DENIED,
    
    // Application responses
    APPLICATION_SUBMITTED,
    APPLICATION_UPDATED,
    APPLICATION_REVIEWED,
    
    // Scholarship responses
    SCHOLARSHIP_PROGRAMS_FOUND
}
