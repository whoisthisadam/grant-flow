package com.kasperovich.ui;

import com.kasperovich.clientconnection.ClientConnection;
import com.kasperovich.config.AlertManager;
import com.kasperovich.dto.scholarship.ScholarshipApplicationDTO;
import com.kasperovich.i18n.LangManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import lombok.Setter;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the application details dialog.
 */
public class ApplicationDetailsDialogController {

    @FXML
    private Label titleLabel;
    
    @FXML
    private Label idLabel;
    
    @FXML
    private Label applicantLabel;
    
    @FXML
    private Label programLabel;
    
    @FXML
    private Label periodLabel;
    
    @FXML
    private Label submissionDateLabel;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private Label reviewerLabel;
    
    @FXML
    private Label decisionDateLabel;
    
    @FXML
    private Label additionalInfoLabel;
    
    @FXML
    private Label decisionCommentsLabel;
    
    @FXML
    private TextField idField;
    
    @FXML
    private TextField applicantField;
    
    @FXML
    private TextField programField;
    
    @FXML
    private TextField periodField;
    
    @FXML
    private TextField submissionDateField;
    
    @FXML
    private TextField statusField;
    
    @FXML
    private TextField reviewerField;
    
    @FXML
    private TextField decisionDateField;
    
    @FXML
    private TextArea additionalInfoArea;
    
    @FXML
    private TextArea decisionCommentsArea;
    
    @FXML
    private Button approveButton;
    
    @FXML
    private Button rejectButton;
    
    @FXML
    private Button closeButton;
    
    @Setter
    private Stage dialogStage;
    
    @Setter
    private ScholarshipApplicationDTO application;
    
    @Setter
    private ClientConnection clientConnection;
    
    @Setter
    private boolean readOnly = false;
    
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    /**
     * Initializes the controller.
     */
    public void initializeData() {
        updateTexts();
        populateFields();
        
        // Show/hide approve/reject buttons based on application status and read-only mode
        boolean isPending = application != null && "PENDING".equals(application.getStatus());
        approveButton.setVisible(!readOnly && isPending);
        rejectButton.setVisible(!readOnly && isPending);
    }
    
    /**
     * Populates the fields with application data.
     */
    private void populateFields() {
        if (application == null) {
            return;
        }
        
        idField.setText(application.getId().toString());
        applicantField.setText(application.getApplicantFullName());
        programField.setText(application.getProgramName());
        periodField.setText(application.getPeriodName());
        
        if (application.getSubmissionDate() != null) {
            submissionDateField.setText(application.getSubmissionDate().format(dateFormatter));
        }
        
        statusField.setText(application.getStatus());
        reviewerField.setText(application.getReviewerUsername() != null ? application.getReviewerUsername() : "");
        
        if (application.getDecisionDate() != null) {
            decisionDateField.setText(application.getDecisionDate().format(dateFormatter));
        }
        
        additionalInfoArea.setText("TEST");
        decisionCommentsArea.setText(application.getDecisionComments());
    }
    
    /**
     * Handles the approve button action.
     *
     * @param event the action event
     */
    @FXML
    private void handleApprove(ActionEvent event) {
        // Show dialog to enter comments
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(LangManager.getBundle().getString("application.approve.title"));
        dialog.setHeaderText(LangManager.getBundle().getString("application.approve.header"));
        dialog.setContentText(LangManager.getBundle().getString("application.approve.comments"));
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String comments = result.get();
            
            try {
                clientConnection.approveApplication(application.getId(), comments);
                AlertManager.showInformationAlert(
                        LangManager.getBundle().getString("success"),
                        LangManager.getBundle().getString("application.approve.success"));
                
                // Close the dialog
                dialogStage.close();
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
        // Show dialog to enter comments
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(LangManager.getBundle().getString("application.reject.title"));
        dialog.setHeaderText(LangManager.getBundle().getString("application.reject.header"));
        dialog.setContentText(LangManager.getBundle().getString("application.reject.comments"));
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String comments = result.get();
            
            try {
                clientConnection.rejectApplication(application.getId(), comments);
                AlertManager.showInformationAlert(
                        LangManager.getBundle().getString("success"),
                        LangManager.getBundle().getString("application.reject.success"));
                
                // Close the dialog
                dialogStage.close();
            } catch (Exception e) {
                AlertManager.showErrorAlert(LangManager.getBundle().getString("error"), e.getMessage());
            }
        }
    }
    
    /**
     * Handles the close button action.
     *
     * @param event the action event
     */
    @FXML
    private void handleClose(ActionEvent event) {
        dialogStage.close();
    }
    
    /**
     * Updates the UI texts with the current language.
     */
    public void updateTexts() {
        ResourceBundle bundle = LangManager.getBundle();
        
        titleLabel.setText(bundle.getString("application.details.title"));
        idLabel.setText(bundle.getString("application.id") + ":");
        applicantLabel.setText(bundle.getString("application.applicant") + ":");
        programLabel.setText(bundle.getString("application.program") + ":");
        periodLabel.setText(bundle.getString("application.period") + ":");
        submissionDateLabel.setText(bundle.getString("application.submission.date") + ":");
        statusLabel.setText(bundle.getString("application.status") + ":");
        reviewerLabel.setText(bundle.getString("application.reviewer") + ":");
        decisionDateLabel.setText(bundle.getString("application.decision.date") + ":");
        additionalInfoLabel.setText(bundle.getString("application.additional.info") + ":");
        decisionCommentsLabel.setText(bundle.getString("application.decision.comments") + ":");
        
        approveButton.setText(bundle.getString("application.approve"));
        rejectButton.setText(bundle.getString("application.reject"));
        closeButton.setText(bundle.getString("close"));
    }
}
