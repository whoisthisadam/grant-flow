package com.kasperovich.ui;

import com.kasperovich.config.AlertManager;
import com.kasperovich.dto.auth.UserDTO;
import com.kasperovich.dto.scholarship.ScholarshipApplicationDTO;
import com.kasperovich.i18n.LangManager;
import com.kasperovich.operations.ChangeScene;
import com.kasperovich.utils.LoggerUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.Setter;
import org.apache.logging.log4j.Logger;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the scholarship applications screen.
 */
public class ScholarshipApplicationsController extends BaseController {
    private static final Logger logger = LoggerUtil.getLogger(ScholarshipApplicationsController.class);
    
    @FXML
    private Label titleLabel;
    
    @FXML
    private TableView<ScholarshipApplicationDTO> applicationsTable;
    
    @FXML
    private TableColumn<ScholarshipApplicationDTO, String> programColumn;
    
    @FXML
    private TableColumn<ScholarshipApplicationDTO, String> periodColumn;
    
    @FXML
    private TableColumn<ScholarshipApplicationDTO, String> submissionDateColumn;
    
    @FXML
    private TableColumn<ScholarshipApplicationDTO, String> statusColumn;
    
    @FXML
    private TableColumn<ScholarshipApplicationDTO, String> decisionDateColumn;
    
    @FXML
    private Button backButton;
    
    @FXML
    private Button viewDetailsButton;
    
    @Setter
    private UserDTO user;

    private List<ScholarshipApplicationDTO> applications;

    public void setApplications(ArrayList<ScholarshipApplicationDTO> applications) {
        this.applications = applications;
    }

    private ObservableList<ScholarshipApplicationDTO> observableApplications = FXCollections.observableArrayList();
    
    /**
     * Initializes the controller.
     * Called after dependencies are injected.
     */
    @Override
    public void initializeData() {
        // Configure table columns
        programColumn.setCellValueFactory(new PropertyValueFactory<>("programName"));
        periodColumn.setCellValueFactory(new PropertyValueFactory<>("periodName"));
        
        // Custom cell value factories for date formatting
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm");
        
        submissionDateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getSubmissionDate() != null) {
                return new SimpleStringProperty(cellData.getValue().getSubmissionDate().format(dateTimeFormatter));
            }
            return new SimpleStringProperty("N/A");
        });
        
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        decisionDateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDecisionDate() != null) {
                return new SimpleStringProperty(cellData.getValue().getDecisionDate().format(dateTimeFormatter));
            }
            return new SimpleStringProperty("N/A");
        });
        
        // Set up selection listener for the view details button
        applicationsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            viewDetailsButton.setDisable(newSelection == null);
        });
        
        // Load applications data
        if (applications != null) {
            observableApplications.setAll(applications);
            applicationsTable.setItems(observableApplications);
            
            // Auto-select the first application if available
            if (!applications.isEmpty()) {
                applicationsTable.getSelectionModel().selectFirst();
            }
        }
        
        updateTexts();
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
     * Handles the view details button action.
     *
     * @param event The action event
     */
    @FXML
    public void handleViewDetailsAction(ActionEvent event) {
        ScholarshipApplicationDTO selectedApplication = applicationsTable.getSelectionModel().getSelectedItem();
        
        if (selectedApplication == null) {
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("applications.no_selection_title"),
                LangManager.getBundle().getString("applications.no_selection_message")
            );
            return;
        }
        
        // Show application details
        StringBuilder details = new StringBuilder();
        details.append(LangManager.getBundle().getString("applications.id")).append(": ").append(selectedApplication.getId()).append("\n\n");
        details.append(LangManager.getBundle().getString("applications.program")).append(": ").append(selectedApplication.getProgramName()).append("\n");
        details.append(LangManager.getBundle().getString("applications.period")).append(": ").append(selectedApplication.getPeriodName()).append("\n");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy HH:mm");
        details.append(LangManager.getBundle().getString("applications.submission_date")).append(": ").append(
            selectedApplication.getSubmissionDate() != null 
            ? selectedApplication.getSubmissionDate().format(formatter) 
            : LangManager.getBundle().getString("scholarship.not_available")
        ).append("\n\n");
        
        details.append(LangManager.getBundle().getString("applications.status")).append(": ").append(selectedApplication.getStatus()).append("\n");
        
        if (selectedApplication.getDecisionDate() != null) {
            details.append(LangManager.getBundle().getString("applications.decision_date")).append(": ").append(selectedApplication.getDecisionDate().format(formatter)).append("\n");
        }
        
        if (selectedApplication.getDecisionComments() != null && !selectedApplication.getDecisionComments().isEmpty()) {
            details.append(LangManager.getBundle().getString("applications.comments")).append(": ").append(selectedApplication.getDecisionComments()).append("\n");
        }
        
        if (selectedApplication.getReviewerUsername() != null && !selectedApplication.getReviewerUsername().isEmpty()) {
            details.append(LangManager.getBundle().getString("applications.reviewed_by")).append(": ").append(selectedApplication.getReviewerUsername()).append("\n");
        }
        
        AlertManager.showInformationAlert(
            LangManager.getBundle().getString("applications.details_title"),
            details.toString()
        );
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
    
    @Override
    public void updateTexts() {
        titleLabel.setText(LangManager.getBundle().getString("applications.title"));
        backButton.setText(LangManager.getBundle().getString("dashboard.back"));
        viewDetailsButton.setText(LangManager.getBundle().getString("applications.view_details"));
        
        programColumn.setText(LangManager.getBundle().getString("applications.program_column"));
        periodColumn.setText(LangManager.getBundle().getString("applications.period_column"));
        submissionDateColumn.setText(LangManager.getBundle().getString("applications.submission_date_column"));
        statusColumn.setText(LangManager.getBundle().getString("applications.status_column"));
        decisionDateColumn.setText(LangManager.getBundle().getString("applications.decision_date_column"));
    }
    
    @Override
    public String getFxmlPath() {
        return "/fxml/scholarship_applications_screen.fxml";
    }
}
