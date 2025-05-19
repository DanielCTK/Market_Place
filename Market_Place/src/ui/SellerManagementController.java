package ui;

import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import dao.SellerDAO;
import impl.SellerDAOImpl;
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
import javafx.scene.control.TreeItem;
// Import TreeTableCell nếu bạn có custom cell factory, ở đây không cần cho SellerState
import javafx.scene.control.TreeTableColumn;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text; // Cho totalSellersCountText
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Seller;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class SellerManagementController implements Initializable {

    @FXML private JFXTreeTableView<SellerWrapper> sellersTable;
    @FXML private JFXTreeTableColumn<SellerWrapper, String> sellerIdColumn;
    @FXML private JFXTreeTableColumn<SellerWrapper, String> sellerStateColumn;

    @FXML private TextField searchField;
    @FXML private JFXComboBox<String> stateFilterComboBox; // Đổi tên từ categoryFilterComboBox
    @FXML private JFXButton searchButton;

    @FXML private JFXButton addButton;
    @FXML private JFXButton editButton;
    @FXML private JFXButton deleteButton;

    @FXML private HBox paginationControls;
    @FXML private JFXButton firstPageButton;
    @FXML private JFXButton prevPageButton;
    @FXML private Label pageInfoLabel; // Label hiển thị thông tin trang và tổng số
    @FXML private JFXButton nextPageButton;
    @FXML private JFXButton lastPageButton;
    
    @FXML private Text totalSellersCountText; // Label riêng để hiển thị tổng số seller ở pane trái

    private SellerDAO sellerDAO;
    private ObservableList<SellerWrapper> sellerWrapperList;

    private int currentPage = 1;
    private final int pageSize = 15; // Hoặc số lượng bạn muốn
    private int totalItems = 0;
    private int totalPages = 1;
    
    private String currentSearchKeyword = "";
    private String currentStateFilter = ""; 

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("SellerManagementController: initialize() CALLED.");
        sellerDAO = new SellerDAOImpl();
        sellerWrapperList = FXCollections.observableArrayList();

        setupTableColumns();
        loadStatesForFilter(); // Load các state cho ComboBox

        if (searchField != null) {
            searchField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    triggerSearch();
                }
            });
        }
        if (stateFilterComboBox != null) {
            stateFilterComboBox.setOnAction(event -> triggerSearch());
        }
        if (searchButton != null) {
             searchButton.setOnAction(event -> triggerSearch());
        }

        loadSellersForCurrentPage();
        System.out.println("SellerManagementController: initialize() FINISHED.");
    }

    private void setupTableColumns() {
        System.out.println("Setting up seller table columns...");
        sellerIdColumn.setCellValueFactory(param -> param.getValue().getValue().sellerIdProperty());
        sellerStateColumn.setCellValueFactory(param -> param.getValue().getValue().sellerStateProperty());
        System.out.println("Seller table columns setup complete.");
    }
    
    private void loadStatesForFilter() {
        System.out.println("Loading states for filter...");
        try {
            List<String> states = sellerDAO.getAllDistinctSellerStates(); 
            ObservableList<String> stateItems = FXCollections.observableArrayList();
            stateItems.add("All States"); 
            if (states != null && !states.isEmpty()) {
                stateItems.addAll(states);
            }
            if (stateFilterComboBox != null) {
                stateFilterComboBox.setItems(stateItems);
                stateFilterComboBox.setValue("All States");
            }
            System.out.println("States loaded: " + (stateItems != null ? stateItems.size() : 0));
        } catch (Exception e) {
            System.err.println("Error loading states: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "State Load Error", "Failed to load states for filter: " + e.getMessage());
        }
    }

    private void triggerSearch() {
        System.out.println("--- triggerSearch (Seller) CALLED ---");
        if (searchField != null) {
            currentSearchKeyword = searchField.getText().trim();
        } else {
            currentSearchKeyword = "";
        }
        
        if (stateFilterComboBox != null && stateFilterComboBox.getValue() != null) {
            String selectedState = stateFilterComboBox.getValue();
            if ("All States".equals(selectedState)) {
                currentStateFilter = ""; 
            } else {
                currentStateFilter = selectedState;
            }
        } else {
            currentStateFilter = ""; 
        }
        
        System.out.println("Search Keyword: '" + currentSearchKeyword + "', State Filter: '" + currentStateFilter + "'");
        currentPage = 1;
        loadSellersForCurrentPage();
        System.out.println("--- triggerSearch (Seller) FINISHED ---");
    }

    private void loadSellersForCurrentPage() {
        System.out.println("--- loadSellersForCurrentPage CALLED --- Page: " + currentPage);
        System.out.println("Using Keyword: '" + currentSearchKeyword + "', State: '" + currentStateFilter + "'");
        try {
            totalItems = sellerDAO.getTotalSellerCount(currentSearchKeyword, currentStateFilter);
            System.out.println("Total sellers from DAO: " + totalItems);

            totalPages = (totalItems == 0) ? 1 : (int) Math.ceil((double) totalItems / pageSize);
            if (currentPage > totalPages && totalPages > 0) {
                currentPage = totalPages;
            }
            if (currentPage < 1) {
                currentPage = 1;
            }

            int offset = (currentPage - 1) * pageSize;
            List<Seller> sellersFromDB = sellerDAO.getSellers(offset, pageSize, currentSearchKeyword, currentStateFilter);
            
            if (sellersFromDB == null) {
                sellersFromDB = Collections.emptyList();
            }
            System.out.println("Sellers fetched from DB for current page: " + sellersFromDB.size());

            List<SellerWrapper> tempList = sellersFromDB.stream()
                .map(SellerWrapper::new)
                .collect(Collectors.toList());
            
            sellerWrapperList.setAll(tempList);
            System.out.println("sellerWrapperList size after mapping: " + sellerWrapperList.size());

            final TreeItem<SellerWrapper> root = new RecursiveTreeItem<>(sellerWrapperList, RecursiveTreeObject::getChildren);
            sellersTable.setRoot(root);
            sellersTable.setShowRoot(false);
            System.out.println("Sellers table updated with " + sellerWrapperList.size() + " items.");

            updatePaginationUI();
        } catch (Exception e) {
            System.err.println("Error in loadSellers: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Load Error", "Failed to load sellers: " + e.getMessage());
        }
        System.out.println("--- loadSellersForCurrentPage FINISHED ---");
    }
    
    private void updatePaginationUI() {
        if (pageInfoLabel != null) {
            pageInfoLabel.setText("Page " + currentPage + " of " + totalPages + " (Total: " + totalItems + ")");
        }
        if (totalSellersCountText != null) { // Cập nhật label tổng số seller ở pane trái
             totalSellersCountText.setText(String.valueOf(totalItems));
        }

        if (firstPageButton != null) firstPageButton.setDisable(currentPage <= 1);
        if (prevPageButton != null) prevPageButton.setDisable(currentPage <= 1);
        if (nextPageButton != null) nextPageButton.setDisable(currentPage >= totalPages);
        if (lastPageButton != null) lastPageButton.setDisable(currentPage >= totalPages);
        
        if (paginationControls != null) {
            paginationControls.setVisible(totalPages > 1 || totalItems > pageSize); 
        }
    }

    @FXML void handleFirstPage(ActionEvent event) { 
        if (currentPage > 1) { 
            currentPage = 1; 
            loadSellersForCurrentPage(); 
        }
    }
    @FXML void handlePreviousPage(ActionEvent event) { 
        if (currentPage > 1) { 
            currentPage--; 
            loadSellersForCurrentPage(); 
        }
    }
    @FXML void handleNextPage(ActionEvent event) { 
        if (currentPage < totalPages) { 
            currentPage++; 
            loadSellersForCurrentPage(); 
        }
    }
    @FXML void handleLastPage(ActionEvent event) { 
        if (currentPage < totalPages) { 
            currentPage = totalPages; 
            loadSellersForCurrentPage(); 
        }
    }

    private void openSellerFormDialog(String title, Seller sellerToEdit) {
        System.out.println("Opening Seller Form Dialog. Title: " + title + 
                           (sellerToEdit != null && sellerToEdit.getSellerId() != null ? 
                            ", Editing Seller ID: " + sellerToEdit.getSellerId() :
                            ", Adding new seller."));
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/SellerFormDialog.fxml"));
            Parent root = loader.load();
            SellerFormDialogController dialogController = loader.getController();
            
            Stage currentStage = null;
            if (addButton != null && addButton.getScene() != null) {
                currentStage = (Stage) addButton.getScene().getWindow();
            } else if (sellersTable != null && sellersTable.getScene() != null) {
                 currentStage = (Stage) sellersTable.getScene().getWindow();
            }

            dialogController.setDialogStage(currentStage); 

            if (sellerToEdit != null) {
                dialogController.setSellerToEdit(sellerToEdit);
            } else {
                dialogController.setSellerToEdit(null); // Đảm bảo form trống khi thêm mới
            }

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.initModality(Modality.WINDOW_MODAL);
            if (currentStage != null) {
                stage.initOwner(currentStage);
            }
            Scene scene = new Scene(root);
            stage.setScene(scene);
            dialogController.setCurrentDialogStage(stage);
            stage.showAndWait();

            if (dialogController.isOkClicked()) {
                loadSellersForCurrentPage();
            }
        } catch (IOException e) {
            System.err.println("Failed to load SellerFormDialog.fxml: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Dialog Error", "Could not open the seller form: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error opening seller form dialog: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Dialog Error", "An unexpected error occurred: " + e.getMessage());
        }
    }
    
    @FXML void handleAddNewSeller(ActionEvent event) { 
        openSellerFormDialog("Add New Seller", null); 
    }

    @FXML void handleEditSeller(ActionEvent event) { 
        TreeItem<SellerWrapper> selectedItem = sellersTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null || selectedItem.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a seller to edit.");
            return;
        }
        Seller sellerToEdit = selectedItem.getValue().getOriginalSeller();
        openSellerFormDialog("Edit Seller", sellerToEdit);
    }

    @FXML
    void handleDeleteSeller(ActionEvent event) {
        TreeItem<SellerWrapper> selectedItem = sellersTable.getSelectionModel().getSelectedItem();

        if (selectedItem == null || selectedItem.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a seller to delete.");
            return;
        }

        Seller sellerToDelete = selectedItem.getValue().getOriginalSeller();
        if (sellerToDelete == null || sellerToDelete.getSellerId() == null) {
            showAlert(Alert.AlertType.ERROR, "Selection Error", "Cannot delete seller: Invalid seller data selected.");
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Deletion");
        confirmDialog.setHeaderText("Delete Seller: " + sellerToDelete.getSellerId());
        confirmDialog.setContentText("Are you sure you want to permanently delete this seller?\n" +
                                     "ID: " + sellerToDelete.getSellerId() + "\n" +
                                     "State: " + (sellerToDelete.getSellerState() != null ? sellerToDelete.getSellerState() : "N/A"));

        if (sellersTable.getScene() != null && sellersTable.getScene().getWindow() != null) {
             confirmDialog.initOwner((Stage) sellersTable.getScene().getWindow());
        }

        Optional<ButtonType> result = confirmDialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (sellerDAO.deleteSeller(sellerToDelete.getSellerId())) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Seller deleted successfully.");
                    loadSellersForCurrentPage(); 
                } else {
                    showAlert(Alert.AlertType.ERROR, "Deletion Failed", "Could not delete the seller.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Deletion Error", "An error occurred: " + e.getMessage());
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (sellersTable.getScene() != null && sellersTable.getScene().getWindow() != null) {
             alert.initOwner((Stage) sellersTable.getScene().getWindow());
        }
        alert.showAndWait();
    }

    public static class SellerWrapper extends RecursiveTreeObject<SellerWrapper> {
        private final StringProperty sellerId;
        private final StringProperty sellerState;
        private final Seller originalSeller;

        public SellerWrapper(Seller seller) {
            this.originalSeller = seller;
            if (seller == null) {
                this.sellerId = new SimpleStringProperty("<Error>");
                this.sellerState = new SimpleStringProperty("");
            } else {
                this.sellerId = new SimpleStringProperty(seller.getSellerId());
                this.sellerState = new SimpleStringProperty(seller.getSellerState() == null ? "" : seller.getSellerState());
            }
        }

        public Seller getOriginalSeller() { 
            return originalSeller; 
        }

        public StringProperty sellerIdProperty() { return sellerId; }
        public StringProperty sellerStateProperty() { return sellerState; }
    }
}