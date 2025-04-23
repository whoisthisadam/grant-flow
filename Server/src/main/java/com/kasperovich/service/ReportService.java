package com.kasperovich.service;

import com.kasperovich.dto.auth.UserDTO;
import com.kasperovich.dto.report.*;
import com.kasperovich.dto.scholarship.AcademicPeriodDTO;
import com.kasperovich.dto.scholarship.ScholarshipApplicationDTO;
import com.kasperovich.dto.scholarship.ScholarshipProgramDTO;
import com.kasperovich.entities.User;
import com.kasperovich.entities.UserRole;
import com.kasperovich.utils.DTOConverter;
import com.kasperovich.utils.LoggerUtil;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating various reports
 */
public class ReportService {
    private static final Logger logger = LoggerUtil.getLogger(ReportService.class);
    private final ScholarshipService scholarshipService;
    private final ScholarshipApplicationService applicationService;
    private final UserService userService;
    private final AcademicPeriodService academicPeriodService;
    private final DTOConverter dtoConverter;

    /**
     * Creates a new report service
     */
    public ReportService() {
        this.scholarshipService = new ScholarshipService();
        this.applicationService = new ScholarshipApplicationService();
        this.userService = new UserService();
        this.academicPeriodService = new AcademicPeriodService();
        this.dtoConverter = new DTOConverter();
        logger.debug("ReportService initialized");
    }

    /**
     * Generates a scholarship distribution report for the given date range
     *
     * @param startDate the start date of the report period
     * @param endDate the end date of the report period
     * @return a list of scholarship distribution data
     */
    public List<ScholarshipDistributionDTO> getScholarshipDistributionReport(LocalDate startDate, LocalDate endDate) throws Exception {
        logger.debug("Generating scholarship distribution report from {} to {}", startDate, endDate);
        
        // Get all scholarship programs
        List<ScholarshipProgramDTO> programs = scholarshipService.getAllScholarshipPrograms();
        
        // Get all applications within the date range
        List<ScholarshipApplicationDTO> applications = applicationService.getAllApplications().stream()
            .filter(app -> {
                LocalDateTime submissionDate = app.getSubmissionDate();
                return submissionDate != null && 
                       !submissionDate.toLocalDate().isBefore(startDate) && 
                       !submissionDate.toLocalDate().isAfter(endDate);
            })
            .toList();
        
        // Group applications by program
        Map<Long, List<ScholarshipApplicationDTO>> applicationsByProgram = new HashMap<>();
        for (ScholarshipApplicationDTO app : applications) {
            Long programId = app.getProgramId();
            if (!applicationsByProgram.containsKey(programId)) {
                applicationsByProgram.put(programId, new ArrayList<>());
            }
            applicationsByProgram.get(programId).add(app);
        }
        
        // Generate report data
        List<ScholarshipDistributionDTO> reportData = new ArrayList<>();
        for (ScholarshipProgramDTO program : programs) {
            Long programId = program.getId();
            List<ScholarshipApplicationDTO> programApplications = applicationsByProgram.getOrDefault(programId, new ArrayList<>());
            
            int totalApplications = programApplications.size();
            if (totalApplications == 0) {
                continue; // Skip programs with no applications in the period
            }
            
            int approvedApplications = (int) programApplications.stream()
                .filter(app -> "APPROVED".equals(app.getStatus()))
                .count();
            
            double approvalRate = totalApplications > 0 
                ? (double) approvedApplications / totalApplications * 100 
                : 0;
            
            reportData.add(new ScholarshipDistributionDTO(
                program.getName(),
                program.getFundingAmount(),
                totalApplications,
                approvedApplications,
                approvalRate
            ));
        }
        
        logger.debug("Generated scholarship distribution report with {} entries", reportData.size());
        return reportData;
    }
    
