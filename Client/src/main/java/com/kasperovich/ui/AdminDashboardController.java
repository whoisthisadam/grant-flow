package com.kasperovich.ui;

import com.kasperovich.config.AlertManager;
import com.kasperovich.dto.auth.UserDTO;
import com.kasperovich.i18n.LangManager;
import com.kasperovich.operations.ChangeScene;
import com.kasperovich.utils.LoggerUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import lombok.Setter;
import org.apache.logging.log4j.Logger;

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
    private Button usersButton;
    
    @FXML
    private Button profileButton;
    
    @Setter
    private UserDTO user;
    
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
                "User data is not available"
            );
        }
        
        versionLabel.setText(LangManager.getBundle().getString("dashboard.version") + ": 1.0.0");
        
        // Set up button actions
        dashboardButton.setDisable(true); // Already on dashboard
        
        // Set up navigation button actions
        programsButton.setOnAction(this::handleProgramsAction);
        profileButton.setOnAction(this::handleProfileAction);
        
        updateTexts();
        
        logger.info("Admin dashboard initialized for user: {}", user != null ? user.getUsername() : "unknown");
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
                "Error during logout: " + e.getMessage()
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
                "Could not navigate to profile screen: " + e.getMessage()
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
                user
            );
            
            logger.debug("Navigated to scholarship programs screen from admin dashboard");
        } catch (Exception e) {
            logger.error("Error navigating to scholarship programs screen", e);
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("navigation.error"),
                "Could not navigate to scholarship programs screen: " + e.getMessage()
            );
        }
    }
    
    @Override
    public String getFxmlPath() {
        return "/fxml/admin_dashboard_screen.fxml";
    }
}
