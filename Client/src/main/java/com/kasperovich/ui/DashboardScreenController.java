package com.kasperovich.ui;

import com.kasperovich.config.AlertManager;
import com.kasperovich.dto.auth.UserDTO;
import com.kasperovich.dto.scholarship.ScholarshipApplicationDTO;
import com.kasperovich.dto.scholarship.ScholarshipProgramDTO;
import com.kasperovich.i18n.LangManager;
import com.kasperovich.operations.ChangeScene;
import com.kasperovich.utils.LoggerUtil;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Setter;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the dashboard screen.
 */
public class DashboardScreenController extends BaseController {
    private static final Logger logger = LoggerUtil.getLogger(DashboardScreenController.class);
    
    @FXML
    private Label userNameLabel;
    
    @FXML
    private Label roleLabel;
    
    @FXML
    private Button logoutButton;
    
    @FXML
    private ListView<String> recentActivityList;
    
    @FXML
    private Button dashboardButton;
    
    @FXML
    private Button scholarshipsButton;
    
    @FXML
    private Button applicationsButton;
    
    @FXML
    private Button profileButton;
    
    @FXML
    private Label availableScholarshipsCount;
    
    @FXML
    private Label myApplicationsCount;

    @Setter
    private UserDTO user;
    
    // Store data to pass to other controllers
    private List<ScholarshipApplicationDTO> userApplications;
    private List<ScholarshipProgramDTO> scholarshipPrograms;
    
    /**
     * Initializes the controller.
     * Called after dependencies are injected.
     */
    @Override
    public void initializeData() {
        // Set up the sidebar navigation buttons
        setupNavigationButtons();
        
        // Initialize user data if available
        if (user != null) {
            initializeUserData();
        }
        
        updateTexts();
    }

    @Override
    public String getFxmlPath() {
        return "/fxml/dashboard_screen.fxml";
    }

    @Override
    public void updateTexts() {
        logoutButton.setText(LangManager.getBundle().getString("dashboard.logout"));
        // Add more components as needed
    }

    @FXML
    public void handleLanguageSwitch(ActionEvent event) {
        super.handleLanguageSwitch(event);
    }
    
    /**
     * Initializes the user data in the UI.
     */
    public void initializeUserData() {
        if (user != null) {
            userNameLabel.setText(user.getFirstName() + " " + user.getLastName());
            roleLabel.setText("Role: " + user.getRole());
            
            // Load data from the server
            loadDashboardData();
        }
    }
    
