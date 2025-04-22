package com.kasperovich.ui;

import com.kasperovich.config.AlertManager;
import com.kasperovich.dto.scholarship.AcademicPeriodDTO;
import com.kasperovich.i18n.LangManager;
import com.kasperovich.operations.ChangeScene;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for the academic period management screen.
 */
public class AcademicPeriodManagementController extends BaseController {
    private static final Logger logger = LogManager.getLogger(AcademicPeriodManagementController.class);
    
    @FXML private TableView<AcademicPeriodDTO> periodsTable;
    @FXML private TableColumn<AcademicPeriodDTO, String> nameColumn;
    @FXML private TableColumn<AcademicPeriodDTO, String> typeColumn;
    @FXML private TableColumn<AcademicPeriodDTO, String> startDateColumn;
    @FXML private TableColumn<AcademicPeriodDTO, String> endDateColumn;
    @FXML private TableColumn<AcademicPeriodDTO, String> statusColumn;
    @FXML private TableColumn<AcademicPeriodDTO, Void> actionsColumn;
    @FXML private CheckBox showInactiveCheckbox;
    @FXML private ComboBox<String> typeFilterComboBox;
    @FXML private Button createButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;
    
    private final ObservableList<AcademicPeriodDTO> periodsList = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    @Override
    public void initializeData() {
        setupTable();
        setupFilters();
        loadAcademicPeriods();
    }
    
