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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Controller for the registration screen.
 */
public class RegisterScreenController {
    private static final Logger logger = LoggerUtil.getLogger(RegisterScreenController.class);
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private PasswordField confirmPasswordField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private TextField firstNameField;
    
    @FXML
    private TextField lastNameField;
    
    @FXML
    private ComboBox<String> roleComboBox;
    
    @FXML
    private Button registerButton;
    
    @FXML
    private Button cancelButton;
    
    @FXML
    private Hyperlink loginLink;
    
    @FXML
    private Label statusLabel;
    
    private ClientConnection clientConnection;
    
    /**
     * Initializes the controller.
     */
    public void initialize() {
        // Initialize role combo box
        roleComboBox.getItems().addAll("STUDENT", "ADMIN");
        roleComboBox.setValue("STUDENT");
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
     * Handles the register button action.
     *
     * @param event The action event
     */
    @FXML
    public void handleRegisterAction(ActionEvent event) {
        // Get form values
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String email = emailField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String role = roleComboBox.getValue();
        
        // Validate input
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || 
                email.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
            showStatus("Please fill in all fields");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showStatus("Passwords do not match");
            return;
        }
        
        if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            showStatus("Please enter a valid email address");
            return;
        }
        
        // Disable register button to prevent multiple clicks
        registerButton.setDisable(true);
        showStatus("Registering...");
        
        // Run registration in background thread to avoid freezing UI
        new Thread(() -> {
            try {
                UserDTO user = clientConnection.register(username, password, email, firstName, lastName, role);
                
                Platform.runLater(() -> {
                    if (user != null) {
                        logger.info("User registered successfully: {}", username);
                        navigateToDashboard(user);
                    } else {
                        logger.warn("Registration failed for user: {}", username);
                        showStatus("Username or email already exists");
                        registerButton.setDisable(false);
                    }
                });
            } catch (Exception e) {
                logger.error("Error during registration", e);
                Platform.runLater(() -> {
                    showStatus("Error connecting to server");
                    registerButton.setDisable(false);
                    AlertManager.showErrorAlert("Registration Error", "Could not connect to server: " + e.getMessage());
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
     * Handles the login link action.
     *
     * @param event The action event
     */
    @FXML
    public void handleLoginLinkAction(ActionEvent event) {
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
            Stage stage = (Stage) loginLink.getScene().getWindow();
            
            // Set the new scene on the current stage
            stage.setScene(scene);
            stage.setTitle("Grant Flow - Login");
            stage.show();
        } catch (IOException e) {
            logger.error("Error loading login screen", e);
            AlertManager.showErrorAlert("Navigation Error", "Could not load login screen: " + e.getMessage());
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
            Stage stage = (Stage) registerButton.getScene().getWindow();
            
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
