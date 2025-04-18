package com.kasperovich.ui;

import com.kasperovich.config.AlertManager;
import com.kasperovich.dto.auth.UserDTO;
import com.kasperovich.dto.scholarship.ScholarshipApplicationDTO;
import com.kasperovich.i18n.LangManager;
import com.kasperovich.operations.ChangeScene;
import com.kasperovich.utils.LoggerUtil;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import lombok.Setter;
import org.apache.logging.log4j.Logger;

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

    /**
     * -- SETTER --
     *  Sets the user for this controller.
     *
     * @param user The user to set
     */
    @Setter
    private UserDTO user;
    
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
            
            // Add some placeholder recent activities
            recentActivityList.setItems(FXCollections.observableArrayList(
                    "Login: " + java.time.LocalDateTime.now(),
                    "Profile updated: Yesterday"
            ));
        }
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
        // Placeholder for profile functionality
        AlertManager.showInformationAlert("Profile", "Profile functionality is not implemented yet.");
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
            ChangeScene.changeScene(new ActionEvent(logoutButton, null), "/fxml/scholarship_programs_screen.fxml", LangManager.getBundle().getString("scholarship_programs.title"), getClientConnection(), user);
            logger.debug("Navigated to scholarship programs screen from {}", source);
            return true;
        } catch (Exception e) {
            logger.error("Error navigating to scholarship programs screen from {}", source, e);
            AlertManager.showErrorAlert(LangManager.getBundle().getString("navigation.error"), LangManager.getBundle().getString("navigation.could_not_load_scholarship_programs_screen") + e.getMessage());
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
            // Get the user's applications
            List<ScholarshipApplicationDTO> applications = getClientConnection().getUserApplications();
            
            if (applications.isEmpty()) {
                AlertManager.showInformationAlert(
                    LangManager.getBundle().getString("applications.no_applications_title"),
                    LangManager.getBundle().getString("applications.no_applications_message")
                );
                return;
            }
            
            // Use the changeSceneWithData method to navigate and pass the applications data
            ChangeScene.changeSceneWithData(
                    event, 
                    "/fxml/scholarship_applications_screen.fxml", 
                    LangManager.getBundle().getString("applications.title"), 
                    getClientConnection(), 
                    user,
                    applications,
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
     * Handles the update profile action button.
     * 
     * @param event The action event
     */
    @FXML
    public void handleUpdateProfileAction(ActionEvent event) {
        // Placeholder for update profile functionality
        AlertManager.showInformationAlert("Profile", 
                "Profile update functionality is not implemented yet.");
        logger.debug("Update profile action requested but functionality is not implemented");
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
}
