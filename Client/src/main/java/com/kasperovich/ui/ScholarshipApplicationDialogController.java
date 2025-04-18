package com.kasperovich.ui;

import com.kasperovich.clientconnection.ClientConnection;
import com.kasperovich.dto.scholarship.AcademicPeriodDTO;
import com.kasperovich.dto.scholarship.ScholarshipApplicationDTO;
import com.kasperovich.dto.scholarship.ScholarshipProgramDTO;
import com.kasperovich.i18n.LangManager;
import com.kasperovich.utils.LoggerUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lombok.Getter;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Controller for the scholarship application dialog.
 */
public class ScholarshipApplicationDialogController extends BaseController {
    private static final Logger logger = LoggerUtil.getLogger(ScholarshipApplicationDialogController.class);
    
    @FXML
    private Label titleLabel;
    
    @FXML
    private Label programNameLabel;
    
    @FXML
    private Label academicPeriodLabel;
    
    @FXML
    private ComboBox<AcademicPeriodDTO> academicPeriodComboBox;
    
    @FXML
    private Label additionalInfoLabel;
    
    @FXML
    private TextArea additionalInfoTextArea;
    
    @FXML
    private Button cancelButton;
    
    @FXML
    private Button submitButton;

    private ScholarshipProgramDTO program;

    @Getter
    private ScholarshipApplicationDTO result;
    
    /**
     * Initializes the controller.
     */

    @Override
    public void initializeData() {
        // Set up academic period combo box
        academicPeriodComboBox.setConverter(new StringConverter<AcademicPeriodDTO>() {
            @Override
            public String toString(AcademicPeriodDTO period) {
                return period == null ? "" : period.getName();
            }

            @Override
            public AcademicPeriodDTO fromString(String string) {
                return null; // Not needed for ComboBox
            }
        });
        
        // Set up button actions
        cancelButton.setOnAction(event -> {
            result = null;
            closeDialog();
        });
        
        submitButton.setOnAction(event -> {
            if (validateInput()) {
                submitApplication();
            }
        });
        
        // Update UI texts
        updateTexts();
    }
    
    /**
     * Sets up the dialog with the necessary data.
     *
     * @param program The scholarship program to apply for
     */
    public void setup(ScholarshipProgramDTO program) {
        this.program = program;
        
        // Update program name label
        programNameLabel.setText(LangManager.getBundle().getString("scholarship.dialog.program") + ": " + program.getName());
        
        // Load academic periods
        loadAcademicPeriods();
    }
    
    /**
     * Loads academic periods from the server.
     */
    private void loadAcademicPeriods() {
        try {
            List<AcademicPeriodDTO> periods = getClientConnection().getAcademicPeriods();
            academicPeriodComboBox.setItems(FXCollections.observableArrayList(periods));
            
            // Select the first period if available
            if (!periods.isEmpty()) {
                academicPeriodComboBox.getSelectionModel().selectFirst();
            }
            
            logger.info("Loaded {} academic periods", periods.size());
        } catch (Exception e) {
            logger.error("Error loading academic periods", e);
            showError(LangManager.getBundle().getString("error.title"), 
                    LangManager.getBundle().getString("scholarship.error.loading_periods") + ": " + e.getMessage());
        }
    }
    
    /**
     * Validates the user input.
     *
     * @return true if input is valid, false otherwise
     */
    private boolean validateInput() {
        if (academicPeriodComboBox.getSelectionModel().getSelectedItem() == null) {
            showError(LangManager.getBundle().getString("error.title"), 
                    LangManager.getBundle().getString("scholarship.error.select_period"));
            return false;
        }
        
        // Additional info is optional, but let's make sure it's not too long
        if (additionalInfoTextArea.getText().length() > 1000) {
            showError(LangManager.getBundle().getString("error.title"), 
                    LangManager.getBundle().getString("scholarship.error.info_too_long"));
            return false;
        }
        
        return true;
    }
    
    /**
     * Submits the scholarship application.
     */
    private void submitApplication() {
        try {
            AcademicPeriodDTO selectedPeriod = academicPeriodComboBox.getSelectionModel().getSelectedItem();
            String additionalInfo = additionalInfoTextArea.getText().trim();
            
            // Submit the application
            result = getClientConnection().submitScholarshipApplication(
                    program.getId(),
                    selectedPeriod.getId(),
                    additionalInfo
            );
            
            if (result != null) {
                logger.info("Successfully submitted scholarship application for program: {}, period: {}", 
                        program.getId(), selectedPeriod.getId());
                showInfo(LangManager.getBundle().getString("success.title"), 
                        LangManager.getBundle().getString("scholarship.success.application_submitted"));
                closeDialog();
            } else {
                logger.error("Failed to submit scholarship application");
                showError(LangManager.getBundle().getString("error.title"), 
                        LangManager.getBundle().getString("scholarship.error.submission_failed"));
            }
        } catch (Exception e) {
            logger.error("Error submitting scholarship application", e);
            showError(LangManager.getBundle().getString("error.title"), 
                    LangManager.getBundle().getString("scholarship.error.submission_failed") + ": " + e.getMessage());
        }
    }

    /**
     * Closes the dialog.
     */
    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Shows an error dialog.
     *
     * @param title The title of the dialog
     * @param message The message to display
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Shows an information dialog.
     *
     * @param title The title of the dialog
     * @param message The message to display
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public String getFxmlPath() {
        return "/fxml/scholarship_application_dialog.fxml";
    }

    /**
     * Updates the UI texts based on the current language.
     */
    @Override
    public void updateTexts() {
        titleLabel.setText(LangManager.getBundle().getString("scholarship.dialog.title"));
        academicPeriodLabel.setText(LangManager.getBundle().getString("scholarship.dialog.select_period"));
        additionalInfoLabel.setText(LangManager.getBundle().getString("scholarship.dialog.additional_info"));
        cancelButton.setText(LangManager.getBundle().getString("button.cancel"));
        submitButton.setText(LangManager.getBundle().getString("button.submit"));
    }
}
