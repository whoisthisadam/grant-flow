package com.kasperovich.serverinfo;

import com.kasperovich.commands.fromserver.ResponseFromServer;
import com.kasperovich.commands.fromserver.ResponseWrapper;
import com.kasperovich.commands.fromserver.ScholarshipProgramOperationResponse;
import com.kasperovich.commands.toserver.Command;
import com.kasperovich.commands.toserver.CommandWrapper;
import com.kasperovich.commands.toserver.UpdateScholarshipProgramCommand;
import com.kasperovich.dto.scholarship.ScholarshipProgramDTO;
import com.kasperovich.service.ScholarshipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the scholarship program update logic.
 * This test focuses on the core business logic without the networking aspects.
 */
public class HandleUpdateScholarshipProgramTest {

    private TestScholarshipHandler scholarshipHandler;
    private TestScholarshipService scholarshipService;
    private final Long AUTHENTICATED_USER_ID = 1L;
    private final Long PROGRAM_ID = 100L;

    @BeforeEach
    void setUp() {
        scholarshipService = new TestScholarshipService();
        scholarshipHandler = new TestScholarshipHandler(scholarshipService, AUTHENTICATED_USER_ID);
    }

    /**
     * Test implementation of ScholarshipService
     */
    private class TestScholarshipService extends ScholarshipService {
        private boolean throwException = false;
        private boolean throwIllegalArgumentException = false;
        private String exceptionMessage = "";
        private ScholarshipProgramDTO returnValue = null;
        
        public void setThrowException(boolean throwException, String message) {
            this.throwException = throwException;
            this.exceptionMessage = message;
        }
        
        public void setThrowIllegalArgumentException(boolean throwIllegalArgumentException, String message) {
            this.throwIllegalArgumentException = throwIllegalArgumentException;
            this.exceptionMessage = message;
        }
        
        public void setReturnValue(ScholarshipProgramDTO returnValue) {
            this.returnValue = returnValue;
        }
        
        @Override
        public ScholarshipProgramDTO updateScholarshipProgram(UpdateScholarshipProgramCommand command, Long userId) {
            if (throwIllegalArgumentException) {
                throw new IllegalArgumentException(exceptionMessage);
            }
            if (throwException) {
                throw new RuntimeException(exceptionMessage);
            }
            return returnValue;
        }
    }
    
    /**
     * A simplified handler class that implements just the scholarship program update logic
     * without the networking aspects of ClientProcessingThread
     */
    private class TestScholarshipHandler {
        private final List<ResponseWrapper> sentResponses = new ArrayList<>();
        private final ScholarshipService scholarshipService;
        private Long authenticatedUserId;
        
        public TestScholarshipHandler(ScholarshipService service, Long userId) {
            this.scholarshipService = service;
            this.authenticatedUserId = userId;
        }
        
        public void setAuthenticatedUserId(Long userId) {
            this.authenticatedUserId = userId;
        }
        
        /**
         * Handles updating a scholarship program
         */
        public void handleUpdateScholarshipProgram(CommandWrapper commandWrapper) {
            try {
                // Extract program data
                UpdateScholarshipProgramCommand command = commandWrapper.getData();

                if (command == null) {
                    sendResponse(new ResponseWrapper(ResponseFromServer.ERROR, 
                            "Scholarship program data is missing"));
                    return;
                }

                // Update the scholarship program
                ScholarshipProgramDTO program = scholarshipService.updateScholarshipProgram(command, authenticatedUserId);

                // Create response with the program
                ScholarshipProgramOperationResponse response = ScholarshipProgramOperationResponse.success(
                        "Scholarship program updated successfully",
                        program,
                        ScholarshipProgramOperationResponse.OperationType.UPDATE
                );

                sendResponse(new ResponseWrapper(ResponseFromServer.SUCCESS, response));
            } catch (IllegalArgumentException e) {
                ScholarshipProgramOperationResponse response = ScholarshipProgramOperationResponse.error(
                        e.getMessage(),
                        ScholarshipProgramOperationResponse.OperationType.UPDATE
                );
                sendResponse(new ResponseWrapper(ResponseFromServer.ERROR, response));
            } catch (Exception e) {
                ScholarshipProgramOperationResponse response = ScholarshipProgramOperationResponse.error(
                        "Error updating scholarship program: " + e.getMessage(),
                        ScholarshipProgramOperationResponse.OperationType.UPDATE
                );
                sendResponse(new ResponseWrapper(ResponseFromServer.ERROR, response));
            }
        }
        
        private void sendResponse(ResponseWrapper response) {
            sentResponses.add(response);
        }
        
        public List<ResponseWrapper> getSentResponses() {
            return sentResponses;
        }
        
        public ResponseWrapper getLastSentResponse() {
            if (sentResponses.isEmpty()) {
                return null;
            }
            return sentResponses.get(sentResponses.size() - 1);
        }
    }

    /**
     * Creates a sample scholarship program DTO for testing
     */
    private ScholarshipProgramDTO createSampleScholarshipProgramDTO() {
        ScholarshipProgramDTO program = new ScholarshipProgramDTO();
        program.setId(PROGRAM_ID);
        program.setName("Engineering Excellence Scholarship");
        program.setDescription("Scholarship for outstanding engineering students");
        program.setFundingAmount(new BigDecimal("10000.00"));
        program.setMinGpa(new BigDecimal("3.5"));
        program.setApplicationDeadline(LocalDate.now().plusMonths(3));
        program.setActive(true);
        program.setCreatedById(1L);
        return program;
    }

