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
    GET_SCHOLARSHIP_PROGRAMS,
    APPLY_FOR_SCHOLARSHIP,
    GET_USER_APPLICATIONS,
    GET_ACADEMIC_PERIODS,
    
    // Admin scholarship management commands
    CREATE_SCHOLARSHIP_PROGRAM,
    UPDATE_SCHOLARSHIP_PROGRAM,
    DELETE_SCHOLARSHIP_PROGRAM
}
