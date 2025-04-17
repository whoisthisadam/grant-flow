package com.kasperovich.ui;

import com.kasperovich.config.AlertManager;
import com.kasperovich.dto.auth.UserDTO;
import com.kasperovich.dto.scholarship.AcademicPeriodDTO;
import com.kasperovich.dto.scholarship.ScholarshipProgramDTO;
import com.kasperovich.i18n.LangManager;
import com.kasperovich.operations.ChangeScene;
import com.kasperovich.utils.LoggerUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.Setter;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;

/**
 * Controller for the scholarship application form.
 * Note: Scholarship functionality has been temporarily disabled.
 */
public class ScholarshipApplicationController extends BaseController {
    private static final Logger logger = LoggerUtil.getLogger(ScholarshipApplicationController.class);
    
    @FXML
    private Label titleLabel;
    
    @FXML
    private Label scholarshipNameLabel;
    
    @FXML
    private Label scholarshipDescriptionLabel;
    
    @FXML
    private Label amountLabel;
    
    @FXML
    private Label deadlineLabel;
    
    @FXML
    private ComboBox<AcademicPeriodDTO> academicPeriodComboBox;
    
    @FXML
    private TextArea motivationTextArea;
    
    @FXML
    private CheckBox termsCheckBox;
    
    @FXML
    private Button submitButton;
    
    @FXML
    private Button cancelButton;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private Button backButton;

    @Setter
    private UserDTO user;
    private ScholarshipProgramDTO scholarshipProgram;
    private ObservableList<AcademicPeriodDTO> academicPeriods = FXCollections.observableArrayList();
    
    /**
     * Initializes the controller.
     * Called after dependencies are injected.
     */
    @Override
    public void initializeData() {
        // Configure academic period combo box
        academicPeriodComboBox.setItems(academicPeriods);
        academicPeriodComboBox.setConverter(new javafx.util.StringConverter<AcademicPeriodDTO>() {
            @Override
            public String toString(AcademicPeriodDTO period) {
                return period == null ? "" : period.getName() + " (" + period.getType() + ")";
            }
            
            @Override
            public AcademicPeriodDTO fromString(String string) {
                return null; // Not needed for combo box
            }
        });
        
        // Add listeners for form validation
        termsCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> validateForm());
        academicPeriodComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
        motivationTextArea.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        
        // Load academic periods if client connection is available
        if (getClientConnection() != null) {
            loadAcademicPeriods();
        }
        
        updateTexts();
    }

    /**
     * Sets the scholarship program for this application.
     *
     * @param program The scholarship program to apply for
     */
    public void setScholarshipProgram(ScholarshipProgramDTO program) {
        this.scholarshipProgram = program;
        
        // Update UI with scholarship details
        if (scholarshipProgram != null) {
            scholarshipNameLabel.setText(scholarshipProgram.getName());
            scholarshipDescriptionLabel.setText(scholarshipProgram.getDescription());
            amountLabel.setText("$" + scholarshipProgram.getFundingAmount());
            
            LocalDate deadline = scholarshipProgram.getApplicationDeadline();
            deadlineLabel.setText(deadline != null ? deadline.toString() : "N/A");
            
            // Validate form
            validateForm();
        }
    }
    
    /**
     * Validates the application form.
     */
    private void validateForm() {
        boolean isValid = termsCheckBox.isSelected() &&
                academicPeriodComboBox.getValue() != null &&
                motivationTextArea.getText() != null &&
                motivationTextArea.getText().length() >= 100;
        
        submitButton.setDisable(!isValid);
    }
    
    /**
     * Handles the submit button action.
     *
     * @param event The action event
     */
    @FXML
    public void handleSubmitAction(ActionEvent event) {
        if (scholarshipProgram == null || user == null) {
            AlertManager.showErrorAlert("Error",  "Missing scholarship program or user data.");
            return;
        }
        
        // Create application DTO
        // ScholarshipApplicationDTO application = new ScholarshipApplicationDTO();
        // application.setUserId(user.getId());
        // application.setScholarshipProgramId(scholarshipProgram.getId());
        // application.setAcademicPeriodId(academicPeriodComboBox.getValue().getId());
        // application.setMotivationLetter(motivationTextArea.getText());
        
        // Submit application
        try {
            // boolean success = clientConnection.submitScholarshipApplication(application);
            boolean success = true; // Temporary placeholder
            
            if (success) {
                AlertManager.showInformationAlert("Success", "Your application for " + scholarshipProgram.getName() + " has been submitted successfully.");
                navigateToDashboard();
            } else {
                AlertManager.showErrorAlert("Error", "Failed to submit your application. Please try again later.");
            }
        } catch (Exception e) {
            logger.error("Error submitting scholarship application", e);
            AlertManager.showErrorAlert("Error", "An error occurred while submitting your application: " + e.getMessage());
        }
    }
    
    /**
     * Handles the cancel button action.
     *
     * @param event The action event
     */
    @FXML
    public void handleCancelAction(ActionEvent event) {
        navigateToDashboard();
    }
    
    /**
     * Handles the back button action.
     *
     * @param event The action event
     */
    @FXML
    public void handleBackAction(ActionEvent event) {
        navigateToDashboard();
    }
    
    /**
     * Navigates back to the dashboard screen.
     */
    private void navigateToDashboard() {
        ChangeScene.changeScene(new ActionEvent(submitButton, null), 
                "/fxml/dashboard_screen.fxml", 
                LangManager.getBundle().getString("dashboard.title"), 
                getClientConnection(), 
                user);
    }
    
    /**
     * Loads academic periods from the server.
     */
    private void loadAcademicPeriods() {
        try {
            // List<AcademicPeriodDTO> periods = clientConnection.getAcademicPeriods();
            // academicPeriods.setAll(periods);
            
            // Temporary placeholder data
            AcademicPeriodDTO period1 = new AcademicPeriodDTO();
            period1.setId(1L);
            period1.setName("Fall 2024");
            period1.setType("Semester");
            
            AcademicPeriodDTO period2 = new AcademicPeriodDTO();
            period2.setId(2L);
            period2.setName("Spring 2025");
            period2.setType("Semester");
            
            academicPeriods.setAll(period1, period2);
        } catch (Exception e) {
            logger.error("Error loading academic periods", e);
            AlertManager.showErrorAlert("Error", "Failed to load academic periods: " + e.getMessage());
        }
    }
    
    @Override
    public void updateTexts() {
        titleLabel.setText(LangManager.getBundle().getString("scholarship.application.title"));
        submitButton.setText(LangManager.getBundle().getString("scholarship.application.submit"));
        cancelButton.setText(LangManager.getBundle().getString("scholarship.application.cancel"));
        backButton.setText(LangManager.getBundle().getString("dashboard.back"));
        statusLabel.setText(LangManager.getBundle().getString("scholarship.status.disabled"));
    }
    
    @Override
    public String getFxmlPath() {
        return "/fxml/scholarship_application_screen.fxml";
    }
}
