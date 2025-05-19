package ui;

import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import dao.OrderNewDAO; // Sử dụng DAO mới
import impl.OrderNewDAOImpl; // Sử dụng Impl mới
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
// import javafx.scene.control.TitledPane; // No longer needed for this specific FXML ID
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text; // Cho totalOrdersCountText
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Order; // Model mới
import model.OrderItem; // Model mới

import ui.OrderNewFormDialogController; 
// Sử dụng controller mới
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class OrderManagementController implements Initializable {

    //<editor-fold desc="FXML Declarations for Orders Table">
    @FXML private JFXTreeTableView<OrderWrapper> ordersTable;
    @FXML private JFXTreeTableColumn<OrderWrapper, String> orderIdColumn;
    @FXML private JFXTreeTableColumn<OrderWrapper, String> timestampColumn; // Hiển thị dạng String đã format
    @FXML private JFXTreeTableColumn<OrderWrapper, String> customerContactColumn;
    //</editor-fold>

    //<editor-fold desc="FXML Declarations for Order Items Table">
    @FXML private JFXTreeTableView<OrderItemWrapper> orderItemsTable;
    @FXML private JFXTreeTableColumn<OrderItemWrapper, Integer> itemPkColumn; // Có thể ẩn
    @FXML private JFXTreeTableColumn<OrderItemWrapper, String> itemProductIdColumn;
    @FXML private JFXTreeTableColumn<OrderItemWrapper, String> itemSellerIdColumn;
    @FXML private JFXTreeTableColumn<OrderItemWrapper, Number> itemPriceColumn;
    // @FXML private TitledPane orderItemsTitledPane; // OLD: Remove this
    @FXML private Label orderItemsHeaderLabel; // NEW: Add this
    //</editor-fold>

    //<editor-fold desc="FXML Declarations for Search and Actions">
    @FXML private TextField searchOrderField;
    @FXML private JFXButton searchOrderButton;
    @FXML private JFXButton addNewOrderButton;
    @FXML private JFXButton editOrderInfoButton;
    @FXML private JFXButton deleteOrderButton;
    @FXML private JFXButton addNewItemButton;
    @FXML private JFXButton editSelectedItemButton;
    @FXML private JFXButton deleteSelectedItemButton;
    //</editor-fold>

    //<editor-fold desc="FXML Declarations for Pagination">
    @FXML private HBox orderPaginationControls;
    @FXML private JFXButton firstOrderPageButton;
    @FXML private JFXButton prevOrderPageButton;
    @FXML private Label orderPageInfoLabel;
    @FXML private JFXButton nextOrderPageButton;
    @FXML private JFXButton lastOrderPageButton;
    @FXML private Text totalOrdersCountText;
    //</editor-fold>

    private OrderNewDAO orderDAO;
    private ObservableList<OrderWrapper> orderWrapperList;
    private ObservableList<OrderItemWrapper> orderItemWrapperList;

    private final SimpleDateFormat timestampFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(); // Hoặc NumberFormat.getNumberInstance()

    //<editor-fold desc="Pagination State for Orders">
    private int currentOrderPage = 1;
    private final int orderPageSize = 10; // Số Orders mỗi trang
    private int totalOrderItemsCount = 0; // Tổng số Orders
    private int totalOrderPages = 1;
    private String currentOrderSearchKeyword = "";
    //</editor-fold>
    
    private Order selectedOrderForItems = null; // Order đang được chọn để hiển thị items

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        orderDAO = new OrderNewDAOImpl();
        orderWrapperList = FXCollections.observableArrayList();
        orderItemWrapperList = FXCollections.observableArrayList();

        setupOrdersTableColumns();
        setupOrderItemsTableColumns();
        
        setInitialItemsPaneState(true); // Ban đầu ẩn các nút quản lý item

        // Listener cho việc chọn Order trong bảng Orders
        ordersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null && newSelection.getValue() != null) {
                selectedOrderForItems = newSelection.getValue().getOriginalOrder();
                // orderItemsTitledPane.setText("Order Items for: " + selectedOrderForItems.getOrderIdValue()); // OLD
                if (orderItemsHeaderLabel != null) orderItemsHeaderLabel.setText("Order Items for: " + selectedOrderForItems.getOrderIdValue()); // NEW
                loadOrderItemsForSelectedOrder();
                setInitialItemsPaneState(false); // Hiện các nút quản lý item
            } else {
                selectedOrderForItems = null;
                // orderItemsTitledPane.setText("Order Items (No Order Selected)"); // OLD
                if (orderItemsHeaderLabel != null) orderItemsHeaderLabel.setText("Order Items (No Order Selected)"); // NEW
                orderItemWrapperList.clear(); // Xóa items khi không có order nào được chọn
                setInitialItemsPaneState(true); // Ẩn các nút quản lý item
            }
        });

        if (searchOrderField != null) {
            searchOrderField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    triggerOrderSearch();
                }
            });
        }
        if (searchOrderButton != null) {
             searchOrderButton.setOnAction(event -> triggerOrderSearch());
        }
        
        loadOrdersForCurrentPage();
    }

    private void setInitialItemsPaneState(boolean disabled) {
        if (addNewItemButton != null) addNewItemButton.setDisable(disabled);
        if (editSelectedItemButton != null) editSelectedItemButton.setDisable(disabled);
        if (deleteSelectedItemButton != null) deleteSelectedItemButton.setDisable(disabled);
        // if(disabled && orderItemsTitledPane != null) { // OLD
        //     orderItemsTitledPane.setText("Order Items (No Order Selected)"); // OLD
        // } // OLD
        if(disabled && orderItemsHeaderLabel != null) { // NEW
            orderItemsHeaderLabel.setText("Order Items (No Order Selected)"); // NEW
        } // NEW
    }

    //<editor-fold desc="Orders Table Setup and Load">
    private void setupOrdersTableColumns() {
        orderIdColumn.setCellValueFactory(param -> param.getValue().getValue().orderIdProperty());
        customerContactColumn.setCellValueFactory(param -> param.getValue().getValue().customerContactProperty());
        timestampColumn.setCellValueFactory(param -> {
            Timestamp ts = param.getValue().getValue().getOriginalOrder().getOrderTimestampValue();
            return new SimpleStringProperty(ts != null ? timestampFormatter.format(ts) : "N/A");
        });
    }

    private void triggerOrderSearch() {
        currentOrderSearchKeyword = searchOrderField.getText().trim();
        currentOrderPage = 1;
        loadOrdersForCurrentPage();
    }

    private void loadOrdersForCurrentPage() {
        try {
            totalOrderItemsCount = orderDAO.countTotalOrders(currentOrderSearchKeyword);
            totalOrderPages = (totalOrderItemsCount == 0) ? 1 : (int) Math.ceil((double) totalOrderItemsCount / orderPageSize);
            if (currentOrderPage > totalOrderPages && totalOrderPages > 0) currentOrderPage = totalOrderPages;
            if (currentOrderPage < 1) currentOrderPage = 1;

            int offset = (currentOrderPage - 1) * orderPageSize;
            List<Order> ordersFromDB = orderDAO.fetchPaginatedOrders(offset, orderPageSize, currentOrderSearchKeyword);
            
            orderWrapperList.setAll(ordersFromDB.stream().map(OrderWrapper::new).collect(Collectors.toList()));
            final TreeItem<OrderWrapper> root = new RecursiveTreeItem<>(orderWrapperList, RecursiveTreeObject::getChildren);
            ordersTable.setRoot(root);
            ordersTable.setShowRoot(false);

            updateOrderPaginationUI();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Load Orders Error", "Failed to load orders: " + e.getMessage());
        }
    }

    private void updateOrderPaginationUI() {
        if (orderPageInfoLabel != null) {
            orderPageInfoLabel.setText("Page " + currentOrderPage + " of " + totalOrderPages + " (Total: " + totalOrderItemsCount + ")");
        }
        if (totalOrdersCountText != null) {
            totalOrdersCountText.setText(String.valueOf(totalOrderItemsCount));
        }
        if (firstOrderPageButton != null) firstOrderPageButton.setDisable(currentOrderPage <= 1);
        if (prevOrderPageButton != null) prevOrderPageButton.setDisable(currentOrderPage <= 1);
        if (nextOrderPageButton != null) nextOrderPageButton.setDisable(currentOrderPage >= totalOrderPages);
        if (lastOrderPageButton != null) lastOrderPageButton.setDisable(currentOrderPage >= totalOrderPages);
        if (orderPaginationControls != null) {
            orderPaginationControls.setVisible(totalOrderPages > 1 || totalOrderItemsCount > orderPageSize);
        }
    }

    @FXML void handleFirstOrderPage(ActionEvent event) { if (currentOrderPage > 1) { currentOrderPage = 1; loadOrdersForCurrentPage(); }}
    @FXML void handlePreviousOrderPage(ActionEvent event) { if (currentOrderPage > 1) { currentOrderPage--; loadOrdersForCurrentPage(); }}
    @FXML void handleNextOrderPage(ActionEvent event) { if (currentOrderPage < totalOrderPages) { currentOrderPage++; loadOrdersForCurrentPage(); }}
    @FXML void handleLastOrderPage(ActionEvent event) { if (currentOrderPage < totalOrderPages) { currentOrderPage = totalOrderPages; loadOrdersForCurrentPage(); }}
    //</editor-fold>

    //<editor-fold desc="Order Actions (Add, Edit Info, Delete)">
    @FXML
    void handleAddNewOrder(ActionEvent event) {
        openOrderFormDialog("Create New Order", null);
    }

    @FXML
    void handleEditSelectedOrderInfo(ActionEvent event) {
        TreeItem<OrderWrapper> selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an order to edit its information.");
            return;
        }
        openOrderFormDialog("Edit Order Information", selected.getValue().getOriginalOrder());
    }
    
    private void openOrderFormDialog(String title, Order orderToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/OrderNewFormDialog.fxml")); // Sử dụng FXML mới
            Parent root = loader.load();
            OrderNewFormDialogController dialogController = loader.getController(); // Sử dụng Controller mới
            
            Stage currentStage = getStage();
            dialogController.setParentOwnerStage(currentStage); 
            dialogController.setOrderForEditing(orderToEdit); // Sử dụng phương thức mới

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.initModality(Modality.WINDOW_MODAL);
            if (currentStage != null) stage.initOwner(currentStage);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            dialogController.setCurrentDialogShowingStage(stage); // Sử dụng phương thức mới
            stage.showAndWait();

            if (dialogController.wasOkButtonClicked()) { // Sử dụng phương thức mới
                loadOrdersForCurrentPage(); // Tải lại danh sách orders
                // Nếu order vừa được sửa là order đang chọn, thì load lại items
                if (selectedOrderForItems != null && orderToEdit != null && selectedOrderForItems.getOrderIdValue().equals(orderToEdit.getOrderIdValue())) {
                    selectedOrderForItems = dialogController.getNewlyCreatedOrEditedOrder(); // Cập nhật selectedOrder
                    loadOrderItemsForSelectedOrder();
                } else if (orderToEdit == null && dialogController.getNewlyCreatedOrEditedOrder() != null) {
                    // Nếu là order mới, có thể chọn nó và load items
                    // (Tùy chọn, hoặc để người dùng tự chọn)
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Dialog Error", "Could not open the order form: " + e.getMessage());
        }
    }

    @FXML
    void handleDeleteSelectedOrder(ActionEvent event) {
        TreeItem<OrderWrapper> selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select an order to delete.");
            return;
        }
        Order orderToDelete = selected.getValue().getOriginalOrder();

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Deletion");
        confirmDialog.setHeaderText("Delete Order: " + orderToDelete.getOrderIdValue());
        confirmDialog.setContentText("Are you sure you want to permanently delete this order and all its items?");
        initOwnerForDialog(confirmDialog);

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (orderDAO.removeOrder(orderToDelete.getOrderIdValue())) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Order deleted successfully.");
                    loadOrdersForCurrentPage();
                    orderItemWrapperList.clear(); // Xóa items của order vừa bị xóa
                    setInitialItemsPaneState(true);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Deletion Failed", "Could not delete the order.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Deletion Error", "An error occurred: " + e.getMessage());
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="Order Items Table Setup and Load">
    private void setupOrderItemsTableColumns() {
        // itemPkColumn.setCellValueFactory(param -> new SimpleIntegerProperty(param.getValue().getValue().getOriginalItem().getItemPkValue()).asObject());
        itemProductIdColumn.setCellValueFactory(param -> param.getValue().getValue().productIdProperty());
        itemSellerIdColumn.setCellValueFactory(param -> param.getValue().getValue().sellerIdProperty());
        itemPriceColumn.setCellValueFactory(param -> param.getValue().getValue().priceProperty());
        itemPriceColumn.setCellFactory(col -> new TreeTableCell<OrderItemWrapper, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : currencyFormatter.format(item.doubleValue()));
            }
        });
    }

    private void loadOrderItemsForSelectedOrder() {
        orderItemWrapperList.clear();
        if (selectedOrderForItems != null && selectedOrderForItems.getOrderIdValue() != null) {
            try {
                List<OrderItem> itemsFromDB = orderDAO.fetchOrderItemsListByOrderId(selectedOrderForItems.getOrderIdValue());
                orderItemWrapperList.setAll(itemsFromDB.stream().map(OrderItemWrapper::new).collect(Collectors.toList()));
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Load Items Error", "Failed to load items for order " + selectedOrderForItems.getOrderIdValue() + ": " + e.getMessage());
            }
        }
        final TreeItem<OrderItemWrapper> rootItems = new RecursiveTreeItem<>(orderItemWrapperList, RecursiveTreeObject::getChildren);
        orderItemsTable.setRoot(rootItems);
        orderItemsTable.setShowRoot(false);
    }
    //</editor-fold>

    //<editor-fold desc="Order Item Actions (Add, Edit, Delete)">
    @FXML
    void handleAddNewItemToOrder(ActionEvent event) {
        if (selectedOrderForItems == null) {
            showAlert(Alert.AlertType.WARNING, "No Order Selected", "Please select an order first to add an item.");
            return;
        }
        openOrderItemFormDialog("Add New Item to Order: " + selectedOrderForItems.getOrderIdValue(), null, selectedOrderForItems.getOrderIdValue());
    }

    @FXML
    void handleEditSelectedItem(ActionEvent event) {
        TreeItem<OrderItemWrapper> selectedItemWrapper = orderItemsTable.getSelectionModel().getSelectedItem();
        if (selectedItemWrapper == null || selectedItemWrapper.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "No Item Selected", "Please select an item to edit.");
            return;
        }
        if (selectedOrderForItems == null) { // Trường hợp hiếm khi xảy ra nếu item được chọn
            showAlert(Alert.AlertType.ERROR, "Error", "No parent order selected for this item.");
            return;
        }
        openOrderItemFormDialog("Edit Item for Order: " + selectedOrderForItems.getOrderIdValue(), 
                                selectedItemWrapper.getValue().getOriginalItem(), 
                                selectedOrderForItems.getOrderIdValue());
    }

    private void openOrderItemFormDialog(String title, OrderItem itemToEdit, String currentOrderId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/OrderItemFormDialog.fxml")); // Sẽ tạo FXML này
            Parent root = loader.load();
            OrderItemFormDialogController dialogController = loader.getController(); // Sẽ tạo Controller này
            
            Stage currentStage = getStage();
            dialogController.setParentOwnerStage(currentStage); 
            dialogController.setOrderItemForEditing(itemToEdit, currentOrderId); // Sử dụng phương thức mới

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.initModality(Modality.WINDOW_MODAL);
            if (currentStage != null) stage.initOwner(currentStage);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            dialogController.setCurrentDialogShowingStage(stage);
            stage.showAndWait();

            if (dialogController.wasOkButtonClicked()) {
                loadOrderItemsForSelectedOrder(); // Tải lại danh sách items
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Dialog Error", "Could not open the order item form: " + e.getMessage());
        }
    }

    @FXML
    void handleDeleteSelectedItem(ActionEvent event) {
        TreeItem<OrderItemWrapper> selectedItemWrapper = orderItemsTable.getSelectionModel().getSelectedItem();
        if (selectedItemWrapper == null || selectedItemWrapper.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "No Item Selected", "Please select an item to delete.");
            return;
        }
        OrderItem itemToDelete = selectedItemWrapper.getValue().getOriginalItem();

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete Item");
        confirmDialog.setHeaderText("Delete Item: PK=" + itemToDelete.getItemPkValue() + ", Product=" + itemToDelete.getItemProductId());
        confirmDialog.setContentText("Are you sure you want to delete this item from the order?");
        initOwnerForDialog(confirmDialog);
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (orderDAO.removeOrderItem(itemToDelete.getItemPkValue())) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Item deleted successfully.");
                    loadOrderItemsForSelectedOrder();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Deletion Failed", "Could not delete the item.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Deletion Error", "An error occurred: " + e.getMessage());
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="Utility Methods">
    private Stage getStage() {
        if (ordersTable != null && ordersTable.getScene() != null) {
            return (Stage) ordersTable.getScene().getWindow();
        } else if (addNewOrderButton != null && addNewOrderButton.getScene() != null) {
            return (Stage) addNewOrderButton.getScene().getWindow();
        }
        return null; // Hoặc ném lỗi nếu không tìm thấy stage
    }

    private void initOwnerForDialog(Alert dialog) {
        Stage stage = getStage();
        if (stage != null) {
            dialog.initOwner(stage);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        initOwnerForDialog(alert);
        alert.showAndWait();
    }
    //</editor-fold>

    //<editor-fold desc="Wrapper Classes">
    public static class OrderWrapper extends RecursiveTreeObject<OrderWrapper> {
        private final StringProperty orderId;
        // Timestamp và CustomerContact không cần Property nếu chỉ hiển thị và không sửa trực tiếp trong bảng
        // Chúng ta sẽ lấy giá trị gốc để hiển thị.
        private final Order originalOrder;

        public OrderWrapper(Order order) {
            this.originalOrder = order;
            if (order == null) {
                this.orderId = new SimpleStringProperty("<Error>");
            } else {
                this.orderId = new SimpleStringProperty(order.getOrderIdValue());
            }
        }
        public Order getOriginalOrder() { return originalOrder; }
        public StringProperty orderIdProperty() { return orderId; }
        public StringProperty customerContactProperty() { // Lấy trực tiếp từ originalOrder
            return new SimpleStringProperty(originalOrder != null ? originalOrder.getCustomerContactValue() : "");
        }
    }

    public static class OrderItemWrapper extends RecursiveTreeObject<OrderItemWrapper> {
        private final StringProperty productId;
        private final StringProperty sellerId;
        private final ObjectProperty<Number> price;
        private final OrderItem originalItem;

        public OrderItemWrapper(OrderItem item) {
            this.originalItem = item;
            if (item == null) {
                this.productId = new SimpleStringProperty("<Error>");
                this.sellerId = new SimpleStringProperty("");
                this.price = new SimpleObjectProperty<>(null);
            } else {
                this.productId = new SimpleStringProperty(item.getItemProductId());
                this.sellerId = new SimpleStringProperty(item.getItemSellerId());
                this.price = new SimpleObjectProperty<>(item.getItemPriceValue());
            }
        }
        public OrderItem getOriginalItem() { return originalItem; }
        public StringProperty productIdProperty() { return productId; }
        public StringProperty sellerIdProperty() { return sellerId; }
        public ObjectProperty<Number> priceProperty() { return price; }
    }
    //</editor-fold>
}