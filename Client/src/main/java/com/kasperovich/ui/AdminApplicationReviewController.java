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
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Setter;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the admin application review dashboard.
 */
public class AdminApplicationReviewController extends BaseController {

    private static final Logger logger = LoggerUtil.getLogger(AdminApplicationReviewController.class);

    @FXML
    private Label titleLabel;
    
    @FXML
    private Label filterLabel;
    
    @FXML
    private ComboBox<String> statusFilterComboBox;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private Button backButton;
    
    @FXML
    private TableView<ScholarshipApplicationDTO> applicationsTable;
    
    @FXML
    private TableColumn<ScholarshipApplicationDTO, Long> idColumn;
    
    @FXML
    private TableColumn<ScholarshipApplicationDTO, String> applicantColumn;
    
    @FXML
    private TableColumn<ScholarshipApplicationDTO, String> programColumn;
    
    @FXML
    private TableColumn<ScholarshipApplicationDTO, String> periodColumn;
    
    @FXML
    private TableColumn<ScholarshipApplicationDTO, String> submissionDateColumn;
    
    @FXML
    private TableColumn<ScholarshipApplicationDTO, String> statusColumn;
    
    @FXML
    private TableColumn<ScholarshipApplicationDTO, String> reviewerColumn;
    
    @FXML
    private Button viewDetailsButton;
    
    @FXML
    private Button approveButton;
    
    @FXML
    private Button rejectButton;
    
    @Setter
    private UserDTO user;
    
    private final ObservableList<ScholarshipApplicationDTO> applicationsList = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    /**
     * Sets the applications list and updates the UI.
     * 
     * @param applications the list of applications to set
     */
    public void setApplications(ArrayList<ScholarshipApplicationDTO> applications) {
        applicationsList.clear();
        if (applications != null) {
            applicationsList.addAll(applications);
        }
        
        // Update the table if it's already initialized
        if (applicationsTable != null && statusFilterComboBox != null && statusFilterComboBox.getValue() != null) {
            filterApplications();
        } else if (applicationsTable != null) {
            // If filter is not initialized yet, just show all applications
            applicationsTable.setItems(applicationsList);
        }
        
        logger.info("Applications set: {}", (applications != null ? applications.size() : 0));
    }
    
    @Override
    public void initializeData() {
        setupUIComponents();
        
        if (getClientConnection() != null && applicationsList.isEmpty()) {
            loadApplications();
        }
        
        updateTexts();
    }
    
