package com.kasperovich.serverinfo;

import com.kasperovich.commands.fromserver.ApplicationReviewResponse;
import com.kasperovich.commands.fromserver.ResponseFromServer;
import com.kasperovich.commands.fromserver.ResponseWrapper;
import com.kasperovich.commands.toserver.ApproveApplicationCommand;
import com.kasperovich.commands.toserver.Command;
import com.kasperovich.commands.toserver.CommandWrapper;
import com.kasperovich.commands.toserver.RejectApplicationCommand;
import com.kasperovich.dto.scholarship.ScholarshipApplicationDTO;
import com.kasperovich.service.ScholarshipApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the scholarship application review logic (approve/reject).
 * This test focuses on the core business logic without the networking aspects.
 */
public class HandleApplicationReviewTest {

    private TestApplicationHandler applicationHandler;
    private TestScholarshipApplicationService applicationService;
    private final Long AUTHENTICATED_USER_ID = 1L;
    private final Long APPLICATION_ID = 100L;

    @BeforeEach
    void setUp() {
        applicationService = new TestScholarshipApplicationService();
        applicationHandler = new TestApplicationHandler(applicationService, AUTHENTICATED_USER_ID);
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
        public ScholarshipApplicationDTO approveApplicationWithAuth(Long applicationId, Long userId, String comments) 
                throws Exception {
            if (throwException) {
                throw new Exception(exceptionMessage);
            }
            return returnValue;
        }
        
        @Override
        public ScholarshipApplicationDTO rejectApplicationWithAuth(Long applicationId, Long userId, String comments) 
                throws Exception {
            if (throwException) {
                throw new Exception(exceptionMessage);
            }
            return returnValue;
        }
    }
    
    /**
     * A simplified handler class that implements just the application review logic
     * without the networking aspects of ClientProcessingThread
     */
    private class TestApplicationHandler {
        private final List<ResponseWrapper> sentResponses = new ArrayList<>();
        private final ScholarshipApplicationService applicationService;
        private Long authenticatedUserId;
        
        public TestApplicationHandler(ScholarshipApplicationService service, Long userId) {
            this.applicationService = service;
            this.authenticatedUserId = userId;
        }
        
        public void setAuthenticatedUserId(Long userId) {
            this.authenticatedUserId = userId;
        }
        
        /**
         * Handles approving a scholarship application
         */
        public void handleApproveApplication(CommandWrapper commandWrapper) {
            try {
                ApproveApplicationCommand command = commandWrapper.getData();
                String comments = command.getComments();

                // Approve the application with auth validation
                ScholarshipApplicationDTO application = applicationService.approveApplicationWithAuth(
                        command.getApplicationId(), authenticatedUserId, comments);

                // Send response
                ApplicationReviewResponse response = new ApplicationReviewResponse(application);
                sendResponse(new ResponseWrapper(ResponseFromServer.SUCCESS, response));
            } catch (Exception e) {
                sendResponse(new ResponseWrapper(ResponseFromServer.ERROR,
                        new ApplicationReviewResponse(e.getMessage())));
            }
        }
        
