package com.kasperovich.ui;

import com.kasperovich.config.AlertManager;
import com.kasperovich.dto.report.ApplicationStatusDTO;
import com.kasperovich.dto.report.ScholarshipDistributionDTO;
import com.kasperovich.dto.report.UserActivityDTO;
import com.kasperovich.dto.scholarship.AcademicPeriodDTO;
import com.kasperovich.dto.scholarship.ScholarshipProgramDTO;
import com.kasperovich.i18n.LangManager;
import com.kasperovich.operations.ChangeScene;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for the admin reports screen
 */
public class AdminReportController extends BaseController {
    private static final Logger logger = LogManager.getLogger(AdminReportController.class);
    
    @FXML private ComboBox<String> reportTypeComboBox;
    @FXML private HBox filterContainer;
    @FXML private TableView<Object> reportTable;
    @FXML private Button exportButton;
    @FXML private Button backButton;
    
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private ComboBox<ScholarshipProgramDTO> programComboBox;
    private ComboBox<AcademicPeriodDTO> periodComboBox;
    
    private final ObservableList<Object> reportData = FXCollections.observableArrayList();
    private String currentReportType;

    @Override
    public String getFxmlPath() {
        return "/fxml/admin_reports_screen.fxml";
    }

    @Override
    public void updateTexts() {
        // Update UI text elements from resource bundle
        backButton.setText(LangManager.getBundle().getString("button.back"));
        exportButton.setText(LangManager.getBundle().getString("button.export"));
        
        // Update report type options
        reportTypeComboBox.setPromptText(LangManager.getBundle().getString("reports.select_type"));
        
        // Clear and reload report types with updated translations
        String currentSelection = reportTypeComboBox.getSelectionModel().getSelectedItem();
        reportTypeComboBox.getItems().clear();
        reportTypeComboBox.getItems().addAll(
            LangManager.getBundle().getString("reports.type.scholarship_distribution"),
            LangManager.getBundle().getString("reports.type.application_status"),
            LangManager.getBundle().getString("reports.type.user_activity")
        );
        
        // Restore selection if possible
        if (currentSelection != null) {
            for (String item : reportTypeComboBox.getItems()) {
                if (item.equals(currentSelection)) {
                    reportTypeComboBox.getSelectionModel().select(item);
                    break;
                }
            }
        }
        
        // Refresh table column headers
        if (reportTable != null && reportTable.getColumns() != null) {
            setupTableForReportType(currentReportType);
        }
    }
    