    private void setupTable() {
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        typeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType()));
        startDateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStartDate().format(dateFormatter)));
        endDateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getEndDate().format(dateFormatter)));
        statusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().isActive() ? 
                LangManager.getBundle().getString("status.active") :
                LangManager.getBundle().getString("status.inactive")));
        
        setupActionsColumn();
        
        periodsTable.setItems(periodsList);
    }
    
    private void setupActionsColumn() {
        actionsColumn.setCellFactory(_ -> new TableCell<>() {
            private final Button editButton = new Button(LangManager.getBundle().getString("button.edit"));
            private final Button toggleButton = new Button();
            private final Button deleteButton = new Button(LangManager.getBundle().getString("button.delete"));
            
            {
                editButton.setOnAction(_ -> {
                    AcademicPeriodDTO period = getTableRow().getItem();
                    if (period != null) {
                        openEditDialog(period);
                    }
                });
                
                toggleButton.setOnAction(_ -> {
                    AcademicPeriodDTO period = getTableRow().getItem();
                    if (period != null) {
                        togglePeriodStatus(period);
                    }
                });
                
                deleteButton.setOnAction(_ -> {
                    AcademicPeriodDTO period = getTableRow().getItem();
                    if (period != null) {
                        confirmAndDeletePeriod(period);
                    }
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    AcademicPeriodDTO period = getTableRow().getItem();
                    if (period != null) {
                        toggleButton.setText(period.isActive() ? 
                            LangManager.getBundle().getString("button.deactivate") : 
                            LangManager.getBundle().getString("button.activate"));
                    }
                    
                    HBox buttons = new HBox(5, editButton, toggleButton, deleteButton);
                    setGraphic(buttons);
                }
            }
        });
    }
    
    private void setupFilters() {
        showInactiveCheckbox.setSelected(false);
        
        typeFilterComboBox.getItems().addAll(
            LangManager.getBundle().getString("academic.period.type.all"),
            LangManager.getBundle().getString("academic.period.type.semester"),
            LangManager.getBundle().getString("academic.period.type.year")
        );
        typeFilterComboBox.getSelectionModel().selectFirst();
        
        showInactiveCheckbox.selectedProperty().addListener((_, _, _) -> applyFilters());
        typeFilterComboBox.getSelectionModel().selectedItemProperty().addListener((_, _, _) -> applyFilters());
    }
    
    private void applyFilters() {
        loadAcademicPeriods();
    }
    
    @FXML
    private void handleFilterChange() {
        applyFilters();
    }
    
    @FXML
    private void handleCreatePeriod() {
        openCreateDialog();
    }
    
    @FXML
    private void handleRefresh() {
        loadAcademicPeriods();
    }
    
    @FXML
    private void handleBackAction(javafx.event.ActionEvent event) {
        try {
            ChangeScene.changeScene(
                event,
                "/fxml/admin_dashboard_screen.fxml",
                LangManager.getBundle().getString("admin.dashboard.title"),
                clientConnection,
                user
            );
        } catch (Exception e) {
            logger.error("Error navigating back to admin dashboard", e);
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("error.title"),
                LangManager.getBundle().getString("error.navigation")
            );
        }
    }
    
    private void loadAcademicPeriods() {
        try {
            boolean includeInactive = showInactiveCheckbox.isSelected();
            String typeFilter = typeFilterComboBox.getValue();
            
            List<AcademicPeriodDTO> periods;
            if (includeInactive) {
                periods = clientConnection.getAllAcademicPeriods();
            } else {
                periods = clientConnection.getActiveAcademicPeriods();
            }
            
            // Apply type filter if not "All"
            if (typeFilter != null && !typeFilter.equals(LangManager.getBundle().getString("academic.period.type.all"))) {
                String actualType = typeFilter.equals(LangManager.getBundle().getString("academic.period.type.semester")) ? 
                    "SEMESTER" : "YEAR";
                periods = periods.stream()
                    .filter(p -> p.getType().equals(actualType))
                    .collect(Collectors.toList());
            }
            
            periodsList.clear();
            periodsList.addAll(periods);
        } catch (Exception e) {
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("error.title"),
                LangManager.getBundle().getString("academic.period.error.loading")
            );
            logger.error("Error loading academic periods", e);
        }
    }
    
    private void openCreateDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/academic_period_dialog.fxml"),
                LangManager.getBundle()
            );
            VBox dialogPane = loader.load();
            
            AcademicPeriodDialogController controller = loader.getController();
            controller.setClientConnection(clientConnection);
            controller.setMode(AcademicPeriodDialogController.Mode.CREATE);
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle(LangManager.getBundle().getString("academic.period.create.title"));
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(dialogPane));
            
            controller.setDialogStage(dialogStage);
            controller.setOnSaveCallback(this::loadAcademicPeriods);
            
            dialogStage.showAndWait();
        } catch (IOException e) {
            logger.error("Error opening create dialog", e);
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("error.title"),
                LangManager.getBundle().getString("error.dialog.load")
            );
        }
    }
    
    private void openEditDialog(AcademicPeriodDTO period) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/academic_period_dialog.fxml"),
                LangManager.getBundle()
            );
            VBox dialogPane = loader.load();
            
            AcademicPeriodDialogController controller = loader.getController();
            controller.setClientConnection(clientConnection);
            controller.setMode(AcademicPeriodDialogController.Mode.EDIT);
            controller.setPeriod(period);
            
            Stage dialogStage = new Stage();
            dialogStage.setTitle(LangManager.getBundle().getString("academic.period.edit.title"));
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(dialogPane));
            
            controller.setDialogStage(dialogStage);
            controller.setOnSaveCallback(this::loadAcademicPeriods);
            
            dialogStage.showAndWait();
        } catch (IOException e) {
            logger.error("Error opening edit dialog", e);
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("error.title"),
                LangManager.getBundle().getString("error.dialog.load")
            );
        }
    }
    
    private void togglePeriodStatus(AcademicPeriodDTO period) {
        try {
            boolean newStatus = !period.isActive();
            clientConnection.updateAcademicPeriodStatus(period.getId(), newStatus);
            loadAcademicPeriods();
            
            String message = newStatus ? 
                LangManager.getBundle().getString("academic.period.activated") :
                LangManager.getBundle().getString("academic.period.deactivated");
            
            AlertManager.showInformationAlert(
                LangManager.getBundle().getString("success.title"),
                message
            );
        } catch (Exception e) {
            logger.error("Error toggling period status", e);
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("error.title"),
                LangManager.getBundle().getString("academic.period.error.update")
            );
        }
    }
    
    private void confirmAndDeletePeriod(AcademicPeriodDTO period) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle(LangManager.getBundle().getString("confirm.title"));
        confirmDialog.setHeaderText(LangManager.getBundle().getString("academic.period.delete.confirm"));
        confirmDialog.setContentText(LangManager.getBundle().getString("academic.period.delete.warning"));
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deletePeriod(period);
            }
        });
    }
    
    private void deletePeriod(AcademicPeriodDTO period) {
        try {
            clientConnection.deleteAcademicPeriod(period.getId());
            loadAcademicPeriods();
            
            AlertManager.showInformationAlert(
                LangManager.getBundle().getString("success.title"),
                LangManager.getBundle().getString("academic.period.deleted")
            );
        } catch (Exception e) {
            logger.error("Error deleting period", e);
            
            // Display the error message from the server
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("error.title"),
                e.getMessage()
            );
        }
    }

    @Override
    public String getFxmlPath() {
        return "/fxml/academic_period_management.fxml";
    }

    @Override
    public void updateTexts() {
        createButton.setText(LangManager.getBundle().getString("academic.period.create"));
        refreshButton.setText(LangManager.getBundle().getString("button.refresh"));
        showInactiveCheckbox.setText(LangManager.getBundle().getString("academic.period.show.inactive"));
        
        nameColumn.setText(LangManager.getBundle().getString("academic.period.name"));
        typeColumn.setText(LangManager.getBundle().getString("academic.period.type"));
        startDateColumn.setText(LangManager.getBundle().getString("academic.period.start_date"));
        endDateColumn.setText(LangManager.getBundle().getString("academic.period.end_date"));
        statusColumn.setText(LangManager.getBundle().getString("academic.period.status"));
        actionsColumn.setText(LangManager.getBundle().getString("table.column.actions"));
        
        // Refresh the table to update the status and action button texts
        periodsTable.refresh();
    }
}
