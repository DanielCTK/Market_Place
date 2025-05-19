package ui;

import com.jfoenix.controls.JFXTextField;
import dao.SellerDAO;
import impl.SellerDAOImpl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import model.Seller;

public class SellerFormDialogController {

    @FXML private JFXTextField sellerIdField;
    @FXML private JFXTextField sellerStateField;

    private Stage dialogStage;
    private Stage ownerStage;
    private Seller sellerToEdit;
    private boolean okClicked = false;
    private SellerDAO sellerDAO;

    public SellerFormDialogController() {
        sellerDAO = new SellerDAOImpl();
    }

    public void setDialogStage(Stage ownerStage) {
        this.ownerStage = ownerStage;
    }
    
    public void setCurrentDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setSellerToEdit(Seller seller) {
        this.sellerToEdit = seller;
        if (seller != null) {
            sellerIdField.setText(seller.getSellerId());
            sellerIdField.setEditable(false); 
            sellerStateField.setText(seller.getSellerState()); // State có thể null
        } else {
            sellerIdField.clear();
            sellerStateField.clear();
            sellerIdField.setEditable(true);
        }
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (isInputValid()) {
            Seller seller = (sellerToEdit == null) ? new Seller() : sellerToEdit;

            seller.setSellerId(sellerIdField.getText().trim());
            // seller_state có thể là chuỗi rỗng hoặc null, DAO sẽ xử lý
            seller.setSellerState(sellerStateField.getText().trim().isEmpty() ? null : sellerStateField.getText().trim());
            
            boolean success = false;
            String operationError = "";

            try {
                if (sellerToEdit == null) {
                    if (sellerDAO.sellerIdExists(seller.getSellerId())) {
                         showAlert(Alert.AlertType.ERROR, "Input Error", "Seller ID '" + seller.getSellerId() + "' already exists!");
                         return;
                    }
                    success = sellerDAO.addSeller(seller);
                    if (!success) operationError = "Failed to add seller to the database.";
                } else {
                    success = sellerDAO.updateSeller(seller);
                     if (!success) operationError = "Failed to update seller in the database.";
                }

                if (success) {
                    okClicked = true;
                    if (dialogStage != null) dialogStage.close();
                } else {
                    String finalErrorMessage = (sellerToEdit == null ? "Failed to add seller." : "Failed to update seller.");
                    if (!operationError.isEmpty()) {
                        finalErrorMessage += " " + operationError;
                    }
                    showAlert(Alert.AlertType.ERROR, "Database Error", finalErrorMessage);
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Operation Failed",
                        "An error occurred: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        if (dialogStage != null) dialogStage.close();
    }

    private boolean isInputValid() {
        String errorMessage = "";
        String sId = sellerIdField.getText();

        if (sId == null || sId.trim().isEmpty()) {
            errorMessage += "Seller ID is required!\n";
        } else if (sId.trim().length() > 11 ) { // Dựa vào DB là varchar(11B)
             errorMessage += "Seller ID cannot be longer than 11 characters.\n";
        }
        // Không cần kiểm tra sellerIdExists ở đây nữa vì đã làm trong handleSave

        // seller_state có thể null, nhưng nếu nhập thì không quá 10 chars
        String sState = sellerStateField.getText();
        if (sState != null && sState.trim().length() > 10) { // Dựa vào DB là varchar(10B)
             errorMessage += "Seller State cannot be longer than 10 characters.\n";
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
        if (dialogStage != null && dialogStage.isShowing()) {
             alert.initOwner(dialogStage);
        } else if (ownerStage != null && ownerStage.isShowing()) {
            alert.initOwner(ownerStage);
        }
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}