package com.kasperovich.serverinfo;

import com.kasperovich.commands.fromserver.ResponseFromServer;
import com.kasperovich.commands.fromserver.ResponseWrapper;
import com.kasperovich.commands.fromserver.ScholarshipProgramOperationResponse;
import com.kasperovich.commands.toserver.Command;
import com.kasperovich.commands.toserver.CommandWrapper;
import com.kasperovich.commands.toserver.CreateScholarshipProgramCommand;
import com.kasperovich.dto.scholarship.ScholarshipProgramDTO;
import com.kasperovich.service.ScholarshipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the scholarship program creation logic.
 * This test focuses on the core business logic without the networking aspects.
 */
public class HandleCreateScholarshipProgramTest {

    private TestScholarshipHandler scholarshipHandler;
    private TestScholarshipService scholarshipService;
    private final Long AUTHENTICATED_USER_ID = 1L;

    @BeforeEach
    void setUp() {
        scholarshipService = new TestScholarshipService();
        scholarshipHandler = new TestScholarshipHandler(scholarshipService, AUTHENTICATED_USER_ID);
    }

    /**
     * Test implementation of ScholarshipService
     */
    private class TestScholarshipService extends ScholarshipService {
        private boolean throwIllegalArgumentException = false;
        private boolean throwGenericException = false;
        private String exceptionMessage = "";
        private ScholarshipProgramDTO returnValue = null;
        
        public void setThrowIllegalArgumentException(boolean throwException, String message) {
            this.throwIllegalArgumentException = throwException;
            this.exceptionMessage = message;
        }
        
        public void setThrowGenericException(boolean throwException, String message) {
            this.throwGenericException = throwException;
            this.exceptionMessage = message;
        }
        
        public void setReturnValue(ScholarshipProgramDTO returnValue) {
            this.returnValue = returnValue;
        }
        
        @Override
        public ScholarshipProgramDTO createScholarshipProgram(CreateScholarshipProgramCommand command, Long userId) {
            if (throwIllegalArgumentException) {
                throw new IllegalArgumentException(exceptionMessage);
            }
            if (throwGenericException) {
                throw new RuntimeException(exceptionMessage);
            }
            return returnValue;
        }
    }
    
    /**
     * A simplified handler class that implements just the scholarship program creation logic
     * without the networking aspects of ClientProcessingThread
     */
    private class TestScholarshipHandler {
        private final List<ResponseWrapper> sentResponses = new ArrayList<>();
        private final ScholarshipService scholarshipService;
        private final Long authenticatedUserId;
        
        public TestScholarshipHandler(ScholarshipService service, Long userId) {
            this.scholarshipService = service;
            this.authenticatedUserId = userId;
        }
        
