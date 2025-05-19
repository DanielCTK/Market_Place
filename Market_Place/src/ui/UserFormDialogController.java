package ui; // Hoặc package của bạn

import dao.UserDAO;
import impl.UserDAOImpl; // Import lớp triển khai DAO
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable; // Thêm Initializable nếu bạn có logic khởi tạo FXML-independent
import javafx.scene.control.*;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;
import model.User;
import Services.SessionManager;
import com.jfoenix.controls.JFXButton; // Thêm nếu bạn dùng JFoenix cho nút
import java.net.URL; // Cần cho Initializable
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle; // Cần cho Initializable

public class UserFormDialogController implements Initializable { // Triển khai Initializable

    public enum DialogMode { ADD, EDIT }
    private DialogMode mode;

    @FXML private Label dialogTitleLabel;
    @FXML private TextField usernameField;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private JFXButton saveButton;
    @FXML private JFXButton cancelButton; 
    // Nút Cancel đã có onAction="#handleCancel" trong FXML, không cần @FXML field trừ khi muốn truy cập từ code

    // Các fx:id này phải có trong FXML để điều khiển visibility/managed state
    @FXML private RowConstraints passwordRow;
    @FXML private RowConstraints confirmPasswordRow;
    @FXML private Label passwordLabel;
    @FXML private Label confirmPasswordLabel;

    private Stage dialogStage;
    private User userToEdit;
    private boolean okClicked = false;
    private UserDAO userDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userDAO = new UserDAOImpl(); // Khởi tạo DAO

        List<String> roles = new ArrayList<>();
        roles.add("CUSTOMER");
        roles.add("SELLER");

        User currentUser = SessionManager.getInstance().getCurrentUser();
        // Chỉ Admin mới có thể gán vai trò ADMIN cho người khác
        if (currentUser != null && "ADMIN".equals(currentUser.getRole())) {
            roles.add("ADMIN");
        }
        roleComboBox.setItems(FXCollections.observableArrayList(roles));
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setDialogMode(DialogMode mode) {
        this.mode = mode;
        if (mode == DialogMode.ADD) {
            dialogTitleLabel.setText("Add New User");
            usernameField.setEditable(true);
            usernameField.setStyle(""); // Reset style
            passwordField.setPromptText("Required (min 6 chars)");
            confirmPasswordField.setPromptText("Required");
            setPasswordFieldVisibility(true); // Luôn hiển thị khi thêm mới
            clearForm(); // Xóa form khi thêm mới
        } else { // EDIT mode
            dialogTitleLabel.setText("Edit User Details");
            usernameField.setEditable(false); // Không cho sửa username
            usernameField.setStyle("-fx-control-inner-background: #e0e0e0; -fx-opacity: 0.7;");
            passwordField.setPromptText("Leave blank to keep current");
            confirmPasswordField.setPromptText("Leave blank to keep current");
            setPasswordFieldVisibility(true); // Vẫn hiện trường password, nhưng không bắt buộc
        }
    }
    
    private void setPasswordFieldVisibility(boolean visible) {
        // Đảm bảo các FXML components này đã được inject (có fx:id trong FXML)
        if (passwordLabel != null) passwordLabel.setVisible(visible);
        if (passwordField != null) passwordField.setVisible(visible);
        if (confirmPasswordLabel != null) confirmPasswordLabel.setVisible(visible);
        if (confirmPasswordField != null) confirmPasswordField.setVisible(visible);

        if (passwordLabel != null) passwordLabel.setManaged(visible);
        if (passwordField != null) passwordField.setManaged(visible);
        if (confirmPasswordLabel != null) confirmPasswordLabel.setManaged(visible);
        if (confirmPasswordField != null) confirmPasswordField.setManaged(visible);

        // Điều chỉnh chiều cao của row nếu cần (nếu ban đầu đặt là 0 và muốn ẩn hoàn toàn)
        if (passwordRow != null) passwordRow.setPrefHeight(visible ? 35.0 : 0); // Sử dụng giá trị từ FXML
        if (confirmPasswordRow != null) confirmPasswordRow.setPrefHeight(visible ? 35.0 : 0);
    }

