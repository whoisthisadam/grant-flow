package com.kasperovich.ui;

import com.kasperovich.dto.auth.UserDTO;
import com.kasperovich.dto.report.AcademicPerformanceReportDTO;
import com.kasperovich.dto.report.CourseGradeDTO;
import com.kasperovich.dto.report.PaymentDTO;
import com.kasperovich.dto.scholarship.ScholarshipApplicationDTO;
import com.kasperovich.i18n.LangManager;
import com.kasperovich.operations.ChangeScene;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Controller for the Academic Performance Report screen.
 */
public class AcademicPerformanceReportController extends BaseController {

    @FXML private Label studentNameLabel;
    @FXML private Label studentIdLabel;
    @FXML private Label majorLabel;
    @FXML private Label departmentLabel;
    @FXML private Label academicYearLabel;
    @FXML private Label enrollmentDateLabel;
    @FXML private Label graduationDateLabel;
    @FXML private Label currentGpaLabel;
    
    @FXML private TableView<CourseGradeDTO> coursesTableView;
    @FXML private TableColumn<CourseGradeDTO, String> courseCodeColumn;
    @FXML private TableColumn<CourseGradeDTO, String> courseNameColumn;
    @FXML private TableColumn<CourseGradeDTO, Integer> courseCreditsColumn;
    @FXML private TableColumn<CourseGradeDTO, Double> courseGradeColumn;
    @FXML private TableColumn<CourseGradeDTO, String> courseLetterColumn;
    @FXML private TableColumn<CourseGradeDTO, String> coursePeriodColumn;
    @FXML private TableColumn<CourseGradeDTO, String> courseCompletionColumn;
    
    @FXML private TableView<ScholarshipApplicationDTO> scholarshipsTableView;
    @FXML private TableColumn<ScholarshipApplicationDTO, String> scholarshipProgramColumn;
    @FXML private TableColumn<ScholarshipApplicationDTO, String> scholarshipPeriodColumn;
    @FXML private TableColumn<ScholarshipApplicationDTO, String> scholarshipSubmissionColumn;
    @FXML private TableColumn<ScholarshipApplicationDTO, String> scholarshipStatusColumn;
    @FXML private TableColumn<ScholarshipApplicationDTO, String> scholarshipDecisionColumn;
    
    @FXML private TableView<PaymentDTO> paymentsTableView;
    @FXML private TableColumn<PaymentDTO, String> paymentProgramColumn;
    @FXML private TableColumn<PaymentDTO, String> paymentAmountColumn;
    @FXML private TableColumn<PaymentDTO, String> paymentDateColumn;
    @FXML private TableColumn<PaymentDTO, String> paymentStatusColumn;
    @FXML private TableColumn<PaymentDTO, String> paymentReferenceColumn;
    
    @FXML private Label totalCreditsCompletedLabel;
    @FXML private Label totalCreditsInProgressLabel;
    @FXML private Label averageGpaLabel;
    @FXML private Label scholarshipsAppliedLabel;
    @FXML private Label scholarshipsApprovedLabel;
    @FXML private Label totalScholarshipAmountLabel;
    
    @FXML private Button backButton;
    @FXML private Button printButton;
    
    private AcademicPerformanceReportDTO report;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance();
    
    @Override
    public void initializeData() {
        setupTableColumns();
        loadReport();
    }
    
