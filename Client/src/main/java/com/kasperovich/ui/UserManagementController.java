package com.kasperovich.ui;

import com.kasperovich.config.AlertManager;
import com.kasperovich.dto.auth.UserDTO;
import com.kasperovich.i18n.LangManager;
import com.kasperovich.operations.ChangeScene;
import com.kasperovich.utils.LoggerUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;

/**
 * Controller for the User Management screen.
 * Allows administrators to view, edit, and manage user accounts.
 */
public class UserManagementController extends BaseController {
    private static final Logger logger = LoggerUtil.getLogger(UserManagementController.class);

    @FXML private Button backButton;
    @FXML private Button refreshButton;
    @FXML private ComboBox<String> roleFilterComboBox;
    @FXML private TextField searchField;
    @FXML private TableView<UserDTO> usersTable;
    @FXML private TableColumn<UserDTO, String> idColumn;
    @FXML private TableColumn<UserDTO, String> usernameColumn;
    @FXML private TableColumn<UserDTO, String> nameColumn;
    @FXML private TableColumn<UserDTO, String> emailColumn;
    @FXML private TableColumn<UserDTO, String> roleColumn;
    @FXML private TableColumn<UserDTO, String> statusColumn;
    @FXML private TableColumn<UserDTO, Void> actionsColumn;
    @FXML private Label statusLabel;

    private ObservableList<UserDTO> usersList = FXCollections.observableArrayList();
    private FilteredList<UserDTO> filteredUsers;

    @Override
    public void initializeData() {
        setupTable();
        setupFilters();
        loadUsers();
    }