    /**
     * Generates an application status report filtered by program and/or period
     *
     * @param programId the program ID to filter by (can be null for all programs)
     * @param periodId the period ID to filter by (can be null for all periods)
     * @return a list of application status data
     */
    public List<ApplicationStatusDTO> getApplicationStatusReport(Long programId, Long periodId) throws Exception {
        logger.debug("Generating application status report for programId: {}, periodId: {}", programId, periodId);
        
        // Get all applications
        List<ScholarshipApplicationDTO> allApplications = applicationService.getAllApplications();
        
        // Filter applications by program and period if specified
        List<ScholarshipApplicationDTO> filteredApplications = allApplications.stream()
            .filter(app -> programId == null || app.getProgramId().equals(programId))
            .filter(app -> periodId == null || app.getPeriodId().equals(periodId))
            .toList();
        
        // Group applications by program and period
        Map<String, ApplicationStatusDTO> reportMap = new HashMap<>();
        
        for (ScholarshipApplicationDTO app : filteredApplications) {
            String programName = Optional.ofNullable(scholarshipService.getScholarshipProgramById(app.getProgramId()))
                .map(ScholarshipProgramDTO::getName)
                .orElse("Unknown Program");
                
            String periodName = Optional.ofNullable(academicPeriodService.getAcademicPeriodById(app.getPeriodId()))
                .map(AcademicPeriodDTO::getName)
                .orElse("Unknown Period");
            
            String key = programName + "|" + periodName;
            
            ApplicationStatusDTO statusDTO = reportMap.getOrDefault(key, 
                new ApplicationStatusDTO(programName, periodName, 0, 0, 0, BigDecimal.ZERO));
            
            // Update counts based on application status
            switch (app.getStatus()) {
                case "PENDING":
                    statusDTO.setPendingCount(statusDTO.getPendingCount() + 1);
                    break;
                case "APPROVED":
                    statusDTO.setApprovedCount(statusDTO.getApprovedCount() + 1);
                    // Add funding amount for approved applications
                    BigDecimal currentTotal = statusDTO.getTotalAmount();
                    BigDecimal programAmount = Optional.ofNullable(scholarshipService.getScholarshipProgramById(app.getProgramId()))
                        .map(ScholarshipProgramDTO::getFundingAmount)
                        .orElse(BigDecimal.ZERO);
                    statusDTO.setTotalAmount(currentTotal.add(programAmount));
                    break;
                case "REJECTED":
                    statusDTO.setRejectedCount(statusDTO.getRejectedCount() + 1);
                    break;
            }
            
            reportMap.put(key, statusDTO);
        }
        
        List<ApplicationStatusDTO> reportData = new ArrayList<>(reportMap.values());
        logger.debug("Generated application status report with {} entries", reportData.size());
        return reportData;
    }
    
    /**
     * Generates a user activity report for the given date range
     *
     * @param startDate the start date of the report period
     * @param endDate the end date of the report period
     * @return a list of user activity data
     */
    public List<UserActivityDTO> getUserActivityReport(LocalDate startDate, LocalDate endDate) throws Exception {
        logger.debug("Generating user activity report from {} to {}", startDate, endDate);
        
        // Get all users and their creation dates
        Map<String, LocalDate> userCreationDates = userService.getAllUsers().stream()
            .filter(user -> UserRole.STUDENT.name().equals(user.getRole()))
            .collect(Collectors.toMap(
                user -> user.getId().toString(),
                user -> user.getCreatedAt().toLocalDate()
            ));
        
        // Get all applications and their submission dates
        Map<String, LocalDateTime> applicationSubmissionDates = applicationService.getAllApplications().stream()
            .collect(Collectors.toMap(
                app -> app.getId().toString(),
                    ScholarshipApplicationDTO::getSubmissionDate
            ));
        
        // Generate monthly data points between start and end dates
        List<UserActivityDTO> reportData = new ArrayList<>();
        YearMonth currentYearMonth = YearMonth.from(startDate);
        YearMonth endYearMonth = YearMonth.from(endDate);
        
        while (!currentYearMonth.isAfter(endYearMonth)) {
            LocalDate monthStart = currentYearMonth.atDay(1);
            LocalDate monthEnd = currentYearMonth.atEndOfMonth();
            
            // Count new users in this month
            int newUserCount = (int) userCreationDates.values().stream()
                .filter(date -> !date.isBefore(monthStart) && !date.isAfter(monthEnd))
                .count();
            
            // Count applications in this month
            int applicationCount = (int) applicationSubmissionDates.values().stream()
                .filter(Objects::nonNull)
                .filter(dateTime -> {
                    LocalDate date = dateTime.toLocalDate();
                    return !date.isBefore(monthStart) && !date.isAfter(monthEnd);
                })
                .count();
            
            // Add data point if there was any activity
            if (newUserCount > 0 || applicationCount > 0) {
                String monthLabel = currentYearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                reportData.add(new UserActivityDTO(monthLabel, newUserCount, applicationCount));
            }
            
            // Move to next month
            currentYearMonth = currentYearMonth.plusMonths(1);
        }
        
        logger.debug("Generated user activity report with {} entries", reportData.size());
        return reportData;
    }
    
