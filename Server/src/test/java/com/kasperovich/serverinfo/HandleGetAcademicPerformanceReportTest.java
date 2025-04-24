package com.kasperovich.serverinfo;

import com.kasperovich.commands.fromserver.AcademicPerformanceReportResponse;
import com.kasperovich.commands.fromserver.ResponseFromServer;
import com.kasperovich.commands.fromserver.ResponseWrapper;
import com.kasperovich.commands.toserver.Command;
import com.kasperovich.commands.toserver.CommandWrapper;
import com.kasperovich.dto.auth.UserDTO;
import com.kasperovich.dto.report.AcademicPerformanceReportDTO;
import com.kasperovich.dto.report.CourseGradeDTO;
import com.kasperovich.dto.report.PaymentDTO;
import com.kasperovich.dto.scholarship.ScholarshipApplicationDTO;
import com.kasperovich.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the academic performance report logic.
 * This test focuses on the core business logic without the networking aspects.
 */
public class HandleGetAcademicPerformanceReportTest {

    private TestReportHandler reportHandler;
    private TestReportService reportService;
    private final Long AUTHENTICATED_USER_ID = 1L;

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
        private AcademicPerformanceReportDTO returnValue = null;
        
        public void setThrowException(boolean throwException, String message) {
            this.throwException = throwException;
            this.exceptionMessage = message;
        }
        
        public void setReturnValue(AcademicPerformanceReportDTO returnValue) {
            this.returnValue = returnValue;
        }
        
