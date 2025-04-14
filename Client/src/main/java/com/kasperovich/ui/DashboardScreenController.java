package com.kasperovich.ui;

import com.kasperovich.clientconnection.ClientConnection;
import com.kasperovich.config.AlertManager;
import com.kasperovich.dto.auth.UserDTO;
import com.kasperovich.utils.LoggerUtil;
import javafx.application.Platform;
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
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;

/**
 * Controller for the dashboard screen.
 */
public class DashboardScreenController {
    private static final Logger logger = LoggerUtil.getLogger(DashboardScreenController.class);
    
    @FXML
    private Label userNameLabel;
    
    @FXML
    private Label roleLabel;
    
    @FXML
    private Button logoutButton;
    
    @FXML
    private Label availableScholarshipsCount;
    
    @FXML
    private Label myApplicationsCount;
    
    @FXML
    private ListView<String> recentActivityList;
    
    private ClientConnection clientConnection;
    private UserDTO user;
    
    /**
     * Initializes the controller.
     */
    public void initialize() {
        // Initialize with placeholder data
        availableScholarshipsCount.setText("0");
        myApplicationsCount.setText("0");
    }
    
    /**
     * Sets the client connection for this controller.
     *
     * @param clientConnection The client connection to set
     */
    public void setClientConnection(ClientConnection clientConnection) {
        this.clientConnection = clientConnection;
    }
    
    /**
     * Sets the user for this controller.
     *
     * @param user The user to set
     */
    public void setUser(UserDTO user) {
        this.user = user;
    }
    
    /**
     * Initializes the user data in the UI.
     */
    public void initializeUserData() {
        if (user != null) {
            userNameLabel.setText(user.getFirstName() + " " + user.getLastName());
            roleLabel.setText("Role: " + user.getRole());
            
            // Add some placeholder data for demonstration
            availableScholarshipsCount.setText("5");
            myApplicationsCount.setText("2");
            
            // Add some placeholder recent activities
            recentActivityList.setItems(FXCollections.observableArrayList(
                    "Login: " + java.time.LocalDateTime.now().toString(),
                    "Profile updated: Yesterday",
                    "Application submitted: Last week",
                    "Scholarship approved: Last month"
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
        // Disable logout button to prevent multiple clicks
        logoutButton.setDisable(true);
        
        // Run logout in background thread to avoid freezing UI
        new Thread(() -> {
            try {
                boolean success = clientConnection.logout();
                
                Platform.runLater(() -> {
                    if (success) {
                        logger.info("User logged out successfully");
                        navigateToLogin();
                    } else {
                        logger.warn("Logout failed");
                        AlertManager.showWarningAlert("Logout Failed", "Could not log out. Please try again.");
                        logoutButton.setDisable(false);
                    }
                });
            } catch (Exception e) {
                logger.error("Error during logout", e);
                Platform.runLater(() -> {
                    AlertManager.showErrorAlert("Logout Error", "Could not connect to server: " + e.getMessage());
                    logoutButton.setDisable(false);
                });
            }
        }).start();
    }
    
    /**
     * Handles the view scholarships action.
     *
     * @param event The action event
     */
    @FXML
    public void handleViewScholarshipsAction(ActionEvent event) {
        AlertManager.showInformationAlert("Not Implemented", "This feature is not yet implemented.");
    }
    
    /**
     * Handles the view applications action.
     *
     * @param event The action event
     */
    @FXML
    public void handleViewApplicationsAction(ActionEvent event) {
        AlertManager.showInformationAlert("Not Implemented", "This feature is not yet implemented.");
    }
    
    /**
     * Handles the apply for scholarship action.
     *
     * @param event The action event
     */
    @FXML
    public void handleApplyAction(ActionEvent event) {
        AlertManager.showInformationAlert("Not Implemented", "This feature is not yet implemented.");
    }
    
    /**
     * Handles the update profile action.
     *
     * @param event The action event
     */
    @FXML
    public void handleUpdateProfileAction(ActionEvent event) {
        AlertManager.showInformationAlert("Not Implemented", "This feature is not yet implemented.");
    }
    
    /**
     * Handles the check application status action.
     *
     * @param event The action event
     */
    @FXML
    public void handleCheckStatusAction(ActionEvent event) {
        AlertManager.showInformationAlert("Not Implemented", "This feature is not yet implemented.");
    }
    
    @FXML
    public void handleStudentInfoAction(ActionEvent event) {
        // Placeholder for student info functionality
        AlertManager.showInformationAlert("Student Information", "This feature is coming soon!");
    }

    /**
     * Handles the scholarship application button action.
     *
     * @param event The action event
     */
    @FXML
    public void handleScholarshipApplicationAction(ActionEvent event) {
        // Placeholder for scholarship application functionality
        AlertManager.showInformationAlert("Scholarship Application", "This feature is coming soon!");
    }

    /**
     * Handles the academic records button action.
     *
     * @param event The action event
     */
    @FXML
    public void handleAcademicRecordsAction(ActionEvent event) {
        // Placeholder for academic records functionality
        AlertManager.showInformationAlert("Academic Records", "This feature is coming soon!");
    }

    /**
     * Handles the scholarship status button action.
     *
     * @param event The action event
     */
    @FXML
    public void handleScholarshipStatusAction(ActionEvent event) {
        // Placeholder for scholarship status functionality
        AlertManager.showInformationAlert("Scholarship Status", "This feature is coming soon!");
    }

    /**
     * Handles the settings button action.
     *
     * @param event The action event
     */
    @FXML
    public void handleSettingsAction(ActionEvent event) {
        // Placeholder for settings functionality
        AlertManager.showInformationAlert("Settings", "This feature is coming soon!");
    }
    
    /**
     * Navigates to the login screen.
     */
    private void navigateToLogin() {
        try {
            // Load the login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login_screen.fxml"));
            Parent root = loader.load();
            
            // Get the controller and set the client connection
            LoginScreenController loginController = loader.getController();
            loginController.setClientConnection(clientConnection);
            
            // Create a new scene
            Scene scene = new Scene(root);
            
            // Get the current stage
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            
            // Set the new scene on the current stage
            stage.setScene(scene);
            stage.setTitle("Grant Flow - Login");
            stage.show();
        } catch (IOException e) {
            logger.error("Error loading login screen", e);
            AlertManager.showErrorAlert("Navigation Error", "Could not load login screen: " + e.getMessage());
        }
    }
}