        /**
         * Handles rejecting a scholarship application
         */
        public void handleRejectApplication(CommandWrapper commandWrapper) {
            try {
                RejectApplicationCommand command = commandWrapper.getData();
                String comments = command.getComments();

                // Reject the application with auth validation
                ScholarshipApplicationDTO application = applicationService.rejectApplicationWithAuth(
                        command.getApplicationId(), authenticatedUserId, comments);

                // Send response
                ApplicationReviewResponse response = new ApplicationReviewResponse(application);
                sendResponse(new ResponseWrapper(ResponseFromServer.SUCCESS, response));
            } catch (Exception e) {
                sendResponse(new ResponseWrapper(ResponseFromServer.ERROR,
                        new ApplicationReviewResponse(e.getMessage())));
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
     * Creates a sample scholarship application DTO for testing
     */
    private ScholarshipApplicationDTO createSampleApplicationDTO(String status) {
        ScholarshipApplicationDTO application = new ScholarshipApplicationDTO();
        application.setId(APPLICATION_ID);
        application.setApplicantId(2L); // Different from authenticated user
        application.setProgramId(1L);
        application.setPeriodId(1L);
        application.setStatus(status);
        application.setSubmissionDate(LocalDateTime.now().minusDays(7));
        application.setDecisionDate(LocalDateTime.now());
        application.setReviewerId(AUTHENTICATED_USER_ID);
        return application;
    }

    // Tests for handleApproveApplication
    
    @Test
    void testHandleApproveApplication_Success() {
        // Arrange
        ApproveApplicationCommand command = new ApproveApplicationCommand();
        command.setApplicationId(APPLICATION_ID);
        command.setComments("Approved after review");
        CommandWrapper commandWrapper = new CommandWrapper(Command.APPROVE_APPLICATION, command);
        
        // Create a sample application DTO for the response
        ScholarshipApplicationDTO approvedApplication = createSampleApplicationDTO("APPROVED");
        
        // Configure the service to return the application DTO
        applicationService.setReturnValue(approvedApplication);
        
        // Act - Call the method under test
        applicationHandler.handleApproveApplication(commandWrapper);
        
        // Assert
        ResponseWrapper response = applicationHandler.getLastSentResponse();
        
        // Verify the response
        assertNotNull(response);
        assertEquals(ResponseFromServer.SUCCESS, response.getResponse());
        
        ApplicationReviewResponse reviewResponse = (ApplicationReviewResponse) response.getData();
        assertNotNull(reviewResponse);
        assertEquals(approvedApplication, reviewResponse.getApplication());
        assertEquals("APPROVED", reviewResponse.getApplication().getStatus());
    }
    
    @Test
    void testHandleApproveApplication_ServiceThrowsException() {
        // Arrange
        ApproveApplicationCommand command = new ApproveApplicationCommand();
        command.setApplicationId(APPLICATION_ID);
        command.setComments("Approved after review");
        CommandWrapper commandWrapper = new CommandWrapper(Command.APPROVE_APPLICATION, command);
        
        // Configure the service to throw an exception
        String errorMessage = "Only administrators can approve applications";
        applicationService.setThrowException(true, errorMessage);
        
        // Act - Call the method under test
        applicationHandler.handleApproveApplication(commandWrapper);
        
        // Assert
        ResponseWrapper response = applicationHandler.getLastSentResponse();
        
        // Verify the response
        assertNotNull(response);
        assertEquals(ResponseFromServer.ERROR, response.getResponse());
        
        ApplicationReviewResponse reviewResponse = (ApplicationReviewResponse) response.getData();
        assertNotNull(reviewResponse);
        assertNull(reviewResponse.getApplication());
    }
    
    // Tests for handleRejectApplication
    
    @Test
    void testHandleRejectApplication_Success() {
        // Arrange
        RejectApplicationCommand command = new RejectApplicationCommand();
        command.setApplicationId(APPLICATION_ID);
        command.setComments("Rejected after review");
        CommandWrapper commandWrapper = new CommandWrapper(Command.REJECT_APPLICATION, command);
        
        // Create a sample application DTO for the response
        ScholarshipApplicationDTO rejectedApplication = createSampleApplicationDTO("REJECTED");
        
        // Configure the service to return the application DTO
        applicationService.setReturnValue(rejectedApplication);
        
        // Act - Call the method under test
        applicationHandler.handleRejectApplication(commandWrapper);
        
        // Assert
        ResponseWrapper response = applicationHandler.getLastSentResponse();
        
        // Verify the response
        assertNotNull(response);
        assertEquals(ResponseFromServer.SUCCESS, response.getResponse());
        
        ApplicationReviewResponse reviewResponse = (ApplicationReviewResponse) response.getData();
        assertNotNull(reviewResponse);
        assertEquals(rejectedApplication, reviewResponse.getApplication());
        assertEquals("REJECTED", reviewResponse.getApplication().getStatus());
    }
    
    @Test
    void testHandleRejectApplication_ServiceThrowsException() {
        // Arrange
        RejectApplicationCommand command = new RejectApplicationCommand();
        command.setApplicationId(APPLICATION_ID);
        command.setComments("Rejected after review");
        CommandWrapper commandWrapper = new CommandWrapper(Command.REJECT_APPLICATION, command);
        
        // Configure the service to throw an exception
        String errorMessage = "Only administrators can reject applications";
        applicationService.setThrowException(true, errorMessage);
        
        // Act - Call the method under test
        applicationHandler.handleRejectApplication(commandWrapper);
        
        // Assert
        ResponseWrapper response = applicationHandler.getLastSentResponse();
        
        // Verify the response
        assertNotNull(response);
        assertEquals(ResponseFromServer.ERROR, response.getResponse());
        
        ApplicationReviewResponse reviewResponse = (ApplicationReviewResponse) response.getData();
        assertNotNull(reviewResponse);
        assertNull(reviewResponse.getApplication());
    }
}
