package ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextInputControl; // Ensure this import is present
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import model.User;
import javafx.scene.Node;
import java.util.Optional;

import com.jfoenix.controls.JFXButton; // Import for JFXButton if used

import Services.AuthService;
import Services.SessionManager;

public class LoginController {
    // Ensure the type of txtUsername matches your FXML (TextField or JFXTextField)
    @FXML private TextInputControl txtUsername; // TextInputControl covers both TextField and JFXTextField
    @FXML private PasswordField txtPassword;    // Standard PasswordField or JFXPasswordField if used
    @FXML private JFXButton btnGoToSignUp;    // Ensure this fx:id exists in your FXML
    @FXML private JFXButton btnLogin;         // Ensure this type matches your FXML (JFXButton or Button)
    private AuthService authService;

    public LoginController() {
        this.authService = new AuthService();
    }

    @FXML
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();
        System.out.println("[LoginController] Username from TextField (trimmed): [" + username + "]");
        System.out.println("[LoginController] Password from PasswordField: [" + password + "]");

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Login Error", "Username and password cannot be empty.");
            return;
        }

        Optional<User> userOptional = authService.login(username, password);

        if (userOptional.isPresent()) {
            User loggedInUser = userOptional.get();
            System.out.println("DEBUG LoginController: Attempting to set user in session. User: " + loggedInUser.getUsername() + ", Role: " + loggedInUser.getRole());
            SessionManager.getInstance().setCurrentUser(loggedInUser);

            User userInSessionAfterSet = SessionManager.getInstance().getCurrentUser();
            if (userInSessionAfterSet != null) {
                System.out.println("DEBUG LoginController: SUCCESSFULLY set user in session. Username: " + userInSessionAfterSet.getUsername() + ", Role: " + userInSessionAfterSet.getRole());
            } else {
                System.err.println("ERROR LoginController: User is NULL in session IMMEDIATELY AFTER setting!");
            }

            // OPTIONAL: Show successful login alert
            // showAlert(Alert.AlertType.INFORMATION, "Login Successful", "Welcome, " + loggedInUser.getUsername() + "!");
            // Usually not needed as the screen will change immediately.

            Stage stage = (Stage) btnLogin.getScene().getWindow();
            System.out.println("DEBUG LoginController: Closing login stage.");
            stage.close();

            try {
                 System.out.println("DEBUG LoginController: Calling Launcher.showMainScreen().");
                 Launcher.showMainScreen(); // Assuming Launcher handles this
                 System.out.println("DEBUG LoginController: Launcher.showMainScreen() finished.");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Application Error", "Could not open the main screen: " + e.getMessage());
            }
        } else {
            // SHOW LOGIN FAILED ALERT
            System.out.println("DEBUG LoginController: Login failed for username: [" + username + "]");
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Incorrect username or password. Please try again.");
            txtPassword.clear(); // Clear the password field for the user to re-enter
        }
    }

    @FXML
    private void handleSignUp() {
        System.out.println("Sign Up button pressed. Opening registration screen.");
        try {
            // Ensure the path to Registration.fxml is correct
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/Registration.fxml"));
            Parent registrationRoot = loader.load();

            Stage registrationStage = new Stage();
            registrationStage.setTitle("Create New Account");
            registrationStage.setScene(new Scene(registrationRoot));

            // Set the registration window as modal relative to the login window
            registrationStage.initModality(Modality.WINDOW_MODAL);
            Window currentWindow = btnGoToSignUp.getScene().getWindow(); // Get the current (Login) window
            registrationStage.initOwner(currentWindow);

            registrationStage.setResizable(false); // Optional: prevent resizing
            registrationStage.showAndWait(); // Show and wait until it's closed

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Application Error", "Could not open the registration screen.\n" + e.getMessage());
        }
    }

    @FXML
    private void handleForgotPassword() {
        System.out.println("Forgot Password button pressed. Needs to open forgot password screen.");
        // Temporarily show a "feature not available" message
        showAlert(Alert.AlertType.INFORMATION, "Feature Coming Soon", "The 'Forgot Password' functionality is currently under development.");
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null); // No header text for a cleaner look
        alert.setContentText(message);

        // Try to get the current window to set as the owner for the Alert
        Node nodeForAlertOwner = btnLogin; // Prefer the login button
        if (nodeForAlertOwner == null || nodeForAlertOwner.getScene() == null || nodeForAlertOwner.getScene().getWindow() == null) {
            nodeForAlertOwner = txtUsername; // Fallback to the username field
        }
        if (nodeForAlertOwner != null && nodeForAlertOwner.getScene() != null && nodeForAlertOwner.getScene().getWindow() != null) {
             alert.initOwner(nodeForAlertOwner.getScene().getWindow());
        }
        alert.showAndWait();
    }
}