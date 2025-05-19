package ui;

import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import dao.ProductDAO;
import impl.ProductDAOImpl;
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
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text; // Đảm bảo import Text
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Product;

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ProductManagementController implements Initializable {

    @FXML private JFXTreeTableView<ProductWrapper> productsTable;
    @FXML private JFXTreeTableColumn<ProductWrapper, String> productIdColumn;
    @FXML private JFXTreeTableColumn<ProductWrapper, String> categoryColumn;
    @FXML private JFXTreeTableColumn<ProductWrapper, Number> weightColumn;

    @FXML private TextField searchField;
    @FXML private JFXComboBox<String> categoryFilterComboBox;
    @FXML private JFXButton searchButton;

    @FXML private JFXButton addButton;
    @FXML private JFXButton editButton;
    @FXML private JFXButton deleteButton;
    
    @FXML private Text totalProductCountText; // Đảm bảo fx:id="totalProductCountText" có trong FXML

    @FXML private HBox paginationControls;
    @FXML private JFXButton firstPageButton;
    @FXML private JFXButton prevPageButton;
    @FXML private Label pageInfoLabel;
    @FXML private JFXButton nextPageButton;
    @FXML private JFXButton lastPageButton;

    private ProductDAO productDAO;
    private ObservableList<ProductWrapper> productWrapperList;
    private final NumberFormat numberFormatter = NumberFormat.getNumberInstance();

    private int currentPage = 1;
    private final int pageSize = 15;
    private int totalItems = 0;
    private int totalPages = 1;
    
    private String currentSearchKeyword = "";
    private String currentCategoryFilter = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("ProductManagementController: initialize() CALLED.");
        System.out.println("ProductManagementController: totalProductCountText is " + (totalProductCountText == null ? "NULL" : "NOT NULL")); // Log kiểm tra

        productDAO = new ProductDAOImpl();
        productWrapperList = FXCollections.observableArrayList();

        setupTableColumns();
        loadCategoriesForFilter();

        if (searchField != null) {
            searchField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    triggerSearch();
                }
            });
        } else {
            System.err.println("ProductManagementController: searchField (fx:id) IS NULL!");
        }

        if (categoryFilterComboBox != null) {
            categoryFilterComboBox.setOnAction(event -> triggerSearch());
        } else {
            System.err.println("ProductManagementController: categoryFilterComboBox (fx:id) IS NULL!");
        }

        if (searchButton != null) {
             searchButton.setOnAction(event -> triggerSearch());
        } else {
            System.err.println("ProductManagementController: searchButton (fx:id) IS NULL!");
        }

        loadProductsForCurrentPage();
        System.out.println("ProductManagementController: initialize() FINISHED.");
    }

    private void setupTableColumns() {
        System.out.println("Setting up product table columns...");
        productIdColumn.setCellValueFactory(param -> param.getValue().getValue().productIdProperty());
        categoryColumn.setCellValueFactory(param -> param.getValue().getValue().productCategoryNameProperty());
        
        weightColumn.setCellValueFactory(param -> param.getValue().getValue().productWeightGProperty());
        weightColumn.setCellFactory(col -> new TreeTableCell<ProductWrapper, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(numberFormatter.format(item.doubleValue()));
                }
            }
        });
        System.out.println("Product table columns setup complete.");
    }
    
    private void loadCategoriesForFilter() {
        System.out.println("Loading categories for filter...");
        try {
            List<String> categories = productDAO.getAllDistinctCategories(); 
            ObservableList<String> categoryItems = FXCollections.observableArrayList();
            categoryItems.add("All Categories"); 
            if (categories != null && !categories.isEmpty()) {
                categoryItems.addAll(categories);
            }
            if (categoryFilterComboBox != null) {
                categoryFilterComboBox.setItems(categoryItems);
                categoryFilterComboBox.setValue("All Categories");
            } else {
                System.err.println("ProductManagementController: categoryFilterComboBox IS NULL in loadCategoriesForFilter!");
            }
            System.out.println("Categories loaded: " + (categoryItems != null ? categoryItems.size() : 0));
        } catch (Exception e) {
            System.err.println("Error loading categories: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Category Load Error", "Failed to load categories: " + e.getMessage());
        }
    }

    private void triggerSearch() {
        System.out.println("--- triggerSearch (Product) CALLED ---");
        if (searchField != null) {
            currentSearchKeyword = searchField.getText().trim();
        } else {
            currentSearchKeyword = "";
        }
        
        if (categoryFilterComboBox != null && categoryFilterComboBox.getValue() != null) {
            String selectedCategory = categoryFilterComboBox.getValue();
            currentCategoryFilter = "All Categories".equals(selectedCategory) ? "" : selectedCategory;
        } else {
            currentCategoryFilter = ""; 
        }
        
        System.out.println("Search Keyword: '" + currentSearchKeyword + "', Category Filter: '" + currentCategoryFilter + "'");
        currentPage = 1;
        loadProductsForCurrentPage();
        System.out.println("--- triggerSearch (Product) FINISHED ---");
    }

    // Phương thức mới để cập nhật Text hiển thị tổng số sản phẩm
    private void updateTotalProductCountDisplay() {
        if (totalProductCountText != null) {
            totalProductCountText.setText(String.valueOf(totalItems));
            System.out.println("ProductManagementController: Updated totalProductCountText to: " + totalItems);
        } else {
             System.err.println("ProductManagementController: totalProductCountText (fx:id) IS NULL! Cannot update display.");
        }
    }

    private void loadProductsForCurrentPage() {
        System.out.println("--- loadProductsForCurrentPage CALLED --- Page: " + currentPage);
        System.out.println("Using Keyword: '" + currentSearchKeyword + "', Category: '" + currentCategoryFilter + "'");
        try {
            totalItems = productDAO.getTotalProductCount(currentSearchKeyword, currentCategoryFilter);
            System.out.println("Total products from DAO: " + totalItems);

            updateTotalProductCountDisplay(); // GỌI CẬP NHẬT Ở ĐÂY

            totalPages = (totalItems == 0) ? 1 : (int) Math.ceil((double) totalItems / pageSize);
            if (currentPage > totalPages && totalPages > 0) {
                currentPage = totalPages;
            }
            if (currentPage < 1) {
                currentPage = 1;
            }

            int offset = (currentPage - 1) * pageSize;
            List<Product> productsFromDB = productDAO.getProducts(offset, pageSize, currentSearchKeyword, currentCategoryFilter);
            
            if (productsFromDB == null) {
                productsFromDB = Collections.emptyList();
            }
            System.out.println("Products fetched from DB for current page: " + productsFromDB.size());

            List<ProductWrapper> tempList = productsFromDB.stream()
                .map(ProductWrapper::new)
                .collect(Collectors.toList());
            
            productWrapperList.setAll(tempList);
            System.out.println("productWrapperList size after mapping: " + productWrapperList.size());

            final TreeItem<ProductWrapper> root = new RecursiveTreeItem<>(productWrapperList, RecursiveTreeObject::getChildren);
            if (productsTable != null) {
                productsTable.setRoot(root);
                productsTable.setShowRoot(false);
            } else {
                System.err.println("ProductManagementController: productsTable (fx:id) IS NULL!");
            }
            System.out.println("Table updated with " + productWrapperList.size() + " items.");

            updatePaginationUI();
        } catch (Exception e) {
            System.err.println("Error in loadProducts: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Load Error", "Failed to load products: " + e.getMessage());
            // Reset state khi có lỗi
            totalItems = 0;
            updateTotalProductCountDisplay();
            totalPages = 1;
            currentPage = 1;
            if(productWrapperList != null) productWrapperList.clear();
            updatePaginationUI();
        }
        System.out.println("--- loadProductsForCurrentPage FINISHED ---");
    }
    
    private void updatePaginationUI() {
        if (pageInfoLabel != null) {
            pageInfoLabel.setText("Page " + currentPage + " of " + totalPages + " (Total: " + totalItems + ")");
        } else { System.err.println("ProductManagementController: pageInfoLabel (fx:id) IS NULL!"); }

        boolean isFirstPage = (currentPage <= 1);
        boolean isLastPage = (currentPage >= totalPages || totalItems == 0);

        if (firstPageButton != null) firstPageButton.setDisable(isFirstPage); else { System.err.println("ProductManagementController: firstPageButton (fx:id) IS NULL!");}
        if (prevPageButton != null) prevPageButton.setDisable(isFirstPage); else { System.err.println("ProductManagementController: prevPageButton (fx:id) IS NULL!");}
        if (nextPageButton != null) nextPageButton.setDisable(isLastPage); else { System.err.println("ProductManagementController: nextPageButton (fx:id) IS NULL!");}
        if (lastPageButton != null) lastPageButton.setDisable(isLastPage); else { System.err.println("ProductManagementController: lastPageButton (fx:id) IS NULL!");}
        
        if (paginationControls != null) {
            paginationControls.setVisible(totalPages > 1 || (totalItems > 0 && totalItems > pageSize) ); // Hiển thị nếu nhiều hơn 1 trang, hoặc có items và nhiều hơn pagesize
        } else {
             System.err.println("ProductManagementController: paginationControls (fx:id) IS NULL!");
        }
    }

    @FXML void handleFirstPage(ActionEvent event) { 
        if (currentPage > 1) { 
            currentPage = 1; 
            loadProductsForCurrentPage(); 
        }
    }
    @FXML void handlePreviousPage(ActionEvent event) { 
        if (currentPage > 1) { 
            currentPage--; 
            loadProductsForCurrentPage(); 
        }
    }
    @FXML void handleNextPage(ActionEvent event) { 
        if (currentPage < totalPages) { 
            currentPage++; 
            loadProductsForCurrentPage(); 
        }
    }
    @FXML void handleLastPage(ActionEvent event) { 
        if (currentPage < totalPages) { 
            currentPage = totalPages; 
            loadProductsForCurrentPage(); 
        }
    }

    private void openProductFormDialog(String title, Product productToEdit) {
        System.out.println("Opening Product Form Dialog. Title: " + title + 
                           (productToEdit != null && productToEdit.getProductId() != null ? 
                            ", Editing Product ID: " + productToEdit.getProductId() : 
                            (productToEdit != null ? ", Editing existing product" : ", Adding new product.")));
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/ProductFormDialog.fxml"));
            Parent root = loader.load();
            ProductFormDialogController dialogController = loader.getController();
            
            Stage currentStage = getStageFromAvailableNode();
            dialogController.setDialogStage(currentStage); 

            if (productToEdit != null) {
                dialogController.setProductToEdit(productToEdit);
            } else {
                dialogController.setProductToEdit(null); // Đảm bảo form trống khi thêm mới
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
                loadProductsForCurrentPage();
                showAlert(Alert.AlertType.INFORMATION, "Success", title.startsWith("Add") ? "Product added successfully!" : "Product updated successfully!");
            }
        } catch (IOException e) {
            System.err.println("Failed to load ProductFormDialog.fxml: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Dialog Error", "Could not open the product form: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error opening product form dialog: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Dialog Error", "An unexpected error occurred: " + e.getMessage());
        }
    }
    
    private Stage getStageFromAvailableNode() {
        if (productsTable != null && productsTable.getScene() != null) return (Stage) productsTable.getScene().getWindow();
        if (addButton != null && addButton.getScene() != null) return (Stage) addButton.getScene().getWindow();
        System.err.println("ProductManagementController: Could not determine current stage from available UI nodes.");
        return null;
    }


    @FXML void handleAddNewProduct(ActionEvent event) { 
        openProductFormDialog("Add New Product", null); 
    }

    @FXML void handleEditProduct(ActionEvent event) { 
        TreeItem<ProductWrapper> selectedItem = productsTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null || selectedItem.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to edit.");
            return;
        }
        Product productToEdit = selectedItem.getValue().getOriginalProduct();
        openProductFormDialog("Edit Product", productToEdit);
    }

    @FXML
    void handleDeleteProduct(ActionEvent event) {
        TreeItem<ProductWrapper> selectedItem = productsTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null || selectedItem.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to delete.");
            return;
        }
        Product productToDelete = selectedItem.getValue().getOriginalProduct();

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Deletion");
        confirmDialog.setHeaderText("Delete Product: " + productToDelete.getProductId());
        confirmDialog.setContentText("Are you sure you want to permanently delete this product?");
        initOwnerForDialog(confirmDialog);

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (productDAO.deleteProduct(productToDelete.getProductId())) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Product deleted successfully.");
                    loadProductsForCurrentPage(); 
                } else {
                    showAlert(Alert.AlertType.ERROR, "Deletion Failed", "Could not delete the product.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Deletion Error", "An error occurred: " + e.getMessage());
            }
        }
    }

    private void initOwnerForDialog(Alert dialog) {
        Stage owner = getStageFromAvailableNode();
        if (owner != null) {
            dialog.initOwner(owner);
        } else {
            System.err.println("Could not set owner for Alert dialog: " + dialog.getTitle());
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

    public static class ProductWrapper extends RecursiveTreeObject<ProductWrapper> {
        private final StringProperty productId;
        private final StringProperty productCategoryName;
        private final ObjectProperty<Number> productWeightG;
        private final Product originalProduct;

        public ProductWrapper(Product product) {
            this.originalProduct = product;
            if (product == null) {
                this.productId = new SimpleStringProperty("<Error>");
                this.productCategoryName = new SimpleStringProperty("");
                this.productWeightG = new SimpleObjectProperty<>(null);
            } else {
                this.productId = new SimpleStringProperty(product.getProductId());
                this.productCategoryName = new SimpleStringProperty(product.getProductCategoryName());
                this.productWeightG = new SimpleObjectProperty<>(product.getProductWeightG());
            }
        }
        public Product getOriginalProduct() { return originalProduct; }
        public StringProperty productIdProperty() { return productId; }
        public StringProperty productCategoryNameProperty() { return productCategoryName; }
        public ObjectProperty<Number> productWeightGProperty() { return productWeightG; }
    }
}