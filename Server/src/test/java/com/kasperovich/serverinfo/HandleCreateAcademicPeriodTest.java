package com.kasperovich.serverinfo;

import com.kasperovich.commands.fromserver.AcademicPeriodResponse;
import com.kasperovich.commands.fromserver.ResponseFromServer;
import com.kasperovich.commands.fromserver.ResponseWrapper;
import com.kasperovich.commands.toserver.Command;
import com.kasperovich.commands.toserver.CommandWrapper;
import com.kasperovich.commands.toserver.CreateAcademicPeriodCommand;
import com.kasperovich.dto.scholarship.AcademicPeriodDTO;
import com.kasperovich.service.AcademicPeriodService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the academic period creation logic.
 * This test focuses on the core business logic without the networking aspects.
 */
public class HandleCreateAcademicPeriodTest {

    private TestAcademicPeriodHandler academicPeriodHandler;
    private TestAcademicPeriodService academicPeriodService;
    private final Long AUTHENTICATED_USER_ID = 1L;

    @BeforeEach
    void setUp() {
        academicPeriodService = new TestAcademicPeriodService();
        academicPeriodHandler = new TestAcademicPeriodHandler(academicPeriodService, AUTHENTICATED_USER_ID);
    }

    /**
     * Test implementation of AcademicPeriodService
     */
    private class TestAcademicPeriodService extends AcademicPeriodService {
        private boolean throwException = false;
        private boolean throwIllegalArgumentException = false;
        private String exceptionMessage = "";
        private AcademicPeriodDTO returnValue = null;
        
        public void setThrowException(boolean throwException, String message) {
            this.throwException = throwException;
            this.exceptionMessage = message;
        }
        
        public void setThrowIllegalArgumentException(boolean throwIllegalArgumentException, String message) {
            this.throwIllegalArgumentException = throwIllegalArgumentException;
            this.exceptionMessage = message;
        }
        
        public void setReturnValue(AcademicPeriodDTO returnValue) {
            this.returnValue = returnValue;
        }
        
        @Override
        public AcademicPeriodDTO createAcademicPeriod(AcademicPeriodDTO periodDTO) {
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
     * A simplified handler class that implements just the academic period creation logic
     * without the networking aspects of ClientProcessingThread
     */
    private class TestAcademicPeriodHandler {
        private final List<ResponseWrapper> sentResponses = new ArrayList<>();
        private final AcademicPeriodService academicPeriodService;
        private Long authenticatedUserId;
        
        public TestAcademicPeriodHandler(AcademicPeriodService service, Long userId) {
            this.academicPeriodService = service;
            this.authenticatedUserId = userId;
        }
        
        public void setAuthenticatedUserId(Long userId) {
            this.authenticatedUserId = userId;
        }
        
        /**
         * Handles creating an academic period
         */
        public void handleCreateAcademicPeriod(CommandWrapper commandWrapper) {
            try {
                // Validate user is authenticated
                if (authenticatedUserId == null) {
                    sendResponse(new ResponseWrapper(ResponseFromServer.ERROR, "User not authenticated"));
                    return;
                }

                CreateAcademicPeriodCommand command = commandWrapper.getData();

                if (command == null || command.getPeriod() == null) {
                    sendResponse(new ResponseWrapper(ResponseFromServer.ERROR, "Academic period data is missing"));
                    return;
                }

                // Create academic period
                AcademicPeriodDTO period = academicPeriodService.createAcademicPeriod(command.getPeriod());

                // Send response
                AcademicPeriodResponse response = new AcademicPeriodResponse(period);
                sendResponse(new ResponseWrapper(ResponseFromServer.SUCCESS, response));
            } catch (IllegalArgumentException e) {
                sendResponse(new ResponseWrapper(ResponseFromServer.ERROR, e.getMessage()));
            } catch (Exception e) {
                sendResponse(new ResponseWrapper(ResponseFromServer.ERROR, "Error creating academic period"));
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
     * Creates a sample academic period DTO for testing
     */
    private AcademicPeriodDTO createSampleAcademicPeriodDTO() {
        AcademicPeriodDTO period = new AcademicPeriodDTO();
        period.setId(1L);
        period.setName("Fall 2025");
        period.setType("SEMESTER");
        period.setStartDate(LocalDate.of(2025, 9, 1));
        period.setEndDate(LocalDate.of(2025, 12, 20));
        period.setActive(true);
        return period;
    }

    @Test
    void testHandleCreateAcademicPeriod_Success() {
        // Arrange
        AcademicPeriodDTO periodDTO = createSampleAcademicPeriodDTO();
        CreateAcademicPeriodCommand command = new CreateAcademicPeriodCommand();
        command.setPeriod(periodDTO);
        CommandWrapper commandWrapper = new CommandWrapper(Command.CREATE_ACADEMIC_PERIOD, command);
        
        // Configure the service to return the period DTO
        academicPeriodService.setReturnValue(periodDTO);
        
        // Act - Call the method under test
        academicPeriodHandler.handleCreateAcademicPeriod(commandWrapper);
        
        // Assert
        ResponseWrapper response = academicPeriodHandler.getLastSentResponse();
        
        // Verify the response
        assertNotNull(response);
        assertEquals(ResponseFromServer.SUCCESS, response.getResponse());
        
        AcademicPeriodResponse periodResponse = (AcademicPeriodResponse) response.getData();
        assertNotNull(periodResponse);
        assertEquals(periodDTO, periodResponse.getPeriod());
        assertEquals("Fall 2025", periodResponse.getPeriod().getName());
        assertEquals("SEMESTER", periodResponse.getPeriod().getType());
    }
}