        @Override
        public AcademicPerformanceReportDTO getAcademicPerformanceReport(Long userId) throws Exception {
            if (throwException) {
                throw new Exception(exceptionMessage);
            }
            return returnValue;
        }
    }
    
    /**
     * A simplified handler class that implements just the academic performance report logic
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
         * Handles getting academic performance report
         */
        public void handleGetAcademicPerformanceReport(CommandWrapper commandWrapper) {
            try {
                // Validate user is authenticated
                if (authenticatedUserId == null) {
                    sendResponse(new ResponseWrapper(ResponseFromServer.ERROR, "User not authenticated"));
                    return;
                }

                // Get academic performance report for the authenticated user
                AcademicPerformanceReportDTO report = reportService.getAcademicPerformanceReport(authenticatedUserId);

                // Send response
                AcademicPerformanceReportResponse response = new AcademicPerformanceReportResponse(report);
                sendResponse(new ResponseWrapper(ResponseFromServer.ACADEMIC_PERFORMANCE_REPORT_GENERATED, response));
            } catch (Exception e) {
                String errorMessage = e.getMessage();
                ResponseWrapper response = new ResponseWrapper(ResponseFromServer.ERROR, errorMessage);
                response.setMessage(errorMessage);
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
     * Creates a sample academic performance report for testing
     */
    private AcademicPerformanceReportDTO createSampleReport() {
        AcademicPerformanceReportDTO report = new AcademicPerformanceReportDTO();
        
        // Set user information
        UserDTO user = new UserDTO();
        user.setId(AUTHENTICATED_USER_ID);
        user.setUsername("testuser");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("test.user@example.com");
        report.setUser(user);
        
        // Set student profile information
        report.setStudentId("STU" + AUTHENTICATED_USER_ID);
        report.setMajor("Computer Science");
        report.setDepartment("Information Technology");
        report.setAcademicYear(3);
        report.setEnrollmentDate(LocalDate.now().minusYears(3));
        report.setExpectedGraduationDate(LocalDate.now().plusYears(1));
        report.setCurrentGpa(3.75);
        
        // Set course grades
        List<CourseGradeDTO> courseGrades = new ArrayList<>();
        courseGrades.add(new CourseGradeDTO("CS101", "Introduction to Programming", 4, 4.0, "A", "Fall 2022", LocalDate.of(2022, 12, 15), true));
        courseGrades.add(new CourseGradeDTO("CS201", "Data Structures", 4, 3.7, "A-", "Spring 2023", LocalDate.of(2023, 5, 10), true));
        report.setCourseGrades(courseGrades);
        
        // Set scholarship applications
        List<ScholarshipApplicationDTO> applications = new ArrayList<>();
        ScholarshipApplicationDTO application = new ScholarshipApplicationDTO();
        application.setId(1L);
        application.setApplicantId(AUTHENTICATED_USER_ID);
        application.setProgramId(1L);
        application.setStatus("APPROVED");
        application.setSubmissionDate(LocalDateTime.now().minusMonths(2));
        application.setDecisionDate(LocalDateTime.now().minusMonths(1));
        applications.add(application);
        report.setScholarshipApplications(applications);
        
        // Set payments
        List<PaymentDTO> payments = new ArrayList<>();
        payments.add(new PaymentDTO(
            1L,
            "Academic Excellence Scholarship",
            new BigDecimal("5000.00"),
            LocalDateTime.now().minusWeeks(3),
            "PROCESSED",
            "REF1001"
        ));
        report.setPayments(payments);
        
        // Set summary statistics
        report.setTotalCreditsCompleted(8);
        report.setTotalCreditsInProgress(0);
        report.setAverageGpa(3.85);
        report.setScholarshipsApplied(1);
        report.setScholarshipsApproved(1);
        report.setTotalScholarshipAmount(new BigDecimal("5000.00"));
        
        return report;
    }

    @Test
    void testHandleGetAcademicPerformanceReport_Success() {
        // Arrange
        CommandWrapper commandWrapper = new CommandWrapper(Command.GET_ACADEMIC_PERFORMANCE_REPORT, null);
        
        // Create a sample report
        AcademicPerformanceReportDTO sampleReport = createSampleReport();
        
        // Configure the service to return the sample report
        reportService.setReturnValue(sampleReport);
        
        // Act - Call the method under test
        reportHandler.handleGetAcademicPerformanceReport(commandWrapper);
        
        // Assert
        ResponseWrapper response = reportHandler.getLastSentResponse();
        
        // Verify the response
        assertNotNull(response);
        assertEquals(ResponseFromServer.ACADEMIC_PERFORMANCE_REPORT_GENERATED, response.getResponse());
        
        AcademicPerformanceReportResponse reportResponse = (AcademicPerformanceReportResponse) response.getData();
        assertNotNull(reportResponse);
        
        AcademicPerformanceReportDTO returnedReport = reportResponse.getReport();
        assertNotNull(returnedReport);
        assertEquals(AUTHENTICATED_USER_ID, returnedReport.getUser().getId());
        assertEquals("Computer Science", returnedReport.getMajor());
        assertEquals(2, returnedReport.getCourseGrades().size());
        assertEquals(1, returnedReport.getScholarshipApplications().size());
        assertEquals(1, returnedReport.getPayments().size());
        assertEquals(new BigDecimal("5000.00"), returnedReport.getTotalScholarshipAmount());
    }

    @Test
    void testHandleGetAcademicPerformanceReport_UserNotAuthenticated() {
        // Arrange
        CommandWrapper commandWrapper = new CommandWrapper(Command.GET_ACADEMIC_PERFORMANCE_REPORT, null);
        
        // Set authenticated user ID to null
        reportHandler.setAuthenticatedUserId(null);
        
        // Act - Call the method under test
        reportHandler.handleGetAcademicPerformanceReport(commandWrapper);
        
        // Assert
        ResponseWrapper response = reportHandler.getLastSentResponse();
        
        // Verify the response
        assertNotNull(response);
        assertEquals(ResponseFromServer.ERROR, response.getResponse());
        assertEquals("User not authenticated", response.getMessage());
    }

    @Test
    void testHandleGetAcademicPerformanceReport_ServiceThrowsException() {
        // Arrange
        CommandWrapper commandWrapper = new CommandWrapper(Command.GET_ACADEMIC_PERFORMANCE_REPORT, null);
        
        // Configure the service to throw an exception
        String errorMessage = "Error retrieving academic data";
        reportService.setThrowException(true, errorMessage);
        
        // Act - Call the method under test
        reportHandler.handleGetAcademicPerformanceReport(commandWrapper);
        
        // Assert
        ResponseWrapper response = reportHandler.getLastSentResponse();
        
        // Verify the response
        assertNotNull(response);
        assertEquals(ResponseFromServer.ERROR, response.getResponse());
        assertEquals(errorMessage, response.getMessage());
    }
}
