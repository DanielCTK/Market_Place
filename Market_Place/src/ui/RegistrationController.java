package ui;

import Services.AuthService;
import utils.PasswordHasher; // Mặc dù không dùng trực tiếp ở đây nhưng AuthService sẽ dùng

import com.jfoenix.controls.JFXButton; // Đảm bảo bạn đã có thư viện JFoenix
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;

public class RegistrationController {

    @FXML private TextField txtUsernameReg;
    @FXML private TextField txtEmailReg;
    @FXML private PasswordField txtPasswordReg;
    @FXML private PasswordField txtConfirmPasswordReg;
    @FXML private JFXButton btnRegister; // Nút "Đăng Ký"
    @FXML private JFXButton btnBackToLogin; // Nút "Quay lại Đăng nhập"
    @FXML private TextField txtFullnameReg; // Trường tên đầy đủ
    private AuthService authService;

    public RegistrationController() {
        this.authService = new AuthService(); // Khởi tạo AuthService
    }

    @FXML
    private void initialize() {
        // Có thể thêm các cài đặt ban đầu nếu cần
    }

    @FXML
    private void handleRegister() {
        String username = txtUsernameReg.getText().trim();
        String email = txtEmailReg.getText().trim();
        String password = txtPasswordReg.getText();
        String confirmPassword = txtConfirmPasswordReg.getText();
        String fullname = txtFullnameReg.getText().trim();
        
        if (fullname.isEmpty()) { // Kiểm tra này là đủ nếu txtFullnameReg.getText() không bao giờ là null
            showAlert(Alert.AlertType.ERROR, "Registration Error", "Full Name cannot be empty. Please fill in all fields.");
            txtFullnameReg.requestFocus(); // Đặt focus vào trường fullname
            return;
        }
        // Tiếp tục kiểm tra các trường khác...
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", "Please fill in all fields.");
            // Có thể đặt focus vào trường trống đầu tiên
            return;
        }
        
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", "Please fill in all fields.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", "Password and authentication password do not match.");
            txtPasswordReg.clear();
            txtConfirmPasswordReg.clear();
            txtPasswordReg.requestFocus();
            return;
        }

        // Kiểm tra định dạng email đơn giản
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", "Invalid email format. Please enter a valid email.");
            txtEmailReg.requestFocus();
            return;
        }

        // Gọi AuthService để đăng ký
        // AuthService.registerUser nên trả về một thông điệp hoặc mã lỗi cụ thể hơn
        AuthService.RegistrationResult result = authService.registerUser(username, email, password);

        switch (result) {
            case SUCCESS:
                showAlert(Alert.AlertType.INFORMATION, "Success", "Account registration successful! You can login now.");
                closeWindow();
                break;
            case USERNAME_EXISTS:
                showAlert(Alert.AlertType.ERROR, "Registration Error", "Username already exists. Please choose another one.");
                txtUsernameReg.requestFocus();
                break;
            case EMAIL_EXISTS:
                showAlert(Alert.AlertType.ERROR, "Registration Error", "Email is already in use. Please choose another email.");
                txtEmailReg.requestFocus();
                break;
            case WEAK_PASSWORD: // Nếu bạn có kiểm tra độ mạnh mật khẩu trong AuthService
                 showAlert(Alert.AlertType.ERROR, "Registration Error", "Password is too weak. Please choose a stronger password.");
                 txtPasswordReg.requestFocus();
                 break;
            case DB_ERROR:
            default:
                showAlert(Alert.AlertType.ERROR, "Registration Error", "Unable to register account due to system error. Please try again later.");
                break;
        }
    }

    @FXML
    private void handleBackToLogin() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) btnRegister.getScene().getWindow(); // Hoặc btnBackToLogin
        stage.close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null); // Không hiển thị header text
        alert.setContentText(message);
        // Đặt owner cho Alert để nó hiển thị đúng vị trí và modal với cửa sổ hiện tại
        Window window = btnRegister.getScene().getWindow();
        alert.initOwner(window);
        alert.showAndWait();
    }
}