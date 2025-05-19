package ui; // Hoặc package ui của bạn

import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import dao.ReviewDAO; 
import impl.ReviewDAOImpl; 
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
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter; // THÊM IMPORT NÀY
import model.Review; 

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ReviewManagementController implements Initializable {

    @FXML private JFXTreeTableView<ReviewWrapper> reviewsTable;
    @FXML private JFXTreeTableColumn<ReviewWrapper, String> reviewIdColumn;
    @FXML private JFXTreeTableColumn<ReviewWrapper, String> orderIdColumn;
    @FXML private JFXTreeTableColumn<ReviewWrapper, Integer> reviewScoreColumn;
    @FXML private JFXTreeTableColumn<ReviewWrapper, String> reviewCommentColumn;

    @FXML private TextField searchReviewField;
    @FXML private JFXComboBox<Integer> scoreFilterComboBox; 
    @FXML private JFXButton searchReviewButton;

    @FXML private JFXButton addNewReviewButton;
    @FXML private JFXButton editReviewButton;
    @FXML private JFXButton deleteReviewButton;

    @FXML private HBox paginationControls;
    @FXML private JFXButton firstPageButton;
    @FXML private JFXButton prevPageButton;
    @FXML private Label pageInfoLabel;
    @FXML private JFXButton nextPageButton;
    @FXML private JFXButton lastPageButton;
    @FXML private Text totalReviewsCountText;

    private ReviewDAO reviewDAO;
    private ObservableList<ReviewWrapper> reviewWrapperList;

    private int currentPage = 1;
    private final int pageSize = 15; 
    private int totalItems = 0;
    private int totalPages = 1;
    
    private String currentSearchKeyword = "";
    private Integer currentScoreFilter = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        reviewDAO = new ReviewDAOImpl();
        reviewWrapperList = FXCollections.observableArrayList();

        setupTableColumns();
        loadScoresForFilter(); 

        if (searchReviewField != null) {
            searchReviewField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    triggerReviewSearch();
                }
            });
        }
        if (scoreFilterComboBox != null) {
            scoreFilterComboBox.setOnAction(event -> triggerReviewSearch());
        }
        if (searchReviewButton != null) {
             searchReviewButton.setOnAction(event -> triggerReviewSearch());
        }
        
        loadReviewsForCurrentPage();
    }

    private void setupTableColumns() {
        reviewIdColumn.setCellValueFactory(param -> param.getValue().getValue().reviewIdProperty());
        orderIdColumn.setCellValueFactory(param -> param.getValue().getValue().orderIdProperty());
        reviewScoreColumn.setCellValueFactory(param -> param.getValue().getValue().reviewScoreProperty());
        reviewCommentColumn.setCellValueFactory(param -> param.getValue().getValue().reviewCommentProperty());

        reviewCommentColumn.setCellFactory(column -> new TreeTableCell<ReviewWrapper, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    if (item.length() > 70) { 
                        setText(item.substring(0, 67) + "...");
                    } else {
                        setText(item);
                    }
                    javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip(item);
                    tooltip.setWrapText(true);
                    tooltip.setPrefWidth(300);
                    setTooltip(tooltip);
                }
            }
        });
    }
    
    private void loadScoresForFilter() {
        try {
            List<Integer> scores = reviewDAO.getDistinctReviewScores();
            ObservableList<Integer> scoreItems = FXCollections.observableArrayList();
            scoreItems.add(null); // Đại diện cho "All Scores"
            if (scores != null && !scores.isEmpty()) {
                scoreItems.addAll(scores);
            }

            if (scoreFilterComboBox != null) {
                scoreFilterComboBox.setItems(scoreItems);
                scoreFilterComboBox.setPromptText("     "); // Prompt text

                // ---- SỬA STRINGCONVERTER Ở ĐÂY ----
                scoreFilterComboBox.setConverter(new StringConverter<Integer>() {
                    @Override
                    public String toString(Integer score) {
                        if (score == null) {
                            return "ANY"; // Hiển thị chuỗi này cho giá trị null
                        }
                        return score.toString();
                    }

                    @Override
                    public Integer fromString(String string) {
                        // Phương thức này quan trọng nếu ComboBox cho phép nhập liệu tự do
                        // hoặc khi bạn muốn chuyển đổi chuỗi người dùng nhập thành Integer.
                        // Với ComboBox chỉ chọn, nó ít được dùng hơn.
                        if ("All Scores".equals(string) || string == null || string.trim().isEmpty()) {
                            return null;
                        }
                        try {
                            return Integer.parseInt(string);
                        } catch (NumberFormatException e) {
                            // Trả về null hoặc ném lỗi tùy theo cách bạn muốn xử lý input sai
                            System.err.println("StringConverter: Could not parse '" + string + "' to Integer.");
                            return null; 
                        }
                    }
                });
                // ---- KẾT THÚC SỬA STRINGCONVERTER ----
                
                scoreFilterComboBox.setValue(null); // Mặc định chọn "All Scores" (tương ứng với giá trị null)
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Score Load Error", "Failed to load review scores for filter: " + e.getMessage());
        }
    }

    private void triggerReviewSearch() {
        currentSearchKeyword = searchReviewField.getText().trim();
        currentScoreFilter = scoreFilterComboBox.getValue(); 
        currentPage = 1;
        loadReviewsForCurrentPage();
    }

    private void loadReviewsForCurrentPage() {
        try {
            totalItems = reviewDAO.countTotalReviews(currentSearchKeyword, currentScoreFilter);
            totalPages = (totalItems == 0) ? 1 : (int) Math.ceil((double) totalItems / pageSize);
            if (currentPage > totalPages && totalPages > 0) currentPage = totalPages;
            if (currentPage < 1) currentPage = 1;

            int offset = (currentPage - 1) * pageSize;
            List<Review> reviewsFromDB = reviewDAO.getPaginatedReviews(offset, pageSize, currentSearchKeyword, currentScoreFilter);
            
            reviewWrapperList.setAll(reviewsFromDB.stream().map(ReviewWrapper::new).collect(Collectors.toList()));
            final TreeItem<ReviewWrapper> root = new RecursiveTreeItem<>(reviewWrapperList, RecursiveTreeObject::getChildren);
            reviewsTable.setRoot(root);
            reviewsTable.setShowRoot(false);

            updatePaginationUI();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Load Reviews Error", "Failed to load reviews: " + e.getMessage());
        }
    }
    
    private void updatePaginationUI() {
        if (pageInfoLabel != null) {
            pageInfoLabel.setText("Page " + currentPage + " of " + totalPages + " (Total: " + totalItems + ")");
        }
        if (totalReviewsCountText != null) {
             totalReviewsCountText.setText(String.valueOf(totalItems));
        }
        if (firstPageButton != null) firstPageButton.setDisable(currentPage <= 1);
        if (prevPageButton != null) prevPageButton.setDisable(currentPage <= 1);
        if (nextPageButton != null) nextPageButton.setDisable(currentPage >= totalPages);
        if (lastPageButton != null) lastPageButton.setDisable(currentPage >= totalPages);
        if (paginationControls != null) {
            paginationControls.setVisible(totalPages > 1 || totalItems > pageSize); 
        }
    }

    @FXML void handleFirstPage(ActionEvent event) { if (currentPage > 1) { currentPage = 1; loadReviewsForCurrentPage(); }}
    @FXML void handlePreviousPage(ActionEvent event) { if (currentPage > 1) { currentPage--; loadReviewsForCurrentPage(); }}
    @FXML void handleNextPage(ActionEvent event) { if (currentPage < totalPages) { currentPage++; loadReviewsForCurrentPage(); }}
    @FXML void handleLastPage(ActionEvent event) { if (currentPage < totalPages) { currentPage = totalPages; loadReviewsForCurrentPage(); }}

    private void openReviewFormDialog(String title, Review reviewToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/OrderReviewFormDialog.fxml"));
            Parent root = loader.load();
            OrderReviewFormDialogController dialogController = loader.getController();
            
            Stage currentStage = getStage();
            dialogController.setParentOwnerStage(currentStage); 
            dialogController.setReviewForEditing(reviewToEdit);

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.initModality(Modality.WINDOW_MODAL);
            if (currentStage != null) stage.initOwner(currentStage);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            dialogController.setCurrentDialogShowingStage(stage);
            stage.showAndWait();

            if (dialogController.wasOkButtonClicked()) {
                loadReviewsForCurrentPage(); 
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Dialog Error", "Could not open the review form: " + e.getMessage());
        }
    }
    
    @FXML void handleAddNewReview(ActionEvent event) { 
        openReviewFormDialog("Add New Review", null); 
    }

    @FXML void handleEditSelectedReview(ActionEvent event) { 
        TreeItem<ReviewWrapper> selectedItem = reviewsTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null || selectedItem.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a review to edit.");
            return;
        }
        Review reviewToEdit = selectedItem.getValue().getOriginalReview();
        openReviewFormDialog("Edit Review", reviewToEdit);
    }

    @FXML
    void handleDeleteSelectedReview(ActionEvent event) {
        TreeItem<ReviewWrapper> selectedItem = reviewsTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null || selectedItem.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a review to delete.");
            return;
        }
        Review reviewToDelete = selectedItem.getValue().getOriginalReview();

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Deletion");
        confirmDialog.setHeaderText("Delete Review: " + reviewToDelete.getReviewId());
        confirmDialog.setContentText("Are you sure you want to permanently delete this review?");
        initOwnerForDialog(confirmDialog);

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (reviewDAO.deleteExistingReview(reviewToDelete.getReviewId())) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Review deleted successfully.");
                    loadReviewsForCurrentPage(); 
                } else {
                    showAlert(Alert.AlertType.ERROR, "Deletion Failed", "Could not delete the review.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Deletion Error", "An error occurred: " + e.getMessage());
            }
        }
    }

    private Stage getStage() {
        if (reviewsTable != null && reviewsTable.getScene() != null) {
            return (Stage) reviewsTable.getScene().getWindow();
        } else if (addNewReviewButton != null && addNewReviewButton.getScene() != null) {
             return (Stage) addNewReviewButton.getScene().getWindow();
        }
        return null;
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

    public static class ReviewWrapper extends RecursiveTreeObject<ReviewWrapper> {
        private final StringProperty reviewId;
        private final StringProperty orderId;
        private final ObjectProperty<Integer> reviewScore; // Đổi thành ObjectProperty để xử lý null tốt hơn
        private final StringProperty reviewComment;
        private final Review originalReview;

        public ReviewWrapper(Review review) {
            this.originalReview = review;
            if (review == null) {
                this.reviewId = new SimpleStringProperty("<Error>");
                this.orderId = new SimpleStringProperty("");
                this.reviewScore = new SimpleObjectProperty<>(null); // Giá trị null cho ObjectProperty
                this.reviewComment = new SimpleStringProperty("");
            } else {
                this.reviewId = new SimpleStringProperty(review.getReviewId());
                this.orderId = new SimpleStringProperty(review.getOrderId() == null ? "" : review.getOrderId());
                this.reviewScore = new SimpleObjectProperty<>(review.getReviewScore()); // Gán trực tiếp Integer
                this.reviewComment = new SimpleStringProperty(review.getReviewComment() == null ? "" : review.getReviewComment());
            }
        }

        public Review getOriginalReview() { return originalReview; }
        public StringProperty reviewIdProperty() { return reviewId; }
        public StringProperty orderIdProperty() { return orderId; }
        public ObjectProperty<Integer> reviewScoreProperty() { return reviewScore; } // Trả về ObjectProperty<Integer>
        public StringProperty reviewCommentProperty() { return reviewComment; }
    }
}