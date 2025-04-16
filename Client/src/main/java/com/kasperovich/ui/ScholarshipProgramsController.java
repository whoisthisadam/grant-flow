package com.kasperovich.ui;

import com.kasperovich.clientconnection.ClientConnection;
import com.kasperovich.config.AlertManager;
import com.kasperovich.dto.auth.UserDTO;
import com.kasperovich.dto.scholarship.ScholarshipProgramDTO;
import com.kasperovich.utils.LoggerUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the scholarship programs screen.
 * Note: Scholarship functionality has been temporarily disabled.
 */
public class ScholarshipProgramsController {
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
    
    private ClientConnection clientConnection;
    private UserDTO user;
    private ObservableList<ScholarshipProgramDTO> programsList = FXCollections.observableArrayList();
    
    /**
     * Initializes the controller.
     */
    public void initialize() {
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
                "All Programs", "Active Programs", "Accepting Applications"));
        
        // Set default filter
        filterComboBox.getSelectionModel().selectFirst();
        
        // Add listener to filter combo box
        filterComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadScholarshipPrograms();
            }
        });
        
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
     * Loads scholarship programs from the server.
     */
    public void loadScholarshipPrograms() {
        String filter = filterComboBox.getSelectionModel().getSelectedItem();
        
        statusLabel.setText("Loading scholarship programs...");
        refreshButton.setDisable(true);
        
        // Clear existing data
        programsList.clear();
        
        try {
            // Get scholarship programs from the server
            List<ScholarshipProgramDTO> programs = clientConnection.getScholarshipPrograms();
            
            // Apply filter if needed
            List<ScholarshipProgramDTO> filteredPrograms = new ArrayList<>();
            
            if ("All Programs".equals(filter)) {
                filteredPrograms.addAll(programs);
            } else if ("Active Programs".equals(filter)) {
                for (ScholarshipProgramDTO program : programs) {
                    if (program.isActive()) {
                        filteredPrograms.add(program);
                    }
                }
            } else if ("Accepting Applications".equals(filter)) {
                for (ScholarshipProgramDTO program : programs) {
                    if (program.isActive() && program.isAcceptingApplications()) {
                        filteredPrograms.add(program);
                    }
                }
            }
            
            // Add filtered programs to the observable list
            programsList.addAll(filteredPrograms);
            
            statusLabel.setText(filteredPrograms.size() + " scholarship programs found");
            logger.info("Loaded {} scholarship programs (filtered from {} total)", 
                    filteredPrograms.size(), programs.size());
        } catch (Exception e) {
            logger.error("Error loading scholarship programs", e);
            statusLabel.setText("Error loading scholarship programs: " + e.getMessage());
            AlertManager.showErrorAlert("Error", "Could not load scholarship programs: " + e.getMessage());
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard_screen.fxml"));
            Parent root = loader.load();
            
            DashboardScreenController controller = loader.getController();
            controller.setClientConnection(clientConnection);
            controller.setUser(user);
            controller.initializeUserData();
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
            
            logger.debug("Navigated back to dashboard");
        } catch (IOException e) {
            logger.error("Error navigating back to dashboard", e);
            AlertManager.showErrorAlert("Navigation Error", "Could not navigate back to dashboard: " + e.getMessage());
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
            AlertManager.showWarningAlert("No Selection", "Please select a scholarship program to apply for.");
            return;
        }
        
        // Show message that functionality is disabled
        AlertManager.showInformationAlert("Feature Disabled", 
                "Scholarship application functionality is temporarily disabled.");
        
        statusLabel.setText("Scholarship functionality is temporarily disabled");
    }
}
