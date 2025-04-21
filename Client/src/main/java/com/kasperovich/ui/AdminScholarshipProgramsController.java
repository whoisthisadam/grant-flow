package com.kasperovich.ui;

import com.kasperovich.config.AlertManager;
import com.kasperovich.commands.toserver.CreateScholarshipProgramCommand;
import com.kasperovich.commands.toserver.UpdateScholarshipProgramCommand;
import com.kasperovich.dto.auth.UserDTO;
import com.kasperovich.dto.scholarship.ScholarshipProgramDTO;
import com.kasperovich.i18n.LangManager;
import com.kasperovich.operations.ChangeScene;
import com.kasperovich.utils.LoggerUtil;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import lombok.Setter;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for the admin scholarship programs screen.
 */
public class AdminScholarshipProgramsController extends BaseController {
    private static final Logger logger = LoggerUtil.getLogger(AdminScholarshipProgramsController.class);
    
    @FXML
    private Button backButton;
    
    @FXML
    private Button createButton;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private CheckBox showInactiveCheckbox;
    
    @FXML
    private TableView<ScholarshipProgramDTO> programsTable;
    
    @FXML
    private TableColumn<ScholarshipProgramDTO, Long> idColumn;
    
    @FXML
    private TableColumn<ScholarshipProgramDTO, String> nameColumn;
    
    @FXML
    private TableColumn<ScholarshipProgramDTO, String> fundingColumn;
    
    @FXML
    private TableColumn<ScholarshipProgramDTO, String> minGpaColumn;
    
    @FXML
    private TableColumn<ScholarshipProgramDTO, String> deadlineColumn;
    
    @FXML
    private TableColumn<ScholarshipProgramDTO, String> statusColumn;
    
    @FXML
    private TableColumn<ScholarshipProgramDTO, Void> actionsColumn;
    
    @FXML
    private Label totalCountLabel;
    
    @Setter
    private UserDTO user;
    
    private ObservableList<ScholarshipProgramDTO> programsList = FXCollections.observableArrayList();
    
    /**
     * Initializes the controller.
     */
    @Override
    public void initializeData() {
        if (user == null) {
            logger.error("User data is null in AdminScholarshipProgramsController");
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("error.title"),
                "User data is not available"
            );
            return;
        }
        
        setupTable();
        loadData();
        updateTexts();
        
