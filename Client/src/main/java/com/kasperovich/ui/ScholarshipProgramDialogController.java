package com.kasperovich.ui;

import com.kasperovich.clientconnection.ClientConnection;
import com.kasperovich.commands.toserver.CreateScholarshipProgramCommand;
import com.kasperovich.commands.toserver.UpdateScholarshipProgramCommand;
import com.kasperovich.config.AlertManager;
import com.kasperovich.dto.auth.UserDTO;
import com.kasperovich.dto.scholarship.ScholarshipProgramDTO;
import com.kasperovich.i18n.LangManager;
import com.kasperovich.utils.LoggerUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Controller for the scholarship program dialog.
 */
public class ScholarshipProgramDialogController extends BaseController {
    private static final Logger logger = LoggerUtil.getLogger(ScholarshipProgramDialogController.class);
    
    @FXML
    private Label titleLabel;
    
    @FXML
    private TextField nameField;
    
    @FXML
    private TextArea descriptionField;
    
    @FXML
    private TextField fundingField;
    
    @FXML
    private TextField minGpaField;
    
    @FXML
    private DatePicker deadlinePicker;
    
    @FXML
    private CheckBox activeCheckbox;
    
    @FXML
    private Label validationMessageLabel;
    
    @FXML
    private Button cancelButton;
    
    @FXML
    private Button saveButton;
    
    @Setter
    private ClientConnection clientConnection;
    
    private ScholarshipProgramDTO program;
    private boolean isNew;
    private UserDTO user;
    @Getter
    private boolean saved = false;
    
    /**
     * Sets up the controller with the scholarship program data.
     * 
     * @param program The scholarship program to edit
     * @param isNew Whether this is a new program
     * @param user The current user
     */
    public void setup(ScholarshipProgramDTO program, boolean isNew, UserDTO user) {
        this.program = program;
        this.isNew = isNew;
        this.user = user;
    }
    
    /**
     * Initializes the controller.
     */
    @Override
    public void initializeData() {
        if (program == null) {
            logger.error("Scholarship program data is null in ScholarshipProgramDialogController");
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("error.title"),
                "Scholarship program data is not available"
            );
            closeDialog();
            return;
        }
        
        // Set the title based on whether this is a new program or an existing one
        titleLabel.setText(isNew ? 
                LangManager.getBundle().getString("admin.programs.create.title") : 
                LangManager.getBundle().getString("admin.programs.edit.title"));
        
        // Populate the fields with the program data
        populateFields();
        
        // Update the texts
        updateTexts();
        
