package com.kasperovich.ui;

import com.kasperovich.config.AlertManager;
import com.kasperovich.commands.toserver.Command;
import com.kasperovich.commands.toserver.CommandWrapper;
import com.kasperovich.commands.toserver.UpdateProfileCommand;
import com.kasperovich.commands.fromserver.ResponseFromServer;
import com.kasperovich.commands.fromserver.ResponseWrapper;
import com.kasperovich.commands.fromserver.UpdateProfileResponse;
import com.kasperovich.dto.auth.UserDTO;
import com.kasperovich.i18n.LangManager;
import com.kasperovich.utils.LoggerUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Setter;
import org.apache.logging.log4j.Logger;

/**
 * Controller for the edit profile dialog.
 */
public class EditProfileDialogController extends BaseController {
    private static final Logger logger = LoggerUtil.getLogger(EditProfileDialogController.class);
    
    @FXML
    private Label titleLabel;
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private TextField firstNameField;
    
    @FXML
    private TextField lastNameField;
    
    @FXML
    private Button cancelButton;
    
    @FXML
    private Button saveButton;
    
    private UserDTO originalUser;
    private UserDTO updatedUser;
    
    // Callback interface for updating the parent controller
    public interface ProfileUpdatedCallback {
        void onProfileUpdated(UserDTO updatedUser);
    }
    
    @Setter
    private ProfileUpdatedCallback callback;
    
    /**
     * Initializes the controller.
     */
    @Override
    public void initializeData() {
        // Set up button actions
        cancelButton.setOnAction(_ -> closeDialog());
        
        saveButton.setOnAction(_ -> {
            if (validateInput()) {
                updateProfile();
            }
        });
        
        // Update UI texts
        updateTexts();
    }
    
    /**
     * Sets up the dialog with the user data.
     *
     * @param user The user data to edit
     */
    public void setup(UserDTO user) {
        this.originalUser = user;
        
        // Populate fields with current user data
        if (user != null) {
            usernameField.setText(user.getUsername());
            emailField.setText(user.getEmail());
            firstNameField.setText(user.getFirstName());
            lastNameField.setText(user.getLastName());
        }
    }
    
    /**
     * Validates the input fields.
     *
     * @return true if input is valid, false otherwise
     */
    private boolean validateInput() {
        // Check if username is empty
        if (usernameField.getText().trim().isEmpty()) {
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("error.title"),
                LangManager.getBundle().getString("profile.error.username_empty")
            );
            return false;
        }
        
        // Check if email is empty
        if (emailField.getText().trim().isEmpty()) {
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("error.title"),
                LangManager.getBundle().getString("profile.error.email_empty")
            );
            return false;
        }
        
        // All validations passed
        return true;
    }
    
    /**
     * Updates the user profile on the server.
     */
    private void updateProfile() {
        // Create command with only modified fields
        String newUsername = null;
        String newEmail = null;
        String newFirstName = null;
        String newLastName = null;
        
        // Only set fields that have been modified
        if (!usernameField.getText().trim().equals(originalUser.getUsername())) {
            newUsername = usernameField.getText().trim();
        }
        
        if (!emailField.getText().trim().equals(originalUser.getEmail())) {
            newEmail = emailField.getText().trim();
        }
        
        if (!firstNameField.getText().trim().equals(originalUser.getFirstName())) {
            newFirstName = firstNameField.getText().trim();
        }
        
        if (!lastNameField.getText().trim().equals(originalUser.getLastName())) {
            newLastName = lastNameField.getText().trim();
        }
        
        // Check if any fields were modified
        if (newUsername == null && newEmail == null && newFirstName == null && newLastName == null) {
            // No changes were made
            AlertManager.showInformationAlert(
                LangManager.getBundle().getString("info.title"),
                LangManager.getBundle().getString("profile.no_changes")
            );
            closeDialog();
            return;
        }
        
        // Send update request to server
        try {
            UserDTO updatedUserDTO = getClientConnection().updateUserProfile(
                newUsername, newFirstName, newLastName, newEmail
            );
            
            if (updatedUserDTO != null) {
                updatedUser = updatedUserDTO;
                
                AlertManager.showInformationAlert(
                    LangManager.getBundle().getString("info.title"),
                    LangManager.getBundle().getString("profile.update_success")
                );
                
                // Notify parent controller
                if (callback != null) {
                    callback.onProfileUpdated(updatedUser);
                }
                
                // Close the dialog
                closeDialog();
            }
        } catch (Exception e) {
            logger.error("Error updating profile", e);
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("error.title"),
                LangManager.getBundle().getString("profile.update_error") + ": " + e.getMessage()
            );
        }
    }
    
    /**
     * Updates the UI texts based on the current language.
     */
    @Override
    public void updateTexts() {
        titleLabel.setText(LangManager.getBundle().getString("profile.edit"));
        cancelButton.setText(LangManager.getBundle().getString("profile.cancel"));
        saveButton.setText(LangManager.getBundle().getString("profile.save"));
    }
    
    /**
     * Closes the dialog.
     */
    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    @Override
    public String getFxmlPath() {
        return "/fxml/edit_profile_dialog.fxml";
    }
}
