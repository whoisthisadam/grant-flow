package com.kasperovich.ui;

import com.kasperovich.clientconnection.ClientConnection;
import com.kasperovich.config.AlertManager;
import com.kasperovich.dto.auth.UserDTO;
import com.kasperovich.entities.UserRole;
import com.kasperovich.i18n.LangManager;
import com.kasperovich.operations.ChangeScene;
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
import java.util.Locale;

/**
 * Controller for the registration screen.
 */
public class RegisterScreenController extends BaseController {
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
    private Button languageButton;
    
    @FXML
    private Hyperlink loginLink;
    
    @FXML
    private Label statusLabel;

    /**
     * Initializes the controller.
     * Called after dependencies are injected.
     */
    @Override
    public void initializeData() {
        // Initialize role combo box
        roleComboBox.getItems().addAll(UserRole.STUDENT.name(), UserRole.ADMIN.name());
        roleComboBox.setValue(UserRole.STUDENT.name());
        updateTexts();
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
            showStatus(LangManager.getBundle().getString("register.error.empty_fields"));
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            showStatus(LangManager.getBundle().getString("register.error.passwords_do_not_match"));
            return;
        }
        
        if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            showStatus(LangManager.getBundle().getString("register.error.invalid_email"));
            return;
        }
        
        // Disable register button to prevent multiple clicks
        registerButton.setDisable(true);
        showStatus(LangManager.getBundle().getString("register.status.registering"));
        
        // Run registration in background thread to avoid freezing UI
        new Thread(() -> {
            try {
                UserDTO user = getClientConnection().register(username, password, email, firstName, lastName, role);
                
                Platform.runLater(() -> {
                    if (user != null) {
                        logger.info("User registered successfully: {}", username);
                        navigateToDashboard(user);
                    } else {
                        logger.warn("Registration failed for user: {}", username);
                        showStatus(LangManager.getBundle().getString("register.error.username_or_email_exists"));
                        registerButton.setDisable(false);
                    }
                });
            } catch (Exception e) {
                logger.error("Error during registration", e);
                Platform.runLater(() -> {
                    showStatus(LangManager.getBundle().getString("register.error.error_connecting_to_server"));
                    registerButton.setDisable(false);
                    AlertManager.showErrorAlert(LangManager.getBundle().getString("register.error.registration_error"), LangManager.getBundle().getString("register.error.error_connecting_to_server") + ": " + e.getMessage());
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
     * Handles the login link action.
     *
     * @param event The action event
     */
    @FXML
    public void handleLoginLinkAction(ActionEvent event) {
        ChangeScene.changeScene(event, "/fxml/login_screen.fxml", LangManager.getBundle().getString("login.title"), getClientConnection(), null);
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
        ChangeScene.changeScene(new ActionEvent(registerButton, null), "/fxml/dashboard_screen.fxml", LangManager.getBundle().getString("dashboard.title"), getClientConnection(), user);
    }

    @Override
    public String getFxmlPath() {
        return "/fxml/register_screen.fxml";
    }

    @Override
    public void updateTexts() {
        registerButton.setText(LangManager.getBundle().getString("register.button"));
        cancelButton.setText(LangManager.getBundle().getString("register.cancel"));
        loginLink.setText(LangManager.getBundle().getString("register.login"));
        usernameField.setPromptText(LangManager.getBundle().getString("register.username"));
        passwordField.setPromptText(LangManager.getBundle().getString("register.password"));
        confirmPasswordField.setPromptText(LangManager.getBundle().getString("register.confirmpassword"));
        emailField.setPromptText(LangManager.getBundle().getString("register.email"));
        firstNameField.setPromptText(LangManager.getBundle().getString("register.firstname"));
        lastNameField.setPromptText(LangManager.getBundle().getString("register.lastname"));
        roleComboBox.setPromptText(LangManager.getBundle().getString("register.role"));
        // Add more components as needed
    }

    @FXML
    public void handleLanguageSwitch(ActionEvent event) {
        super.handleLanguageSwitch(event);
    }

    /**
     * Shows the registration screen.
     *
     * @param stage The stage to show the screen on
     * @param clientConnection The client connection to use
     * @throws IOException If an error occurs while loading the screen
     */
    public static void show(Stage stage, ClientConnection clientConnection) throws IOException {
        FXMLLoader loader = new FXMLLoader(RegisterScreenController.class.getResource("/fxml/register_screen.fxml"));
        Parent root = loader.load();
        RegisterScreenController controller = loader.getController();
        controller.setClientConnection(clientConnection);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle(LangManager.getBundle().getString("register.title"));
        stage.show();
    }
}
