package com.kasperovich.ui;

import com.kasperovich.config.AlertManager;
import com.kasperovich.dto.auth.UserDTO;
import com.kasperovich.dto.scholarship.BudgetDTO;
import com.kasperovich.dto.scholarship.FundAllocationDTO;
import com.kasperovich.dto.scholarship.ScholarshipProgramDTO;
import com.kasperovich.entities.AllocationStatus;
import com.kasperovich.i18n.LangManager;
import com.kasperovich.operations.ChangeScene;
import com.kasperovich.utils.LoggerUtil;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import lombok.Setter;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for the fund allocation screen.
 */
public class FundAllocationController extends BaseController {
    private static final Logger logger = LoggerUtil.getLogger(FundAllocationController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance();

    @FXML
    private Label titleLabel;

    @FXML
    private Label userNameLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private Label versionLabel;

    @FXML
    private Button logoutButton;

    @FXML
    private Button dashboardButton;

    @FXML
    private Button programsButton;

    @FXML
    private Button applicationsButton;

    @FXML
    private Button budgetsButton;

    @FXML
    private Button fundAllocationButton;

    @FXML
    private Button profileButton;

    @FXML
    private ComboBox<BudgetDTO> budgetComboBox;

    @FXML
    private TextArea budgetDetailsTextArea;

    @FXML
    private ComboBox<ScholarshipProgramDTO> programComboBox;

    @FXML
    private TextField amountTextField;

    @FXML
    private TextArea notesTextArea;

    @FXML
    private Button allocateButton;

    @FXML
    private ComboBox<String> filterComboBox;

    @FXML
    private Button refreshButton;

    @FXML
    private TableView<FundAllocationDTO> allocationsTableView;

    @FXML
    private TableColumn<FundAllocationDTO, Long> idColumn;

    @FXML
    private TableColumn<FundAllocationDTO, String> budgetColumn;

    @FXML
    private TableColumn<FundAllocationDTO, String> programColumn;

    @FXML
    private TableColumn<FundAllocationDTO, String> amountColumn;

    @FXML
    private TableColumn<FundAllocationDTO, String> dateColumn;

    @FXML
    private TableColumn<FundAllocationDTO, String> statusColumn;

    @Setter
    private UserDTO user;

    private ObservableList<BudgetDTO> budgets = FXCollections.observableArrayList();
    private ObservableList<ScholarshipProgramDTO> programs = FXCollections.observableArrayList();
    private ObservableList<FundAllocationDTO> allocations = FXCollections.observableArrayList();

    /**
     * Initializes the controller.
     */
    @Override
    public void initializeData() {
        if (user != null) {
            userNameLabel.setText(user.getUsername());
            roleLabel.setText(LangManager.getBundle().getString("dashboard.role") + ": " + user.getRole());
        } else {
            logger.error("User data is null in FundAllocationController");
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("error.title"),
                "User data is not available"
            );
        }

        versionLabel.setText(LangManager.getBundle().getString("dashboard.version") + ": 1.0.0");

        // Set up UI components
        setupUIComponents();

        // Load data from server
        if (getClientConnection() != null) {
            loadData();
        }

        // Update texts
        updateTexts();

        logger.info("Fund allocation screen initialized for user: {}", user != null ? user.getUsername() : "unknown");
    }

