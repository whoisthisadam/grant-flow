package com.kasperovich.service;

import com.kasperovich.dto.report.ApplicationStatusDTO;
import com.kasperovich.dto.report.ScholarshipDistributionDTO;
import com.kasperovich.dto.report.UserActivityDTO;
import com.kasperovich.dto.scholarship.AcademicPeriodDTO;
import com.kasperovich.dto.scholarship.ScholarshipApplicationDTO;
import com.kasperovich.dto.scholarship.ScholarshipProgramDTO;
import com.kasperovich.entities.UserRole;
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

    /**
     * Creates a new report service
     */
    public ReportService() {
        this.scholarshipService = new ScholarshipService();
        this.applicationService = new ScholarshipApplicationService();
        this.userService = new UserService();
        this.academicPeriodService = new AcademicPeriodService();
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
}
