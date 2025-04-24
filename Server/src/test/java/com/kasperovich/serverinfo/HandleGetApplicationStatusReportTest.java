package com.kasperovich.serverinfo;

import com.kasperovich.commands.fromserver.ApplicationStatusReportResponse;
import com.kasperovich.commands.fromserver.ResponseFromServer;
import com.kasperovich.commands.fromserver.ResponseWrapper;
import com.kasperovich.commands.toserver.Command;
import com.kasperovich.commands.toserver.CommandWrapper;
import com.kasperovich.commands.toserver.GetApplicationStatusReportCommand;
import com.kasperovich.dto.report.ApplicationStatusDTO;
import com.kasperovich.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the application status report logic.
 * This test focuses on the core business logic without the networking aspects.
 */
public class HandleGetApplicationStatusReportTest {

    private TestReportHandler reportHandler;
    private TestReportService reportService;
    private final Long AUTHENTICATED_USER_ID = 1L;
    private final Long PROGRAM_ID = 100L;
    private final Long PERIOD_ID = 200L;

    @BeforeEach
    void setUp() {
        reportService = new TestReportService();
        reportHandler = new TestReportHandler(reportService, AUTHENTICATED_USER_ID);
    }

    /**
     * Test implementation of ReportService
     */
    private class TestReportService extends ReportService {
        private boolean throwException = false;
        private String exceptionMessage = "";
        private List<ApplicationStatusDTO> returnValue = new ArrayList<>();
        
        public void setThrowException(boolean throwException, String message) {
            this.throwException = throwException;
            this.exceptionMessage = message;
        }
        
        public void setReturnValue(List<ApplicationStatusDTO> returnValue) {
            this.returnValue = returnValue;
        }
        
        @Override
        public List<ApplicationStatusDTO> getApplicationStatusReport(Long programId, Long periodId) throws Exception {
            if (throwException) {
                throw new Exception(exceptionMessage);
            }
            return returnValue;
        }
    }
    
    /**
     * A simplified handler class that implements just the application status report logic
     * without the networking aspects of ClientProcessingThread
     */
    private class TestReportHandler {
        private final List<ResponseWrapper> sentResponses = new ArrayList<>();
        private final ReportService reportService;
        private Long authenticatedUserId;
        
        public TestReportHandler(ReportService service, Long userId) {
            this.reportService = service;
            this.authenticatedUserId = userId;
        }
        
        public void setAuthenticatedUserId(Long userId) {
            this.authenticatedUserId = userId;
        }
        