    @Override
    public void initializeData() {
        // Set up report type combo box
        reportTypeComboBox.getItems().addAll(
            LangManager.getBundle().getString("reports.type.scholarship_distribution"),
            LangManager.getBundle().getString("reports.type.application_status"),
            LangManager.getBundle().getString("reports.type.user_activity")
        );
        
        // Add listener for report type changes
        reportTypeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                currentReportType = newVal;
                setupFiltersForReportType(newVal);
                setupTableForReportType(newVal);
                loadReportData();
            }
        });
        
        // Select first report type by default
        reportTypeComboBox.getSelectionModel().selectFirst();
    }
    
    /**
     * Sets up filter controls based on the selected report type
     * 
     * @param reportType The selected report type
     */
    private void setupFiltersForReportType(String reportType) {
        // Clear existing filters
        filterContainer.getChildren().clear();
        
        if (reportType.equals(LangManager.getBundle().getString("reports.type.scholarship_distribution")) ||
            reportType.equals(LangManager.getBundle().getString("reports.type.user_activity"))) {
            // Date range filters for scholarship distribution and user activity reports
            setupDateRangeFilters();
        } else if (reportType.equals(LangManager.getBundle().getString("reports.type.application_status"))) {
            // Program and period filters for application status report
            setupProgramPeriodFilters();
        }
    }
    
    /**
     * Sets up date range filters
     */
    private void setupDateRangeFilters() {
        Label dateRangeLabel = new Label(LangManager.getBundle().getString("reports.filter.date_range") + ":");
        
        startDatePicker = new DatePicker(LocalDate.now().minusMonths(6));
        startDatePicker.setPromptText(LangManager.getBundle().getString("reports.filter.start_date"));
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> loadReportData());
        
        endDatePicker = new DatePicker(LocalDate.now());
        endDatePicker.setPromptText(LangManager.getBundle().getString("reports.filter.end_date"));
        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> loadReportData());
        
        filterContainer.getChildren().addAll(dateRangeLabel, startDatePicker, endDatePicker);
    }
    
    /**
     * Sets up program and period filters
     */
    private void setupProgramPeriodFilters() {
        // Program filter
        Label programLabel = new Label(LangManager.getBundle().getString("reports.filter.program") + ":");
        programComboBox = new ComboBox<>();
        programComboBox.setPromptText(LangManager.getBundle().getString("reports.filter.all_programs"));
        programComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(ScholarshipProgramDTO program) {
                return program == null ? "" : program.getName();
            }

            @Override
            public ScholarshipProgramDTO fromString(String string) {
                return null; // Not needed for ComboBox
            }
        });
        programComboBox.valueProperty().addListener((obs, oldVal, newVal) -> loadReportData());
        
        // Period filter
        Label periodLabel = new Label(LangManager.getBundle().getString("reports.filter.period") + ":");
        periodComboBox = new ComboBox<>();
        periodComboBox.setPromptText(LangManager.getBundle().getString("reports.filter.all_periods"));
        periodComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(AcademicPeriodDTO period) {
                return period == null ? "" : period.getName();
            }

            @Override
            public AcademicPeriodDTO fromString(String string) {
                return null; // Not needed for ComboBox
            }
        });
        periodComboBox.valueProperty().addListener((obs, oldVal, newVal) -> loadReportData());
        
        // Load programs and periods
        loadProgramsAndPeriods();
        
        filterContainer.getChildren().addAll(programLabel, programComboBox, periodLabel, periodComboBox);
    }
    
    /**
     * Loads scholarship programs and academic periods for filters
     */
    private void loadProgramsAndPeriods() {
        try {
            // Load programs
            List<ScholarshipProgramDTO> programs = clientConnection.getScholarshipPrograms();
            programComboBox.getItems().clear();
            programComboBox.getItems().add(null); // Add null for "All Programs"
            programComboBox.getItems().addAll(programs);
            
            // Load periods
            List<AcademicPeriodDTO> periods = clientConnection.getAcademicPeriods();
            periodComboBox.getItems().clear();
            periodComboBox.getItems().add(null); // Add null for "All Periods"
            periodComboBox.getItems().addAll(periods);
        } catch (Exception e) {
            logger.error("Error loading programs and periods", e);
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("error.title"),
                LangManager.getBundle().getString("error.message") + e.getMessage()
            );
        }
    }
    
    /**
     * Sets up the table columns based on the selected report type
     * 
     * @param reportType The selected report type
     */
    @SuppressWarnings("unchecked")
    private void setupTableForReportType(String reportType) {
        reportTable.getColumns().clear();
        reportTable.setItems(reportData);
        
        if (reportType.equals(LangManager.getBundle().getString("reports.type.scholarship_distribution"))) {
            // Scholarship Distribution Report columns
            TableColumn<Object, String> programNameColumn = new TableColumn<>(
                LangManager.getBundle().getString("reports.column.program_name")
            );
            programNameColumn.setCellValueFactory(data -> 
                new SimpleStringProperty(((ScholarshipDistributionDTO) data.getValue()).getProgramName())
            );
            
            TableColumn<Object, BigDecimal> totalAmountColumn = new TableColumn<>(
                LangManager.getBundle().getString("reports.column.total_amount")
            );
            totalAmountColumn.setCellValueFactory(data -> 
                new SimpleObjectProperty<>(((ScholarshipDistributionDTO) data.getValue()).getTotalAmount())
            );
            totalAmountColumn.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(BigDecimal amount, boolean empty) {
                    super.updateItem(amount, empty);
                    if (empty || amount == null) {
                        setText(null);
                    } else {
                        setText("$" + new DecimalFormat("#,##0.00").format(amount));
                    }
                }
            });
            
            TableColumn<Object, Integer> applicationsCountColumn = new TableColumn<>(
                LangManager.getBundle().getString("reports.column.applications_count")
            );
            applicationsCountColumn.setCellValueFactory(data -> 
                new SimpleIntegerProperty(((ScholarshipDistributionDTO) data.getValue()).getApplicationsCount()).asObject()
            );
            
            TableColumn<Object, Integer> approvedCountColumn = new TableColumn<>(
                LangManager.getBundle().getString("reports.column.approved_count")
            );
            approvedCountColumn.setCellValueFactory(data -> 
                new SimpleIntegerProperty(((ScholarshipDistributionDTO) data.getValue()).getApprovedCount()).asObject()
            );
            
            TableColumn<Object, Double> approvalRateColumn = new TableColumn<>(
                LangManager.getBundle().getString("reports.column.approval_rate")
            );
            approvalRateColumn.setCellValueFactory(data -> 
                new SimpleDoubleProperty(((ScholarshipDistributionDTO) data.getValue()).getApprovalRate()).asObject()
            );
            approvalRateColumn.setCellFactory(_ -> new TableCell<>() {
                @Override
                protected void updateItem(Double rate, boolean empty) {
                    super.updateItem(rate, empty);
                    if (empty || rate == null) {
                        setText(null);
                    } else {
                        setText(new DecimalFormat("#0.0").format(rate) + "%");
                    }
                }
            });
            
            reportTable.getColumns().addAll(
                programNameColumn, 
                totalAmountColumn, 
                applicationsCountColumn, 
                approvedCountColumn, 
                approvalRateColumn
            );
        } else if (reportType.equals(LangManager.getBundle().getString("reports.type.application_status"))) {
            // Application Status Report columns
            TableColumn<Object, String> programNameColumn = new TableColumn<>(
                LangManager.getBundle().getString("reports.column.program_name")
            );
            programNameColumn.setCellValueFactory(data -> 
                new SimpleStringProperty(((ApplicationStatusDTO) data.getValue()).getProgramName())
            );
            
            TableColumn<Object, String> periodNameColumn = new TableColumn<>(
                LangManager.getBundle().getString("reports.column.period_name")
            );
            periodNameColumn.setCellValueFactory(data -> 
                new SimpleStringProperty(((ApplicationStatusDTO) data.getValue()).getPeriodName())
            );
            
            TableColumn<Object, Integer> pendingCountColumn = new TableColumn<>(
                LangManager.getBundle().getString("reports.column.pending_count")
            );
            pendingCountColumn.setCellValueFactory(data -> 
                new SimpleIntegerProperty(((ApplicationStatusDTO) data.getValue()).getPendingCount()).asObject()
            );
            
            TableColumn<Object, Integer> approvedCountColumn = new TableColumn<>(
                LangManager.getBundle().getString("reports.column.approved_count")
            );
            approvedCountColumn.setCellValueFactory(data -> 
                new SimpleIntegerProperty(((ApplicationStatusDTO) data.getValue()).getApprovedCount()).asObject()
            );
            
            TableColumn<Object, Integer> rejectedCountColumn = new TableColumn<>(
                LangManager.getBundle().getString("reports.column.rejected_count")
            );
            rejectedCountColumn.setCellValueFactory(data -> 
                new SimpleIntegerProperty(((ApplicationStatusDTO) data.getValue()).getRejectedCount()).asObject()
            );
            
            TableColumn<Object, BigDecimal> totalAmountColumn = new TableColumn<>(
                LangManager.getBundle().getString("reports.column.total_amount")
            );
            totalAmountColumn.setCellValueFactory(data -> 
                new SimpleObjectProperty<>(((ApplicationStatusDTO) data.getValue()).getTotalAmount())
            );
            totalAmountColumn.setCellFactory(_ -> new TableCell<>() {
                @Override
                protected void updateItem(BigDecimal amount, boolean empty) {
                    super.updateItem(amount, empty);
                    if (empty || amount == null) {
                        setText(null);
                    } else {
                        setText("$" + new DecimalFormat("#,##0.00").format(amount));
                    }
                }
            });
            
            reportTable.getColumns().addAll(
                programNameColumn, 
                periodNameColumn, 
                pendingCountColumn, 
                approvedCountColumn, 
                rejectedCountColumn, 
                totalAmountColumn
            );
        } else if (reportType.equals(LangManager.getBundle().getString("reports.type.user_activity"))) {
            // User Activity Report columns
            TableColumn<Object, String> monthColumn = new TableColumn<>(
                LangManager.getBundle().getString("reports.column.month")
            );
            monthColumn.setCellValueFactory(data -> 
                new SimpleStringProperty(((UserActivityDTO) data.getValue()).getMonth())
            );
            
            TableColumn<Object, Integer> newUsersColumn = new TableColumn<>(
                LangManager.getBundle().getString("reports.column.new_users")
            );
            newUsersColumn.setCellValueFactory(data -> 
                new SimpleIntegerProperty(((UserActivityDTO) data.getValue()).getNewUserCount()).asObject()
            );
            
            TableColumn<Object, Integer> applicationCountColumn = new TableColumn<>(
                LangManager.getBundle().getString("reports.column.application_count")
            );
            applicationCountColumn.setCellValueFactory(data -> 
                new SimpleIntegerProperty(((UserActivityDTO) data.getValue()).getApplicationCount()).asObject()
            );
            
            reportTable.getColumns().addAll(
                monthColumn, 
                newUsersColumn, 
                applicationCountColumn
            );
        }
    }
    
    /**
     * Loads report data based on the selected report type and filters
     */
    private void loadReportData() {
        reportData.clear();
        
        try {
            if (currentReportType.equals(LangManager.getBundle().getString("reports.type.scholarship_distribution"))) {
                // Load scholarship distribution report
                if (startDatePicker != null && endDatePicker != null) {
                    LocalDate startDate = startDatePicker.getValue();
                    LocalDate endDate = endDatePicker.getValue();
                    
                    List<ScholarshipDistributionDTO> data = clientConnection.getScholarshipDistributionReport(startDate, endDate);
                    reportData.addAll(data);
                }
            } else if (currentReportType.equals(LangManager.getBundle().getString("reports.type.application_status"))) {
                // Load application status report
                if (programComboBox != null && periodComboBox != null) {
                    Long programId = programComboBox.getValue() != null ? programComboBox.getValue().getId() : null;
                    Long periodId = periodComboBox.getValue() != null ? periodComboBox.getValue().getId() : null;
                    
                    List<ApplicationStatusDTO> data = clientConnection.getApplicationStatusReport(programId, periodId);
                    reportData.addAll(data);
                }
            } else if (currentReportType.equals(LangManager.getBundle().getString("reports.type.user_activity"))) {
                // Load user activity report
                if (startDatePicker != null && endDatePicker != null) {
                    LocalDate startDate = startDatePicker.getValue();
                    LocalDate endDate = endDatePicker.getValue();
                    
                    List<UserActivityDTO> data = clientConnection.getUserActivityReport(startDate, endDate);
                    reportData.addAll(data);
                }
            }
        } catch (IOException e) {
            logger.error("Error loading report data", e);
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("error.title"),
                LangManager.getBundle().getString("error.message") + e.getMessage()
            );
        }
    }
    
    /**
     * Handles the export button action
     */
    @FXML
    private void handleExport() {
        if (reportData.isEmpty()) {
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(LangManager.getBundle().getString("button.export"));
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        
        // Set default file name based on report type
        String fileName = currentReportType.replace(" ", "_").toLowerCase() + "_report.csv";
        fileChooser.setInitialFileName(fileName);
        
        File file = fileChooser.showSaveDialog(reportTable.getScene().getWindow());
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Write CSV header
                StringBuilder header = new StringBuilder();
                for (TableColumn<Object, ?> column : reportTable.getColumns()) {
                    if (!header.isEmpty()) {
                        header.append(",");
                    }
                    header.append("\"").append(column.getText()).append("\"");
                }
                writer.write(header + "\n");
                
                // Write data rows
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                DecimalFormat decimalFormat = new DecimalFormat("#0.00");
                
                for (Object row : reportData) {
                    StringBuilder line = new StringBuilder();
                    
                    if (row instanceof ScholarshipDistributionDTO data) {
                        line.append("\"").append(data.getProgramName()).append("\",");
                        line.append("\"").append(data.getTotalAmount()).append("\",");
                        line.append(data.getApplicationsCount()).append(",");
                        line.append(data.getApprovedCount()).append(",");
                        line.append(decimalFormat.format(data.getApprovalRate())).append("%");
                    } else if (row instanceof ApplicationStatusDTO data) {
                        line.append("\"").append(data.getProgramName()).append("\",");
                        line.append("\"").append(data.getPeriodName()).append("\",");
                        line.append(data.getPendingCount()).append(",");
                        line.append(data.getApprovedCount()).append(",");
                        line.append(data.getRejectedCount()).append(",");
                        line.append("\"").append(data.getTotalAmount()).append("\"");
                    } else if (row instanceof UserActivityDTO data) {
                        line.append("\"").append(data.getMonth()).append("\",");
                        line.append(data.getNewUserCount()).append(",");
                        line.append(data.getApplicationCount());
                    }
                    
                    writer.write(line + "\n");
                }
                
                AlertManager.showInformationAlert(
                    LangManager.getBundle().getString("success.title"),
                    LangManager.getBundle().getString("reports.export.success")
                );
            } catch (IOException e) {
                logger.error("Error exporting report", e);
                AlertManager.showErrorAlert(
                    LangManager.getBundle().getString("error.title"),
                    LangManager.getBundle().getString("reports.export.error") + ": " + e.getMessage()
                );
            }
        }
    }
    
    /**
     * Handles the back button action
     */
    @FXML
    private void handleBackAction(ActionEvent event) {
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
}
