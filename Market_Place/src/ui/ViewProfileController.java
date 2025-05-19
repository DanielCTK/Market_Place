package ui;

import dao.UserDAO;
import impl.UserDAOImpl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.User;
import Services.SessionManager; // Assuming this is your SessionManager package

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.ResourceBundle;

public class ViewProfileController implements Initializable {

    @FXML private Label usernameLabel;
    @FXML private Label fullNameLabel;
    @FXML private Label emailLabel;
    @FXML private Label roleLabel;
    @FXML private Label createdAtLabel;
    @FXML private Label updatedAtLabel;
    // @FXML private JFXButton changePasswordButton; // Not strictly needed if only action is used

    private UserDAO userDAO;
    private User currentUserFromDB; // Store the user fetched from DB
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userDAO = new UserDAOImpl();
        loadUserProfile();
    }

    private void loadUserProfile() {
        String loggedInUsername = SessionManager.getInstance().getCurrentUsername();
        if (loggedInUsername == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Logged-in user information not found.");
            // Optionally, disable the panel or navigate away
            setFieldsToNotAvailable();
            if (usernameLabel != null && usernameLabel.getScene() != null) {
                 // Example: ((Stage) usernameLabel.getScene().getWindow()).close();
                 // Or disable change password button
                 // changePasswordButton.setDisable(true);
            }
            return;
        }

        try {
            // Use findByUsername which returns Optional<User>
            Optional<User> userOpt = userDAO.findByUsername(loggedInUsername);
            if (userOpt.isPresent()) {
                currentUserFromDB = userOpt.get();
                usernameLabel.setText(currentUserFromDB.getUsername());
                fullNameLabel.setText(currentUserFromDB.getFullName() != null ? currentUserFromDB.getFullName() : "Not set");
                emailLabel.setText(currentUserFromDB.getEmail() != null ? currentUserFromDB.getEmail() : "Not set");
                roleLabel.setText(currentUserFromDB.getRole());
                createdAtLabel.setText(currentUserFromDB.getCreatedAt() != null ? dateFormat.format(currentUserFromDB.getCreatedAt()) : "N/A");
                updatedAtLabel.setText(currentUserFromDB.getUpdatedAt() != null ? dateFormat.format(currentUserFromDB.getUpdatedAt()) : "N/A");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not load profile information.");
                setFieldsToNotAvailable();
                 // if (changePasswordButton != null) changePasswordButton.setDisable(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error loading profile: " + e.getMessage());
            setFieldsToNotAvailable();
            // if (changePasswordButton != null) changePasswordButton.setDisable(true);
        }
    }
    
    private void setFieldsToNotAvailable() {
        usernameLabel.setText("N/A");
        fullNameLabel.setText("N/A");
        emailLabel.setText("N/A");
        roleLabel.setText("N/A");
        createdAtLabel.setText("N/A");
        updatedAtLabel.setText("N/A");
    }


    @FXML
    void handleChangePasswordButton(ActionEvent event) {
        if (currentUserFromDB == null) {
            showAlert(Alert.AlertType.WARNING, "Information", "No user information available to change password.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/ChangePasswordDialog.fxml")); // Ensure path is correct
            Parent root = loader.load();

            ChangePasswordDialogController controller = loader.getController();
            controller.setCurrentUser(currentUserFromDB); // Pass the current user object

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Change Password");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            
            Stage ownerStage = null;
            if (usernameLabel.getScene() != null && usernameLabel.getScene().getWindow() != null) {
                 ownerStage = (Stage) usernameLabel.getScene().getWindow();
                 dialogStage.initOwner(ownerStage);
            }
           
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);
            controller.setDialogStage(dialogStage); // Allow controller to close the dialog

            dialogStage.showAndWait();

            // After the dialog is closed, refresh the profile info (especially 'updatedAt')
            loadUserProfile(); 

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "UI Error", "Could not open the change password dialog: " + e.getMessage());
        }
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        Stage owner = null;
        if (usernameLabel != null && usernameLabel.getScene() != null && usernameLabel.getScene().getWindow() != null) {
            owner = (Stage) usernameLabel.getScene().getWindow();
        }
        // If the main window is not available, the alert will be application modal
        if (owner != null && owner.isShowing()) {
             alert.initOwner(owner);
        }
        alert.showAndWait();
    }
}