package ui;

import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import dao.UserDAO;
import impl.UserDAOImpl;
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
import javafx.scene.image.ImageView; // Giữ lại nếu bạn thực sự dùng searchIconImageView
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.User;
import Services.SessionManager; // Đảm bảo viết hoa đúng tên package

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException; // Nên bắt SQLException cụ thể
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class UserManagementController implements Initializable {

    @FXML private JFXTreeTableView<UserWrapper> usersTable;
    @FXML private JFXTreeTableColumn<UserWrapper, Integer> idColumn;
    @FXML private JFXTreeTableColumn<UserWrapper, String> usernameColumn;
    @FXML private JFXTreeTableColumn<UserWrapper, String> fullNameColumn;
    @FXML private JFXTreeTableColumn<UserWrapper, String> emailColumn;
    @FXML private JFXTreeTableColumn<UserWrapper, String> roleColumn;
    @FXML private JFXTreeTableColumn<UserWrapper, String> createdAtColumnFormatted; // Đổi tên để rõ ràng là cột đã format

    @FXML private JFXButton addButton;
    @FXML private JFXButton editButton;
    @FXML private JFXButton deleteButton;
    
    @FXML private TextField searchField;
    @FXML private ImageView searchIconImageView; 

    @FXML private Text greetingText;
    @FXML private Text totalUserCountText;

    @FXML private HBox paginationControls;
    @FXML private JFXButton firstPageButton;
    @FXML private JFXButton prevPageButton;
    @FXML private Label pageInfoLabel;
    @FXML private JFXButton nextPageButton;
    @FXML private JFXButton lastPageButton;

    private UserDAO userDAO;
    private ObservableList<UserWrapper> userWrapperList;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); // Thêm giây cho chính xác

    private int currentPage = 1;
    private final int pageSize = 10; 
    private int totalItems = 0;
    private int totalPages = 1;
    private String currentSearchKeyword = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("UserManagementController: initialize() CALLED. Location: " + (location != null ? location.getPath() : "null"));
        userDAO = new UserDAOImpl();
        userWrapperList = FXCollections.observableArrayList();

        setupTableColumns();
        configureButtonAccess(); // Gọi sau khi userDAO được khởi tạo và trước khi load data
        updateGreetingText(); 
        
        // Kiểm tra null cho các control trước khi gán sự kiện
        if (searchIconImageView != null) {
            searchIconImageView.setOnMouseClicked(event -> triggerSearch());
        } else {
            System.err.println("UserManagementController: searchIconImageView IS NULL! Check FXML.");
        }

        if (searchField != null) {
            searchField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    triggerSearch();
                }
            });
        } else {
            System.err.println("UserManagementController: searchField IS NULL! Check FXML.");
        }

        loadUsersForCurrentPage(); // Tải dữ liệu lần đầu
        System.out.println("UserManagementController: initialize() FINISHED.");
    }
    
    private void triggerSearch() {
        if (searchField == null) {
            System.err.println("triggerSearch: searchField is NULL.");
            return;
        }
        currentSearchKeyword = searchField.getText().trim();
        currentPage = 1; 
        loadUsersForCurrentPage();
    }

    private void updateGreetingText() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (greetingText != null) { 
            if (currentUser != null && currentUser.getUsername() != null) {
                greetingText.setText("Hi, " + currentUser.getUsername() + "!");
            } else {
                greetingText.setText("Hi, Guest!");
            }
        } else {
            System.err.println("UserManagementController: greetingText IS NULL! Check FXML.");
        }
    }

    private void updateTotalUserCountDisplay() {
        if (totalUserCountText != null) { 
            totalUserCountText.setText(String.valueOf(totalItems));
        } else {
             System.err.println("UserManagementController: totalUserCountText IS NULL! Check FXML.");
        }
    }

    private void setupTableColumns() {
        // Đảm bảo các Property trả về đúng kiểu cho cột
        idColumn.setCellValueFactory(param -> param.getValue().getValue().idProperty().asObject());
        usernameColumn.setCellValueFactory(param -> param.getValue().getValue().usernameProperty());
        fullNameColumn.setCellValueFactory(param -> param.getValue().getValue().fullNameProperty());
        emailColumn.setCellValueFactory(param -> param.getValue().getValue().emailProperty());
        roleColumn.setCellValueFactory(param -> param.getValue().getValue().roleProperty());
        
        // Cột createdAtColumnFormatted sẽ hiển thị String đã được format
        createdAtColumnFormatted.setCellValueFactory(param -> {
            Timestamp ts = param.getValue().getValue().createdAtProperty().get();
            return new SimpleStringProperty(ts != null ? sdf.format(ts) : "N/A");
        });
    }

    private void loadUsersForCurrentPage() {
        System.out.println("--- loadUsersForCurrentPage() CALLED --- Page: " + currentPage + ", Keyword: '" + currentSearchKeyword + "'");
        try {
            totalItems = userDAO.getTotalUserCount(currentSearchKeyword);
            updateTotalUserCountDisplay();

            totalPages = (totalItems == 0) ? 1 : (int) Math.ceil((double) totalItems / pageSize);
            
            if (currentPage > totalPages) currentPage = totalPages > 0 ? totalPages : 1;
            if (currentPage < 1) currentPage = 1;

            int offset = (currentPage - 1) * pageSize;
            List<User> usersFromDB = userDAO.getUsers(offset, pageSize, currentSearchKeyword);

            userWrapperList.setAll(
                    usersFromDB.stream().map(UserWrapper::new).collect(Collectors.toList())
            );

            final TreeItem<UserWrapper> root = new RecursiveTreeItem<>(userWrapperList, RecursiveTreeObject::getChildren);
            usersTable.setRoot(root);
            usersTable.setShowRoot(false);

            System.out.println("Displayed page " + currentPage + "/" + totalPages + ". Items: " + userWrapperList.size() + ". Total DB: " + totalItems);
            updatePaginationUI();

        } catch (Exception e) { // Bắt các lỗi khác
            System.err.println("Unexpected error in loadUsersForCurrentPage: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Unexpected Error", "An unexpected error occurred: " + e.getMessage());
        }
        System.out.println("--- loadUsersForCurrentPage() FINISHED ---");
    }
    
    private void updatePaginationUI() {
        if (pageInfoLabel != null) {
            pageInfoLabel.setText("Page " + currentPage + " of " + totalPages);
        }
        if (firstPageButton != null) firstPageButton.setDisable(currentPage <= 1);
        if (prevPageButton != null) prevPageButton.setDisable(currentPage <= 1);
        if (nextPageButton != null) nextPageButton.setDisable(currentPage >= totalPages || totalItems == 0);
        if (lastPageButton != null) lastPageButton.setDisable(currentPage >= totalPages || totalItems == 0);
        
        if (paginationControls != null) {
             boolean shouldBeVisible = totalItems > 0 && totalPages > 1;
             paginationControls.setVisible(shouldBeVisible);
             paginationControls.setManaged(shouldBeVisible);
        }
    }

    @FXML void handleFirstPage(ActionEvent event) { if (currentPage > 1) { currentPage = 1; loadUsersForCurrentPage(); }}
    @FXML void handlePreviousPage(ActionEvent event) { if (currentPage > 1) { currentPage--; loadUsersForCurrentPage(); }}
    @FXML void handleNextPage(ActionEvent event) { if (currentPage < totalPages) { currentPage++; loadUsersForCurrentPage(); }}
    @FXML void handleLastPage(ActionEvent event) { if (currentPage < totalPages) { currentPage = totalPages; loadUsersForCurrentPage(); }}

    private void configureButtonAccess() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        boolean isAdmin = currentUser != null && "ADMIN".equals(currentUser.getRole());

        System.out.println("UserManagementController: configureButtonAccess - isAdmin: " + isAdmin);

        if (addButton != null) addButton.setDisable(!isAdmin); else System.err.println("addButton is NULL!");
        if (editButton != null) editButton.setDisable(!isAdmin); else System.err.println("editButton is NULL!");
        if (deleteButton != null) deleteButton.setDisable(!isAdmin); else System.err.println("deleteButton is NULL!");
    }

    private void showUserFormDialog(String title, UserFormDialogController.DialogMode mode, User userToEdit) {
        try {
            // Đảm bảo đường dẫn FXML chính xác và file tồn tại trong resources/ui
            URL fxmlUrl = getClass().getResource("/ui/UserFormDialog.fxml");
            if (fxmlUrl == null) {
                showAlert(Alert.AlertType.ERROR, "Resource Error", "Cannot find UserFormDialog.fxml. Check path.");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            UserFormDialogController dialogController = loader.getController();
            if (dialogController == null) {
                 showAlert(Alert.AlertType.ERROR, "Controller Error", "Could not get controller for UserFormDialog.");
                 return;
            }
            
            dialogController.setDialogMode(mode);
            if (mode == UserFormDialogController.DialogMode.EDIT && userToEdit != null) {
                dialogController.setUserToEdit(userToEdit);
            }

            Stage dialogStage = new Stage();
            dialogStage.setTitle(title);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            
            // Cố gắng lấy owner stage một cách an toàn hơn
            Stage ownerStage = getStageFromAvailableNode();
            if (ownerStage != null) {
                 dialogStage.initOwner(ownerStage);
            } else {
                System.err.println("Owner stage is null for dialog: " + title);
            }

            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);

            dialogController.setDialogStage(dialogStage); // Truyền stage của dialog cho controller của nó
            dialogStage.showAndWait();

            if (dialogController.isOkClicked()) {
                loadUsersForCurrentPage(); 
                showAlert(Alert.AlertType.INFORMATION, "Success",
                        mode == UserFormDialogController.DialogMode.ADD ? "User added successfully!" : "User updated successfully!");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Dialog Load Error", "Could not load the User form: " + e.getMessage());
        }
    }
    
    private Stage getStageFromAvailableNode() {
        if (usersTable != null && usersTable.getScene() != null) return (Stage) usersTable.getScene().getWindow();
        if (addButton != null && addButton.getScene() != null) return (Stage) addButton.getScene().getWindow();
        // Thêm các node khác nếu cần
        return null;
    }

    @FXML
    void handleAddNewUser(ActionEvent event) {
        showUserFormDialog("Add New User", UserFormDialogController.DialogMode.ADD, null);
    }

    @FXML
    void handleEditUser(ActionEvent event) {
        TreeItem<UserWrapper> selectedTreeItem = usersTable.getSelectionModel().getSelectedItem();
        if (selectedTreeItem == null || selectedTreeItem.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a user to edit.");
            return;
        }
        User selectedUser = selectedTreeItem.getValue().getOriginalUser();
        showUserFormDialog("Edit User: " + selectedUser.getUsername(), UserFormDialogController.DialogMode.EDIT, selectedUser);
    }

    @FXML
    void handleDeleteUser(ActionEvent event) {
        TreeItem<UserWrapper> selectedTreeItem = usersTable.getSelectionModel().getSelectedItem();
        if (selectedTreeItem == null || selectedTreeItem.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a user to delete.");
            return;
        }
        User selectedUser = selectedTreeItem.getValue().getOriginalUser();

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getId() == selectedUser.getId()) {
             showAlert(Alert.AlertType.ERROR, "Action Denied", "You cannot delete your own account.");
             return;
        }
        // Cân nhắc bỏ điều kiện cấm xóa ADMIN ở đây nếu bạn là ADMIN duy nhất và cần test
        // if ("ADMIN".equals(selectedUser.getRole())) {
        //     showAlert(Alert.AlertType.WARNING, "Deletion Policy", "Admin accounts cannot be deleted directly.");
        //     return;
        // }

        Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationDialog.setTitle("Confirm Deletion");
        confirmationDialog.setHeaderText("Delete user: " + selectedUser.getUsername() + " (ID: " + selectedUser.getId() + ")");
        confirmationDialog.setContentText("Are you sure you want to permanently delete this user?");
        initOwnerForDialog(confirmationDialog);

        Optional<ButtonType> result = confirmationDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (userDAO.deleteUser(selectedUser.getId())) {
                    loadUsersForCurrentPage(); 
                    showAlert(Alert.AlertType.INFORMATION, "Success", "User deleted successfully.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Deletion Failed", "Could not delete the user.");
                }
            } catch (Exception e) { // Bắt các lỗi khác
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred: " + e.getMessage());
            }
        }
    }
    
    private void initOwnerForDialog(Alert dialog) {
        Stage owner = getStageFromAvailableNode();
        if (owner != null) {
            dialog.initOwner(owner);
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

    // UserWrapper class giữ nguyên như bạn đã cung cấp
    public static class UserWrapper extends RecursiveTreeObject<UserWrapper> {
        private final User originalUser;
        final SimpleIntegerProperty id;
        final SimpleStringProperty username;
        final SimpleStringProperty fullName;
        final SimpleStringProperty email;
        final SimpleStringProperty role;
        final SimpleObjectProperty<Timestamp> createdAt;

        public UserWrapper(User user) {
            this.originalUser = user;
            this.id = new SimpleIntegerProperty(user.getId());
            this.username = new SimpleStringProperty(user.getUsername());
            this.fullName = new SimpleStringProperty(user.getFullName());
            this.email = new SimpleStringProperty(user.getEmail());
            this.role = new SimpleStringProperty(user.getRole());
            this.createdAt = new SimpleObjectProperty<>(user.getCreatedAt());
        }
        public User getOriginalUser() { return originalUser; }
        public SimpleIntegerProperty idProperty() { return id; }
        public SimpleStringProperty usernameProperty() { return username; }
        public SimpleStringProperty fullNameProperty() { return fullName; }
        public SimpleStringProperty emailProperty() { return email; }
        public SimpleStringProperty roleProperty() { return role; }
        public SimpleObjectProperty<Timestamp> createdAtProperty() { return createdAt; }
        
        // Override equals và hashCode để JFXTreeTableView hoạt động chính xác khi cập nhật item
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UserWrapper that = (UserWrapper) o;
            // So sánh dựa trên ID của User gốc
            return originalUser != null && that.originalUser != null &&
                   originalUser.getId() == that.originalUser.getId();
        }
        @Override
        public int hashCode() {
            return originalUser != null ? Integer.hashCode(originalUser.getId()) : 0;
        }
    }
}