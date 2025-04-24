package com.kasperovich.serverinfo;

import com.kasperovich.commands.fromserver.ResponseFromServer;
import com.kasperovich.commands.fromserver.ResponseWrapper;
import com.kasperovich.commands.fromserver.ScholarshipApplicationResponse;
import com.kasperovich.commands.toserver.Command;
import com.kasperovich.commands.toserver.CommandWrapper;
import com.kasperovich.commands.toserver.SubmitScholarshipApplicationCommand;
import com.kasperovich.dto.scholarship.ScholarshipApplicationDTO;
import com.kasperovich.service.ScholarshipApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the scholarship application logic.
 * This test focuses on the core business logic without the networking aspects.
 */
public class HandleApplyForScholarshipTest {

    private TestScholarshipHandler scholarshipHandler;
    private TestScholarshipApplicationService scholarshipApplicationService;
    private final Long AUTHENTICATED_USER_ID = 1L;
    private final Long PROGRAM_ID = 2L;
    private final Long PERIOD_ID = 3L;
    private final String ADDITIONAL_INFO = "Additional information for the application";

    @BeforeEach
    void setUp() {
        scholarshipApplicationService = new TestScholarshipApplicationService();
        scholarshipHandler = new TestScholarshipHandler(scholarshipApplicationService, AUTHENTICATED_USER_ID);
    }

    /**
     * Test implementation of ScholarshipApplicationService
     */
    private class TestScholarshipApplicationService extends ScholarshipApplicationService {
        private boolean throwException = false;
        private String exceptionMessage = "";
        private ScholarshipApplicationDTO returnValue = null;
        
        public void setThrowException(boolean throwException, String message) {
            this.throwException = throwException;
            this.exceptionMessage = message;
        }
        
        public void setReturnValue(ScholarshipApplicationDTO returnValue) {
            this.returnValue = returnValue;
        }
        
        @Override
        public ScholarshipApplicationDTO submitApplication(Long userId, Long programId, Long periodId, String additionalInfo) 
                throws Exception {
            if (throwException) {
                throw new Exception(exceptionMessage);
            }
            return returnValue;
        }
    }
    
    /**
     * A simplified handler class that implements just the scholarship application logic
     * without the networking aspects of ClientProcessingThread
     */
    private class TestScholarshipHandler {
        private final List<ResponseWrapper> sentResponses = new ArrayList<>();
        private final ScholarshipApplicationService scholarshipApplicationService;
        private final Long authenticatedUserId;
        
        public TestScholarshipHandler(ScholarshipApplicationService service, Long userId) {
            this.scholarshipApplicationService = service;
            this.authenticatedUserId = userId;
        }
        
        /**
         * Handles a scholarship application command
         */
        public void handleApplyForScholarship(CommandWrapper commandWrapper) {
            try {
                // Extract application data
                SubmitScholarshipApplicationCommand command = commandWrapper.getData();
                
                if (command == null) {
                    ScholarshipApplicationResponse response = new ScholarshipApplicationResponse(
                            false,
                            "Scholarship application data is missing",
                            null
                    );
                    sendResponse(new ResponseWrapper(ResponseFromServer.ERROR, response));
                    return;
                }
                
                // Submit the application
                ScholarshipApplicationDTO application = scholarshipApplicationService.submitApplication(
                        authenticatedUserId,
                        command.getProgramId(),
                        command.getPeriodId(),
                        command.getAdditionalInfo()
                );
                
                // Create success response
                ScholarshipApplicationResponse response = new ScholarshipApplicationResponse(
                        true,
                        "Application submitted successfully",
                        application
                );
                
                sendResponse(new ResponseWrapper(ResponseFromServer.SUCCESS, response));
                
            } catch (Exception e) {
                // Create error response
                ScholarshipApplicationResponse response = new ScholarshipApplicationResponse(
                        false,
                        e.getMessage(),
                        null
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

    @Test
    void testHandleApplyForScholarship_Success() {
        // Arrange
        SubmitScholarshipApplicationCommand command = new SubmitScholarshipApplicationCommand(
                PROGRAM_ID, PERIOD_ID, ADDITIONAL_INFO);
        CommandWrapper commandWrapper = new CommandWrapper(Command.APPLY_FOR_SCHOLARSHIP, command);
        
        // Create a mock ScholarshipApplicationDTO for the response
        ScholarshipApplicationDTO applicationDTO = new ScholarshipApplicationDTO();
        applicationDTO.setId(5L);
        applicationDTO.setApplicantId(AUTHENTICATED_USER_ID);
        applicationDTO.setProgramId(PROGRAM_ID);
        applicationDTO.setPeriodId(PERIOD_ID);
        applicationDTO.setStatus("PENDING");
        applicationDTO.setSubmissionDate(LocalDateTime.now());
        
        // Configure the service to return the application DTO
        scholarshipApplicationService.setReturnValue(applicationDTO);
        
        // Act - Call the method under test
        scholarshipHandler.handleApplyForScholarship(commandWrapper);
        
        // Assert
        ResponseWrapper response = scholarshipHandler.getLastSentResponse();
        
        // Verify the response
        assertNotNull(response);
        assertEquals(ResponseFromServer.SUCCESS, response.getResponse());
        
        ScholarshipApplicationResponse applicationResponse = (ScholarshipApplicationResponse) response.getData();
        assertTrue(applicationResponse.isSuccess());
        assertEquals("Application submitted successfully", applicationResponse.getMessage());
        assertEquals(applicationDTO, applicationResponse.getApplication());
    }

    @Test
    void testHandleApplyForScholarship_NullCommand() {
        // Arrange
        CommandWrapper commandWrapper = new CommandWrapper(Command.APPLY_FOR_SCHOLARSHIP, null);
        
        // Act - Call the method under test
        scholarshipHandler.handleApplyForScholarship(commandWrapper);
        
        // Assert
        ResponseWrapper response = scholarshipHandler.getLastSentResponse();
        
        // Verify the response
        assertNotNull(response);
        assertEquals(ResponseFromServer.ERROR, response.getResponse());
        
        ScholarshipApplicationResponse applicationResponse = (ScholarshipApplicationResponse) response.getData();
        assertFalse(applicationResponse.isSuccess());
        assertEquals("Scholarship application data is missing", applicationResponse.getMessage());
        assertNull(applicationResponse.getApplication());
    }

    @Test
    void testHandleApplyForScholarship_ServiceThrowsException() {
        // Arrange
        SubmitScholarshipApplicationCommand command = new SubmitScholarshipApplicationCommand(
                PROGRAM_ID, PERIOD_ID, ADDITIONAL_INFO);
        CommandWrapper commandWrapper = new CommandWrapper(Command.APPLY_FOR_SCHOLARSHIP, command);
        
        // Configure the service to throw an exception
        String errorMessage = "Application deadline has passed for this program";
        scholarshipApplicationService.setThrowException(true, errorMessage);
        
        // Act - Call the method under test
        scholarshipHandler.handleApplyForScholarship(commandWrapper);
        
        // Assert
        ResponseWrapper response = scholarshipHandler.getLastSentResponse();
        
        // Verify the response
        assertNotNull(response);
        assertEquals(ResponseFromServer.ERROR, response.getResponse());
        
        ScholarshipApplicationResponse applicationResponse = (ScholarshipApplicationResponse) response.getData();
        assertFalse(applicationResponse.isSuccess());
        assertEquals(errorMessage, applicationResponse.getMessage());
        assertNull(applicationResponse.getApplication());
    }
}
