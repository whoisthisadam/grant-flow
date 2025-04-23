package com.kasperovich.ui;

import com.kasperovich.config.AlertManager;
import com.kasperovich.dto.admin.ActivityDTO;
import com.kasperovich.dto.auth.UserDTO;
import com.kasperovich.dto.scholarship.BudgetDTO;
import com.kasperovich.dto.scholarship.ScholarshipApplicationDTO;
import com.kasperovich.dto.scholarship.ScholarshipProgramDTO;
import com.kasperovich.i18n.LangManager;
import com.kasperovich.operations.ChangeScene;
import com.kasperovich.utils.LoggerUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for the admin dashboard screen.
 */
public class AdminDashboardController extends BaseController {
    private static final Logger logger = LoggerUtil.getLogger(AdminDashboardController.class);
    
    @FXML
    private Label userNameLabel;
    
    @FXML
    private Label roleLabel;
    
    @FXML
    private Label versionLabel;
    
    @FXML
    private Button logoutButton;
    
    @FXML
    private Button dashboardButton;
    
    @FXML
    private Button programsButton;
    
    @FXML
    private Button applicationsButton;
    
    @FXML
    private Button fundAllocationButton;
    
    @FXML
    private Button academicPeriodsButton;
    
    @FXML
    private Button reportsButton;
    
    @FXML
    private Button usersButton;
    
    @FXML
    private Button profileButton;
    
    @FXML
    private Label activeProgramsCount;
    
    @FXML
    private Label pendingApplicationsCount;
    
    @FXML
    private Label totalAllocatedAmount;
    
    @FXML
    private TableView<ActivityDTO> recentActivityTable;
    
    @FXML
    private TableColumn<ActivityDTO, String> activityDateColumn;
    
    @FXML
    private TableColumn<ActivityDTO, String> activityTypeColumn;
    
    @FXML
    private TableColumn<ActivityDTO, String> activityDetailsColumn;
    
    @Setter
    private UserDTO user;
    
    // Store data to pass to other controllers
    @Getter
    private List<ScholarshipProgramDTO> scholarshipPrograms;
    
    @Getter
    private List<ScholarshipApplicationDTO> applications;
    
    @Getter
    private List<BudgetDTO> budgets;
    
    private ObservableList<ActivityDTO> activityData = FXCollections.observableArrayList();
    
    /**
     * Initializes the controller.
     */
    @Override
    public void initializeData() {
        if (user != null) {
            userNameLabel.setText(user.getUsername());
            roleLabel.setText(LangManager.getBundle().getString("dashboard.role") + ": " + user.getRole());
        } else {
            logger.error("User data is null in AdminDashboardController");
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("error.title"),
                LangManager.getBundle().getString("error.user.data.unavailable")
            );
        }
        
        versionLabel.setText(LangManager.getBundle().getString("dashboard.version") + ": " + LangManager.getBundle().getString("app.version"));
        
        // Set up button actions
        dashboardButton.setDisable(true); // Already on dashboard
        
        // Set up navigation button actions
        programsButton.setOnAction(this::handleProgramsAction);
        applicationsButton.setOnAction(this::handleApplicationsAction);
        fundAllocationButton.setOnAction(this::handleFundAllocationAction);
        academicPeriodsButton.setOnAction(this::handleAcademicPeriodsAction);
        reportsButton.setOnAction(this::handleReportsAction);
        profileButton.setOnAction(this::handleProfileAction);
        
        // Set up the activity table
        setupActivityTable();
        
        // Load dashboard data
        loadDashboardData();
        
        updateTexts();
        
