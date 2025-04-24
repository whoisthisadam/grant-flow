package com.kasperovich.serverinfo;

import com.kasperovich.commands.fromserver.ResponseFromServer;
import com.kasperovich.commands.fromserver.ResponseWrapper;
import com.kasperovich.commands.toserver.Command;
import com.kasperovich.commands.toserver.CommandWrapper;
import com.kasperovich.dto.scholarship.ScholarshipProgramDTO;
import com.kasperovich.service.ScholarshipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the scholarship program listing logic.
 * This test focuses on the core business logic without the networking aspects.
 */
public class HandleGetScholarshipProgramsTest {

    private TestScholarshipHandler scholarshipHandler;
    private TestScholarshipService scholarshipService;

    @BeforeEach
    void setUp() {
        scholarshipService = new TestScholarshipService();
        scholarshipHandler = new TestScholarshipHandler(scholarshipService);
    }

    /**
     * Test implementation of ScholarshipService
     */
    private class TestScholarshipService extends ScholarshipService {
        private boolean throwException = false;
        private String exceptionMessage = "";
        private List<ScholarshipProgramDTO> returnValue = new ArrayList<>();
        
        public void setThrowException(boolean throwException, String message) {
            this.throwException = throwException;
            this.exceptionMessage = message;
        }
        
        public void setReturnValue(List<ScholarshipProgramDTO> returnValue) {
            this.returnValue = returnValue;
        }
        
        @Override
        public List<ScholarshipProgramDTO> getAllScholarshipPrograms() {
            if (throwException) {
                throw new RuntimeException(exceptionMessage);
            }
            return returnValue;
        }
    }
    
    /**
     * A simplified handler class that implements just the scholarship program listing logic
     * without the networking aspects of ClientProcessingThread
     */
    private class TestScholarshipHandler {
        private final List<ResponseWrapper> sentResponses = new ArrayList<>();
        private final ScholarshipService scholarshipService;
        
        public TestScholarshipHandler(ScholarshipService service) {
            this.scholarshipService = service;
        }
        
        /**
         * Handles getting scholarship programs
         */
        public void handleGetScholarshipPrograms(CommandWrapper commandWrapper) {
            try {
                // Get all scholarship programs
                List<ScholarshipProgramDTO> programs = scholarshipService.getAllScholarshipPrograms();

                // Create response with the programs list wrapped as a serializable ArrayList
                ResponseWrapper response = new ResponseWrapper(
                        ResponseFromServer.SCHOLARSHIP_PROGRAMS_FOUND, 
                        new ArrayList<>(programs)
                );

                sendResponse(response);
            } catch (Exception e) {
                String errorMessage = "Error getting scholarship programs: " + e.getMessage();
                sendResponse(new ResponseWrapper(ResponseFromServer.ERROR, errorMessage));
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
    void testHandleGetScholarshipPrograms_Success() {
        // Arrange
        CommandWrapper commandWrapper = new CommandWrapper(Command.GET_SCHOLARSHIP_PROGRAMS, null);
        
        // Create test scholarship programs
        List<ScholarshipProgramDTO> programs = new ArrayList<>();
        
        ScholarshipProgramDTO program1 = new ScholarshipProgramDTO();
        program1.setId(1L);
        program1.setName("Academic Excellence Scholarship");
        program1.setDescription("For students with outstanding academic achievements");
        programs.add(program1);
        
        ScholarshipProgramDTO program2 = new ScholarshipProgramDTO();
        program2.setId(2L);
        program2.setName("Research Innovation Grant");
        program2.setDescription("For students conducting innovative research");
        programs.add(program2);
        
        // Configure the service to return the programs
        scholarshipService.setReturnValue(programs);
        
        // Act - Call the method under test
        scholarshipHandler.handleGetScholarshipPrograms(commandWrapper);
        
        // Assert
        ResponseWrapper response = scholarshipHandler.getLastSentResponse();
        
        // Verify the response
        assertNotNull(response);
        assertEquals(ResponseFromServer.SCHOLARSHIP_PROGRAMS_FOUND, response.getResponse());
        
        List<ScholarshipProgramDTO> returnedPrograms = (List<ScholarshipProgramDTO>) response.getData();
        assertEquals(2, returnedPrograms.size());
        assertEquals("Academic Excellence Scholarship", returnedPrograms.get(0).getName());
        assertEquals("Research Innovation Grant", returnedPrograms.get(1).getName());
    }

    @Test
    void testHandleGetScholarshipPrograms_EmptyList() {
        // Arrange
        CommandWrapper commandWrapper = new CommandWrapper(Command.GET_SCHOLARSHIP_PROGRAMS, null);
        
        // Configure the service to return an empty list
        scholarshipService.setReturnValue(new ArrayList<>());
        
        // Act - Call the method under test
        scholarshipHandler.handleGetScholarshipPrograms(commandWrapper);
        
        // Assert
        ResponseWrapper response = scholarshipHandler.getLastSentResponse();
        
        // Verify the response
        assertNotNull(response);
        assertEquals(ResponseFromServer.SCHOLARSHIP_PROGRAMS_FOUND, response.getResponse());
        
        List<ScholarshipProgramDTO> returnedPrograms = (List<ScholarshipProgramDTO>) response.getData();
        assertTrue(returnedPrograms.isEmpty());
    }

    @Test
    void testHandleGetScholarshipPrograms_ServiceThrowsException() {
        // Arrange
        CommandWrapper commandWrapper = new CommandWrapper(Command.GET_SCHOLARSHIP_PROGRAMS, null);
        
        // Configure the service to throw an exception
        String errorMessage = "Database connection failed";
        scholarshipService.setThrowException(true, errorMessage);
        
        // Act - Call the method under test
        scholarshipHandler.handleGetScholarshipPrograms(commandWrapper);
        
        // Assert
        ResponseWrapper response = scholarshipHandler.getLastSentResponse();
        
        // Verify the response
        assertNotNull(response);
        assertEquals(ResponseFromServer.ERROR, response.getResponse());
        assertEquals("Error getting scholarship programs: " + errorMessage, response.getMessage());
    }
}
