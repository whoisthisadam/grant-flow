package com.kasperovich.commands.toserver;

import java.io.Serializable;

/**
 * Enum representing commands that can be sent from the client to the server.
 */
public enum Command implements Serializable {
    // System commands
    HEALTH_CHECK,
    
    // Authentication commands
    LOGIN,
    REGISTER,
    LOGOUT,
    
    // User profile commands
    GET_USER_PROFILE,
    UPDATE_USER_PROFILE,
    
    // Student profile commands
    GET_STUDENT_PROFILE,
    UPDATE_STUDENT_PROFILE,
    
    // Scholarship commands
    GET_AVAILABLE_SCHOLARSHIPS,
    APPLY_FOR_SCHOLARSHIP,
    GET_MY_APPLICATIONS,
    
    // Admin commands
    CREATE_SCHOLARSHIP_PROGRAM,
    UPDATE_SCHOLARSHIP_PROGRAM,
    REVIEW_APPLICATION,
    GET_ALL_APPLICATIONS,
    GET_ALL_USERS
}