        logger.info("Admin scholarship programs screen initialized for user: {}", user.getUsername());
    }
    
    /**
     * Sets up the table columns and cell factories.
     */
    private void setupTable() {
        // Set up the columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        // Format funding amount as currency
        fundingColumn.setCellValueFactory(cellData -> {
            BigDecimal amount = cellData.getValue().getFundingAmount();
            return new SimpleStringProperty(amount != null ? "$" + amount.toString() : "");
        });
        
        // Format min GPA
        minGpaColumn.setCellValueFactory(cellData -> {
            BigDecimal gpa = cellData.getValue().getMinGpa();
            return new SimpleStringProperty(gpa != null ? gpa.toString() : "");
        });
        
        // Format deadline date
        deadlineColumn.setCellValueFactory(cellData -> {
            LocalDate deadline = cellData.getValue().getApplicationDeadline();
            return new SimpleStringProperty(deadline != null ? 
                    deadline.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) : "");
        });
        
        // Format status
        statusColumn.setCellValueFactory(cellData -> {
            boolean active = cellData.getValue().isActive();
            return new SimpleStringProperty(active ? 
                    LangManager.getBundle().getString("admin.programs.status.active") : 
                    LangManager.getBundle().getString("admin.programs.status.inactive"));
        });
        
        // Add action buttons
        setupActionColumn();
        
        // Set the items
        programsTable.setItems(programsList);
    }
    
    /**
     * Sets up the action column with edit and delete buttons.
     */
    private void setupActionColumn() {
        Callback<TableColumn<ScholarshipProgramDTO, Void>, TableCell<ScholarshipProgramDTO, Void>> cellFactory = 
                new Callback<>() {
            @Override
            public TableCell<ScholarshipProgramDTO, Void> call(TableColumn<ScholarshipProgramDTO, Void> param) {
                return new TableCell<>() {
                    private final Button editButton = new Button(LangManager.getBundle().getString("admin.programs.edit"));
                    private final Button deleteButton = new Button(LangManager.getBundle().getString("admin.programs.delete"));
                    private final HBox pane = new HBox(5, editButton, deleteButton);
                    
                    {
                        // Set up edit button
                        editButton.setOnAction(event -> {
                            ScholarshipProgramDTO program = getTableView().getItems().get(getIndex());
                            handleEditAction(program);
                        });
                        
                        // Set up delete button
                        deleteButton.setOnAction(event -> {
                            ScholarshipProgramDTO program = getTableView().getItems().get(getIndex());
                            handleDeleteAction(program);
                        });
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : pane);
                    }
                };
            }
        };
        
        actionsColumn.setCellFactory(cellFactory);
    }
    
    /**
     * Loads the scholarship programs data.
     */
    private void loadData() {
        try {
            List<ScholarshipProgramDTO> programs;
            
            if (showInactiveCheckbox.isSelected()) {
                // Show all programs
                programs = getClientConnection().getAllScholarshipPrograms();
            } else {
                // Show only active programs
                programs = getClientConnection().getActiveScholarshipPrograms();
            }
            
            programsList.clear();
            programsList.addAll(programs);
            
            updateTotalCount();
            
            logger.debug("Loaded {} scholarship programs", programs.size());
        } catch (Exception e) {
            logger.error("Error loading scholarship programs", e);
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("error.title"),
                "Error loading scholarship programs: " + e.getMessage()
            );
        }
    }
    
    /**
     * Updates the total count label.
     */
    private void updateTotalCount() {
        totalCountLabel.setText(LangManager.getBundle().getString("admin.programs.total_count") + ": " + programsList.size());
    }
    
    /**
     * Handles the back button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleBackAction(ActionEvent event) {
        try {
            ChangeScene.changeScene(
                event,
                "/fxml/admin_dashboard_screen.fxml",
                LangManager.getBundle().getString("admin.dashboard.title"),
                getClientConnection(),
                user
            );
            
            logger.debug("Navigated back to admin dashboard from scholarship programs screen");
        } catch (Exception e) {
            logger.error("Error navigating back to admin dashboard", e);
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("navigation.error"),
                "Could not navigate back to admin dashboard: " + e.getMessage()
            );
        }
    }
    
    /**
     * Handles the create button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleCreateAction(ActionEvent event) {
        try {
            // Create a new scholarship program
            ScholarshipProgramDTO newProgram = new ScholarshipProgramDTO();
            newProgram.setActive(true);
            newProgram.setApplicationDeadline(LocalDate.now().plusMonths(3));
            
            // Open the edit dialog for the new program
            if (openEditDialog(newProgram, true)) {
                loadData(); // Refresh the data
            }
        } catch (Exception e) {
            logger.error("Error creating scholarship program", e);
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("error.title"),
                "Error creating scholarship program: " + e.getMessage()
            );
        }
    }
    
    /**
     * Handles the refresh button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleRefreshAction(ActionEvent event) {
        loadData();
    }
    
    /**
     * Handles the show inactive checkbox action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleShowInactiveAction(ActionEvent event) {
        loadData();
    }
    
    /**
     * Handles the edit action for a scholarship program.
     * 
     * @param program The scholarship program to edit
     */
    private void handleEditAction(ScholarshipProgramDTO program) {
        try {
            if (openEditDialog(program, false)) {
                loadData(); // Refresh the data
            }
        } catch (Exception e) {
            logger.error("Error editing scholarship program", e);
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("error.title"),
                "Error editing scholarship program: " + e.getMessage()
            );
        }
    }
    
    /**
     * Handles the delete action for a scholarship program.
     * 
     * @param program The scholarship program to delete
     */
    private void handleDeleteAction(ScholarshipProgramDTO program) {
        try {
            // Confirm deletion
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle(LangManager.getBundle().getString("admin.programs.delete.title"));
            confirmDialog.setHeaderText(LangManager.getBundle().getString("admin.programs.delete.header"));
            confirmDialog.setContentText(LangManager.getBundle().getString("admin.programs.delete.content") + 
                    " " + program.getName());
            
            if (confirmDialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                // Delete the program
                boolean deleted = getClientConnection().deleteScholarshipProgram(program.getId());
                
                if (deleted) {
                    AlertManager.showInformationAlert(
                        LangManager.getBundle().getString("admin.programs.delete.success.title"),
                        LangManager.getBundle().getString("admin.programs.delete.success.content")
                    );
                    
                    loadData(); // Refresh the data
                } else {
                    AlertManager.showErrorAlert(
                        LangManager.getBundle().getString("error.title"),
                        LangManager.getBundle().getString("admin.programs.delete.error")
                    );
                }
            }
        } catch (Exception e) {
            logger.error("Error deleting scholarship program", e);
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("error.title"),
                "Error deleting scholarship program: " + e.getMessage()
            );
        }
    }
    
    /**
     * Opens the edit dialog for a scholarship program.
     * 
     * @param program The scholarship program to edit
     * @param isNew Whether this is a new program
     * @return true if the program was saved, false otherwise
     */
    private boolean openEditDialog(ScholarshipProgramDTO program, boolean isNew) {
        try {
            // Load the edit dialog FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/scholarship_program_dialog.fxml"));
            loader.setResources(LangManager.getBundle());
            Parent root = loader.load();
            
            // Get the controller and set up the dialog
            ScholarshipProgramDialogController controller = loader.getController();
            controller.setClientConnection(getClientConnection());
            controller.setup(program, isNew, user);
            
            // Initialize the controller data
            controller.initializeData();
            
            // Create and show the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle(isNew ? 
                    LangManager.getBundle().getString("admin.programs.create.title") : 
                    LangManager.getBundle().getString("admin.programs.edit.title"));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(programsTable.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();
            
            // Return whether the program was saved
            return controller.isSaved();
        } catch (Exception e) {
            logger.error("Error opening scholarship program dialog", e);
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("error.title"),
                "Error opening scholarship program dialog: " + e.getMessage()
            );
            return false;
        }
    }
    
    /**
     * Updates the UI texts based on the current language.
     */
    @Override
    public void updateTexts() {
        backButton.setText(LangManager.getBundle().getString("button.back"));
        createButton.setText(LangManager.getBundle().getString("admin.programs.create"));
        refreshButton.setText(LangManager.getBundle().getString("admin.programs.refresh"));
        showInactiveCheckbox.setText(LangManager.getBundle().getString("admin.programs.show_inactive"));
        
        // Update table column headers
        idColumn.setText(LangManager.getBundle().getString("admin.programs.column.id"));
        nameColumn.setText(LangManager.getBundle().getString("admin.programs.column.name"));
        fundingColumn.setText(LangManager.getBundle().getString("admin.programs.column.funding"));
        minGpaColumn.setText(LangManager.getBundle().getString("admin.programs.column.min_gpa"));
        deadlineColumn.setText(LangManager.getBundle().getString("admin.programs.column.deadline"));
        statusColumn.setText(LangManager.getBundle().getString("admin.programs.column.status"));
        actionsColumn.setText(LangManager.getBundle().getString("admin.programs.column.actions"));
        
        // Update total count
        updateTotalCount();
        
        // Refresh the table to update the status column
        programsTable.refresh();
    }
    
    @Override
    public String getFxmlPath() {
        return "/fxml/admin_scholarship_programs_screen.fxml";
    }
}