    public void setUserToEdit(User user) {
        this.userToEdit = user;
        if (user != null) {
            usernameField.setText(user.getUsername());
            fullNameField.setText(user.getFullName());
            emailField.setText(user.getEmail());
            roleComboBox.setValue(user.getRole());
            // Không điền password cũ vào form
            passwordField.clear();
            confirmPasswordField.clear();
        } else {
            clearForm(); // Nếu user là null (dù trường hợp này ít khi xảy ra với EDIT mode)
        }
    }
    
    private void clearForm() {
        usernameField.clear();
        fullNameField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        if (!roleComboBox.getItems().isEmpty()) {
            roleComboBox.getSelectionModel().selectFirst(); // Hoặc clearSelection()
        }
    }


    public boolean isOkClicked() {
        return okClicked;
    }

    @FXML
    private void handleSave(ActionEvent event) throws SQLException {
        if (isInputValid()) {
            boolean success = false;
            String operation = "";

            try {
                if (mode == DialogMode.ADD) {
                    operation = "add";
                    User newUser = new User(
                        usernameField.getText().trim(),
                        passwordField.getText(), // Mật khẩu thô, DAO sẽ hash
                        fullNameField.getText().trim(),
                        emailField.getText().trim(),
                        roleComboBox.getValue()
                    );
                    success = userDAO.addUser(newUser);
                    if (!success) {
                        // DAO nên trả về lỗi cụ thể hoặc log, đây chỉ là phỏng đoán
                        if (userDAO.findByUsername(newUser.getUsername()).isPresent()) {
                            showAlert(Alert.AlertType.ERROR, "Save Error", "Username '" + newUser.getUsername() + "' already exists.");
                        } else if (userDAO.emailExists(newUser.getEmail(), 0)) {
                            showAlert(Alert.AlertType.ERROR, "Save Error", "Email '" + newUser.getEmail() + "' already exists.");
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Save Error", "Failed to add user. Please check application logs.");
                        }
                    }
                } else if (mode == DialogMode.EDIT && userToEdit != null) {
                    operation = "update";
                    userToEdit.setFullName(fullNameField.getText().trim());
                    userToEdit.setEmail(emailField.getText().trim());

                    String newRole = roleComboBox.getValue();
                    User currentUser = SessionManager.getInstance().getCurrentUser();
                    // Ngăn Admin tự hạ vai trò của chính mình hoặc sửa vai trò của Admin khác thành non-Admin
                    if (currentUser != null && "ADMIN".equals(userToEdit.getRole()) && !"ADMIN".equals(newRole) && currentUser.getId() == userToEdit.getId()){
                         showAlert(Alert.AlertType.ERROR, "Role Change Denied", "Admin cannot change their own role to a non-Admin level.");
                         return;
                    }
                    // Logic bổ sung: Admin không được thay đổi vai trò của một Admin khác thành non-Admin
                    // (Trừ khi đó là chính sách của bạn)
                    if (currentUser != null && "ADMIN".equals(currentUser.getRole()) &&
                        "ADMIN".equals(userToEdit.getRole()) && !"ADMIN".equals(newRole) &&
                        currentUser.getId() != userToEdit.getId()) {
                        // Hỏi xác nhận nếu muốn cho phép
                        // showAlert(Alert.AlertType.WARNING, "Role Change", "Are you sure you want to change another Admin's role to non-Admin?");
                    }

                    userToEdit.setRole(newRole);

                    String newPassword = passwordField.getText();
                    if (newPassword != null && !newPassword.isEmpty()) {
                        userToEdit.setPassword(newPassword); // DAO sẽ hash
                        success = userDAO.updateUserWithPassword(userToEdit);
                    } else {
                        success = userDAO.updateUser(userToEdit); // Không đổi mật khẩu
                    }
                    if (!success) {
                         if (userDAO.emailExists(userToEdit.getEmail(), userToEdit.getId())) {
                             showAlert(Alert.AlertType.ERROR, "Save Error", "Email '" + userToEdit.getEmail() + "' already exists for another user.");
                         } else {
                            showAlert(Alert.AlertType.ERROR, "Save Error", "Failed to update user. Please check application logs.");
                         }
                    }
                }

                if (success) {
                    okClicked = true;
                    dialogStage.close();
                }
            } catch (Exception e) { // Bắt lỗi chung từ DAO hoặc logic khác
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Operation Failed", "An unexpected error occurred during user " + operation + ": " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        dialogStage.close();
    }

    private boolean isInputValid() throws SQLException {
        StringBuilder errorMessage = new StringBuilder();

        String username = usernameField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText(); // Không trim password
        String confirmPassword = confirmPasswordField.getText();
        String role = roleComboBox.getValue();

        // Validate Username
        if (username.isEmpty()) {
            errorMessage.append("Username is required.\n");
        } else if (username.length() < 3 || username.length() > 50) {
             errorMessage.append("Username must be between 3 and 50 characters.\n");
        } else if (!username.matches("^[a-zA-Z0-9_.-]+$")) {
            errorMessage.append("Username can only contain letters, numbers, underscore, dot, and hyphen.\n");
        } else if (mode == DialogMode.ADD && userDAO.findByUsername(username).isPresent()) {
            errorMessage.append("Username '").append(username).append("' already exists.\n");
        }

        // Validate Full Name
        if (fullName.isEmpty()) {
            errorMessage.append("Full Name is required.\n");
        } else if (fullName.length() > 100) {
            errorMessage.append("Full Name cannot exceed 100 characters.\n");
        }

        // Validate Email
        if (email.isEmpty()) {
            errorMessage.append("Email is required.\n");
        } else if (!email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) { // Basic email regex
            errorMessage.append("Invalid email format.\n");
        } else if (email.length() > 100) {
            errorMessage.append("Email cannot exceed 100 characters.\n");
        } else {
             if (mode == DialogMode.ADD && userDAO.emailExists(email, 0)) { // 0 for new user ID
                 errorMessage.append("Email '").append(email).append("' already exists.\n");
             } else if (mode == DialogMode.EDIT && userToEdit != null && !email.equalsIgnoreCase(userToEdit.getEmail()) && userDAO.emailExists(email, userToEdit.getId())) {
                 errorMessage.append("Email '").append(email).append("' already exists for another user.\n");
             }
        }

        // Validate Password
        if (mode == DialogMode.ADD) { // Password bắt buộc khi thêm mới
            if (password.isEmpty()) {
                errorMessage.append("Password is required for new users.\n");
            } else if (password.length() < 6 || password.length() > 255) { // 255 là ví dụ, tùy thuộc vào DB
                errorMessage.append("Password must be between 6 and 255 characters.\n");
            } else if (!password.equals(confirmPassword)) {
                errorMessage.append("Passwords do not match.\n");
            }
        } else { // Chế độ EDIT, password là tùy chọn
            if (!password.isEmpty()) { // Nếu người dùng nhập mật khẩu mới
                 if (password.length() < 6 || password.length() > 255) {
                    errorMessage.append("New password must be between 6 and 255 characters.\n");
                } else if (!password.equals(confirmPassword)) {
                    errorMessage.append("New passwords do not match.\n");
                }
            }
        }

        // Validate Role
        if (role == null || role.isEmpty()) {
            errorMessage.append("Role is required.\n");
        }

        if (errorMessage.length() == 0) {
            return true;
        } else {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", errorMessage.toString());
            return false;
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (dialogStage != null) { // Đảm bảo dialogStage đã được set
            alert.initOwner(dialogStage);
        }
        alert.showAndWait();
    }
}