    /**
     * Sets up the table columns with cell value factories.
     */
    private void setupTableColumns() {
        // Course table columns
        courseCodeColumn.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        courseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        courseCreditsColumn.setCellValueFactory(new PropertyValueFactory<>("credits"));
        courseGradeColumn.setCellValueFactory(new PropertyValueFactory<>("gradeValue"));
        courseLetterColumn.setCellValueFactory(new PropertyValueFactory<>("gradeLetter"));
        coursePeriodColumn.setCellValueFactory(new PropertyValueFactory<>("academicPeriod"));
        courseCompletionColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCompletionDate() != null ? 
                cellData.getValue().getCompletionDate().format(dateFormatter) : ""));
        
        // Scholarship table columns
        scholarshipProgramColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getProgramName()));
        scholarshipPeriodColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getPeriodName()));
        scholarshipSubmissionColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getSubmissionDate() != null ? 
                cellData.getValue().getSubmissionDate().format(dateTimeFormatter) : ""));
        scholarshipStatusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStatus()));
        scholarshipDecisionColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDecisionDate() != null ? 
                cellData.getValue().getDecisionDate().format(dateTimeFormatter) : ""));
        
        // Payment table columns
        paymentProgramColumn.setCellValueFactory(new PropertyValueFactory<>("programName"));
        paymentAmountColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(currencyFormatter.format(cellData.getValue().getAmount())));
        paymentDateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getPaymentDate() != null ? 
                cellData.getValue().getPaymentDate().format(dateTimeFormatter) : ""));
        paymentStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        paymentReferenceColumn.setCellValueFactory(new PropertyValueFactory<>("referenceNumber"));
    }
    
    /**
     * Loads the academic performance report from the server.
     */
    private void loadReport() {
        try {
            report = clientConnection.getAcademicPerformanceReport();
            if (report != null) {
                populateStudentInfo();
                populateCourseTable();
                populateScholarshipTable();
                populatePaymentTable();
                populateSummaryInfo();
            } else {
                showAlert(Alert.AlertType.ERROR, 
                    LangManager.getBundle().getString("error.title"),
                    LangManager.getBundle().getString("error.report.load"));
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, 
                LangManager.getBundle().getString("error.title"), 
                e.getMessage());
        }
    }
    
    /**
     * Populates the student information section.
     */
    private void populateStudentInfo() {
        UserDTO user = report.getUser();
        studentNameLabel.setText(user.getFirstName() + " " + user.getLastName());
        studentIdLabel.setText(report.getStudentId());
        majorLabel.setText(report.getMajor());
        departmentLabel.setText(report.getDepartment());
        academicYearLabel.setText(String.valueOf(report.getAcademicYear()));
        enrollmentDateLabel.setText(report.getEnrollmentDate().format(dateFormatter));
        graduationDateLabel.setText(report.getExpectedGraduationDate().format(dateFormatter));
        currentGpaLabel.setText(String.format("%.2f", report.getCurrentGpa()));
    }
    
    /**
     * Populates the course table.
     */
    private void populateCourseTable() {
        coursesTableView.setItems(FXCollections.observableArrayList(report.getCourseGrades()));
    }
    
    /**
     * Populates the scholarship table.
     */
    private void populateScholarshipTable() {
        scholarshipsTableView.setItems(FXCollections.observableArrayList(report.getScholarshipApplications()));
    }
    
    /**
     * Populates the payment table.
     */
    private void populatePaymentTable() {
        paymentsTableView.setItems(FXCollections.observableArrayList(report.getPayments()));
    }
    
    /**
     * Populates the summary information section.
     */
    private void populateSummaryInfo() {
        totalCreditsCompletedLabel.setText(String.valueOf(report.getTotalCreditsCompleted()));
        totalCreditsInProgressLabel.setText(String.valueOf(report.getTotalCreditsInProgress()));
        averageGpaLabel.setText(String.format("%.2f", report.getAverageGpa()));
        scholarshipsAppliedLabel.setText(String.valueOf(report.getScholarshipsApplied()));
        scholarshipsApprovedLabel.setText(String.valueOf(report.getScholarshipsApproved()));
        totalScholarshipAmountLabel.setText(currencyFormatter.format(report.getTotalScholarshipAmount()));
    }
    
    /**
     * Handles the back button action.
     */
    @FXML
    private void handleBackButtonAction(ActionEvent event) {
        ChangeScene.changeScene(event,
            "/fxml/dashboard_screen.fxml",
            LangManager.getBundle().getString("dashboard.title"),
            getClientConnection(),
            user);
    }
    
    /**
     * Handles the print button action.
     */
    @FXML
    private void handlePrintButtonAction(ActionEvent event) {
        // For now, just show a message that printing is not implemented
        showAlert(Alert.AlertType.INFORMATION, 
            LangManager.getBundle().getString("info.title"), 
            "Printing functionality will be implemented in a future version.");
    }
    
    /**
     * Shows an alert dialog.
     *
     * @param type the alert type
     * @param title the alert title
     * @param message the alert message
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public String getFxmlPath() {
        return "/fxml/academic_performance_report_screen.fxml";
    }

    @Override
    public void updateTexts() {
        // Update button texts
        backButton.setText(LangManager.getBundle().getString("button.back"));
        printButton.setText(LangManager.getBundle().getString("button.print"));
        
        // Update tab texts if needed
        // Note: Tab texts are already handled by the FXML using resource bundle references
    }
}