    /**
     * Generates an academic performance report for the specified user
     *
     * @param userId the ID of the user to generate the report for
     * @return the academic performance report data
     * @throws Exception if an error occurs while generating the report
     */
    public AcademicPerformanceReportDTO getAcademicPerformanceReport(Long userId) throws Exception {
        logger.debug("Generating academic performance report for user ID: {}", userId);
        
        // Get user information
        User u= userService.getUserById(userId);
        UserDTO user = dtoConverter.convertToDTO(u);
        if (user == null) {
            throw new Exception("User not found");
        }
        
        // Create a new report DTO
        AcademicPerformanceReportDTO report = new AcademicPerformanceReportDTO();
        report.setUser(user);
        
        try {
            // Get student profile information
            // In a real implementation, this would fetch from a StudentProfileRepository
            // For now, we'll use placeholder data based on the user
            report.setStudentId("STU" + userId);
            report.setMajor("Computer Science");
            report.setDepartment("Information Technology");
            report.setAcademicYear(3);
            report.setEnrollmentDate(LocalDate.now().minusYears(3));
            report.setExpectedGraduationDate(LocalDate.now().plusYears(1));
            report.setCurrentGpa(3.75);
            
            // Get course grades
            // In a real implementation, this would fetch from a CourseGradeRepository
            // For now, we'll use placeholder data
            List<CourseGradeDTO> courseGrades = new ArrayList<>();
            courseGrades.add(new CourseGradeDTO("CS101", "Introduction to Programming", 4, 4.0, "A", "Fall 2022", LocalDate.of(2022, 12, 15), true));
            courseGrades.add(new CourseGradeDTO("CS201", "Data Structures", 4, 3.7, "A-", "Spring 2023", LocalDate.of(2023, 5, 10), true));
            courseGrades.add(new CourseGradeDTO("CS301", "Algorithms", 3, 3.3, "B+", "Fall 2023", LocalDate.of(2023, 12, 15), true));
            courseGrades.add(new CourseGradeDTO("CS401", "Database Systems", 4, 3.7, "A-", "Spring 2024", LocalDate.of(2024, 5, 10), true));
            report.setCourseGrades(courseGrades);
            
            // Get scholarship applications
            List<ScholarshipApplicationDTO> applications = applicationService.getUserApplications(userId);
            report.setScholarshipApplications(applications);
            
            // Get payment information
            // In a real implementation, this would fetch from a PaymentRepository
            // For now, we'll use placeholder data based on approved applications
            List<PaymentDTO> payments = new ArrayList<>();
            for (ScholarshipApplicationDTO app : applications) {
                if ("APPROVED".equals(app.getStatus())) {
                    ScholarshipProgramDTO program = scholarshipService.getScholarshipProgramById(app.getProgramId());
                    if (program != null) {
                        payments.add(new PaymentDTO(
                            (long) payments.size() + 1,
                            program.getName(),
                            program.getFundingAmount(),
                            app.getDecisionDate() != null ? app.getDecisionDate().plusDays(7) : LocalDateTime.now(),
                            "PROCESSED",
                            "REF" + (1000 + payments.size())
                        ));
                    }
                }
            }
            report.setPayments(payments);
            
            // Calculate summary statistics
            int totalCredits = courseGrades.stream().mapToInt(CourseGradeDTO::getCredits).sum();
            report.setTotalCreditsCompleted(totalCredits);
            report.setTotalCreditsInProgress(0); // Placeholder
            
            double averageGpa = courseGrades.stream()
                .filter(CourseGradeDTO::isIncludedInGpa)
                .mapToDouble(grade -> grade.getGradeValue() * grade.getCredits())
                .sum() / courseGrades.stream()
                    .filter(CourseGradeDTO::isIncludedInGpa)
                    .mapToInt(CourseGradeDTO::getCredits)
                    .sum();
            report.setAverageGpa(averageGpa);
            
            report.setScholarshipsApplied(applications.size());
            report.setScholarshipsApproved((int) applications.stream()
                .filter(app -> "APPROVED".equals(app.getStatus()))
                .count());
            
            BigDecimal totalAmount = payments.stream()
                .map(PaymentDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            report.setTotalScholarshipAmount(totalAmount);
            
            logger.debug("Generated academic performance report for user ID: {}", userId);
            return report;
        } catch (Exception e) {
            logger.error("Error generating academic performance report", e);
            throw new Exception("Error generating academic performance report: " + e.getMessage());
        }
    }
}