    /**
     * Creates a sample update command for testing
     */
    private UpdateScholarshipProgramCommand createSampleUpdateCommand() {
        UpdateScholarshipProgramCommand command = new UpdateScholarshipProgramCommand();
        command.setId(PROGRAM_ID);
        command.setName("Updated Engineering Excellence Scholarship");
        command.setDescription("Updated scholarship for outstanding engineering students");
        command.setFundingAmount(new BigDecimal("12000.00"));
        command.setMinGpa(new BigDecimal("3.7"));
        command.setApplicationDeadline(LocalDate.now().plusMonths(4));
        command.setActive(true);
        return command;
    }

    @Test
    void testHandleUpdateScholarshipProgram_Success() {
        // Arrange
        UpdateScholarshipProgramCommand command = createSampleUpdateCommand();
        CommandWrapper commandWrapper = new CommandWrapper(Command.UPDATE_SCHOLARSHIP_PROGRAM, command);
        
        // Create a sample updated program DTO for the response
        ScholarshipProgramDTO updatedProgram = createSampleScholarshipProgramDTO();
        updatedProgram.setName(command.getName());
        updatedProgram.setDescription(command.getDescription());
        updatedProgram.setFundingAmount(command.getFundingAmount());
        updatedProgram.setMinGpa(command.getMinGpa());
        updatedProgram.setApplicationDeadline(command.getApplicationDeadline());
        
        // Configure the service to return the updated program DTO
        scholarshipService.setReturnValue(updatedProgram);
        
        // Act - Call the method under test
        scholarshipHandler.handleUpdateScholarshipProgram(commandWrapper);
        
        // Assert
        ResponseWrapper response = scholarshipHandler.getLastSentResponse();
        
        // Verify the response
        assertNotNull(response);
        assertEquals(ResponseFromServer.SUCCESS, response.getResponse());
        
        ScholarshipProgramOperationResponse operationResponse = (ScholarshipProgramOperationResponse) response.getData();
        assertNotNull(operationResponse);
        assertTrue(operationResponse.isSuccess());
        assertEquals("Scholarship program updated successfully", operationResponse.getMessage());
        assertEquals(ScholarshipProgramOperationResponse.OperationType.UPDATE, operationResponse.getOperationType());
        
        ScholarshipProgramDTO returnedProgram = operationResponse.getProgram();
        assertNotNull(returnedProgram);
        assertEquals(PROGRAM_ID, returnedProgram.getId());
        assertEquals("Updated Engineering Excellence Scholarship", returnedProgram.getName());
        assertEquals("Updated scholarship for outstanding engineering students", returnedProgram.getDescription());
        assertEquals(new BigDecimal("12000.00"), returnedProgram.getFundingAmount());
        assertEquals(new BigDecimal("3.7"), returnedProgram.getMinGpa());
    }
    
    @Test
    void testHandleUpdateScholarshipProgram_IllegalArgumentException() {
        // Arrange
        UpdateScholarshipProgramCommand command = createSampleUpdateCommand();
        CommandWrapper commandWrapper = new CommandWrapper(Command.UPDATE_SCHOLARSHIP_PROGRAM, command);
        
        // Configure the service to throw an IllegalArgumentException
        String errorMessage = "Only administrators can update scholarship programs";
        scholarshipService.setThrowIllegalArgumentException(true, errorMessage);
        
        // Act - Call the method under test
        scholarshipHandler.handleUpdateScholarshipProgram(commandWrapper);
        
        // Assert
        ResponseWrapper response = scholarshipHandler.getLastSentResponse();
        
        // Verify the response
        assertNotNull(response);
        assertEquals(ResponseFromServer.ERROR, response.getResponse());
        
        ScholarshipProgramOperationResponse operationResponse = (ScholarshipProgramOperationResponse) response.getData();
        assertNotNull(operationResponse);
        assertFalse(operationResponse.isSuccess());
        assertEquals(errorMessage, operationResponse.getMessage());
        assertEquals(ScholarshipProgramOperationResponse.OperationType.UPDATE, operationResponse.getOperationType());
    }
    
    @Test
    void testHandleUpdateScholarshipProgram_GeneralException() {
        // Arrange
        UpdateScholarshipProgramCommand command = createSampleUpdateCommand();
        CommandWrapper commandWrapper = new CommandWrapper(Command.UPDATE_SCHOLARSHIP_PROGRAM, command);
        
        // Configure the service to throw a general exception
        String errorMessage = "Database error";
        scholarshipService.setThrowException(true, errorMessage);
        
        // Act - Call the method under test
        scholarshipHandler.handleUpdateScholarshipProgram(commandWrapper);
        
        // Assert
        ResponseWrapper response = scholarshipHandler.getLastSentResponse();
        
        // Verify the response
        assertNotNull(response);
        assertEquals(ResponseFromServer.ERROR, response.getResponse());
        
        ScholarshipProgramOperationResponse operationResponse = (ScholarshipProgramOperationResponse) response.getData();
        assertNotNull(operationResponse);
        assertFalse(operationResponse.isSuccess());
        assertEquals("Error updating scholarship program: " + errorMessage, operationResponse.getMessage());
        assertEquals(ScholarshipProgramOperationResponse.OperationType.UPDATE, operationResponse.getOperationType());
    }
}
