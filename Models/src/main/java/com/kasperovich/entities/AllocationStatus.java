package com.kasperovich.entities;

/**
 * Enum representing the status of a fund allocation.
 */
public enum AllocationStatus {
    PENDING,    // Allocation is pending approval
    APPROVED,   // Allocation has been approved
    REJECTED,   // Allocation has been rejected
    CANCELLED   // Allocation has been cancelled
}
