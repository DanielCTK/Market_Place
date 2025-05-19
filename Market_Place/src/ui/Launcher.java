package ui; // Đảm bảo package này đúng với vị trí file

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.stage.Window;
import model.User;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import java.io.IOException;
import java.net.URL;
import Services.SessionManager;
public class Launcher extends Application {

    private static Stage primaryStageInstance;

    @Override
    public void start(Stage stage) {
        primaryStageInstance = stage;
        primaryStageInstance.setTitle("Marketplace App");

        try {
            showLoginScreen();
        } catch (IOException e) {
            System.err.println("Không thể tải màn hình Login ban đầu!");
            e.printStackTrace(); // Quan trọng: In ra đầy đủ lỗi để debug
            // Cân nhắc hiển thị một Alert cho người dùng nếu có thể
            // hoặc thoát ứng dụng nếu lỗi quá nghiêm trọng
        }
    }

    public static void showLoginScreen() throws IOException {
        // Cách 1: Đường dẫn tương đối từ package của Launcher (ui)
        // Vì Login.fxml cũng nằm trong thư mục 'ui' bên trong 'Resources' (được coi là gốc classpath)
        // String fxmlPath = "Login.fxml"; // Sẽ tìm kiếm ui/Login.fxml trong classpath

        // Cách 2 (An toàn và rõ ràng hơn): Đường dẫn tuyệt đối từ gốc classpath
        String fxmlPath = "/ui/Login.fxml"; // Dấu "/" ở đầu chỉ gốc của classpath
                                            // và 'ui/Login.fxml' là đường dẫn bên trong 'Resources'

        URL fxmlLocation = Launcher.class.getResource(fxmlPath);
        if (fxmlLocation == null) {
            System.err.println("CRITICAL: Không tìm thấy file Login.fxml tại đường dẫn: " + fxmlPath);
            System.err.println("Hãy kiểm tra lại cấu trúc thư mục 'Resources' và đảm bảo nó được đặt làm Source Folder trong Eclipse.");
            throw new IOException("Không tìm thấy file Login.fxml");
        }
        FXMLLoader loader = new FXMLLoader(fxmlLocation);
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStageInstance.setScene(scene);
        primaryStageInstance.setTitle("Đăng nhập");
        primaryStageInstance.show();
    }

 // Giả sử phương thức của bạn trông giống như thế này
    public static void showMainScreen() throws IOException {
        System.out.println("DEBUG Launcher: showMainScreen() CALLED.");
        User userBeforeMainLoad = SessionManager.getInstance().getCurrentUser();
        if (userBeforeMainLoad != null) {
            System.out.println("DEBUG Launcher: User IN SESSION BEFORE loading Main.fxml - Username: " + userBeforeMainLoad.getUsername() + ", Role: " + userBeforeMainLoad.getRole());
        } else {
            System.err.println("ERROR Launcher: User is NULL IN SESSION BEFORE loading Main.fxml!");
        }

        FXMLLoader loader = new FXMLLoader(Launcher.class.getResource("/ui/Main.fxml")); // Hoặc đường dẫn đúng
        Parent root = loader.load();
        // MainController mainController = loader.getController(); // instance của MainController được tạo ở đây

        // Lấy lại Stage chính hoặc tạo Stage mới
        // Stage primaryStage = ... (cách bạn lấy hoặc tạo primaryStage)
        // primaryStage.setScene(new Scene(root));
        // primaryStage.setTitle("Marketplace Dashboard");
        // primaryStage.show();

        // Ví dụ nếu bạn tạo Stage mới hoàn toàn:
        Stage mainStage = new Stage(); // HOẶC lấy lại primary stage từ login
        mainStage.setScene(new Scene(root));
        mainStage.setTitle("Marketplace Dashboard");
        mainStage.show();


        System.out.println("DEBUG Launcher: Main.fxml loaded and shown.");
        User userAfterMainLoad = SessionManager.getInstance().getCurrentUser();
        if (userAfterMainLoad != null) {
            System.out.println("DEBUG Launcher: User IN SESSION AFTER loading Main.fxml - Username: " + userAfterMainLoad.getUsername() + ", Role: " + userAfterMainLoad.getRole());
        } else {
            System.err.println("ERROR Launcher: User is NULL IN SESSION AFTER loading Main.fxml!");
        }
    }
    
    public static void showRegistrationScreen(Window ownerWindow) {
        try {
            FXMLLoader loader = new FXMLLoader(Launcher.class.getResource("/ui/Registration.fxml")); // Đường dẫn đến FXML
            Parent root = loader.load();
            Stage registrationStage = new Stage();
            registrationStage.setTitle("Đăng Ký Tài Khoản");
            registrationStage.setScene(new Scene(root));
            registrationStage.initModality(Modality.WINDOW_MODAL);
            if (ownerWindow != null) {
                registrationStage.initOwner(ownerWindow);
            }
            registrationStage.setResizable(false);
            registrationStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            // Xử lý lỗi, ví dụ hiển thị Alert
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Lỗi Hệ Thống");
            errorAlert.setHeaderText("Không thể tải màn hình đăng ký");
            errorAlert.setContentText("Đã có lỗi xảy ra: " + e.getMessage());
            if (ownerWindow != null) errorAlert.initOwner(ownerWindow);
            errorAlert.showAndWait();
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStageInstance;
    }

    public static void main(String[] args) {
        launch(args);
    }
}