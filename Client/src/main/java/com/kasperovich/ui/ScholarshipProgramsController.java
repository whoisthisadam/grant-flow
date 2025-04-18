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
    
    private UserDTO user;
    private final ObservableList<ScholarshipProgramDTO> programsList = FXCollections.observableArrayList();


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
        endDateColumn.setCellValueFactory(cellData ->
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
        programsTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            applyButton.setDisable(newSelection == null);
        });

        // Initialize filter combo box
        filterComboBox.setItems(FXCollections.observableArrayList(
                LangManager.getBundle().getString("scholarship.filter.all"),
                LangManager.getBundle().getString("scholarship.filter.active"),
                LangManager.getBundle().getString("scholarship.filter.accepting")));

        // Set default filter
        filterComboBox.getSelectionModel().selectFirst();

        // Add listener to filter combo box
        filterComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadScholarshipPrograms();
            }
        });

        // Load scholarship programs
        loadScholarshipPrograms();
        
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
     * Handles the back button action.
     *
     * @param event The action event
     */
    @FXML
    public void handleBackAction(ActionEvent event) {
        ChangeScene.changeScene(event, "/fxml/dashboard_screen.fxml", LangManager.getBundle().getString("dashboard.title"), getClientConnection(), user);
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
            
            // Create and show the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle(LangManager.getBundle().getString("scholarship.dialog.title"));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(((Node) event.getSource()).getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();
            
            // Check if an application was submitted
            ScholarshipApplicationDTO application = controller.getResult();
            if (application != null) {
                statusLabel.setText(LangManager.getBundle().getString("scholarship.status.application_submitted"));
                logger.info("Scholarship application submitted for program: {}", selectedProgram.getId());
            }
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