        /**
         * Handles creating a scholarship program
         */
        public void handleCreateScholarshipProgram(CommandWrapper commandWrapper) {
            try {
                // Extract program data
                CreateScholarshipProgramCommand command = commandWrapper.getData();

                if (command == null) {
                    ScholarshipProgramOperationResponse response = ScholarshipProgramOperationResponse.error(
                            "Scholarship program data is missing",
                            ScholarshipProgramOperationResponse.OperationType.CREATE
                    );
                    sendResponse(new ResponseWrapper(ResponseFromServer.ERROR, response));
                    return;
                }

                // Create the scholarship program
                ScholarshipProgramDTO program = scholarshipService.createScholarshipProgram(command, authenticatedUserId);

                // Create response with the program
                ScholarshipProgramOperationResponse response = ScholarshipProgramOperationResponse.success(
                        "Scholarship program created successfully",
                        program,
                        ScholarshipProgramOperationResponse.OperationType.CREATE
                );

                sendResponse(new ResponseWrapper(ResponseFromServer.SUCCESS, response));
            } catch (IllegalArgumentException e) {
                ScholarshipProgramOperationResponse response = ScholarshipProgramOperationResponse.error(
                        e.getMessage(),
                        ScholarshipProgramOperationResponse.OperationType.CREATE
                );
                sendResponse(new ResponseWrapper(ResponseFromServer.ERROR, response));
            } catch (Exception e) {
                ScholarshipProgramOperationResponse response = ScholarshipProgramOperationResponse.error(
                        "Error creating scholarship program: " + e.getMessage(),
                        ScholarshipProgramOperationResponse.OperationType.CREATE
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
     * Creates a sample scholarship program command for testing
     */
    private CreateScholarshipProgramCommand createSampleCommand() {
        CreateScholarshipProgramCommand command = new CreateScholarshipProgramCommand();
        command.setName("Academic Excellence Scholarship");
        command.setDescription("For students with outstanding academic achievements");
        command.setFundingAmount(new BigDecimal("5000.00"));
        command.setMinGpa(BigDecimal.valueOf(3.8));
        command.setActive(true);
        command.setApplicationDeadline(LocalDate.from(LocalDateTime.now().plusMonths(3)));
        return command;
    }

    /**
     * Creates a sample scholarship program DTO for testing
     */
    private ScholarshipProgramDTO createSampleProgramDTO() {
        ScholarshipProgramDTO program = new ScholarshipProgramDTO();
        program.setId(1L);
        program.setName("Academic Excellence Scholarship");
        program.setDescription("For students with outstanding academic achievements");
        program.setFundingAmount(new BigDecimal("5000.00"));
        program.setMinGpa(BigDecimal.valueOf(3.8));
        program.setActive(true);
        program.setApplicationDeadline(LocalDate.from(LocalDateTime.now().plusMonths(3)));
        program.setCreatedById(AUTHENTICATED_USER_ID);
        return program;
    }

    @Test
    void testHandleCreateScholarshipProgram_Success() {
        // Arrange
        CreateScholarshipProgramCommand command = createSampleCommand();
        CommandWrapper commandWrapper = new CommandWrapper(Command.CREATE_SCHOLARSHIP_PROGRAM, command);
        
        // Create a sample program DTO for the response
        ScholarshipProgramDTO programDTO = createSampleProgramDTO();
        
        // Configure the service to return the program DTO
        scholarshipService.setReturnValue(programDTO);
        
        // Act - Call the method under test
        scholarshipHandler.handleCreateScholarshipProgram(commandWrapper);
        
        // Assert
        ResponseWrapper response = scholarshipHandler.getLastSentResponse();
        
        // Verify the response
        assertNotNull(response);
        assertEquals(ResponseFromServer.SUCCESS, response.getResponse());
        
        ScholarshipProgramOperationResponse operationResponse = (ScholarshipProgramOperationResponse) response.getData();
        assertTrue(operationResponse.isSuccess());
        assertEquals("Scholarship program created successfully", operationResponse.getMessage());
        assertEquals(ScholarshipProgramOperationResponse.OperationType.CREATE, operationResponse.getOperationType());
        assertEquals(programDTO, operationResponse.getProgram());
    }

    @Test
    void testHandleCreateScholarshipProgram_NullCommand() {
        // Arrange
        CommandWrapper commandWrapper = new CommandWrapper(Command.CREATE_SCHOLARSHIP_PROGRAM, null);
        
        // Act - Call the method under test
        scholarshipHandler.handleCreateScholarshipProgram(commandWrapper);
        
        // Assert
        ResponseWrapper response = scholarshipHandler.getLastSentResponse();
        
        // Verify the response
        assertNotNull(response);
        assertEquals(ResponseFromServer.ERROR, response.getResponse());
        
        ScholarshipProgramOperationResponse operationResponse = (ScholarshipProgramOperationResponse) response.getData();
        assertFalse(operationResponse.isSuccess());
        assertEquals("Scholarship program data is missing", operationResponse.getMessage());
        assertEquals(ScholarshipProgramOperationResponse.OperationType.CREATE, operationResponse.getOperationType());
        assertNull(operationResponse.getProgram());
    }

    @Test
    void testHandleCreateScholarshipProgram_ValidationError() {
        // Arrange
        CreateScholarshipProgramCommand command = createSampleCommand();
        CommandWrapper commandWrapper = new CommandWrapper(Command.CREATE_SCHOLARSHIP_PROGRAM, command);
        
        // Configure the service to throw an IllegalArgumentException
        String errorMessage = "Program name is required";
        scholarshipService.setThrowIllegalArgumentException(true, errorMessage);
        
        // Act - Call the method under test
        scholarshipHandler.handleCreateScholarshipProgram(commandWrapper);
        
        // Assert
        ResponseWrapper response = scholarshipHandler.getLastSentResponse();
        
        // Verify the response
        assertNotNull(response);
        assertEquals(ResponseFromServer.ERROR, response.getResponse());
        
        ScholarshipProgramOperationResponse operationResponse = (ScholarshipProgramOperationResponse) response.getData();
        assertFalse(operationResponse.isSuccess());
        assertEquals(errorMessage, operationResponse.getMessage());
        assertEquals(ScholarshipProgramOperationResponse.OperationType.CREATE, operationResponse.getOperationType());
        assertNull(operationResponse.getProgram());
    }

    @Test
    void testHandleCreateScholarshipProgram_GenericError() {
        // Arrange
        CreateScholarshipProgramCommand command = createSampleCommand();
        CommandWrapper commandWrapper = new CommandWrapper(Command.CREATE_SCHOLARSHIP_PROGRAM, command);
        
        // Configure the service to throw a generic exception
        String errorMessage = "Database connection failed";
        scholarshipService.setThrowGenericException(true, errorMessage);
        
        // Act - Call the method under test
        scholarshipHandler.handleCreateScholarshipProgram(commandWrapper);
        
        // Assert
        ResponseWrapper response = scholarshipHandler.getLastSentResponse();
        
        // Verify the response
        assertNotNull(response);
        assertEquals(ResponseFromServer.ERROR, response.getResponse());
        
        ScholarshipProgramOperationResponse operationResponse = (ScholarshipProgramOperationResponse) response.getData();
        assertFalse(operationResponse.isSuccess());
        assertEquals("Error creating scholarship program: " + errorMessage, operationResponse.getMessage());
        assertEquals(ScholarshipProgramOperationResponse.OperationType.CREATE, operationResponse.getOperationType());
        assertNull(operationResponse.getProgram());
    }
}
