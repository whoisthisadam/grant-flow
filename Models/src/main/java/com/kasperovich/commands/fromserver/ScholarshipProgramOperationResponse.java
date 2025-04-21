package com.kasperovich.commands.fromserver;

import com.kasperovich.dto.scholarship.ScholarshipProgramDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Response for scholarship program operations (create, update, delete).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScholarshipProgramOperationResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private boolean success;
    private String message;
    private ScholarshipProgramDTO program;
    private OperationType operationType;
    
    /**
     * Enum representing the type of operation performed.
     */
    public enum OperationType {
        CREATE,
        UPDATE,
        DELETE
    }
    
    /**
     * Creates a success response for a scholarship program operation.
     * 
     * @param message the success message
     * @param program the scholarship program
     * @param operationType the type of operation
     * @return the response
     */
    public static ScholarshipProgramOperationResponse success(String message, ScholarshipProgramDTO program, OperationType operationType) {
        return new ScholarshipProgramOperationResponse(true, message, program, operationType);
    }
    
    /**
     * Creates an error response for a scholarship program operation.
     * 
     * @param message the error message
     * @param operationType the type of operation
     * @return the response
     */
    public static ScholarshipProgramOperationResponse error(String message, OperationType operationType) {
        return new ScholarshipProgramOperationResponse(false, message, null, operationType);
    }
}
