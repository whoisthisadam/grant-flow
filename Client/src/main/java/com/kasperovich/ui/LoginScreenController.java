package com.kasperovich.ui;

import com.kasperovich.config.AlertManager;
import com.kasperovich.dto.auth.UserDTO;
import com.kasperovich.i18n.LangManager;
import com.kasperovich.operations.ChangeScene;
import com.kasperovich.utils.LoggerUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.apache.logging.log4j.Logger;

/**
 * Controller for the login screen.
 */
public class LoginScreenController extends BaseController {
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

    /**
     * Initializes the controller.
     * Called after dependencies are injected.
     */
    @Override
    public void initializeData() {
        // Add enter key event handler to password field
        passwordField.setOnAction(this::handleLoginAction);
        updateTexts();
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
            showStatus(LangManager.getBundle().getString("login.error.empty"));
            return;
        }
        
        // Disable login button to prevent multiple clicks
        loginButton.setDisable(true);
        showStatus(LangManager.getBundle().getString("login.logging_in"));
        
        // Run login in background thread to avoid freezing UI
        new Thread(() -> {
            try {
                UserDTO user = getClientConnection().login(username, password);
                
                Platform.runLater(() -> {
                    if (user != null) {
                        logger.info("User logged in successfully: {}", username);
                        navigateToDashboard(user);
                    } else {
                        logger.warn("Login failed for user: {}", username);
                        showStatus(LangManager.getBundle().getString("login.error.invalid"));
                        loginButton.setDisable(false);
                    }
                });
            } catch (Exception e) {
                logger.error("Error during login", e);
                Platform.runLater(() -> {
                    showStatus(LangManager.getBundle().getString("login.error.server"));
                    loginButton.setDisable(false);
                    AlertManager.showErrorAlert(LangManager.getBundle().getString("login.error.title"), LangManager.getBundle().getString("login.error.message") + e.getMessage());
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
        ChangeScene.changeScene(event, "/fxml/main_screen.fxml", LangManager.getBundle().getString("main.title"), getClientConnection(), null);
    }
    
    /**
     * Handles the register link action.
     *
     * @param event The action event
     */
    @FXML
    public void handleRegisterLinkAction(ActionEvent event) {
        ChangeScene.changeScene(event, "/fxml/register_screen.fxml", LangManager.getBundle().getString("register.title"), getClientConnection(), null);
    }
    
    /**
     * Handles the language switch button action.
     *
     * @param event The action event
     */
    @FXML
    public void handleLanguageSwitch(ActionEvent event) {
        super.handleLanguageSwitch(event);
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
        ChangeScene.changeScene(new ActionEvent(loginButton, null), "/fxml/dashboard_screen.fxml", LangManager.getBundle().getString("dashboard.title"), getClientConnection(), user);
    }
    
    @Override
    public String getFxmlPath() {
        return "/fxml/login_screen.fxml";
    }
    
    @Override
    public void updateTexts() {
        loginButton.setText(LangManager.getBundle().getString("login.button"));
        cancelButton.setText(LangManager.getBundle().getString("login.cancel"));
        registerLink.setText(LangManager.getBundle().getString("login.register"));
        // Update other UI elements as needed
    }
}