        /**
         * Handles getting an application status report
         */
        public void handleGetApplicationStatusReport(CommandWrapper commandWrapper) {
            try {
                // Validate user is authenticated
                if (authenticatedUserId == null) {
                    sendResponse(new ResponseWrapper(ResponseFromServer.ERROR, "User not authenticated"));
                    return;
                }

                var command = (GetApplicationStatusReportCommand)commandWrapper.getData();

                // Get application status report
                var report = reportService.getApplicationStatusReport(command.getProgramId(), command.getPeriodId());

                // Send response
                ApplicationStatusReportResponse response = new ApplicationStatusReportResponse(report);
                sendResponse(new ResponseWrapper(ResponseFromServer.SUCCESS, response));
            } catch (Exception e) {
                var response = new ResponseWrapper(ResponseFromServer.ERROR, e.getMessage());
                response.setMessage(e.getMessage());
                sendResponse(response);
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
     * Creates a sample list of application status DTOs for testing
     */
    private List<ApplicationStatusDTO> createSampleApplicationStatusList() {
        List<ApplicationStatusDTO> statusList = new ArrayList<>();
        
        // Add a status report for a program and period
        ApplicationStatusDTO status1 = new ApplicationStatusDTO(
                "Engineering Scholarship",
                "Fall 2025",
                5,  // pending
                3,  // approved
                2,  // rejected
                new BigDecimal("15000.00")  // total amount
        );
        
        // Add another status report for a different program and period
        ApplicationStatusDTO status2 = new ApplicationStatusDTO(
                "Computer Science Scholarship",
                "Spring 2026",
                2,  // pending
                4,  // approved
                1,  // rejected
                new BigDecimal("20000.00")  // total amount
        );
        
        statusList.add(status1);
        statusList.add(status2);
        
        return statusList;
    }

    @Test
    void testHandleGetApplicationStatusReport_Success() {
        // Arrange
        GetApplicationStatusReportCommand command = new GetApplicationStatusReportCommand();
        command.setProgramId(PROGRAM_ID);
        command.setPeriodId(PERIOD_ID);
        CommandWrapper commandWrapper = new CommandWrapper(Command.GET_APPLICATION_STATUS_REPORT, command);
        
        // Create a sample report for the response
        List<ApplicationStatusDTO> statusList = createSampleApplicationStatusList();
        
        // Configure the service to return the status list
        reportService.setReturnValue(statusList);
        
        // Act - Call the method under test
        reportHandler.handleGetApplicationStatusReport(commandWrapper);
        
        // Assert
        ResponseWrapper response = reportHandler.getLastSentResponse();
        
        // Verify the response
        assertNotNull(response);
        assertEquals(ResponseFromServer.SUCCESS, response.getResponse());
        
        ApplicationStatusReportResponse reportResponse = (ApplicationStatusReportResponse) response.getData();
        assertNotNull(reportResponse);
        assertEquals(statusList, reportResponse.getReportData());
        assertEquals(2, reportResponse.getReportData().size());
        
        // Verify the first status report
        ApplicationStatusDTO firstStatus = reportResponse.getReportData().get(0);
        assertEquals("Engineering Scholarship", firstStatus.getProgramName());
        assertEquals("Fall 2025", firstStatus.getPeriodName());
        assertEquals(5, firstStatus.getPendingCount());
        assertEquals(3, firstStatus.getApprovedCount());
        assertEquals(2, firstStatus.getRejectedCount());
        assertEquals(new BigDecimal("15000.00"), firstStatus.getTotalAmount());
    }
    
    @Test
    void testHandleGetApplicationStatusReport_ServiceThrowsException() {
        // Arrange
        GetApplicationStatusReportCommand command = new GetApplicationStatusReportCommand();
        command.setProgramId(PROGRAM_ID);
        command.setPeriodId(PERIOD_ID);
        CommandWrapper commandWrapper = new CommandWrapper(Command.GET_APPLICATION_STATUS_REPORT, command);
        
        // Configure the service to throw an exception
        String errorMessage = "Error generating report: Program not found";
        reportService.setThrowException(true, errorMessage);
        
        // Act - Call the method under test
        reportHandler.handleGetApplicationStatusReport(commandWrapper);
        
        // Assert
        ResponseWrapper response = reportHandler.getLastSentResponse();
        
        // Verify the response
        assertNotNull(response);
        assertEquals(ResponseFromServer.ERROR, response.getResponse());
        assertEquals(errorMessage, response.getMessage());
    }
    
    @Test
    void testHandleGetApplicationStatusReport_EmptyReport() {
        // Arrange
        GetApplicationStatusReportCommand command = new GetApplicationStatusReportCommand();
        command.setProgramId(PROGRAM_ID);
        command.setPeriodId(PERIOD_ID);
        CommandWrapper commandWrapper = new CommandWrapper(Command.GET_APPLICATION_STATUS_REPORT, command);
        
        // Configure the service to return an empty list
        reportService.setReturnValue(new ArrayList<>());
        
        // Act - Call the method under test
        reportHandler.handleGetApplicationStatusReport(commandWrapper);
        
        // Assert
        ResponseWrapper response = reportHandler.getLastSentResponse();
        
        // Verify the response
        assertNotNull(response);
        assertEquals(ResponseFromServer.SUCCESS, response.getResponse());
        
        ApplicationStatusReportResponse reportResponse = (ApplicationStatusReportResponse) response.getData();
        assertNotNull(reportResponse);
        assertTrue(reportResponse.getReportData().isEmpty());
    }
}