    /**
     * Sets up the table columns and cell factories.
     */
    private void setupTable() {
        // Set up table columns
        idColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId().toString()));
        usernameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getFirstName() + " " + cellData.getValue().getLastName()));
        emailColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmail()));
        roleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRole()));
        
        // Status column with custom cell factory for active/inactive
        statusColumn.setCellValueFactory(cellData -> {
            boolean isActive = true; // Default to active since we don't have this info in UserDTO
            String statusKey = isActive ? 
                    "admin.user.management.status.active" : 
                    "admin.user.management.status.inactive";
            return new SimpleStringProperty(LangManager.getBundle().getString(statusKey));
        });
        
        // Actions column with edit and activate/deactivate buttons
        actionsColumn.setCellFactory(createActionsColumnCellFactory());
        
        // Set up filtered list
        filteredUsers = new FilteredList<>(usersList);
        usersTable.setItems(filteredUsers);
        
        // Add placeholder text for empty table
        usersTable.setPlaceholder(new Label(LangManager.getBundle().getString("admin.user.management.no.users")));
    }
    
    /**
     * Sets up the role filter and search functionality.
     */
    private void setupFilters() {
        // Set up role filter combo box
        roleFilterComboBox.getItems().addAll(
                LangManager.getBundle().getString("admin.user.management.filter.all"),
                LangManager.getBundle().getString("admin.user.management.filter.admin"),
                LangManager.getBundle().getString("admin.user.management.filter.student")
        );
        roleFilterComboBox.getSelectionModel().selectFirst();
        
        // Add listener to role filter combo box
        roleFilterComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            applyFilters();
        });
        
        // Add listener to search field
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            applyFilters();
        });
    }
    
    /**
     * Applies the current filters to the users list.
     */
    private void applyFilters() {
        String roleFilter = roleFilterComboBox.getValue();
        String searchText = searchField.getText().toLowerCase();
        
        Predicate<UserDTO> rolePredicate = user -> {
            if (roleFilter.equals(LangManager.getBundle().getString("admin.user.management.filter.all"))) {
                return true;
            } else if (roleFilter.equals(LangManager.getBundle().getString("admin.user.management.filter.admin"))) {
                return "ADMIN".equals(user.getRole());
            } else {
                return "STUDENT".equals(user.getRole());
            }
        };
        
        Predicate<UserDTO> searchPredicate = user -> {
            if (searchText == null || searchText.isEmpty()) {
                return true;
            }
            
            return user.getUsername().toLowerCase().contains(searchText) ||
                   user.getFirstName().toLowerCase().contains(searchText) ||
                   user.getLastName().toLowerCase().contains(searchText);
        };
        
        filteredUsers.setPredicate(rolePredicate.and(searchPredicate));
        
        // Update status label with count
        updateStatusLabel();
    }
    
    /**
     * Updates the status label with the current count of users.
     */
    private void updateStatusLabel() {
        int totalUsers = usersList.size();
        int filteredCount = filteredUsers.size();
        
        if (totalUsers == filteredCount) {
            statusLabel.setText(MessageFormat.format(
                    LangManager.getBundle().getString("admin.user.management.total"), 
                    totalUsers));
        } else {
            statusLabel.setText(MessageFormat.format(
                    LangManager.getBundle().getString("admin.user.management.total"), 
                    filteredCount + " / " + totalUsers));
        }
    }
    
    /**
     * Creates a cell factory for the actions column.
     *
     * @return the cell factory
     */
    private Callback<TableColumn<UserDTO, Void>, TableCell<UserDTO, Void>> createActionsColumnCellFactory() {
        return new Callback<>() {
            @Override
            public TableCell<UserDTO, Void> call(TableColumn<UserDTO, Void> param) {
                return new TableCell<>() {
                    private final Button editButton = new Button(LangManager.getBundle().getString("admin.user.management.edit"));
                    private final Button statusButton = new Button();
                    private final HBox buttonsBox = new HBox(5, editButton, statusButton);
                    
                    {
                        // Set up edit button
                        editButton.getStyleClass().add("small-button");
                        editButton.setOnAction(event -> {
                            UserDTO user = getTableRow().getItem();
                            if (user != null) {
                                openEditDialog(user);
                            }
                        });
                        
                        // Set up status button
                        statusButton.getStyleClass().add("small-button");
                        statusButton.setOnAction(event -> {
                            UserDTO user = getTableRow().getItem();
                            if (user != null) {
                                boolean isActive = true; // Default to active since we don't have this info in UserDTO
                                updateUserStatus(user, !isActive);
                            }
                        });
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        
                        if (empty) {
                            setGraphic(null);
                        } else {
                            UserDTO user = getTableRow().getItem();
                            if (user != null) {
                                boolean isActive = true; // Default to active since we don't have this info in UserDTO
                                
                                // Update status button text based on current status
                                if (isActive) {
                                    statusButton.setText(LangManager.getBundle().getString("admin.user.management.deactivate"));
                                    statusButton.getStyleClass().remove("success-button");
                                    statusButton.getStyleClass().add("danger-button");
                                } else {
                                    statusButton.setText(LangManager.getBundle().getString("admin.user.management.activate"));
                                    statusButton.getStyleClass().remove("danger-button");
                                    statusButton.getStyleClass().add("success-button");
                                }
                                
                                // Don't allow deactivating the current user
                                if (user.getId().equals(UserManagementController.this.user.getId())) {
                                    statusButton.setDisable(true);
                                } else {
                                    statusButton.setDisable(false);
                                }
                                
                                setGraphic(buttonsBox);
                            } else {
                                setGraphic(null);
                            }
                        }
                    }
                };
            }
        };
    }
    
    /**
     * Loads all users from the server.
     */
    private void loadUsers() {
        statusLabel.setText(LangManager.getBundle().getString("admin.user.management.loading"));
        
        // Run in background thread
        new Thread(() -> {
            try {
                List<UserDTO> users = getClientConnection().getAllUsers();
                
                Platform.runLater(() -> {
                    usersList.clear();
                    usersList.addAll(users);
                    applyFilters();
                    logger.info("Loaded {} users", users.size());
                });
            } catch (Exception e) {
                logger.error("Error loading users", e);
                Platform.runLater(() -> {
                    statusLabel.setText(LangManager.getBundle().getString("admin.user.management.error"));
                    AlertManager.showErrorAlert(
                            LangManager.getBundle().getString("error.title"),
                            LangManager.getBundle().getString("admin.user.management.error") + ": " + e.getMessage()
                    );
                });
            }
        }).start();
    }
    
    /**
     * Opens the edit dialog for a user.
     *
     * @param user the user to edit
     */
    private void openEditDialog(UserDTO user) {
        try {
            // Load the edit dialog FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/edit_profile_dialog.fxml"));
            loader.setResources(LangManager.getBundle());
            Parent root = loader.load();
            
            // Get the controller and set up the dialog
            EditProfileDialogController controller = loader.getController();
            controller.setClientConnection(getClientConnection());
            controller.setup(user, this.user); // Pass both the user to edit and the current admin user
            controller.initializeData();
            
            // Set up the callback to update the user in the table
            controller.setCallback(updatedUser -> {
                int index = -1;
                for (int i = 0; i < usersList.size(); i++) {
                    if (usersList.get(i).getId().equals(updatedUser.getId())) {
                        index = i;
                        break;
                    }
                }
                
                if (index >= 0) {
                    usersList.set(index, updatedUser);
                }
            });
            
            // Create and show the dialog
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle(LangManager.getBundle().getString("profile.edit.title"));
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();
            
        } catch (IOException e) {
            logger.error("Error opening edit dialog", e);
            AlertManager.showErrorAlert(
                    LangManager.getBundle().getString("error.title"),
                    "Error opening edit dialog: " + e.getMessage()
            );
        }
    }
    
    /**
     * Updates a user's active status.
     *
     * @param user the user to update
     * @param active the new active status
     */
    private void updateUserStatus(UserDTO user, boolean active) {
        // Run in background thread
            try {
                UserDTO updatedUser = getClientConnection().updateUserStatus(user.getId(), active);
                
                Platform.runLater(() -> {
                    // Update the user in the list
                    int index = -1;
                    for (int i = 0; i < usersList.size(); i++) {
                        if (usersList.get(i).getId().equals(updatedUser.getId())) {
                            index = i;
                            break;
                        }
                    }
                    
                    if (index >= 0) {
                        usersList.set(index, updatedUser);
                    }
                    
                    // Show success message
                    AlertManager.showInformationAlert(
                            LangManager.getBundle().getString("success"),
                            "User status updated successfully"
                    );
                });
            } catch (Exception e) {
                logger.error("Error updating user status", e);
                Platform.runLater(() -> {
                    AlertManager.showErrorAlert(
                            LangManager.getBundle().getString("error.title"),
                            "Error updating user status: " + e.getMessage()
                    );
                });
            }
    }
    
    /**
     * Handles the back button action.
     */
    @FXML
    private void handleBackButtonAction(ActionEvent event) {
        try {
            ChangeScene.changeScene(
                event,
                "/fxml/admin_dashboard_screen.fxml",
                LangManager.getBundle().getString("admin.dashboard.title"),
                getClientConnection(),
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
    
    /**
     * Handles the refresh button action.
     */
    @FXML
    private void handleRefreshButtonAction() {
        loadUsers();
    }
    
    @Override
    public void updateTexts() {
        // Update all text elements with current language
        backButton.setText(LangManager.getBundle().getString("button.back"));
        refreshButton.setText(LangManager.getBundle().getString("dashboard.refresh"));
        
        // Update table column headers
        idColumn.setText(LangManager.getBundle().getString("admin.user.management.id"));
        usernameColumn.setText(LangManager.getBundle().getString("admin.user.management.username"));
        nameColumn.setText(LangManager.getBundle().getString("admin.user.management.name"));
        emailColumn.setText(LangManager.getBundle().getString("admin.user.management.email"));
        roleColumn.setText(LangManager.getBundle().getString("admin.user.management.role"));
        statusColumn.setText(LangManager.getBundle().getString("admin.user.management.status"));
        actionsColumn.setText(LangManager.getBundle().getString("admin.user.management.actions"));
        
        // Update filter labels
        roleFilterComboBox.getItems().clear();
        roleFilterComboBox.getItems().addAll(
                LangManager.getBundle().getString("admin.user.management.filter.all"),
                LangManager.getBundle().getString("admin.user.management.filter.admin"),
                LangManager.getBundle().getString("admin.user.management.filter.student")
        );
        roleFilterComboBox.getSelectionModel().selectFirst();
        
        // Update placeholder
        usersTable.setPlaceholder(new Label(LangManager.getBundle().getString("admin.user.management.no.users")));
        
        // Refresh the table to update cell texts
        usersTable.refresh();
        
        // Update status label
        updateStatusLabel();
    }
    
    @Override
    public String getFxmlPath() {
        return "/fxml/user_management_screen.fxml";
    }
}