        logger.info("Scholarship program dialog initialized for program: {}", 
                isNew ? "New Program" : program.getName());
    }
    
    /**
     * Populates the fields with the program data.
     */
    private void populateFields() {
        nameField.setText(program.getName() != null ? program.getName() : "");
        descriptionField.setText(program.getDescription() != null ? program.getDescription() : "");
        
        if (program.getFundingAmount() != null) {
            fundingField.setText(program.getFundingAmount().toString());
        }
        
        if (program.getMinGpa() != null) {
            minGpaField.setText(program.getMinGpa().toString());
        }
        
        if (program.getApplicationDeadline() != null) {
            deadlinePicker.setValue(program.getApplicationDeadline());
        } else {
            deadlinePicker.setValue(LocalDate.now().plusMonths(3));
        }
        
        activeCheckbox.setSelected(program.isActive());
    }
    
    /**
     * Handles the save button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleSaveAction(ActionEvent event) {
        try {
            // Validate the input
            if (!validateInput()) {
                return;
            }
            
            // Create or update the program
            if (isNew) {
                createProgram();
            } else {
                updateProgram();
            }
            
            // Close the dialog
            saved = true;
            closeDialog();
        } catch (Exception e) {
            logger.error("Error saving scholarship program", e);
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("error.title"),
                "Error saving scholarship program: " + e.getMessage()
            );
        }
    }
    
    /**
     * Validates the input fields.
     * 
     * @return true if the input is valid, false otherwise
     */
    private boolean validateInput() {
        // Clear any previous validation messages
        validationMessageLabel.setText("");
        
        // Validate name
        if (nameField.getText().trim().isEmpty()) {
            validationMessageLabel.setText(LangManager.getBundle().getString("admin.programs.validation.name_required"));
            nameField.requestFocus();
            return false;
        }
        
        // Validate description
        if (descriptionField.getText().trim().isEmpty()) {
            validationMessageLabel.setText(LangManager.getBundle().getString("admin.programs.validation.description_required"));
            descriptionField.requestFocus();
            return false;
        }
        
        // Validate funding amount
        try {
            if (fundingField.getText().trim().isEmpty()) {
                validationMessageLabel.setText(LangManager.getBundle().getString("admin.programs.validation.funding_required"));
                fundingField.requestFocus();
                return false;
            }
            
            BigDecimal funding = new BigDecimal(fundingField.getText().trim());
            if (funding.compareTo(BigDecimal.ZERO) <= 0) {
                validationMessageLabel.setText(LangManager.getBundle().getString("admin.programs.validation.funding_positive"));
                fundingField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            validationMessageLabel.setText(LangManager.getBundle().getString("admin.programs.validation.funding_invalid"));
            fundingField.requestFocus();
            return false;
        }
        
        // Validate min GPA
        try {
            if (minGpaField.getText().trim().isEmpty()) {
                validationMessageLabel.setText(LangManager.getBundle().getString("admin.programs.validation.min_gpa_required"));
                minGpaField.requestFocus();
                return false;
            }
            
            BigDecimal minGpa = new BigDecimal(minGpaField.getText().trim());
            if (minGpa.compareTo(BigDecimal.ZERO) < 0 || minGpa.compareTo(new BigDecimal("4.0")) > 0) {
                validationMessageLabel.setText(LangManager.getBundle().getString("admin.programs.validation.min_gpa_range"));
                minGpaField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            validationMessageLabel.setText(LangManager.getBundle().getString("admin.programs.validation.min_gpa_invalid"));
            minGpaField.requestFocus();
            return false;
        }
        
        // Validate deadline
        if (deadlinePicker.getValue() == null) {
            validationMessageLabel.setText(LangManager.getBundle().getString("admin.programs.validation.deadline_required"));
            deadlinePicker.requestFocus();
            return false;
        }
        
        return true;
    }
    
    /**
     * Creates a new scholarship program.
     * 
     * @throws Exception if an error occurs
     */
    private void createProgram() throws Exception {
        // Create the command
        CreateScholarshipProgramCommand command = new CreateScholarshipProgramCommand(
                nameField.getText().trim(),
                descriptionField.getText().trim(),
                new BigDecimal(fundingField.getText().trim()),
                new BigDecimal(minGpaField.getText().trim()),
                deadlinePicker.getValue(),
                activeCheckbox.isSelected()
        );
        
        // Create the program
        ScholarshipProgramDTO createdProgram = clientConnection.createScholarshipProgram(command);
        
        // Show success message
        AlertManager.showInformationAlert(
            LangManager.getBundle().getString("admin.programs.create.success.title"),
            LangManager.getBundle().getString("admin.programs.create.success.content")
        );
        
        logger.info("Created new scholarship program: {}", createdProgram.getName());
    }
    
    /**
     * Updates an existing scholarship program.
     * 
     * @throws Exception if an error occurs
     */
    private void updateProgram() throws Exception {
        // Create the command
        UpdateScholarshipProgramCommand command = new UpdateScholarshipProgramCommand(
                program.getId(),
                nameField.getText().trim(),
                descriptionField.getText().trim(),
                new BigDecimal(fundingField.getText().trim()),
                new BigDecimal(minGpaField.getText().trim()),
                deadlinePicker.getValue(),
                activeCheckbox.isSelected()
        );
        
        // Update the program
        ScholarshipProgramDTO updatedProgram = clientConnection.updateScholarshipProgram(command);
        
        // Show success message
        AlertManager.showInformationAlert(
            LangManager.getBundle().getString("admin.programs.edit.success.title"),
            LangManager.getBundle().getString("admin.programs.edit.success.content")
        );
        
        logger.info("Updated scholarship program: {}", updatedProgram.getName());
    }
    
    /**
     * Handles the cancel button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleCancelAction(ActionEvent event) {
        closeDialog();
    }
    
    /**
     * Closes the dialog.
     */
    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Updates the UI texts based on the current language.
     */
    @Override
    public void updateTexts() {
        titleLabel.setText(isNew ? 
                LangManager.getBundle().getString("admin.programs.create.title") : 
                LangManager.getBundle().getString("admin.programs.edit.title"));
        
        cancelButton.setText(LangManager.getBundle().getString("button.cancel"));
        saveButton.setText(LangManager.getBundle().getString("button.save"));
        
        activeCheckbox.setText(LangManager.getBundle().getString("admin.programs.field.active"));
    }
    
    @Override
    public String getFxmlPath() {
        return "/fxml/scholarship_program_dialog.fxml";
    }
}
