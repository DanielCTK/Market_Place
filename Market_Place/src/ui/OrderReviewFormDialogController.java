package ui;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import dao.ReviewDAO; // Sử dụng ReviewDAO mới
import impl.ReviewDAOImpl; // Sử dụng Impl mới
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import model.Review; // Sử dụng model Review mới

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;

public class OrderReviewFormDialogController implements Initializable {

    @FXML private Label dialogTitleLabel;
    @FXML private JFXTextField reviewIdField;
    @FXML private JFXTextField orderIdField;
    @FXML private JFXComboBox<Integer> reviewScoreComboBox;
    @FXML private JFXTextArea reviewCommentArea;

    private Stage currentDialogStage;
    private Stage parentOwnerStage;
    private Review reviewBeingEdited; // Review đang được sửa
    private boolean okButtonWasClicked = false;
    private ReviewDAO reviewDataAccessObject;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        reviewDataAccessObject = new ReviewDAOImpl();
        // Khởi tạo ComboBox cho điểm số
        reviewScoreComboBox.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        // Bạn có thể thêm một mục "No Score" hoặc để trống nếu điểm số là tùy chọn
        // reviewScoreComboBox.getItems().add(0, null); // Nếu muốn cho phép bỏ chọn
    }

    public void setParentOwnerStage(Stage ownerStage) {
        this.parentOwnerStage = ownerStage;
    }
    
    public void setCurrentDialogShowingStage(Stage dialogStage) {
        this.currentDialogStage = dialogStage;
    }

    public void setReviewForEditing(Review review) {
        this.reviewBeingEdited = review;
        if (review != null) { // Chế độ sửa
            dialogTitleLabel.setText("Edit Review");
            reviewIdField.setText(review.getReviewId());
            reviewIdField.setEditable(false); // Không cho sửa Review ID
            
            orderIdField.setText(review.getOrderId()); // orderId có thể null
            reviewScoreComboBox.setValue(review.getReviewScore()); // score có thể null
            reviewCommentArea.setText(review.getReviewComment()); // comment có thể null
        } else { // Chế độ thêm mới
            dialogTitleLabel.setText("Add New Review");
            reviewIdField.clear();
            reviewIdField.setEditable(true);
            orderIdField.clear();
            reviewScoreComboBox.setValue(null); // Hoặc giá trị mặc định
            reviewCommentArea.clear();
        }
    }

    public boolean wasOkButtonClicked() {
        return okButtonWasClicked;
    }

    public Review getNewlyCreatedOrEditedReview() { 
        return reviewBeingEdited;
    }

    @FXML
    private void handleSaveReview(ActionEvent event) {
        if (isUserInputValid()) {
            boolean isNewEntry = (reviewBeingEdited == null);
            if (isNewEntry) {
                reviewBeingEdited = new Review();
            }

            // Lấy thông tin từ các trường vào đối tượng reviewBeingEdited
            reviewBeingEdited.setReviewId(reviewIdField.getText().trim());
            
            String orderIdStr = orderIdField.getText();
            reviewBeingEdited.setOrderId(orderIdStr == null || orderIdStr.trim().isEmpty() ? null : orderIdStr.trim());
            
            reviewBeingEdited.setReviewScore(reviewScoreComboBox.getValue()); // getValue() trả về null nếu không chọn
            
            String commentStr = reviewCommentArea.getText();
            reviewBeingEdited.setReviewComment(commentStr == null || commentStr.trim().isEmpty() ? null : commentStr.trim());
            
            boolean success = false;
            try {
                if (isNewEntry) {
                    // Kiểm tra ID review tồn tại trước khi thêm
                    if (reviewDataAccessObject.checkIfReviewIdExists(reviewBeingEdited.getReviewId())) {
                         showAlertDialog(Alert.AlertType.ERROR, "Input Error", "Review ID '" + reviewBeingEdited.getReviewId() + "' already exists!");
                         return; 
                    }
                    success = reviewDataAccessObject.createReview(reviewBeingEdited);
                } else {
                    success = reviewDataAccessObject.updateExistingReview(reviewBeingEdited);
                }

                if (success) {
                    okButtonWasClicked = true;
                    if (currentDialogStage != null) currentDialogStage.close();
                } else {
                    showAlertDialog(Alert.AlertType.ERROR, "Database Error",
                        (isNewEntry ? "Failed to add new review." : "Failed to update review."));
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlertDialog(Alert.AlertType.ERROR, "Database Operation Failed",
                        "An error occurred: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleCancelDialog(ActionEvent event) {
        if (currentDialogStage != null) currentDialogStage.close();
    }

    private boolean isUserInputValid() {
        String errorMessage = "";
        String rId = reviewIdField.getText();

        if (rId == null || rId.trim().isEmpty()) {
            errorMessage += "Review ID is required!\n";
        } else if (rId.trim().length() > 11) { // Dựa vào schema
             errorMessage += "Review ID cannot be longer than 11 characters.\n";
        }
        
        // Order ID có thể null, nhưng nếu nhập thì không quá 11 chars
        String oId = orderIdField.getText();
        if (oId != null && !oId.trim().isEmpty() && oId.trim().length() > 11) {
            errorMessage += "Order ID, if provided, cannot be longer than 11 characters.\n";
        }
        
        // Review Score là tùy chọn (Integer), JFXComboBox đã xử lý giá trị null.
        // Không cần kiểm tra bắt buộc ở đây trừ khi bạn muốn.

        // Review Comment là tùy chọn (TEXT), JFXTextArea đã xử lý giá trị null/trống.
        // Giới hạn độ dài cho TEXT thường rất lớn, không cần kiểm tra ở đây trừ khi có yêu cầu cụ thể.

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