	package ui;
	
	import com.jfoenix.controls.JFXTextField;
	import dao.OrderNewDAO; // Sử dụng DAO mới
	import impl.OrderNewDAOImpl; // Sử dụng Impl mới
	import javafx.event.ActionEvent;
	import javafx.fxml.FXML;
	import javafx.scene.control.Alert;
	import javafx.scene.control.Label;
	import javafx.stage.Stage;
	import model.OrderItem; // Sử dụng model OrderItem mới
	import javafx.scene.control.TextField;
	import java.sql.SQLException;
	
	public class OrderItemFormDialogController { // Tên controller mới
	
	    @FXML private Label dialogTitleLabel; // fx:id từ OrderItemFormDialog.fxml
	    @FXML private TextField itemProductIdField;   // fx:id từ OrderItemFormDialog.fxml
	    @FXML private JFXTextField itemSellerIdField;  // fx:id từ OrderItemFormDialog.fxml
	    @FXML private JFXTextField itemPriceField;     // fx:id từ OrderItemFormDialog.fxml
	
	    private Stage currentDialogStage;
	    private Stage parentOwnerStage;
	    private OrderItem orderItemBeingEdited; // Item đang được sửa (có thể null nếu thêm mới)
	    private String parentOrderId; // ID của Order chứa item này
	    private boolean okButtonWasClicked = false;
	    private OrderNewDAO orderDataAccessObject;
	
	    public OrderItemFormDialogController() {
	        orderDataAccessObject = new OrderNewDAOImpl();
	    }
	
	    public void setParentOwnerStage(Stage ownerStage) {
	        this.parentOwnerStage = ownerStage;
	    }
	
	    public void setCurrentDialogShowingStage(Stage dialogStage) {
	        this.currentDialogStage = dialogStage;
	    }
	
	    /**
	     * Được gọi bởi OrderManagementController để thiết lập item cần sửa
	     * hoặc để chuẩn bị cho việc thêm item mới.
	     * @param item Item để sửa (null nếu là thêm mới).
	     * @param orderId ID của Order cha.
	     */
	    public void setOrderItemForEditing(OrderItem item, String orderId) {
	        this.parentOrderId = orderId; // Luôn cần orderId của order cha
	        this.orderItemBeingEdited = item;
	
	        if (item != null) { // Chế độ sửa
	            if(dialogTitleLabel != null) dialogTitleLabel.setText("Edit Item in Order: " + orderId);
	            // Sử dụng getter mới từ model.OrderItem
	            itemProductIdField.setText(item.getItemProductId());
	            itemSellerIdField.setText(item.getItemSellerId());
	            if (item.getItemPriceValue() != null) {
	                itemPriceField.setText(String.valueOf(item.getItemPriceValue()));
	            } else {
	                itemPriceField.clear();
	            }
	        } else { // Chế độ thêm mới
	            if(dialogTitleLabel != null) dialogTitleLabel.setText("Add New Item to Order: " + orderId);
	            itemProductIdField.clear();
	            itemSellerIdField.clear();
	            itemPriceField.clear();
	        }
	    }
	
	    public boolean wasOkButtonClicked() {
	        return okButtonWasClicked;
	    }
	
	    @FXML
	    private void handleSaveItem(ActionEvent event) { // Đổi tên onAction trong FXML nếu cần
	        if (isUserInputValid()) {
	            boolean isNewItem = (orderItemBeingEdited == null);
	            
	            if (isNewItem) {
	                // Tạo OrderItem mới, gán parentOrderId cho nó
	                orderItemBeingEdited = new OrderItem();
	                orderItemBeingEdited.setReferencedOrderId(this.parentOrderId); // Quan trọng
	            }
	
	            // Cập nhật thông tin từ các trường vào đối tượng orderItemBeingEdited
	            // Sử dụng setter mới
	            orderItemBeingEdited.setItemProductId(itemProductIdField.getText().trim());
	            orderItemBeingEdited.setItemSellerId(itemSellerIdField.getText().trim());
	
	            String priceStr = itemPriceField.getText().trim();
	            if (!priceStr.isEmpty()) {
	                try {
	                    orderItemBeingEdited.setItemPriceValue(Float.parseFloat(priceStr));
	                } catch (NumberFormatException e) {
	                    // isUserInputValid() đã kiểm tra, nhưng để an toàn
	                    showAlertDialog(Alert.AlertType.ERROR, "Invalid Input", "Price must be a valid number.");
	                    return;
	                }
	            } else {
	                orderItemBeingEdited.setItemPriceValue(null); // Hoặc giá trị mặc định nếu cần
	            }
	            
	            boolean success = false;
	            try {
	                if (isNewItem) {
	                    success = orderDataAccessObject.addNewOrderItem(orderItemBeingEdited); // Sử dụng phương thức DAO mới
	                } else {
	                    success = orderDataAccessObject.updateExistingOrderItem(orderItemBeingEdited); // Sử dụng phương thức DAO mới
	                }
	
	                if (success) {
	                    okButtonWasClicked = true;
	                    if (currentDialogStage != null) currentDialogStage.close();
	                } else {
	                    showAlertDialog(Alert.AlertType.ERROR, "Database Error",
	                        (isNewItem ? "Failed to add item." : "Failed to update item."));
	                }
	            } catch (SQLException e) {
	                e.printStackTrace();
	                showAlertDialog(Alert.AlertType.ERROR, "Database Operation Failed",
	                        "An error occurred: " + e.getMessage());
	            }
	        }
	    }
	
	    @FXML
	    private void handleCancelItemDialog(ActionEvent event) { // Đổi tên onAction trong FXML nếu cần
	        if (currentDialogStage != null) currentDialogStage.close();
	    }
	
	    private boolean isUserInputValid() {
	        String errorMessage = "";
	
	        if (itemProductIdField.getText() == null || itemProductIdField.getText().trim().isEmpty()) {
	            errorMessage += "Product ID is required!\n";
	        }
	        if (itemSellerIdField.getText() == null || itemSellerIdField.getText().trim().isEmpty()) {
	            errorMessage += "Seller ID is required!\n";
	        }
	        
	        String priceStr = itemPriceField.getText().trim();
	        if (priceStr.isEmpty()) {
	             errorMessage += "Price is required!\n"; // Giả sử giá là bắt buộc
	        } else {
	            try {
	                float price = Float.parseFloat(priceStr);
	                if (price < 0) {
	                    errorMessage += "Price cannot be negative!\n";
	                }
	            } catch (NumberFormatException e) {
	                errorMessage += "Price must be a valid number (e.g., 123.45)!\n";
	            }
	        }
	        // Bạn có thể thêm kiểm tra product ID, seller ID có tồn tại trong DB không nếu cần
	
	        if (errorMessage.isEmpty()) {
	            return true;
	        } else {
	            showAlertDialog(Alert.AlertType.ERROR, "Invalid Item Fields", errorMessage);
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