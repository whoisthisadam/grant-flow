package com.kasperovich.ui;

import com.kasperovich.config.AlertManager;
import com.kasperovich.dto.auth.UserDTO;
import com.kasperovich.i18n.LangManager;
import com.kasperovich.operations.ChangeScene;
import com.kasperovich.utils.LoggerUtil;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import lombok.Setter;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Locale;

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
                // Navigate back to the main screen
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main_screen.fxml"));
                Parent root = loader.load();
                
                // Get the controller and set the client connection
                MainScreenController mainController = loader.getController();
                mainController.setClientConnection(getClientConnection());
                
                // Create a new scene
                Scene scene = new Scene(root);
                
                // Get the current stage
                Stage stage = (Stage) logoutButton.getScene().getWindow();
                
                // Set the new scene on the current stage
                stage.setScene(scene);
                stage.setTitle("Grant Flow");
                stage.show();
                
                logger.info("User logged out and navigated to main screen");
            } else {
                logger.warn("Logout failed");
                AlertManager.showErrorAlert("Logout Failed", "Could not log out. Please try again.");
            }
        } catch (IOException e) {
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
        // Placeholder for view applications functionality
        AlertManager.showInformationAlert("Feature Disabled", 
                "Scholarship application functionality is temporarily disabled.");
        logger.debug("View applications action requested but functionality is disabled");
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
}
