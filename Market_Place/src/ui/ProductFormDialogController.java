package ui;

import com.jfoenix.controls.JFXTextField;
import dao.ProductDAO;
import impl.ProductDAOImpl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import model.Product; // Quan trọng: Model Product đã được đơn giản hóa

public class ProductFormDialogController {

    @FXML private JFXTextField productIdField;
    @FXML private JFXTextField categoryNameField;
    @FXML private JFXTextField weightField;

    private Stage dialogStage;
    private Stage ownerStage;
    private Product productToEdit;
    private boolean okClicked = false;
    private ProductDAO productDAO;

    public ProductFormDialogController() {
        productDAO = new ProductDAOImpl();
    }

    public void setDialogStage(Stage ownerStage) {
        this.ownerStage = ownerStage;
    }
    
    public void setCurrentDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setProductToEdit(Product product) {
        this.productToEdit = product;
        if (product != null) {
            // Sử dụng getter chuẩn từ Product model đã đơn giản hóa
            productIdField.setText(product.getProductId());
            productIdField.setEditable(false); 
            categoryNameField.setText(product.getProductCategoryName());
            if (product.getProductWeightG() != null) {
                weightField.setText(String.valueOf(product.getProductWeightG()));
            } else {
                weightField.clear();
            }
        } else {
            productIdField.setEditable(true);
        }
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    @FXML
    private void handleSave(ActionEvent event) throws Exception {
        if (isInputValid()) {
            Product product = (productToEdit == null) ? new Product() : productToEdit;

            product.setProductId(productIdField.getText().trim());
            product.setProductCategoryName(categoryNameField.getText().trim());
            
            String weightStr = weightField.getText().trim();
            if (!weightStr.isEmpty()) {
                product.setProductWeightG(Double.parseDouble(weightStr));
            } else {
                product.setProductWeightG(null);
            }

            boolean success;
            if (productToEdit == null) {
                success = productDAO.addProduct(product);
            } else {
                success = productDAO.updateProduct(product);
            }

            if (success) {
                okClicked = true;
                if (dialogStage != null) dialogStage.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error",
                        (productToEdit == null ? "Failed to add product." : "Failed to update product.") +
                        (productToEdit == null && productDAO.productIdExists(product.getProductId()) ? " Product ID might already exist." : "")
                );
            }
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        if (dialogStage != null) dialogStage.close();
    }

    private boolean isInputValid() throws Exception {
        String errorMessage = "";

        if (productIdField.getText() == null || productIdField.getText().trim().isEmpty()) {
            errorMessage += "Product ID is required!\n";
        } else if (productToEdit == null && productDAO.productIdExists(productIdField.getText().trim())) {
             errorMessage += "Product ID already exists!\n";
        }
        
        // Category name có thể để trống nếu CSDL cho phép NULL
        // if (categoryNameField.getText() == null || categoryNameField.getText().trim().isEmpty()) {
        // errorMessage += "Category Name is required!\n";
        // }

        String weightStr = weightField.getText().trim();
        if (!weightStr.isEmpty()) {
            try {
                Double.parseDouble(weightStr);
            } catch (NumberFormatException e) {
                errorMessage += "Weight must be a valid number (e.g., 500.75)!\n";
            }
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            showAlert(Alert.AlertType.ERROR, "Invalid Fields", errorMessage);
            return false;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        // Đảm bảo owner được set đúng cách
        if (dialogStage != null) alert.initOwner(dialogStage);
        else if (ownerStage != null) alert.initOwner(ownerStage);
        
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}