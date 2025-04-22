package com.kasperovich.ui;

import com.kasperovich.clientconnection.ClientConnection;
import com.kasperovich.config.AlertManager;
import com.kasperovich.dto.scholarship.AcademicPeriodDTO;
import com.kasperovich.i18n.LangManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;

/**
 * Controller for the academic period dialog.
 */
public class AcademicPeriodDialogController {
    private static final Logger logger = LogManager.getLogger(AcademicPeriodDialogController.class);
    
    public enum Mode {
        CREATE, EDIT
    }
    
    @FXML private TextField nameField;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private CheckBox activeCheckbox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    
    @Setter
    private ClientConnection clientConnection;
    @Setter
    private Stage dialogStage;
    private AcademicPeriodDTO period;
    @Setter
    private Mode mode;
    @Setter
    private Runnable onSaveCallback;
    
    @FXML
    private void initialize() {
        typeComboBox.getItems().addAll("SEMESTER", "YEAR");
        
        // Set default values
        activeCheckbox.setSelected(true);
        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now().plusMonths(6));
        
        // Add validation listeners
        nameField.textProperty().addListener((_, _, _) -> validateInput());
        typeComboBox.valueProperty().addListener((_, _, _) -> validateInput());
        startDatePicker.valueProperty().addListener((_, _, _) -> validateInput());
        endDatePicker.valueProperty().addListener((_, _, _) -> validateInput());
    }

    public void setPeriod(AcademicPeriodDTO period) {
        this.period = period;
        
        // Populate fields with period data
        nameField.setText(period.getName());
        typeComboBox.setValue(period.getType());
        startDatePicker.setValue(period.getStartDate());
        endDatePicker.setValue(period.getEndDate());
        activeCheckbox.setSelected(period.isActive());
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }
        
        try {
            AcademicPeriodDTO periodDTO = createPeriodFromInput();
            
            if (mode == Mode.CREATE) {
                clientConnection.createAcademicPeriod(periodDTO);
                AlertManager.showInformationAlert(
                    LangManager.getBundle().getString("success.title"),
                    LangManager.getBundle().getString("academic.period.created")
                );
            } else {
                periodDTO.setId(period.getId());
                clientConnection.updateAcademicPeriod(periodDTO);
                AlertManager.showInformationAlert(
                    LangManager.getBundle().getString("success.title"),
                    LangManager.getBundle().getString("academic.period.updated")
                );
            }
            
            if (onSaveCallback != null) {
                onSaveCallback.run();
            }
            
            dialogStage.close();
        } catch (Exception e) {
            String errorKey = mode == Mode.CREATE ? 
                "academic.period.error.create" : "academic.period.error.update";
                
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("error.title"),
                LangManager.getBundle().getString(errorKey) + ": " + e.getMessage()
            );
            
            logger.error("Error saving academic period", e);
        }
    }
    
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }
    
    private AcademicPeriodDTO createPeriodFromInput() {
        AcademicPeriodDTO periodDTO = new AcademicPeriodDTO();
        periodDTO.setName(nameField.getText().trim());
        periodDTO.setType(typeComboBox.getValue());
        periodDTO.setStartDate(startDatePicker.getValue());
        periodDTO.setEndDate(endDatePicker.getValue());
        periodDTO.setActive(activeCheckbox.isSelected());
        return periodDTO;
    }
    
    private boolean validateInput() {
        boolean valid = true;
        StringBuilder errorMessage = new StringBuilder();
        
        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            errorMessage.append(LangManager.getBundle().getString("academic.period.error.name_required")).append("\n");
            valid = false;
        }
        
        if (typeComboBox.getValue() == null) {
            errorMessage.append(LangManager.getBundle().getString("academic.period.error.type_required")).append("\n");
            valid = false;
        }
        
        if (startDatePicker.getValue() == null) {
            errorMessage.append(LangManager.getBundle().getString("academic.period.error.start_date_required")).append("\n");
            valid = false;
        }
        
        if (endDatePicker.getValue() == null) {
            errorMessage.append(LangManager.getBundle().getString("academic.period.error.end_date_required")).append("\n");
            valid = false;
        } else if (startDatePicker.getValue() != null && 
                  endDatePicker.getValue().isBefore(startDatePicker.getValue())) {
            errorMessage.append(LangManager.getBundle().getString("academic.period.error.end_date_before_start")).append("\n");
            valid = false;
        }
        
        saveButton.setDisable(!valid);
        
        return valid;
    }
}
