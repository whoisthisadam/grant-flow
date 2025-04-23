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
    DELETE_SCHOLARSHIP_PROGRAM,
    
    // Application review commands
    GET_PENDING_APPLICATIONS,
    GET_ALL_APPLICATIONS,
    APPROVE_APPLICATION,
    REJECT_APPLICATION,
    
    // Fund management commands
    GET_ALL_BUDGETS,
    GET_ACTIVE_BUDGET,
    CREATE_BUDGET,
    UPDATE_BUDGET,
    ACTIVATE_BUDGET,
    CLOSE_BUDGET,
    
    // Fund allocation commands
    ALLOCATE_FUNDS,
    GET_ALLOCATIONS_BY_BUDGET,
    GET_ALLOCATIONS_BY_PROGRAM,
    
    // Academic period management commands
    CREATE_ACADEMIC_PERIOD,
    UPDATE_ACADEMIC_PERIOD,
    UPDATE_ACADEMIC_PERIOD_STATUS,
    DELETE_ACADEMIC_PERIOD,
    
    // Report commands
    GET_SCHOLARSHIP_DISTRIBUTION_REPORT,
    GET_APPLICATION_STATUS_REPORT,
    GET_USER_ACTIVITY_REPORT
}
