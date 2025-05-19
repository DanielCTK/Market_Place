package ui;

import com.jfoenix.controls.JFXPasswordField;
import dao.UserDAO;
import impl.UserDAOImpl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import model.User;
import utils.PasswordHasher; // Your password hashing utility

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ChangePasswordDialogController implements Initializable {

    @FXML private JFXPasswordField currentPasswordField;
    @FXML private JFXPasswordField newPasswordField;
    @FXML private JFXPasswordField confirmNewPasswordField;
    @FXML private Label errorLabel;

    private Stage dialogStage;
    private User currentUser; // The user whose password is to be changed
    private UserDAO userDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userDAO = new UserDAOImpl();
        errorLabel.setText(""); // Clear any initial error message
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user == null) {
            errorLabel.setText("Error: User information is missing.");
            disableFields();
        }
    }

    private void disableFields() {
        currentPasswordField.setDisable(true);
        newPasswordField.setDisable(true);
        confirmNewPasswordField.setDisable(true);
        // saveButton.setDisable(true); // Assuming fx:id="saveButton"
    }

    @FXML
    void handleSavePassword(ActionEvent event) {
        errorLabel.setText(""); // Clear previous errors

        if (currentUser == null) {
            errorLabel.setText("Cannot change password. User data is missing.");
            return;
        }

        String currentPasswordText = currentPasswordField.getText();
        String newPasswordText = newPasswordField.getText();
        String confirmNewPasswordText = confirmNewPasswordField.getText();

        if (currentPasswordText.isEmpty() || newPasswordText.isEmpty() || confirmNewPasswordText.isEmpty()) {
            errorLabel.setText("All fields are required.");
            return;
        }

        if (!newPasswordText.equals(confirmNewPasswordText)) {
            errorLabel.setText("New password and confirmation do not match.");
            return;
        }

        if (newPasswordText.length() < 6) { // Basic password strength check
            errorLabel.setText("New password must be at least 6 characters long.");
            return;
        }
        
        if (newPasswordText.equals(currentPasswordText)) {
            errorLabel.setText("New password cannot be the same as the current password.");
            return;
        }

        try {
            // The currentUser object passed to this dialog might be stale.
            // It's safer to re-fetch the user from DB to get the latest hashed password.
            // However, currentUser.getPassword() should already contain the HASHED password
            // if it was fetched correctly in ViewProfileController.
            
            // Let's assume currentUser.getPassword() is the HASHED password.
            if (!PasswordHasher.checkPassword(currentPasswordText, currentUser.getPassword())) {
                errorLabel.setText("Current password is incorrect.");
                return;
            }

            // Hash the new password
            String newHashedPassword = PasswordHasher.hashPassword(newPasswordText);
            if (newHashedPassword == null) {
                errorLabel.setText("Error hashing new password.");
                return;
            }

            // Update password in the database using the newHashedPassword
            boolean success = userDAO.updatePassword(currentUser.getId(), newHashedPassword);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Password changed successfully.");
                if (dialogStage != null) {
                    dialogStage.close();
                }
            } else {
                errorLabel.setText("Failed to update password. Please try again.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            errorLabel.setText("Database error: " + e.getMessage());
        }
    }

    @FXML
    void handleCancel(ActionEvent event) {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (dialogStage != null) {
             alert.initOwner(dialogStage);
        }
        alert.showAndWait();
    }
}