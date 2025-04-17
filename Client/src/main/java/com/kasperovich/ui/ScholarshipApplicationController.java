package com.kasperovich.ui;

import com.kasperovich.clientconnection.ClientConnection;
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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.LocalDate;

/**
 * Controller for the scholarship application form.
 * Note: Scholarship functionality has been temporarily disabled.
 */
public class ScholarshipApplicationController {
    private static final Logger logger = LoggerUtil.getLogger(ScholarshipApplicationController.class);
    
    @FXML
    private Button backButton;
    
    @FXML
    private Label programNameLabel;
    
    @FXML
    private Label programDescriptionLabel;
    
    @FXML
    private ComboBox<AcademicPeriodDTO> academicPeriodComboBox;
    
    @FXML
    private TextField firstNameField;
    
    @FXML
    private TextField lastNameField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private TextArea motivationTextArea;
    
    @FXML
    private TextArea achievementsTextArea;
    
    @FXML
    private CheckBox termsCheckBox;
    
    @FXML
    private Button clearButton;
    
    @FXML
    private Button submitButton;
    
    @FXML
    private Label statusLabel;
    
    private ClientConnection clientConnection;
    private UserDTO user;
    private ScholarshipProgramDTO scholarshipProgram;
    private ObservableList<AcademicPeriodDTO> academicPeriods = FXCollections.observableArrayList();
    
    /**
     * Initializes the controller.
     */
    public void initialize() {
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
        
        // Set status message
        statusLabel.setText("Scholarship functionality is temporarily disabled");
    }
    
    /**
     * Sets the client connection for this controller.
     *
     * @param clientConnection The client connection to set
     */
    public void setClientConnection(ClientConnection clientConnection) {
        this.clientConnection = clientConnection;
    }
    
    /**
     * Sets the user for this controller.
     *
     * @param user The user to set
     */
    public void setUser(UserDTO user) {
        this.user = user;
    }
    
    /**
     * Sets the scholarship program for this controller.
     *
     * @param scholarshipProgram The scholarship program to set
     */
    public void setScholarshipProgram(ScholarshipProgramDTO scholarshipProgram) {
        this.scholarshipProgram = scholarshipProgram;
    }
    
    /**
     * Initializes the form with user and program data.
     */
    public void initializeForm() {
        if (user != null) {
            firstNameField.setText(user.getFirstName());
            lastNameField.setText(user.getLastName());
            emailField.setText(user.getEmail());
        }
        
        if (scholarshipProgram != null) {
            programNameLabel.setText("Program: " + scholarshipProgram.getName());
            programDescriptionLabel.setText("Description: " + scholarshipProgram.getDescription());
        }
        
        // Display placeholder academic periods
        AcademicPeriodDTO spring = new AcademicPeriodDTO();
        spring.setId(1L);
        spring.setName("Spring 2025");
        spring.setStartDate(LocalDate.of(2025, 1, 15));
        spring.setEndDate(LocalDate.of(2025, 5, 15));
        spring.setType("SEMESTER");
        spring.setActive(true);
        
        AcademicPeriodDTO fall = new AcademicPeriodDTO();
        fall.setId(2L);
        fall.setName("Fall 2025");
        fall.setStartDate(LocalDate.of(2025, 8, 15));
        fall.setEndDate(LocalDate.of(2025, 12, 15));
        fall.setType("SEMESTER");
        fall.setActive(true);
        
        academicPeriods.add(spring);
        academicPeriods.add(fall);
        academicPeriodComboBox.getSelectionModel().selectFirst();
        
        statusLabel.setText("Scholarship functionality is temporarily disabled");
    }
    
    /**
     * Validates the form and enables/disables the submit button.
     */
    private void validateForm() {
        boolean isValid = 
                academicPeriodComboBox.getValue() != null && 
                !motivationTextArea.getText().trim().isEmpty() && 
                termsCheckBox.isSelected();
        
        submitButton.setDisable(!isValid);
    }
    
    /**
     * Handles the back button action.
     *
     * @param event The action event
     */
    @FXML
    public void handleBackAction(ActionEvent event) {
        ChangeScene.changeScene(event, "/fxml/scholarship_programs_screen.fxml", LangManager.getBundle().getString("scholarship_programs.title"), clientConnection, user);
    }
    
    /**
     * Handles the clear button action.
     *
     * @param event The action event
     */
    @FXML
    public void handleClearAction(ActionEvent event) {
        motivationTextArea.clear();
        achievementsTextArea.clear();
        termsCheckBox.setSelected(false);
        
        if (!academicPeriods.isEmpty()) {
            academicPeriodComboBox.getSelectionModel().selectFirst();
        } else {
            academicPeriodComboBox.getSelectionModel().clearSelection();
        }
        
        statusLabel.setText("Form cleared. Scholarship functionality is temporarily disabled");
    }
    
    /**
     * Handles the submit button action.
     *
     * @param event The action event
     */
    @FXML
    public void handleSubmitAction(ActionEvent event) {
        // Display message that functionality is disabled
        AlertManager.showInformationAlert("Feature Disabled", 
                "Scholarship application functionality is temporarily disabled.");
        
        statusLabel.setText("Scholarship functionality is temporarily disabled");
    }
    
    /**
     * Navigates to the dashboard screen.
     */
    private void navigateToDashboard() {
        ChangeScene.changeScene(new ActionEvent(submitButton, null), "/fxml/dashboard_screen.fxml", LangManager.getBundle().getString("dashboard.title"), clientConnection, user);
    }
}
