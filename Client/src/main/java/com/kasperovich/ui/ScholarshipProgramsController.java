package com.kasperovich.ui;

import com.kasperovich.config.AlertManager;
import com.kasperovich.dto.auth.UserDTO;
import com.kasperovich.dto.scholarship.ScholarshipApplicationDTO;
import com.kasperovich.dto.scholarship.ScholarshipProgramDTO;
import com.kasperovich.i18n.LangManager;
import com.kasperovich.operations.ChangeScene;
import com.kasperovich.utils.LoggerUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Setter;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the scholarship programs screen.
 * Note: Scholarship functionality has been temporarily disabled.
 */
public class ScholarshipProgramsController extends BaseController {
    private static final Logger logger = LoggerUtil.getLogger(ScholarshipProgramsController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    @FXML
    private Button backButton;
    
    @FXML
    private ComboBox<String> filterComboBox;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private TableView<ScholarshipProgramDTO> programsTableView;
    
    @FXML
    private TableColumn<ScholarshipProgramDTO, String> nameColumn;
    
    @FXML
    private TableColumn<ScholarshipProgramDTO, String> descriptionColumn;
    
    @FXML
    private TableColumn<ScholarshipProgramDTO, String> amountColumn;
    
    @FXML
    private TableColumn<ScholarshipProgramDTO, String> startDateColumn;
    
    @FXML
    private TableColumn<ScholarshipProgramDTO, String> endDateColumn;
    
    @FXML
    private TableColumn<ScholarshipProgramDTO, String> statusColumn;
    
    @FXML
    private Button applyButton;
    
    @FXML
    private Label statusLabel;


    @Setter
    private UserDTO user;
    private final ObservableList<ScholarshipProgramDTO> programsList = FXCollections.observableArrayList();
    
    // Store the newly submitted application to update the dashboard
    private ScholarshipApplicationDTO newApplication;

    /**
     * Initializes the controller.
     */
    @Override
    public void initializeData() {
        updateTexts();
        // Initialize table columns
        nameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getName()));

        descriptionColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDescription()));

        amountColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty("$" + cellData.getValue().getFundingAmount()));

        // Use application deadline for start date
        startDateColumn.setCellValueFactory(cellData -> {
            LocalDate deadline = cellData.getValue().getApplicationDeadline();
            return new SimpleStringProperty(deadline != null ? deadline.format(DATE_FORMATTER) : "N/A");
        });

        // No end date in DTO, use N/A
        endDateColumn.setCellValueFactory(_ ->
                new SimpleStringProperty("N/A"));

        statusColumn.setCellValueFactory(cellData -> {
            boolean isActive = cellData.getValue().isActive();
            boolean isAccepting = cellData.getValue().isAcceptingApplications();

            if (!isActive) {
                return new SimpleStringProperty("Inactive");
            } else if (isAccepting) {
                return new SimpleStringProperty("Accepting");
            } else {
                return new SimpleStringProperty("Closed");
            }
        });

        // Set table items
        programsTableView.setItems(programsList);

        // Add selection listener to enable/disable apply button
        programsTableView.getSelectionModel().selectedItemProperty().addListener((_, _, newSelection) ->
                applyButton.setDisable(newSelection == null));

        // Initialize filter combo box
        filterComboBox.setItems(FXCollections.observableArrayList(
                LangManager.getBundle().getString("scholarship.filter.all"),
                LangManager.getBundle().getString("scholarship.filter.active"),
                LangManager.getBundle().getString("scholarship.filter.accepting")));

        // Set default filter
        filterComboBox.getSelectionModel().selectFirst();

        // Add listener to filter combo box
        filterComboBox.getSelectionModel().selectedItemProperty().addListener((_, _, newVal) -> {
            if (newVal != null) {
                applyFilter();
            }
        });

        // Only load scholarship programs if the list is empty (not passed from dashboard)
        if (programsList.isEmpty()) {
            loadScholarshipPrograms();
        }
        
        // Set status message
        statusLabel.setText(LangManager.getBundle().getString("scholarship.status.disabled"));
    }

    /**
     * Loads scholarship programs from the server.
     */
    public void loadScholarshipPrograms() {
        String filter = filterComboBox.getSelectionModel().getSelectedItem();
        
        statusLabel.setText(LangManager.getBundle().getString("scholarship.status.loading"));
        refreshButton.setDisable(true);
        
        // Clear existing data
        programsList.clear();
        
        try {
            // Get scholarship programs from the server
            List<ScholarshipProgramDTO> programs = getClientConnection().getScholarshipPrograms();
            
            // Apply filter if needed
            List<ScholarshipProgramDTO> filteredPrograms = new ArrayList<>();
            
            if (LangManager.getBundle().getString("scholarship.filter.all").equals(filter)) {
                filteredPrograms.addAll(programs);
            } else if (LangManager.getBundle().getString("scholarship.filter.active").equals(filter)) {
                for (ScholarshipProgramDTO program : programs) {
                    if (program.isActive()) {
                        filteredPrograms.add(program);
                    }
                }
            } else if (LangManager.getBundle().getString("scholarship.filter.accepting").equals(filter)) {
                for (ScholarshipProgramDTO program : programs) {
                    if (program.isActive() && program.isAcceptingApplications()) {
                        filteredPrograms.add(program);
                    }
                }
            }
            
            // Add filtered programs to the observable list
            programsList.addAll(filteredPrograms);
            
            statusLabel.setText(LangManager.getBundle().getString("scholarship.status.loaded") + " " + filteredPrograms.size());
            logger.info("Loaded {} scholarship programs (filtered from {} total)", 
                    filteredPrograms.size(), programs.size());
        } catch (Exception e) {
            logger.error("Error loading scholarship programs", e);
            statusLabel.setText(LangManager.getBundle().getString("scholarship.status.error") + " " + e.getMessage());
            AlertManager.showErrorAlert(LangManager.getBundle().getString("error.title"), 
                    LangManager.getBundle().getString("scholarship.status.error") + " " + e.getMessage());
        } finally {
            refreshButton.setDisable(false);
        }
    }
    
    /**
     * Sets the scholarship programs for this controller.
     * 
     * @param programs The scholarship programs to set
     */
    public void setScholarshipPrograms(ArrayList<ScholarshipProgramDTO> programs) {
        if (programs != null) {
            this.programsList.clear();
            this.programsList.addAll(programs);
            
            // Apply the current filter
            applyFilter();
            
            logger.info("Scholarship programs data set from dashboard: {} programs", programs.size());
        }
    }
    
    /**
     * Applies the current filter to the scholarship programs.
     */
    private void applyFilter() {
        String filter = filterComboBox.getSelectionModel().getSelectedItem();
        if(filter == null) {
            filter = LangManager.getBundle().getString("scholarship.filter.all");
        }
        
        // Create a temporary list with all programs (either from the original list or by loading from server)
        List<ScholarshipProgramDTO> allPrograms = new ArrayList<>(programsList);
        if (allPrograms.isEmpty() && getClientConnection() != null) {
            try {
                allPrograms = getClientConnection().getScholarshipPrograms();
            } catch (Exception e) {
                logger.error("Error loading scholarship programs for filtering", e);
                statusLabel.setText(LangManager.getBundle().getString("scholarship.status.error") + " " + e.getMessage());
                return;
            }
        }
        
        // Clear the current list
        programsList.clear();
        
        // Apply filter
        List<ScholarshipProgramDTO> filteredPrograms = new ArrayList<>();
        if (LangManager.getBundle().getString("scholarship.filter.all").equals(filter)) {
            filteredPrograms.addAll(allPrograms);
        } else if (LangManager.getBundle().getString("scholarship.filter.active").equals(filter)) {
            for (ScholarshipProgramDTO program : allPrograms) {
                if (program.isActive()) {
                    filteredPrograms.add(program);
                }
            }
        } else if (LangManager.getBundle().getString("scholarship.filter.accepting").equals(filter)) {
            for (ScholarshipProgramDTO program : allPrograms) {
                if (program.isActive() && program.isAcceptingApplications()) {
                    filteredPrograms.add(program);
                }
            }
        }
        
        // Add filtered programs to the observable list
        programsList.addAll(filteredPrograms);
        
        // Update status label
        statusLabel.setText(LangManager.getBundle().getString("scholarship.status.loaded") + " " + filteredPrograms.size());
    }
    
    /**
     * Handles the back button action.
     *
     * @param event The action event
     */
    @FXML
    public void handleBackAction(ActionEvent event) {
        if (newApplication != null) {
            // Navigate back to dashboard with the new application
            ChangeScene.changeSceneWithData(
                    event, 
                    "/fxml/dashboard_screen.fxml", 
                    LangManager.getBundle().getString("dashboard.title"), 
                    getClientConnection(), 
                    user,
                    newApplication,
                    "addNewApplication");
        } else {
            // Navigate back to dashboard normally
            ChangeScene.changeScene(
                    event, 
                    "/fxml/dashboard_screen.fxml", 
                    LangManager.getBundle().getString("dashboard.title"), 
                    getClientConnection(), 
                    user);
        }
    }
    
    /**
     * Handles the refresh button action.
     *
     * @param event The action event
     */
    @FXML
    public void handleRefreshAction(ActionEvent event) {
        loadScholarshipPrograms();
    }
    
    /**
     * Handles the apply button action.
     *
     * @param event The action event
     */
    @FXML
    public void handleApplyAction(ActionEvent event) {
        ScholarshipProgramDTO selectedProgram = programsTableView.getSelectionModel().getSelectedItem();
        
        if (selectedProgram == null) {
            AlertManager.showWarningAlert(LangManager.getBundle().getString("warning.title"), 
                    LangManager.getBundle().getString("scholarship.warning.select"));
            return;
        }
        
        // Check if the program is active and accepting applications
        if (!selectedProgram.isActive() || !selectedProgram.isAcceptingApplications()) {
            AlertManager.showWarningAlert(LangManager.getBundle().getString("warning.title"), 
                    LangManager.getBundle().getString("scholarship.warning.not_accepting"));
            return;
        }
        
        try {
            // Load the application dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/scholarship_application_dialog.fxml"));
            loader.setResources(LangManager.getBundle());
            Parent root = loader.load();
            
            // Get controller and set up the dialog
            ScholarshipApplicationDialogController controller = loader.getController();
            controller.setAccess(this.clientConnection);
            controller.initializeData();
            controller.setup(selectedProgram);
            
            // Set the callback to receive the submitted application
            controller.setCallback(application -> {
                newApplication = application;
                statusLabel.setText(LangManager.getBundle().getString("scholarship.status.application_submitted"));
                logger.info("Scholarship application submitted for program: {}", selectedProgram.getId());
            });
            
            // Create and show the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle(LangManager.getBundle().getString("scholarship.dialog.title"));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(((Node) event.getSource()).getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();
        } catch (IOException e) {
            logger.error("Error opening scholarship application dialog", e);
            AlertManager.showErrorAlert(LangManager.getBundle().getString("error.title"), 
                    LangManager.getBundle().getString("scholarship.error.dialog") + ": " + e.getMessage());
        }
    }

    @Override
    public String getFxmlPath() {
        return "/fxml/scholarship_programs_screen.fxml";
    }

    @FXML
    public void handleLanguageSwitch(ActionEvent event) {
        super.handleLanguageSwitch(event);
    }

    @Override
    public void updateTexts() {
        backButton.setText(LangManager.getBundle().getString("dashboard.back"));
        refreshButton.setText(LangManager.getBundle().getString("dashboard.refresh"));
        applyButton.setText(LangManager.getBundle().getString("scholarship.apply"));
    }
}