    /**
     * Sets up the UI components.
     */
    private void setupUIComponents() {
        // Configure table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        applicantColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getApplicantFullName()));
        
        programColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getProgramName()));
        
        periodColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getPeriodName()));
        
        submissionDateColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getSubmissionDate() != null ? 
                        cellData.getValue().getSubmissionDate().format(dateFormatter) : ""));
        
        statusColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getStatus()));
        
        reviewerColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getReviewerUsername() != null ? 
                        cellData.getValue().getReviewerUsername() : ""));
        
        // Set up the table
        applicationsTable.setItems(applicationsList);
        
        // Set up status filter
        statusFilterComboBox.getItems().addAll(
            LangManager.getBundle().getString("application.filter.all"),
            LangManager.getBundle().getString("application.filter.pending"),
            LangManager.getBundle().getString("application.filter.approved"),
            LangManager.getBundle().getString("application.filter.rejected")
        );
        statusFilterComboBox.setValue(LangManager.getBundle().getString("application.filter.all"));
        statusFilterComboBox.setOnAction(event -> filterApplications());
        
        // Configure button states
        viewDetailsButton.setDisable(true);
        approveButton.setDisable(true);
        rejectButton.setDisable(true);
        
        // Add selection listener
        applicationsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            viewDetailsButton.setDisable(!hasSelection);
            
            // Only enable approve/reject buttons for pending applications
            boolean isPending = hasSelection && "PENDING".equals(newSelection.getStatus());
            approveButton.setDisable(!isPending);
            rejectButton.setDisable(!isPending);
        });
    }
    
    /**
     * Loads applications from the server.
     */
    private void loadApplications() {
        try {
            List<ScholarshipApplicationDTO> applications = getClientConnection().getAllApplications();
            applicationsList.clear();
            applicationsList.addAll(applications);
            filterApplications();
        } catch (Exception e) {
            AlertManager.showErrorAlert(LangManager.getBundle().getString("error"), e.getMessage());
        }
    }
    
    /**
     * Filters applications based on the selected status.
     */
    private void filterApplications() {
        String filter = statusFilterComboBox.getValue();
        
        if (LangManager.getBundle().getString("application.filter.all").equals(filter)) {
            applicationsTable.setItems(applicationsList);
        } else {
            ObservableList<ScholarshipApplicationDTO> filteredList = FXCollections.observableArrayList();
            for (ScholarshipApplicationDTO app : applicationsList) {
                if (filter.equalsIgnoreCase(app.getStatus())) {
                    filteredList.add(app);
                }
            }
            applicationsTable.setItems(filteredList);
        }
    }
    
    /**
     * Handles the refresh button action.
     *
     * @param event the action event
     */
    @FXML
    private void handleRefresh(ActionEvent event) {
        loadApplications();
    }
    
    /**
     * Handles the back button action.
     *
     * @param event the action event
     */
    @FXML
    private void handleBack(ActionEvent event) {
            ChangeScene.changeScene(event, "/fxml/admin_dashboard_screen.fxml",
                    LangManager.getBundle().getString("admin.dashboard.title"), 
                    getClientConnection(), user);
    }
    
    /**
     * Handles the view details button action.
     *
     * @param event the action event
     */
    @FXML
    private void handleViewDetails(ActionEvent event) {
        ScholarshipApplicationDTO selectedApplication = applicationsTable.getSelectionModel().getSelectedItem();
        if (selectedApplication == null) {
            return;
        }
        
        try {
            // Show application details in a dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/application_details_dialog.fxml"));
            loader.setResources(LangManager.getBundle());
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle(LangManager.getBundle().getString("application.details.title"));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(applicationsTable.getScene().getWindow());
            
            Scene scene = new Scene(loader.load());
            dialogStage.setScene(scene);
            
            ApplicationDetailsDialogController controller = loader.getController();
            controller.setApplication(selectedApplication);
            controller.setClientConnection(getClientConnection());
            controller.setDialogStage(dialogStage);
            controller.setReadOnly(true);
            controller.initializeData();
            
            dialogStage.showAndWait();
            
            // Refresh the table after dialog is closed
            loadApplications();
            
        } catch (IOException e) {
            AlertManager.showErrorAlert(LangManager.getBundle().getString("error"), e.getMessage());
        }
    }
    
    /**
     * Handles the approve button action.
     *
     * @param event the action event
     */
    @FXML
    private void handleApprove(ActionEvent event) {
        ScholarshipApplicationDTO selectedApplication = applicationsTable.getSelectionModel().getSelectedItem();
        if (selectedApplication == null) {
            return;
        }
        
        // Show dialog to enter comments
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(LangManager.getBundle().getString("application.approve.title"));
        dialog.setHeaderText(LangManager.getBundle().getString("application.approve.header"));
        dialog.setContentText(LangManager.getBundle().getString("application.approve.comments"));
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String comments = result.get();
            
            try {
                getClientConnection().approveApplication(selectedApplication.getId(), comments);
                AlertManager.showInformationAlert(
                        LangManager.getBundle().getString("success"),
                        LangManager.getBundle().getString("application.approve.success"));
                
                // Refresh the table
                loadApplications();
            } catch (Exception e) {
                AlertManager.showErrorAlert(LangManager.getBundle().getString("error"), e.getMessage());
            }
        }
    }
    
    /**
     * Handles the reject button action.
     *
     * @param event the action event
     */
    @FXML
    private void handleReject(ActionEvent event) {
        ScholarshipApplicationDTO selectedApplication = applicationsTable.getSelectionModel().getSelectedItem();
        if (selectedApplication == null) {
            return;
        }
        
        // Show dialog to enter comments
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(LangManager.getBundle().getString("application.reject.title"));
        dialog.setHeaderText(LangManager.getBundle().getString("application.reject.header"));
        dialog.setContentText(LangManager.getBundle().getString("application.reject.comments"));
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String comments = result.get();
            
            try {
                getClientConnection().rejectApplication(selectedApplication.getId(), comments);
                AlertManager.showInformationAlert(
                        LangManager.getBundle().getString("success"),
                        LangManager.getBundle().getString("application.reject.success"));
                
                // Refresh the table
                loadApplications();
            } catch (Exception e) {
                AlertManager.showErrorAlert(LangManager.getBundle().getString("error"), e.getMessage());
            }
        }
    }

    @Override
    public String getFxmlPath() {
        return "/fxml/application_details_dialog.fxml";
    }

    @Override
    public void updateTexts() {
        ResourceBundle bundle = LangManager.getBundle();
        
        titleLabel.setText(bundle.getString("application.review.title"));
        filterLabel.setText(bundle.getString("application.filter.label"));
        refreshButton.setText(bundle.getString("refresh"));
        backButton.setText(bundle.getString("back.to.dashboard"));
        
        idColumn.setText(bundle.getString("application.id"));
        applicantColumn.setText(bundle.getString("application.applicant"));
        programColumn.setText(bundle.getString("application.program"));
        periodColumn.setText(bundle.getString("application.period"));
        submissionDateColumn.setText(bundle.getString("application.submission.date"));
        statusColumn.setText(bundle.getString("application.status"));
        reviewerColumn.setText(bundle.getString("application.reviewer"));
        
        viewDetailsButton.setText(bundle.getString("application.view.details"));
        approveButton.setText(bundle.getString("application.approve"));
        rejectButton.setText(bundle.getString("application.reject"));
        
        // Update filter combobox
        String currentValue = statusFilterComboBox.getValue();
        statusFilterComboBox.getItems().clear();
        statusFilterComboBox.getItems().addAll(
            LangManager.getBundle().getString("application.filter.all"),
            LangManager.getBundle().getString("application.filter.pending"),
            LangManager.getBundle().getString("application.filter.approved"),
            LangManager.getBundle().getString("application.filter.rejected")
        );
        statusFilterComboBox.setValue(currentValue);
    }
}
