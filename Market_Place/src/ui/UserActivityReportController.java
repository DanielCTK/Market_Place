package ui;

// Import JFoenix components
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXSpinner;

import Services.DatabaseManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*; // Giữ lại các import JavaFX chuẩn khác
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserActivityReportController implements Initializable {

    // Sử dụng kiểu JFoenix
    @FXML private JFXDatePicker startDatePicker;
    @FXML private JFXDatePicker endDatePicker;
    @FXML private JFXButton generateReportButton;
    @FXML private JFXSpinner progressIndicator; // JFXSpinner thay cho ProgressIndicator

    // Labels vẫn là JavaFX chuẩn
    @FXML private Label totalLoginsLabel;
    @FXML private Label newRegistrationsLabel;
    @FXML private Label uniqueActiveUsersLabel;

    // TableView và TableColumn vẫn là JavaFX chuẩn
    @FXML private TableView<ActivityLogEntry> activityTableView;
    @FXML private TableColumn<ActivityLogEntry, String> timestampColumn;
    @FXML private TableColumn<ActivityLogEntry, String> userIdColumn;
    @FXML private TableColumn<ActivityLogEntry, String> usernameColumn;
    @FXML private TableColumn<ActivityLogEntry, String> actionColumn;
    @FXML private TableColumn<ActivityLogEntry, String> detailsColumn;

    private ObservableList<ActivityLogEntry> activityData = FXCollections.observableArrayList();
    private ExecutorService executorService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        executorService = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r); t.setDaemon(true); return t;
        });

        // Các thiết lập cho TableColumn vẫn giữ nguyên
        timestampColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedTimestamp()));
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
        detailsColumn.setCellValueFactory(new PropertyValueFactory<>("details"));

        if (activityTableView != null) {
            activityTableView.setItems(activityData);
            // Đặt columnResizePolicy trong code Java để đảm bảo (nếu FXML có vấn đề)
            activityTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        } else {
            System.err.println("UserActivityReportController: activityTableView is null.");
        }


        if (startDatePicker != null) startDatePicker.setValue(LocalDate.now().minusDays(7));
        else System.err.println("UserActivityReportController: startDatePicker is null.");

        if (endDatePicker != null) endDatePicker.setValue(LocalDate.now());
        else System.err.println("UserActivityReportController: endDatePicker is null.");


        // Do không có dữ liệu thực cho các mục này từ schema hiện tại
        if (totalLoginsLabel != null) totalLoginsLabel.setText("N/A");
        else System.err.println("UserActivityReportController: totalLoginsLabel is null.");

        if (newRegistrationsLabel != null) newRegistrationsLabel.setText("N/A");
        else System.err.println("UserActivityReportController: newRegistrationsLabel is null.");
    }

    @FXML
    void handleGenerateReport(ActionEvent event) {
        if (startDatePicker == null || endDatePicker == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Date pickers are not available.");
            return;
        }
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            showAlert(Alert.AlertType.ERROR, "Invalid Date Range", "Please select valid start and end dates.");
            return;
        }

        if (progressIndicator != null) progressIndicator.setVisible(true);
        else System.err.println("UserActivityReportController: progressIndicator is null.");

        if (generateReportButton != null) generateReportButton.setDisable(true);
        else System.err.println("UserActivityReportController: generateReportButton is null.");

        if (activityData != null) activityData.clear();
        if (uniqueActiveUsersLabel != null) uniqueActiveUsersLabel.setText("0");


        Task<ActivityReportResult> reportTask = new Task<>() {
            @Override
            protected ActivityReportResult call() throws Exception {
                List<ActivityLogEntry> logs = new ArrayList<>();
                Set<String> activeUserIds = new HashSet<>();

                LocalDateTime startDateTime = startDate.atStartOfDay();
                LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

                String activityQuery = "SELECT o.timestamp, o.customer_contact, COALESCE(u.username, 'Unknown') AS username, o.order_id " +
                                       "FROM orders o " +
                                       "LEFT JOIN users u ON o.customer_contact = u.id " +
                                       "WHERE o.timestamp >= ? AND o.timestamp <= ? " +
                                       "ORDER BY o.timestamp DESC";
                Connection conn = null;
                PreparedStatement pstmt = null;
                ResultSet rs = null;

                try {
                    conn = DatabaseManager.getConnection();
                    pstmt = conn.prepareStatement(activityQuery);
                    pstmt.setTimestamp(1, Timestamp.valueOf(startDateTime));
                    pstmt.setTimestamp(2, Timestamp.valueOf(endDateTime));
                    rs = pstmt.executeQuery();

                    while (rs.next()) {
                        LocalDateTime timestamp = rs.getTimestamp("timestamp").toLocalDateTime();
                        String userId = rs.getString("customer_contact");
                        String username = rs.getString("username");
                        String orderId = rs.getString("order_id");

                        logs.add(new ActivityLogEntry(timestamp, userId, username, "Placed Order", "Order ID: " + orderId));
                        if (userId != null) {
                            activeUserIds.add(userId);
                        }
                    }
                } catch (SQLException e) {
                     e.printStackTrace();
                     throw new RuntimeException("Database error fetching user activity: " + e.getMessage(), e);
                } finally {
                    DatabaseManager.close(conn, pstmt, rs);
                }
                return new ActivityReportResult(logs, 0, 0, activeUserIds.size());
            }
        };

        reportTask.setOnSucceeded(e -> {
            ActivityReportResult result = reportTask.getValue();
            if (activityData != null) activityData.setAll(result.logs());
            if (uniqueActiveUsersLabel != null) uniqueActiveUsersLabel.setText(String.valueOf(result.uniqueActiveUsers()));

            if (progressIndicator != null) progressIndicator.setVisible(false);
            if (generateReportButton != null) generateReportButton.setDisable(false);
        });

        reportTask.setOnFailed(e -> {
            if (progressIndicator != null) progressIndicator.setVisible(false);
            if (generateReportButton != null) generateReportButton.setDisable(false);
            Throwable ex = reportTask.getException();
            if (ex != null) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Report Generation Failed", "An error occurred: " + ex.getMessage());
            } else {
                showAlert(Alert.AlertType.ERROR, "Report Generation Failed", "An unknown error occurred.");
            }
        });

        if (executorService != null) executorService.submit(reportTask);
        else System.err.println("UserActivityReportController: executorService is null.");
    }

    public void shutdownExecutor() {
        System.out.println("UserActivityReportController: Shutting down executor service.");
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
         if (generateReportButton != null && generateReportButton.getScene() != null && generateReportButton.getScene().getWindow() != null) {
             alert.initOwner(generateReportButton.getScene().getWindow());
        }
        alert.showAndWait();
    }

    private record ActivityReportResult(List<ActivityLogEntry> logs, int totalLogins, int newRegistrations, int uniqueActiveUsers) {}

    public static class ActivityLogEntry {
        private final SimpleObjectProperty<LocalDateTime> timestamp;
        private final SimpleStringProperty userId;
        private final SimpleStringProperty username;
        private final SimpleStringProperty action;
        private final SimpleStringProperty details;
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        public ActivityLogEntry(LocalDateTime timestamp, String userId, String username, String action, String details) {
            this.timestamp = new SimpleObjectProperty<>(timestamp);
            this.userId = new SimpleStringProperty(userId != null ? userId : "N/A");
            this.username = new SimpleStringProperty(username != null ? username : "N/A");
            this.action = new SimpleStringProperty(action);
            this.details = new SimpleStringProperty(details);
        }
        public LocalDateTime getTimestamp() { return timestamp.get(); }
        public String getFormattedTimestamp() { return (timestamp.get() != null) ? timestamp.get().format(FORMATTER) : ""; }
        public String getUserId() { return userId.get(); }
        public String getUsername() { return username.get(); }
        public String getAction() { return action.get(); }
        public String getDetails() { return details.get(); }

        // Cần các property getter để PropertyValueFactory hoạt động
        public SimpleObjectProperty<LocalDateTime> timestampProperty() { return timestamp; }
        public SimpleStringProperty userIdProperty() { return userId; }
        public SimpleStringProperty usernameProperty() { return username; }
        public SimpleStringProperty actionProperty() { return action; }
        public SimpleStringProperty detailsProperty() { return details; }
    }
}