    /**
     * Loads dashboard data from the server.
     */
    private void loadDashboardData() {
        try {
            // Load user applications
            userApplications = getClientConnection().getUserApplications();
            
            // Load scholarship programs
            scholarshipPrograms = getClientConnection().getScholarshipPrograms();
            
            // Update UI with counts
            updateDashboardCounts();
            
            // Update recent activity list
            updateRecentActivityList();
            
            logger.info("Dashboard data loaded successfully");
        } catch (Exception e) {
            logger.error("Error loading dashboard data", e);
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("error.title"),
                LangManager.getBundle().getString("dashboard.error.loading_data") + ": " + e.getMessage()
            );
        }
    }
    
    /**
     * Updates the dashboard counts based on loaded data.
     */
    private void updateDashboardCounts() {
        // Update applications count
        if (userApplications != null) {
            myApplicationsCount.setText(String.valueOf(userApplications.size()));
        } else {
            myApplicationsCount.setText("0");
        }
        
        // Update available scholarships count (only count active and accepting applications)
        if (scholarshipPrograms != null) {
            long availableCount = scholarshipPrograms.stream()
                    .filter(ScholarshipProgramDTO::isActive)
                    .filter(ScholarshipProgramDTO::isAcceptingApplications)
                    .count();
            availableScholarshipsCount.setText(String.valueOf(availableCount));
        } else {
            availableScholarshipsCount.setText("0");
        }
    }
    
    /**
     * Updates the recent activity list based on loaded data.
     */
    private void updateRecentActivityList() {
        // Create a list for recent activities
        List<String> activities = new ArrayList<>();
        
        // Add login activity
        activities.add("Login: " + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        // Add recent applications if any
        if (userApplications != null && !userApplications.isEmpty()) {
            // Sort applications by submission date (newest first)
            userApplications.sort((a1, a2) -> a2.getSubmissionDate().compareTo(a1.getSubmissionDate()));
            
            // Add up to 5 most recent applications
            int count = Math.min(userApplications.size(), 5);
            for (int i = 0; i < count; i++) {
                ScholarshipApplicationDTO app = userApplications.get(i);
                activities.add(String.format(
                    "%s: %s - %s",
                    LangManager.getBundle().getString("dashboard.activity.application_submitted"),
                    app.getProgramName(),
                    app.getSubmissionDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                ));
            }
        }
        
        // Update the list view
        recentActivityList.setItems(FXCollections.observableArrayList(activities));
    }
    
    /**
     * Handles the logout button action.
     *
     * @param event The action event
     */
    @FXML
    public void handleLogoutAction(ActionEvent event) {
        try {
            // Logout the user
            boolean success;
            try {
                success = getClientConnection().logout();
            } catch (ClassNotFoundException e) {
                logger.error("ClassNotFoundException during logout", e);
                AlertManager.showErrorAlert("Logout Error", "Error during logout: " + e.getMessage());
                return;
            }
            
            if (success) {
                // Navigate back to the main screen using ChangeScene utility
                ChangeScene.changeScene(event, 
                        "/fxml/main_screen.fxml", 
                        "Grant Flow", 
                        getClientConnection(), 
                        null);
                
                logger.info("User logged out and navigated to main screen");
            } else {
                logger.warn("Logout failed");
                AlertManager.showErrorAlert("Logout Failed", "Could not log out. Please try again.");
            }
        } catch (Exception e) {
            logger.error("Error navigating to main screen after logout", e);
            AlertManager.showErrorAlert("Navigation Error", "Could not navigate to main screen: " + e.getMessage());
        }
    }
    
    /**
     * Handles the profile button action.
     *
     * @param event The action event
     */
    @FXML
    public void handleProfileAction(ActionEvent event) {
        try {
            ChangeScene.changeScene(
                    event, 
                    "/fxml/profile_screen.fxml", 
                    LangManager.getBundle().getString("profile.title"), 
                    getClientConnection(), 
                    user);
            
            logger.info("Navigated to profile screen");
        } catch (Exception e) {
            logger.error("Error navigating to profile screen", e);
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("navigation.error"),
                "Could not navigate to profile screen: " + e.getMessage()
            );
        }
    }
    
    /**
     * Handles the settings button action.
     *
     * @param event The action event
     */
    @FXML
    public void handleSettingsAction(ActionEvent event) {
        // Placeholder for settings functionality
        AlertManager.showInformationAlert("Settings", "Settings functionality is not implemented yet.");
    }
    
    /**
     * Handles the help button action.
     *
     * @param event The action event
     */
    @FXML
    public void handleHelpAction(ActionEvent event) {
        // Placeholder for help functionality
        AlertManager.showInformationAlert("Help", "Help functionality is not implemented yet.");
    }
    
    /**
     * Navigates to the scholarship programs screen.
     * 
     * @param source The source of the navigation (for logging purposes)
     * @return true if navigation was successful, false otherwise
     */
    private boolean navigateToScholarshipProgramsScreen(String source) {
        try {
            // Use the changeSceneWithData method to navigate and pass the scholarship programs data
            ChangeScene.changeScene(
                    new ActionEvent(logoutButton, null), 
                    "/fxml/scholarship_programs_screen.fxml", 
                    LangManager.getBundle().getString("scholarship_programs.title"), 
                    getClientConnection(), 
                    user,
                    scholarshipPrograms,
                    "setScholarshipPrograms");
            
            logger.debug("Navigated to scholarship programs screen from {}", source);
            return true;
        } catch (Exception e) {
            logger.error("Error navigating to scholarship programs screen from {}", source, e);
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("navigation.error"), 
                LangManager.getBundle().getString("navigation.could_not_load_scholarship_programs_screen") + e.getMessage()
            );
            return false;
        }
    }
    
    /**
     * Handles the view scholarships button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleViewScholarshipsAction(ActionEvent event) {
        navigateToScholarshipProgramsScreen("View Scholarships button");
    }
    
    /**
     * Handles the view applications button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleViewApplicationsAction(ActionEvent event) {
        try {
            // Check if we have applications data
            if (userApplications == null) {
                // Load applications if not already loaded
                userApplications = getClientConnection().getUserApplications();
            }
            
            if (userApplications.isEmpty()) {
                AlertManager.showInformationAlert(
                    LangManager.getBundle().getString("applications.no_applications_title"),
                    LangManager.getBundle().getString("applications.no_applications_message")
                );
                return;
            }
            
            // Use the changeSceneWithData method to navigate and pass the applications data
            ChangeScene.changeScene(
                    event, 
                    "/fxml/scholarship_applications_screen.fxml", 
                    LangManager.getBundle().getString("applications.title"), 
                    getClientConnection(), 
                    user,
                    userApplications,
                    "setApplications");
            
            logger.info("Navigated to scholarship applications screen");
        } catch (Exception e) {
            logger.error("Error navigating to scholarship applications screen", e);
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("navigation.error"),
                LangManager.getBundle().getString("navigation.could_not_load_applications_screen") + e.getMessage()
            );
        }
    }
    
    /**
     * Handles the apply action button.
     * 
     * @param event The action event
     */
    @FXML
    public void handleApplyAction(ActionEvent event) {
        // Navigate to the scholarship programs screen
        navigateToScholarshipProgramsScreen("Apply button");
    }
    
    /**
     * Handles the update profile action.
     * Opens the edit profile dialog directly.
     * 
     * @param event the action event
     */
    @FXML
    public void handleUpdateProfileAction(ActionEvent event) {
        try {
            // Load the edit profile dialog FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/edit_profile_dialog.fxml"));
            loader.setResources(LangManager.getBundle());
            Parent root = loader.load();
            
            // Get the controller and set up the dialog
            EditProfileDialogController controller = loader.getController();
            controller.setClientConnection(getClientConnection());
            controller.setup(user);
            
            // Set up the callback for when profile is updated
            controller.setCallback(updatedUser -> {
                // Update the user data
                this.user = updatedUser;
                
                // Update the display
                userNameLabel.setText(user.getUsername());
                
                logger.info("User profile updated successfully: {}", updatedUser.getUsername());
            });
            
            // Initialize the controller data
            controller.initializeData();
            
            // Create and show the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle(LangManager.getBundle().getString("profile.edit"));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(dashboardButton.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();
            
            logger.debug("Opened edit profile dialog for user: {}", user.getUsername());
        } catch (Exception e) {
            logger.error("Error opening edit profile dialog", e);
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("error.title"),
                "Could not open edit profile dialog: " + e.getMessage()
            );
        }
    }
    
    /**
     * Handles the check status action button.
     * 
     * @param event The action event
     */
    @FXML
    public void handleCheckStatusAction(ActionEvent event) {
        // Placeholder for check status functionality
        AlertManager.showInformationAlert("Feature Disabled", 
                "Application status check functionality is temporarily disabled.");
        logger.debug("Check status action requested but functionality is disabled");
    }

    /**
     * Adds a new application to the user's applications list and updates the dashboard.
     * 
     * @param application The new application to add
     */
    public void addNewApplication(ScholarshipApplicationDTO application) {
        if (application != null) {
            // Initialize the applications list if needed
            if (userApplications == null) {
                userApplications = new ArrayList<>();
            }
            
            // Add the new application to the list
            userApplications.add(application);
            
            // Update the dashboard counts and activity list
            updateDashboardCounts();
            updateRecentActivityList();
            
            logger.info("Added new application to dashboard: {}", application.getId());
        }
    }
    
    /**
     * Sets up the sidebar navigation buttons with their action handlers.
     */
    private void setupNavigationButtons() {
        // Dashboard button just refreshes the current view
        dashboardButton.setOnAction(event -> {
            // Refresh dashboard data
            initializeUserData();
        });
        
        // Scholarships button navigates to scholarship programs screen
        scholarshipsButton.setOnAction(this::handleViewScholarshipsAction);
        
        // Applications button navigates to applications screen
        applicationsButton.setOnAction(this::handleViewApplicationsAction);
        
        // Profile button shows profile screen
        profileButton.setOnAction(this::handleProfileAction);
    }
    
    /**
     * Handles the reports button action.
     *
     * @param event the action event
     */
    @FXML
    private void handleReportsButtonAction(ActionEvent event) {
        try {
            logger.info("Navigating to academic performance report screen");
            ChangeScene.changeScene(event, 
                "/fxml/academic_performance_report_screen.fxml", 
                LangManager.getBundle().getString("report.academic.performance.title"), 
                getClientConnection(), 
                user);
        } catch (Exception e) {
            logger.error("Error navigating to academic performance report screen", e);
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("error.title"),
                e.getMessage()
            );
        }
    }
}
