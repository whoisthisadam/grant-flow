package com.kasperovich.ui;

import com.kasperovich.config.AlertManager;
import com.kasperovich.dto.auth.UserDTO;
import com.kasperovich.dto.scholarship.AcademicPeriodDTO;
import com.kasperovich.dto.scholarship.ScholarshipApplicationDTO;
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
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for the scholarship application form.
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
                if (period == null) {
                    return "";
                }
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
                return period.getName() + " (" + 
                       period.getStartDate().format(formatter) + " - " + 
                       period.getEndDate().format(formatter) + ")";
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
            if (deadline != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
                deadlineLabel.setText(deadline.format(formatter));
            } else {
                deadlineLabel.setText(LangManager.getBundle().getString("scholarship.not_available"));
            }
            
            // Update status label
            statusLabel.setText(LangManager.getBundle().getString("scholarship.status.ready_to_apply"));
            statusLabel.setStyle("-fx-text-fill: green;");
            
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
        
        // Update character count feedback
        int currentLength = motivationTextArea.getText().length();
        if (currentLength < 100) {
            statusLabel.setText(LangManager.getBundle().getString("scholarship.status.motivation_length") + " " + currentLength);
            statusLabel.setStyle("-fx-text-fill: red;");
        } else {
            statusLabel.setText(LangManager.getBundle().getString("scholarship.status.ready_to_submit"));
            statusLabel.setStyle("-fx-text-fill: green;");
        }
    }
    
    /**
     * Handles the submit button action.
     *
     * @param event The action event
     */
    @FXML
    public void handleSubmitAction(ActionEvent event) {
        if (scholarshipProgram == null || user == null) {
            AlertManager.showErrorAlert("Error", "Missing scholarship program or user data.");
            return;
        }
        
        // Get selected academic period
        AcademicPeriodDTO selectedPeriod = academicPeriodComboBox.getValue();
        if (selectedPeriod == null) {
            AlertManager.showErrorAlert("Error", "Please select an academic period.");
            return;
        }
        
        // Submit application
        try {
            ScholarshipApplicationDTO application = getClientConnection().submitScholarshipApplication(
                    scholarshipProgram.getId(),
                    selectedPeriod.getId(),
                    motivationTextArea.getText()
            );
            
            if (application != null) {
                AlertManager.showInformationAlert(
                    "Application Submitted", 
                    "Your application for " + scholarshipProgram.getName() + " has been submitted successfully.\n\n" +
                    "Application ID: " + application.getId() + "\n" +
                    "Status: " + application.getStatus() + "\n" +
                    "Submission Date: " + application.getSubmissionDate().format(DateTimeFormatter.ofPattern("MMMM d, yyyy HH:mm"))
                );
                navigateToDashboard(event);
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
        navigateToDashboard(event);
    }
    
    /**
     * Handles the back button action.
     *
     * @param event The action event
     */
    @FXML
    public void handleBackAction(ActionEvent event) {
        navigateToDashboard(event);
    }
    
    /**
     * Navigates back to the dashboard screen.
     * 
     * @param event The action event that triggered the navigation
     */
    private void navigateToDashboard(ActionEvent event) {
        ChangeScene.changeScene(event, 
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
            List<AcademicPeriodDTO> periods = getClientConnection().getAcademicPeriods();
            
            // Filter to only show active periods
            List<AcademicPeriodDTO> activePeriods = periods.stream()
                    .filter(AcademicPeriodDTO::isActive)
                    .toList();
            
            academicPeriods.setAll(activePeriods);
            
            if (activePeriods.isEmpty()) {
                statusLabel.setText(LangManager.getBundle().getString("scholarship.no_active_periods"));
                statusLabel.setStyle("-fx-text-fill: red;");
                submitButton.setDisable(true);
            } else if (activePeriods.size() == 1) {
                // Auto-select if there's only one option
                academicPeriodComboBox.setValue(activePeriods.get(0));
            }
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
    }
    
    @Override
    public String getFxmlPath() {
        return "/fxml/scholarship_application_screen.fxml";
    }
}
