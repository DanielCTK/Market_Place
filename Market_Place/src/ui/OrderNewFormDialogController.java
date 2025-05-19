package ui;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;
import dao.OrderNewDAO; // Sử dụng DAO mới
import impl.OrderNewDAOImpl; // Sử dụng Impl mới
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import model.Order; // Sử dụng model Order đã được làm mới
import model.OrderItem;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class OrderNewFormDialogController { // Tên controller mới

    @FXML private Label dialogTitleLabel;
    @FXML private JFXTextField orderIdNewField; // fx:id mới
    @FXML private JFXDatePicker timestampNewPicker; // fx:id mới
    @FXML private JFXTextField customerContactNewField; // fx:id mới

    private Stage currentDialogStage; // Stage của chính dialog này
    private Stage parentOwnerStage;  // Stage của cửa sổ cha
    private Order orderBeingEdited; // Sử dụng model Order mới
    private boolean okButtonWasClicked = false;
    private OrderNewDAO orderDataAccessObject; // Sử dụng DAO mới

    public OrderNewFormDialogController() {
        orderDataAccessObject = new OrderNewDAOImpl(); // Khởi tạo DAO mới
    }

    public void setParentOwnerStage(Stage ownerStage) {
        this.parentOwnerStage = ownerStage;
    }
    
    public void setCurrentDialogShowingStage(Stage dialogStage) {
        this.currentDialogStage = dialogStage;
    }

    public void setOrderForEditing(Order order) { // Sử dụng model Order mới
        this.orderBeingEdited = order;
        if (order != null) {
            dialogTitleLabel.setText("Edit Order Information");
            // Sử dụng getter mới từ model.Order
            orderIdNewField.setText(order.getOrderIdValue());
            orderIdNewField.setEditable(false); 
            
            if (order.getOrderTimestampValue() != null) {
                timestampNewPicker.setValue(order.getOrderTimestampValue().toLocalDateTime().toLocalDate());
            } else {
                timestampNewPicker.setValue(LocalDate.now());
            }
            customerContactNewField.setText(order.getCustomerContactValue());
        } else {
            dialogTitleLabel.setText("Create New Order");
            orderIdNewField.clear();
            orderIdNewField.setEditable(true);
            timestampNewPicker.setValue(LocalDate.now()); 
            customerContactNewField.clear();
        }
    }

    public boolean wasOkButtonClicked() {
        return okButtonWasClicked;
    }

    public Order getNewlyCreatedOrEditedOrder() { 
        return orderBeingEdited;
    }

    @FXML
    private void handleSaveNewOrder(ActionEvent event) { // Tên phương thức mới
        if (isUserInputValid()) {
            boolean isNewEntry = (orderBeingEdited == null);
            if (isNewEntry) {
                orderBeingEdited = new Order(); // Tạo đối tượng Order mới
            }

            // Sử dụng getter/setter mới
            orderBeingEdited.setOrderIdValue(orderIdNewField.getText().trim());
            
            LocalDate localDate = timestampNewPicker.getValue();
            if (localDate != null) {
                LocalDateTime localDateTime = LocalDateTime.of(localDate, LocalTime.now());
                orderBeingEdited.setOrderTimestampValue(Timestamp.valueOf(localDateTime));
            } else {
                 orderBeingEdited.setOrderTimestampValue(new Timestamp(System.currentTimeMillis()));
            }
            
            orderBeingEdited.setCustomerContactValue(customerContactNewField.getText().trim());
            
            boolean success = false;
            try {
                if (isNewEntry) {
                    if (orderDataAccessObject.checkOrderIdExists(orderBeingEdited.getOrderIdValue())) { // Sử dụng phương thức DAO mới
                         showAlertDialog(Alert.AlertType.ERROR, "Input Error", "Order ID '" + orderBeingEdited.getOrderIdValue() + "' already exists!");
                         return; 
                    }
                    success = orderDataAccessObject.addNewOrder(orderBeingEdited); // Sử dụng phương thức DAO mới
                } else {
                    success = orderDataAccessObject.updateExistingOrderInfo(orderBeingEdited); // Sử dụng phương thức DAO mới
                }

                if (success) {
                    okButtonWasClicked = true;
                    if (currentDialogStage != null) currentDialogStage.close();
                } else {
                    showAlertDialog(Alert.AlertType.ERROR, "Database Error",
                        (isNewEntry ? "Failed to create new order." : "Failed to update order information."));
                }
            } catch (SQLException e) { // Bắt SQLException cụ thể
                e.printStackTrace();
                showAlertDialog(Alert.AlertType.ERROR, "Database Operation Failed",
                        "An error occurred: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleCancelNewDialog(ActionEvent event) { // Tên phương thức mới
        if (currentDialogStage != null) currentDialogStage.close();
    }

    private boolean isUserInputValid() {
        String errorMessage = "";
        String oId = orderIdNewField.getText();

        if (oId == null || oId.trim().isEmpty()) {
            errorMessage += "Order ID is required!\n";
        } else if (oId.trim().length() > 11) {
             errorMessage += "Order ID cannot be longer than 11 characters.\n";
        }
        
        if (timestampNewPicker.getValue() == null) {
            // Timestamp có thể được tự động gán nếu không bắt buộc, hoặc bạn có thể thêm lỗi ở đây
            // errorMessage += "Timestamp is required!\n"; 
        }

        String contact = customerContactNewField.getText();
        if (contact == null || contact.trim().isEmpty()) {
            errorMessage += "Customer Contact is required!\n";
        } else if (contact.trim().length() > 11) {
             errorMessage += "Customer Contact cannot be longer than 11 characters.\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            showAlertDialog(Alert.AlertType.ERROR, "Invalid Fields", errorMessage);
            return false;
        }
    }

    private void showAlertDialog(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        if (currentDialogStage != null && currentDialogStage.isShowing()) {
             alert.initOwner(currentDialogStage);
        } else if (parentOwnerStage != null && parentOwnerStage.isShowing()) {
            alert.initOwner(parentOwnerStage);
        }
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}