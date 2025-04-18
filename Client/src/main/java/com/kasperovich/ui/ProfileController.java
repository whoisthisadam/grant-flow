package com.kasperovich.ui;

import com.kasperovich.config.AlertManager;
import com.kasperovich.dto.auth.UserDTO;
import com.kasperovich.i18n.LangManager;
import com.kasperovich.operations.ChangeScene;
import com.kasperovich.utils.LoggerUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Setter;
import org.apache.logging.log4j.Logger;

/**
 * Controller for the user profile screen.
 */
public class ProfileController extends BaseController {
    private static final Logger logger = LoggerUtil.getLogger(ProfileController.class);
    
    @FXML
    private Label usernameLabel;
    
    @FXML
    private Label emailLabel;
    
    @FXML
    private Label firstNameLabel;
    
    @FXML
    private Label lastNameLabel;
    
    @FXML
    private Label roleLabel;
    
    @FXML
    private Button backButton;
    
    @FXML
    private Button editButton;
    
    @Setter
    private UserDTO user;
    
    /**
     * Initializes the controller.
     * Called after dependencies are injected.
     */
    @Override
    public void initializeData() {
        // Display user information
        if (user != null) {
            displayUserInfo();
        } else {
            logger.error("User data is null in ProfileController");
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("error.title"),
                "User data is not available"
            );
        }
        
        updateTexts();
    }
    
    /**
     * Displays the user information in the UI.
     */
    private void displayUserInfo() {
        usernameLabel.setText(user.getUsername());
        emailLabel.setText(user.getEmail());
        firstNameLabel.setText(user.getFirstName());
        lastNameLabel.setText(user.getLastName());
        roleLabel.setText(user.getRole());
        
        logger.debug("Displayed user profile information for user: {}", user.getUsername());
    }
    
    /**
     * Updates the UI texts based on the current language.
     */
    @Override
    public void updateTexts() {
        backButton.setText(LangManager.getBundle().getString("button.back"));
        editButton.setText(LangManager.getBundle().getString("profile.edit"));
    }
    
    /**
     * Handles the back button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleBackAction(ActionEvent event) {
        try {
            ChangeScene.changeScene(
                event,
                "/fxml/dashboard_screen.fxml",
                LangManager.getBundle().getString("dashboard.title"),
                getClientConnection(),
                user
            );
            logger.debug("Navigated back to dashboard from profile screen");
        } catch (Exception e) {
            logger.error("Error navigating back to dashboard", e);
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("navigation.error"),
                "Could not navigate back to dashboard: " + e.getMessage()
            );
        }
    }
    
    /**
     * Handles the edit button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleEditAction(ActionEvent event) {
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
                
                // Refresh the display
                displayUserInfo();
                
                logger.info("User profile updated successfully: {}", updatedUser.getUsername());
            });
            
            // Initialize the controller data
            controller.initializeData();
            
            // Create and show the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle(LangManager.getBundle().getString("profile.edit"));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(editButton.getScene().getWindow());
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
    
    @Override
    public String getFxmlPath() {
        return "/fxml/profile_screen.fxml";
    }
}