        logger.info("Admin dashboard initialized for user: {}", user != null ? user.getUsername() : "unknown");
    }
    
    /**
     * Sets up the activity table.
     */
    private void setupActivityTable() {
        // Initialize the activity table
        recentActivityTable.setItems(activityData);
    }
    
    /**
     * Loads dashboard data from the server.
     */
    private void loadDashboardData() {
        try {
            // Load scholarship programs
            scholarshipPrograms = getClientConnection().getScholarshipPrograms();
            
            // Load applications
            applications = getClientConnection().getAllApplications();
            
            // Load budgets
            budgets = getClientConnection().getAllBudgets();
            
            // Update UI with counts
            updateDashboardCounts();
            
            // Update recent activity
            updateRecentActivity();
            
            logger.info("Admin dashboard data loaded successfully");
        } catch (Exception e) {
            logger.error("Error loading admin dashboard data", e);
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("error.title"),
                LangManager.getBundle().getString("dashboard.error.loading_data") + ": " + e.getMessage()
            );
        }
    }
    
    /**
     * Updates the recent activity table with data.
     */
    private void updateRecentActivity() {
        // Clear existing data
        activityData.clear();
        
        // Add login activity
        activityData.add(new ActivityDTO(
            LocalDateTime.now(),
            LangManager.getBundle().getString("activity.login"),
            LangManager.getBundle().getString("activity.admin.login")
        ));
        
        // Add recent application activities
        if (applications != null && !applications.isEmpty()) {
            // Sort applications by submission date (newest first)
            applications.sort((a1, a2) -> a2.getSubmissionDate().compareTo(a1.getSubmissionDate()));
            
            // Add up to 5 most recent applications
            int count = Math.min(applications.size(), 5);
            for (int i = 0; i < count; i++) {
                ScholarshipApplicationDTO app = applications.get(i);
                activityData.add(new ActivityDTO(
                    app.getSubmissionDate(),
                    LangManager.getBundle().getString("activity.application"),
                    String.format(
                        LangManager.getBundle().getString("activity.new.application"),
                        app.getProgramName(),
                        app.getApplicantFullName()
                    )
                ));
            }
        }
        
        // Add recent program activities if any
        if (scholarshipPrograms != null && !scholarshipPrograms.isEmpty()) {
            // Add up to 3 most recent program activities
            int count = Math.min(scholarshipPrograms.size(), 3);
            for (int i = 0; i < count; i++) {
                ScholarshipProgramDTO program = scholarshipPrograms.get(i);
                String activeStatus = program.isActive() 
                    ? LangManager.getBundle().getString("activity.program.active")
                    : LangManager.getBundle().getString("activity.program.inactive");
                    
                activityData.add(new ActivityDTO(
                    LocalDateTime.now().minusDays(i + 1), // Use a placeholder date
                    LangManager.getBundle().getString("activity.program"),
                    String.format(
                        LangManager.getBundle().getString("activity.scholarship.program"),
                        program.getName(),
                        activeStatus,
                        program.getFundingAmount()
                    )
                ));
            }
        }
        
        // Add budget allocation activities
        if (budgets != null && !budgets.isEmpty()) {
            // Add up to 3 most recent budget allocations
            int count = Math.min(budgets.size(), 3);
            for (int i = 0; i < count; i++) {
                BudgetDTO budget = budgets.get(i);
                activityData.add(new ActivityDTO(
                    LocalDateTime.now().minusDays(i), // Placeholder date
                    LangManager.getBundle().getString("activity.budget"),
                    String.format(
                        LangManager.getBundle().getString("activity.budget.allocated"),
                        budget.getAllocatedAmount(),
                        budget.getAllocatedAmount()
                    )
                ));
            }
        }
        
        // Sort all activities by timestamp (newest first)
        activityData.sort((a1, a2) -> a2.getTimestamp().compareTo(a1.getTimestamp()));
    }
    
    /**
     * Updates the dashboard counts based on loaded data.
     */
    private void updateDashboardCounts() {
        // Update active programs count
        if (scholarshipPrograms != null) {
            long activeCount = scholarshipPrograms.stream()
                    .filter(ScholarshipProgramDTO::isActive)
                    .count();
            activeProgramsCount.setText(String.valueOf(activeCount));
        } else {
            activeProgramsCount.setText("0");
        }
        
        // Update pending applications count
        if (applications != null) {
            long pendingCount = applications.stream()
                    .filter(app -> "PENDING".equals(app.getStatus()))
                    .count();
            pendingApplicationsCount.setText(String.valueOf(pendingCount));
        } else {
            pendingApplicationsCount.setText("0");
        }
        
        // Update total allocated amount
        if (budgets != null) {
            BigDecimal totalAllocated = budgets.stream()
                    .map(BudgetDTO::getAllocatedAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            totalAllocatedAmount.setText(totalAllocated.toString());
        } else {
            totalAllocatedAmount.setText("0");
        }
    }
    
    /**
     * Sets the scholarship programs and updates the UI.
     * 
     * @param scholarshipPrograms the scholarship programs
     */
    public void setScholarshipPrograms(List<ScholarshipProgramDTO> scholarshipPrograms) {
        this.scholarshipPrograms = scholarshipPrograms;
        updateDashboardCounts();
    }
    
    /**
     * Sets the applications and updates the UI.
     * 
     * @param applications the applications
     */
    public void setApplications(List<ScholarshipApplicationDTO> applications) {
        this.applications = applications;
        updateDashboardCounts();
    }
    
    /**
     * Sets the budgets and updates the UI.
     * 
     * @param budgets the budgets
     */
    public void setBudgets(ArrayList<BudgetDTO> budgets) {
        this.budgets = budgets;
        updateDashboardCounts();
    }
    
    /**
     * Updates the UI texts based on the current language.
     */
    @Override
    public void updateTexts() {
        logoutButton.setText(LangManager.getBundle().getString("dashboard.logout"));
        dashboardButton.setText(LangManager.getBundle().getString("admin.button.dashboard"));
        programsButton.setText(LangManager.getBundle().getString("admin.button.programs"));
        applicationsButton.setText(LangManager.getBundle().getString("admin.button.applications"));
        fundAllocationButton.setText(LangManager.getBundle().getString("admin.button.fund-allocation"));
        academicPeriodsButton.setText(LangManager.getBundle().getString("admin.button.academic-periods"));
        reportsButton.setText(LangManager.getBundle().getString("admin.button.reports"));
        usersButton.setText(LangManager.getBundle().getString("admin.button.users"));
        profileButton.setText(LangManager.getBundle().getString("dashboard.button.profile"));
    }
    
    /**
     * Handles the logout button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleLogoutAction(ActionEvent event) {
        try {
            // Close the client connection
            getClientConnection().logout();
            
            // Navigate back to login screen
            ChangeScene.changeScene(
                event,
                "/fxml/login_screen.fxml",
                LangManager.getBundle().getString("login.title"),
                getClientConnection(),
                null
            );
            
            logger.info("User logged out: {}", user.getUsername());
        } catch (Exception e) {
            logger.error("Error during logout", e);
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("error.title"),
                LangManager.getBundle().getString("error.logout") + ": " + e.getMessage()
            );
        }
    }
    
    /**
     * Handles the profile button action.
     * 
     * @param event The action event
     */
    public void handleProfileAction(ActionEvent event) {
        try {
            ChangeScene.changeScene(
                event,
                "/fxml/profile_screen.fxml",
                LangManager.getBundle().getString("profile.title"),
                getClientConnection(),
                user
            );
            
            logger.debug("Navigated to profile screen from admin dashboard");
        } catch (Exception e) {
            logger.error("Error navigating to profile screen", e);
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("navigation.error"),
                LangManager.getBundle().getString("error.navigation.profile") + ": " + e.getMessage()
            );
        }
    }
    
    /**
     * Handles the programs button action.
     * 
     * @param event The action event
     */
    public void handleProgramsAction(ActionEvent event) {
        try {
            ChangeScene.changeScene(
                event,
                "/fxml/admin_scholarship_programs_screen.fxml",
                LangManager.getBundle().getString("admin.programs.title"),
                getClientConnection(),
                user,
                scholarshipPrograms,
                "setScholarshipPrograms"
            );
            
            logger.debug("Navigated to scholarship programs screen from admin dashboard");
        } catch (Exception e) {
            logger.error("Error navigating to scholarship programs screen", e);
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("navigation.error"),
                LangManager.getBundle().getString("error.navigation.programs") + ": " + e.getMessage()
            );
        }
    }
    
    /**
     * Handles the applications button action.
     * 
     * @param event The action event
     */
    public void handleApplicationsAction(ActionEvent event) {
        try {
            ChangeScene.changeScene(
                event,
                "/fxml/admin_application_review.fxml",
                LangManager.getBundle().getString("application.review.title"),
                getClientConnection(),
                user,
                applications,
                "setApplications"
            );
            
            logger.debug("Navigated to application review screen from admin dashboard");
        } catch (Exception e) {
            logger.error("Error navigating to application review screen", e);
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("navigation.error"),
                LangManager.getBundle().getString("error.navigation.applications") + ": " + e.getMessage()
            );
        }
    }
    
    /**
     * Handles the fund allocation button action.
     * 
     * @param event The action event
     */
    public void handleFundAllocationAction(ActionEvent event) {
        try {
            ChangeScene.changeScene(
                event,
                "/fxml/fund_allocation_screen.fxml",
                LangManager.getBundle().getString("fund.allocation.title"),
                getClientConnection(),
                user
            );
            
            logger.debug("Navigated to fund allocation screen from admin dashboard");
        } catch (Exception e) {
            logger.error("Error navigating to fund allocation screen", e);
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("navigation.error"),
                LangManager.getBundle().getString("error.navigation.fund") + ": " + e.getMessage()
            );
        }
    }
    
    /**
     * Handles the academic periods button action.
     * 
     * @param event The action event
     */
    @FXML
    private void handleAcademicPeriodsAction(ActionEvent event) {
        try {
            logger.info("Navigating to academic period management screen");
            ChangeScene.changeScene(
                event,
                "/fxml/academic_period_management.fxml",
                LangManager.getBundle().getString("academic.period.management.title"),
                getClientConnection(),
                user
            );
        } catch (Exception e) {
            logger.error("Error navigating to academic period management screen", e);
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("error.title"),
                LangManager.getBundle().getString("error.navigation")
            );
        }
    }
    
    /**
     * Handles the reports button action.
     * 
     * @param event The action event
     */
    @FXML
    private void handleReportsAction(ActionEvent event) {
        try {
            ChangeScene.changeScene(
                event,
                "/fxml/admin_reports_screen.fxml",
                LangManager.getBundle().getString("reports.title"),
                getClientConnection(),
                user
            );
        } catch (Exception e) {
            logger.error("Error navigating to reports screen", e);
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("error.title"),
                LangManager.getBundle().getString("error.navigation")
            );
        }
    }
    
    @Override
    public String getFxmlPath() {
        return "/fxml/admin_dashboard_screen.fxml";
    }
}