    /**
     * Sets up UI components.
     */
    private void setupUIComponents() {
        // Setup budget combo box
        budgetComboBox.setItems(budgets);
        budgetComboBox.setConverter(new StringConverter<BudgetDTO>() {
            @Override
            public String toString(BudgetDTO budget) {
                if (budget == null) {
                    return null;
                }
                return String.format("FY%d - %s (%s)",
                        budget.getFiscalYear(),
                        budget.getFiscalPeriod(),
                        CURRENCY_FORMAT.format(budget.getTotalAmount()));
            }

            @Override
            public BudgetDTO fromString(String string) {
                return null; // Not needed for combo box
            }
        });
        budgetComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateBudgetDetails(newVal);
        });

        // Setup program combo box
        programComboBox.setItems(programs);
        programComboBox.setConverter(new StringConverter<ScholarshipProgramDTO>() {
            @Override
            public String toString(ScholarshipProgramDTO program) {
                if (program == null) {
                    return null;
                }
                return program.getName();
            }

            @Override
            public ScholarshipProgramDTO fromString(String string) {
                return null; // Not needed for combo box
            }
        });

        // Setup filter combo box
        filterComboBox.setItems(FXCollections.observableArrayList(
            "All Allocations",
            "By Current Budget",
            "By Selected Program"
        ));
        filterComboBox.getSelectionModel().selectFirst();
        filterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                applyFilter(newVal);
            }
        });

        // Setup allocations table
        setupAllocationsTable();
    }

    /**
     * Sets up the allocations table.
     */
    private void setupAllocationsTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        budgetColumn.setCellValueFactory(cellData -> {
            String budgetInfo = cellData.getValue().getBudgetFiscalYear() + " - " + 
                                cellData.getValue().getBudgetFiscalPeriod();
            return new SimpleStringProperty(budgetInfo);
        });
        
        programColumn.setCellValueFactory(cellData -> {
            String programName = cellData.getValue().getProgramName();
            if (programName != null) {
                return new SimpleStringProperty(programName);
            }
            return new SimpleStringProperty("N/A");
        });
        
        amountColumn.setCellValueFactory(cellData -> {
            BigDecimal amount = cellData.getValue().getAmount();
            if (amount != null) {
                return new SimpleStringProperty(CURRENCY_FORMAT.format(amount));
            }
            return new SimpleStringProperty("$0.00");
        });
        
        dateColumn.setCellValueFactory(cellData -> {
            LocalDateTime date = cellData.getValue().getAllocationDate();
            if (date != null) {
                return new SimpleStringProperty(DATE_FORMATTER.format(date));
            }
            return new SimpleStringProperty("N/A");
        });
        
        statusColumn.setCellValueFactory(cellData -> {
            AllocationStatus status = cellData.getValue().getStatus();
            if (status != null) {
                return new SimpleStringProperty(status.name());
            }
            return new SimpleStringProperty("N/A");
        });
        
        allocationsTableView.setItems(allocations);
    }

    /**
     * Updates the budget details text area.
     * 
     * @param budget The selected budget
     */
    private void updateBudgetDetails(BudgetDTO budget) {
        if (budget == null) {
            budgetDetailsTextArea.setText("");
            return;
        }
        
        StringBuilder details = new StringBuilder();
        details.append("Fiscal Year: ").append(budget.getFiscalYear()).append("\n");
        details.append("Period: ").append(budget.getFiscalPeriod()).append("\n");
        details.append("Total Amount: ").append(CURRENCY_FORMAT.format(budget.getTotalAmount())).append("\n");
        details.append("Allocated: ").append(CURRENCY_FORMAT.format(budget.getAllocatedAmount())).append("\n");
        details.append("Remaining: ").append(CURRENCY_FORMAT.format(budget.getRemainingAmount())).append("\n");
        details.append("Status: ").append(budget.getStatus());
        
        budgetDetailsTextArea.setText(details.toString());
    }

    /**
     * Applies a filter to the allocations table.
     * 
     * @param filterType The type of filter to apply
     */
    private void applyFilter(String filterType) {
        try {
            if (filterType.equals("All Allocations")) {
                loadAllAllocations();
            } else if (filterType.equals("By Current Budget")) {
                BudgetDTO selectedBudget = budgetComboBox.getValue();
                if (selectedBudget != null) {
                    loadAllocationsByBudget(selectedBudget.getId());
                } else {
                    AlertManager.showWarningAlert(
                        LangManager.getBundle().getString("warning.title"),
                        LangManager.getBundle().getString("fund.allocation.error.select_budget")
                    );
                }
            } else if (filterType.equals("By Selected Program")) {
                ScholarshipProgramDTO selectedProgram = programComboBox.getValue();
                if (selectedProgram != null) {
                    loadAllocationsByProgram(selectedProgram.getId());
                } else {
                    AlertManager.showWarningAlert(
                        LangManager.getBundle().getString("warning.title"),
                        LangManager.getBundle().getString("fund.allocation.error.select_program")
                    );
                }
            }
        } catch (Exception e) {
            logger.error("Error applying filter", e);
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("error.title"),
                "Error applying filter: " + e.getMessage()
            );
        }
    }

    /**
     * Loads data from the server.
     */
    private void loadData() {
        try {
            // Load active budgets
            List<BudgetDTO> activeBudgets = getClientConnection().getAllBudgets();
            budgets.clear();
            budgets.addAll(activeBudgets);
            
            if (!budgets.isEmpty()) {
                budgetComboBox.getSelectionModel().selectFirst();
            }
            
            // Load scholarship programs
            List<ScholarshipProgramDTO> scholarshipPrograms = getClientConnection().getScholarshipPrograms();
            programs.clear();
            programs.addAll(scholarshipPrograms);
            
            if (!programs.isEmpty()) {
                programComboBox.getSelectionModel().selectFirst();
            }
            
            // Load all allocations initially
            loadAllAllocations();
            
            logger.debug("Loaded {} budgets and {} programs", budgets.size(), programs.size());
        } catch (Exception e) {
            logger.error("Error loading data", e);
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("error.title"),
                "Error loading data: " + e.getMessage()
            );
        }
    }

    /**
     * Loads all fund allocations.
     */
    private void loadAllAllocations() {
        try {
            allocations.clear();
            
            // Load allocations for each budget
            for (BudgetDTO budget : budgets) {
                List<FundAllocationDTO> budgetAllocations = getClientConnection().getAllocationsByBudget(budget.getId());
                allocations.addAll(budgetAllocations);
            }
            
            logger.debug("Loaded {} allocations", allocations.size());
        } catch (Exception e) {
            logger.error("Error loading allocations", e);
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("error.title"),
                "Error loading allocations: " + e.getMessage()
            );
        }
    }

    /**
     * Loads fund allocations for a specific budget.
     * 
     * @param budgetId The budget ID
     */
    private void loadAllocationsByBudget(Long budgetId) {
        try {
            List<FundAllocationDTO> budgetAllocations = getClientConnection().getAllocationsByBudget(budgetId);
            allocations.clear();
            allocations.addAll(budgetAllocations);
            
            logger.debug("Loaded {} allocations for budget {}", allocations.size(), budgetId);
        } catch (Exception e) {
            logger.error("Error loading allocations for budget {}", budgetId, e);
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("error.title"),
                "Error loading allocations: " + e.getMessage()
            );
        }
    }

    /**
     * Loads fund allocations for a specific program.
     * 
     * @param programId The program ID
     */
    private void loadAllocationsByProgram(Long programId) {
        try {
            List<FundAllocationDTO> programAllocations = getClientConnection().getAllocationsByProgram(programId);
            allocations.clear();
            allocations.addAll(programAllocations);
            
            logger.debug("Loaded {} allocations for program {}", allocations.size(), programId);
        } catch (Exception e) {
            logger.error("Error loading allocations for program {}", programId, e);
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("error.title"),
                "Error loading allocations: " + e.getMessage()
            );
        }
    }

    /**
     * Handles the allocate funds button action.
     */
    @FXML
    private void handleAllocateFunds() {
        try {
            BudgetDTO selectedBudget = budgetComboBox.getValue();
            ScholarshipProgramDTO selectedProgram = programComboBox.getValue();
            
            if (selectedBudget == null) {
                AlertManager.showWarningAlert(
                    LangManager.getBundle().getString("warning.title"),
                    LangManager.getBundle().getString("fund.allocation.error.select_budget")
                );
                return;
            }
            
            if (selectedProgram == null) {
                AlertManager.showWarningAlert(
                    LangManager.getBundle().getString("warning.title"),
                    LangManager.getBundle().getString("fund.allocation.error.select_program")
                );
                return;
            }
            
            String amountText = amountTextField.getText().trim();
            if (amountText.isEmpty()) {
                AlertManager.showWarningAlert(
                    LangManager.getBundle().getString("warning.title"),
                    LangManager.getBundle().getString("fund.allocation.error.enter_amount")
                );
                return;
            }
            
            BigDecimal amount;
            try {
                amount = new BigDecimal(amountText);
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException(LangManager.getBundle().getString("fund.allocation.error.invalid_amount"));
                }
            } catch (NumberFormatException e) {
                AlertManager.showWarningAlert(
                    LangManager.getBundle().getString("warning.title"),
                    LangManager.getBundle().getString("fund.allocation.error.invalid_amount")
                );
                return;
            } catch (IllegalArgumentException e) {
                AlertManager.showWarningAlert(
                    LangManager.getBundle().getString("warning.title"),
                    e.getMessage()
                );
                return;
            }
            
            // Check if amount exceeds budget's remaining amount
            if (amount.compareTo(selectedBudget.getRemainingAmount()) > 0) {
                AlertManager.showWarningAlert(
                    LangManager.getBundle().getString("warning.title"),
                    LangManager.getBundle().getString("fund.allocation.error.amount_exceeds")
                );
                return;
            }
            
            String notes = notesTextArea.getText();
            
            // Allocate funds
            FundAllocationDTO allocation = getClientConnection().allocateFunds(
                selectedBudget.getId(),
                selectedProgram.getId(),
                amount,
                notes
            );
            
            // Show success message
            AlertManager.showInformationAlert(
                LangManager.getBundle().getString("success.title"),
                LangManager.getBundle().getString("fund.allocation.success")
            );
            
            // Refresh data
            loadData();
            
            // Clear form
            amountTextField.clear();
            notesTextArea.clear();
            
            logger.info("Allocated {} to program {} from budget {}", 
                CURRENCY_FORMAT.format(amount), 
                selectedProgram.getName(), 
                selectedBudget.getId());
        } catch (Exception e) {
            logger.error("Error allocating funds", e);
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("error.title"),
                "Error allocating funds: " + e.getMessage()
            );
        }
    }

    /**
     * Handles the refresh button action.
     */
    @FXML
    private void handleRefresh() {
        loadData();
    }

    /**
     * Handles the logout button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleLogoutAction(ActionEvent event) {
        try {
            // Close the client connection
            getClientConnection().logout();
            
            // Navigate back to login screen
            ChangeScene.changeScene(
                event,
                "/fxml/login_screen.fxml",
                LangManager.getBundle().getString("login.title"),
                getClientConnection(),
                null
            );
            
            logger.info("User logged out: {}", user.getUsername());
        } catch (Exception e) {
            logger.error("Error during logout", e);
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("error.title"),
                "Error during logout: " + e.getMessage()
            );
        }
    }

    /**
     * Handles the dashboard button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleDashboardAction(ActionEvent event) {
        try {
            ChangeScene.changeScene(
                event,
                "/fxml/admin_dashboard_screen.fxml",
                LangManager.getBundle().getString("admin.dashboard.title"),
                getClientConnection(),
                user
            );
            
            logger.debug("Navigated to admin dashboard from fund allocation screen");
        } catch (Exception e) {
            logger.error("Error navigating to admin dashboard", e);
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("navigation.error"),
                "Could not navigate to admin dashboard: " + e.getMessage()
            );
        }
    }

    /**
     * Handles the programs button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleProgramsAction(ActionEvent event) {
        try {
            ChangeScene.changeScene(
                event,
                "/fxml/admin_scholarship_programs_screen.fxml",
                LangManager.getBundle().getString("admin.programs.title"),
                getClientConnection(),
                user
            );
            
            logger.debug("Navigated to scholarship programs from fund allocation screen");
        } catch (Exception e) {
            logger.error("Error navigating to scholarship programs", e);
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("navigation.error"),
                "Could not navigate to scholarship programs: " + e.getMessage()
            );
        }
    }

    /**
     * Handles the applications button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleApplicationsAction(ActionEvent event) {
        try {
            ChangeScene.changeScene(
                event,
                "/fxml/admin_application_review.fxml",
                LangManager.getBundle().getString("admin.applications.title"),
                getClientConnection(),
                user
            );
            
            logger.debug("Navigated to application review from fund allocation screen");
        } catch (Exception e) {
            logger.error("Error navigating to application review", e);
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("navigation.error"),
                "Could not navigate to application review: " + e.getMessage()
            );
        }
    }

    /**
     * Handles the budgets button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleBudgetsAction(ActionEvent event) {
        // This will be implemented when we create the budget management screen
        AlertManager.showInformationAlert(
            "Information",
            "Budget management screen will be implemented next"
        );
    }

    /**
     * Handles the profile button action.
     * 
     * @param event The action event
     */
    @FXML
    public void handleProfileAction(ActionEvent event) {
        try {
            ChangeScene.changeScene(
                event,
                "/fxml/profile_screen.fxml",
                LangManager.getBundle().getString("profile.title"),
                getClientConnection(),
                user
            );
            
            logger.debug("Navigated to profile screen from fund allocation screen");
        } catch (Exception e) {
            logger.error("Error navigating to profile screen", e);
            AlertManager.showErrorAlert(
                LangManager.getBundle().getString("navigation.error"),
                "Could not navigate to profile screen: " + e.getMessage()
            );
        }
    }

    /**
     * Updates the UI texts based on the current language.
     */
    @Override
    public void updateTexts() {
        titleLabel.setText(LangManager.getBundle().getString("fund.allocation.title"));
        logoutButton.setText(LangManager.getBundle().getString("dashboard.logout"));
        dashboardButton.setText(LangManager.getBundle().getString("admin.button.dashboard"));
        programsButton.setText(LangManager.getBundle().getString("admin.button.programs"));
        applicationsButton.setText(LangManager.getBundle().getString("admin.button.applications"));
        budgetsButton.setText(LangManager.getBundle().getString("admin.button.budgets"));
        fundAllocationButton.setText(LangManager.getBundle().getString("admin.button.fund-allocation"));
        profileButton.setText(LangManager.getBundle().getString("dashboard.button.profile"));
        allocateButton.setText(LangManager.getBundle().getString("fund.allocation.allocate"));
        refreshButton.setText(LangManager.getBundle().getString("fund.allocation.refresh"));
        
        // Update table column headers
        idColumn.setText(LangManager.getBundle().getString("fund.allocation.id"));
        budgetColumn.setText(LangManager.getBundle().getString("fund.allocation.budget_column"));
        programColumn.setText(LangManager.getBundle().getString("fund.allocation.program_column"));
        amountColumn.setText(LangManager.getBundle().getString("fund.allocation.amount_column"));
        dateColumn.setText(LangManager.getBundle().getString("fund.allocation.date_column"));
        statusColumn.setText(LangManager.getBundle().getString("fund.allocation.status_column"));
    }

    /**
     * Gets the FXML path for this controller.
     * 
     * @return The FXML path
     */
    @Override
    public String getFxmlPath() {
        return "/fxml/fund_allocation_screen.fxml";
    }
}
