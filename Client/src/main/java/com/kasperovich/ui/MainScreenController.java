package com.kasperovich.ui;

import com.kasperovich.clientconnection.ClientConnection;
import com.kasperovich.config.AlertManager;
import com.kasperovich.utils.LoggerUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Controller for the main screen of the application.
 */
public class MainScreenController {
    private static final Logger logger = LoggerUtil.getLogger(MainScreenController.class);
    
    @FXML
    private Button loginButton;
    
    @FXML
    private Button registerButton;
    
    private ClientConnection clientConnection;
    
    /**
     * Initializes the controller.
     */
    public void initialize() {
        // Initialization code here
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
            Stage stage = (Stage) loginButton.getScene().getWindow();
            
            // Set the new scene on the current stage
            stage.setScene(scene);
            stage.setTitle("Grant Flow - Login");
            stage.show();
            
            logger.info("Navigated to login screen");
        } catch (IOException e) {
            logger.error("Error loading login screen", e);
            AlertManager.showErrorAlert("Navigation Error", "Could not load login screen: " + e.getMessage());
        }
    }
    
    /**
     * Handles the register button action.
     *
     * @param event The action event
     */
    @FXML
    public void handleRegisterAction(ActionEvent event) {
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
            Stage stage = (Stage) registerButton.getScene().getWindow();
            
            // Set the new scene on the current stage
            stage.setScene(scene);
            stage.setTitle("Grant Flow - Registration");
            stage.show();
            
            logger.info("Navigated to registration screen");
        } catch (IOException e) {
            logger.error("Error loading registration screen", e);
            AlertManager.showErrorAlert("Navigation Error", "Could not load registration screen: " + e.getMessage());
        }
    }
}
