package com.kasperovich.ui;

import com.kasperovich.clientconnection.ClientConnection;
import com.kasperovich.config.AlertManager;
import com.kasperovich.dto.auth.UserDTO;
import com.kasperovich.utils.LoggerUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Controller for the login screen.
 */
public class LoginScreenController {
    private static final Logger logger = LoggerUtil.getLogger(LoginScreenController.class);
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Button loginButton;
    
    @FXML
    private Button cancelButton;
    
    @FXML
    private Hyperlink registerLink;
    
    @FXML
    private Label statusLabel;
    
    private ClientConnection clientConnection;
    
    /**
     * Initializes the controller.
     */
    public void initialize() {
        // Add enter key event handler to password field
        passwordField.setOnAction(event -> handleLoginAction(event));
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
     * Handles the login button action.
     *
     * @param event The action event
     */
    @FXML
    public void handleLoginAction(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            showStatus("Please enter both username and password");
            return;
        }
        
        // Disable login button to prevent multiple clicks
        loginButton.setDisable(true);
        showStatus("Logging in...");
        
        // Run login in background thread to avoid freezing UI
        new Thread(() -> {
            try {
                UserDTO user = clientConnection.login(username, password);
                
                Platform.runLater(() -> {
                    if (user != null) {
                        logger.info("User logged in successfully: {}", username);
                        navigateToDashboard(user);
                    } else {
                        logger.warn("Login failed for user: {}", username);
                        showStatus("Invalid username or password");
                        loginButton.setDisable(false);
                    }
                });
            } catch (Exception e) {
                logger.error("Error during login", e);
                Platform.runLater(() -> {
                    showStatus("Error connecting to server");
                    loginButton.setDisable(false);
                    AlertManager.showErrorAlert("Login Error", "Could not connect to server: " + e.getMessage());
                });
            }
        }).start();
    }
    
    /**
     * Handles the cancel button action.
     *
     * @param event The action event
     */
    @FXML
    public void handleCancelAction(ActionEvent event) {
        try {
            // Load the main welcome screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main_screen.fxml"));
            Parent root = loader.load();
            
            // Get the controller and set the client connection
            MainScreenController mainController = loader.getController();
            mainController.setClientConnection(clientConnection);
            
            // Create a new scene
            Scene scene = new Scene(root);
            
            // Get the current stage
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            
            // Set the new scene on the current stage
            stage.setScene(scene);
            stage.setTitle("Grant Flow");
            stage.show();
            
            logger.info("Navigated back to main welcome screen");
        } catch (IOException e) {
            logger.error("Error loading main welcome screen", e);
            AlertManager.showErrorAlert("Navigation Error", "Could not load main screen: " + e.getMessage());
        }
    }
    
    /**
     * Handles the register link action.
     *
     * @param event The action event
     */
    @FXML
    public void handleRegisterLinkAction(ActionEvent event) {
        try {
            // Load the register screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register_screen.fxml"));
            Parent root = loader.load();
            
            // Get the controller and set the client connection
            RegisterScreenController registerController = loader.getController();
            registerController.setClientConnection(clientConnection);
            
            // Create a new scene
            Scene scene = new Scene(root);
            
            // Get the current stage
            Stage stage = (Stage) registerLink.getScene().getWindow();
            
            // Set the new scene on the current stage
            stage.setScene(scene);
            stage.setTitle("Grant Flow - Registration");
            stage.show();
        } catch (IOException e) {
            logger.error("Error loading register screen", e);
            AlertManager.showErrorAlert("Navigation Error", "Could not load registration screen: " + e.getMessage());
        }
    }
    
    /**
     * Shows a status message.
     *
     * @param message The message to show
     */
    private void showStatus(String message) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
    }
    
    /**
     * Navigates to the dashboard screen.
     *
     * @param user The authenticated user
     */
    private void navigateToDashboard(UserDTO user) {
        try {
            // Load the dashboard screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard_screen.fxml"));
            Parent root = loader.load();
            
            // Get the controller and set the client connection and user
            DashboardScreenController dashboardController = loader.getController();
            dashboardController.setClientConnection(clientConnection);
            dashboardController.setUser(user);
            dashboardController.initializeUserData();
            
            // Create a new scene
            Scene scene = new Scene(root);
            
            // Get the current stage
            Stage stage = (Stage) loginButton.getScene().getWindow();
            
            // Set the new scene on the current stage
            stage.setScene(scene);
            stage.setTitle("Grant Flow - Dashboard");
            stage.show();
        } catch (IOException e) {
            logger.error("Error loading dashboard screen", e);
            AlertManager.showErrorAlert("Navigation Error", "Could not load dashboard screen: " + e.getMessage());
        }
    }
}
