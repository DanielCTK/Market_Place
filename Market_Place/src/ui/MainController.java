package ui;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.User;
import Services.SessionManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private HBox root;
    @FXML private AnchorPane mainContentPane;
    @FXML private VBox menuContainer;

    @FXML private HBox homeMenuButton;

    // Menu Headers
    @FXML private HBox accountMenuHeader;
    @FXML private HBox managementMenuHeader;
    @FXML private HBox myStoreMenuHeader;
    @FXML private HBox reportsMenuHeader;

    // Sub Menus
    @FXML private VBox accountSubMenu;
    @FXML private VBox managementSubMenu;
    @FXML private VBox myStoreSubMenu;
    @FXML private VBox reportsSubMenu;

    // Arrow Icons
    @FXML private ImageView accountArrow;
    @FXML private ImageView managementArrow;
    @FXML private ImageView myStoreArrow;
    @FXML private ImageView reportsArrow;

    @FXML private Button logoutButton;

    // VBox cha của các mục menu chính
    @FXML private VBox accountMenuItemContainer;
    @FXML private VBox managementMenuItemContainer;
    @FXML private VBox myStoreMenuItemContainer;
    @FXML private VBox reportsMenuItemContainer;

    private Image arrowDownImage;
    private Image arrowRightImage;

    private VBox currentOpenSubMenu = null;
    private ImageView currentOpenArrow = null;

    private final List<Node> initialMainContentNodesSnapshot = new ArrayList<>();
    private Object currentContentController = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("DEBUG MainController: initialize() CALLED.");
        User userInMainControllerInit = SessionManager.getInstance().getCurrentUser();
        if (userInMainControllerInit != null) {
            System.out.println("DEBUG MainController: User IN SESSION - Username: " + userInMainControllerInit.getUsername() + ", Role: " + userInMainControllerInit.getRole());
        } else {
            System.err.println("ERROR MainController: User is NULL IN SESSION at MainController.initialize()!");
        }

        if (mainContentPane != null && mainContentPane.getChildren() != null) {
            initialMainContentNodesSnapshot.addAll(new ArrayList<>(mainContentPane.getChildren()));
            System.out.println("DEBUG: Captured " + initialMainContentNodesSnapshot.size() + " initial child node(s) from mainContentPane.");
        } else {
            System.err.println("ERROR: mainContentPane or its children are null during initial content capture.");
        }

        loadArrowImages();
        setInitialArrowStates();
        setupMenuClickHandlers();
        configureRoleBasedVisibility();
    }

    private void loadArrowImages() {
        String downArrowPath = "/Image For Market/arrow_down_black.png";
        String rightArrowPath = "/Image For Market/arrow_right_black.png";

        System.out.println("Attempting to load down arrow from classpath: " + downArrowPath);
        System.out.println("Attempting to load right arrow from classpath: " + rightArrowPath);

        try (InputStream downStream = getClass().getResourceAsStream(downArrowPath);
             InputStream rightStream = getClass().getResourceAsStream(rightArrowPath)) {

            if (downStream == null) {
                System.err.println("Down arrow resource not found: " + downArrowPath);
                arrowDownImage = null;
            } else {
                arrowDownImage = new Image(downStream);
                if (arrowDownImage.isError()) {
                    System.err.println("Error creating Down Arrow Image object from stream: " + arrowDownImage.getException());
                    arrowDownImage = null;
                }
            }

            if (rightStream == null) {
                System.err.println("Right arrow resource not found: " + rightArrowPath);
                arrowRightImage = null;
            } else {
                arrowRightImage = new Image(rightStream);
                if (arrowRightImage.isError()) {
                    System.err.println("Error creating Right Arrow Image object from stream: " + arrowRightImage.getException());
                    arrowRightImage = null;
                }
            }

        } catch (IOException e) {
            System.err.println("Error processing arrow images: " + e.getMessage());
            e.printStackTrace();
            arrowDownImage = null;
            arrowRightImage = null;
        } catch (NullPointerException e) {
            System.err.println("NullPointerException during image loading (likely resource not found): " + e.getMessage());
            e.printStackTrace();
            arrowDownImage = null;
            arrowRightImage = null;
        }
        System.out.println("Arrow images loaded. DownImg null: " + (arrowDownImage == null) + ", RightImg null: " + (arrowRightImage == null));
    }


    private void setInitialArrowStates() {
        if (arrowRightImage != null) {
            if (accountArrow != null) accountArrow.setImage(arrowRightImage);
            else System.err.println("MainController: accountArrow is null.");
            if (managementArrow != null) managementArrow.setImage(arrowRightImage);
            else System.err.println("MainController: managementArrow is null.");
            if (myStoreArrow != null) myStoreArrow.setImage(arrowRightImage);
            else System.err.println("MainController: myStoreArrow is null.");
            if (reportsArrow != null) reportsArrow.setImage(arrowRightImage);
            else System.err.println("MainController: reportsArrow is null.");
        } else {
            System.err.println("Cannot set initial arrow states: arrowRightImage is null.");
        }
    }

    private void setupMenuClickHandlers() {
        if (homeMenuButton == null) {
             System.err.println("ERROR: homeMenuButton (fx:id for Home) is NULL! Check FXML.");
        }
        if (accountMenuHeader != null) accountMenuHeader.setOnMouseClicked(event -> toggleSubMenu(accountSubMenu, accountArrow));
        else System.err.println("ERROR: accountMenuHeader is NULL!");
        if (managementMenuHeader != null) managementMenuHeader.setOnMouseClicked(event -> toggleSubMenu(managementSubMenu, managementArrow));
        else System.err.println("ERROR: managementMenuHeader is NULL!");
        if (myStoreMenuHeader != null) myStoreMenuHeader.setOnMouseClicked(event -> toggleSubMenu(myStoreSubMenu, myStoreArrow));
        else System.err.println("ERROR: myStoreMenuHeader is NULL!");
        if (reportsMenuHeader != null) reportsMenuHeader.setOnMouseClicked(event -> toggleSubMenu(reportsSubMenu, reportsArrow));
        else System.err.println("ERROR: reportsMenuHeader is NULL!");
    }
    
    private void configureRoleBasedVisibility() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (managementMenuItemContainer != null) {
            managementMenuItemContainer.setVisible(false);
            managementMenuItemContainer.setManaged(false);
        }
        if (myStoreMenuItemContainer != null) {
            myStoreMenuItemContainer.setVisible(false);
            myStoreMenuItemContainer.setManaged(false);
        }
        if (reportsMenuItemContainer != null) {
            reportsMenuItemContainer.setVisible(false);
            reportsMenuItemContainer.setManaged(false);
        }
        if (accountMenuItemContainer != null) {
            accountMenuItemContainer.setVisible(currentUser != null);
            accountMenuItemContainer.setManaged(currentUser != null);
        }

        if (currentUser == null) {
            System.err.println("No user in session, cannot configure role-based menu visibility beyond basic account menu.");
            return;
        }

        String role = currentUser.getRole();
        boolean isAdmin = "ADMIN".equalsIgnoreCase(role);
        boolean isSeller = "SELLER".equalsIgnoreCase(role);

        if (managementMenuItemContainer != null && isAdmin) {
            managementMenuItemContainer.setVisible(true);
            managementMenuItemContainer.setManaged(true);
        }
        if (reportsMenuItemContainer != null && isAdmin) {
            reportsMenuItemContainer.setVisible(true);
            reportsMenuItemContainer.setManaged(true);
        }
        if (myStoreMenuItemContainer != null && isSeller) {
            myStoreMenuItemContainer.setVisible(true);
            myStoreMenuItemContainer.setManaged(true);
        }
    }

    private void toggleSubMenu(VBox subMenuToToggle, ImageView arrowIconToToggle) {
        if (subMenuToToggle == null) {
            System.err.println("toggleSubMenu: subMenuToToggle is null.");
            return;
        }
        if (arrowIconToToggle == null && (arrowDownImage != null || arrowRightImage != null) ) {
            System.err.println("toggleSubMenu: arrowIconToToggle is null but arrow images seem loaded.");
        }

        if (arrowDownImage == null || arrowRightImage == null) {
            System.err.println("toggleSubMenu: Arrow images not loaded. Toggling visibility only.");
            boolean shouldOpenSimple = !subMenuToToggle.isVisible();
            if (currentOpenSubMenu != null && currentOpenSubMenu != subMenuToToggle && currentOpenSubMenu.isVisible()) {
                currentOpenSubMenu.setVisible(false);
                currentOpenSubMenu.setManaged(false);
            }
            subMenuToToggle.setVisible(shouldOpenSimple);
            subMenuToToggle.setManaged(shouldOpenSimple);
            currentOpenSubMenu = shouldOpenSimple ? subMenuToToggle : null;
            currentOpenArrow = null; 
            return;
        }

        if (currentOpenSubMenu != null && currentOpenSubMenu != subMenuToToggle && currentOpenSubMenu.isVisible()) {
            animateSubMenu(currentOpenSubMenu, currentOpenArrow, false); 
        }

        boolean shouldOpen = !subMenuToToggle.isVisible();
        animateSubMenu(subMenuToToggle, arrowIconToToggle, shouldOpen);

        if (shouldOpen) {
            currentOpenSubMenu = subMenuToToggle;
            currentOpenArrow = arrowIconToToggle;
        } else {
            if (currentOpenSubMenu == subMenuToToggle) {
                currentOpenSubMenu = null;
                currentOpenArrow = null;
            }
        }
    }

    private void animateSubMenu(VBox subMenu, ImageView arrowIcon, boolean open) {
        if (arrowIcon != null && arrowDownImage != null && arrowRightImage != null) {
             arrowIcon.setImage(open ? arrowDownImage : arrowRightImage);
        }

        if (open) {
            subMenu.setVisible(true); 
            subMenu.setManaged(true);
            subMenu.setOpacity(0); 
            FadeTransition ft = new FadeTransition(Duration.millis(250), subMenu);
            ft.setFromValue(0.0);
            ft.setToValue(1.0);
            ft.play();
        } else {
            FadeTransition ft = new FadeTransition(Duration.millis(200), subMenu);
            ft.setFromValue(1.0);
            ft.setToValue(0.0);
            ft.setOnFinished(e -> {
                subMenu.setVisible(false);
                subMenu.setManaged(false);
            });
            ft.play();
        }
    }

    @FXML
    private void handleGoHome(MouseEvent event) {
        System.out.println("DEBUG: Home button/area clicked.");
        if (mainContentPane != null) {
            shutdownCurrentContentController();
            if (currentOpenSubMenu != null && currentOpenSubMenu.isVisible()) {
                 animateSubMenu(currentOpenSubMenu, currentOpenArrow, false);
                 currentOpenSubMenu = null;
                 currentOpenArrow = null;
            }
            mainContentPane.getChildren().clear(); 
            if (!initialMainContentNodesSnapshot.isEmpty()) {
                mainContentPane.getChildren().addAll(initialMainContentNodesSnapshot);
                System.out.println("DEBUG: Restored initial content to mainContentPane (" + initialMainContentNodesSnapshot.size() + " nodes).");
            } else {
                System.out.println("DEBUG: mainContentPane cleared as no initial content was captured or to restore.");
                mainContentPane.getChildren().add(new Text("Welcome to the Marketplace!"));
            }
            currentContentController = null;
        } else {
            System.err.println("ERROR: mainContentPane is null. Cannot go to home.");
        }
    }

    // Menu Item Handlers
    @FXML private void handleViewProfile(ActionEvent event) { loadContent("/ui/ViewProfilePanel.fxml"); }
    @FXML private void handleChangePassword(ActionEvent event) { loadContent("/ui/ChangePasswordDialog.fxml"); }
    @FXML private void handleUserManagement(ActionEvent event) { loadContent("/ui/UserManagementPanel.fxml"); }
    @FXML private void handleProductManagement(ActionEvent event) { loadContent("/ui/ProductManagementPanel.fxml"); }
    @FXML private void handleSellerManagement(ActionEvent event) { loadContent("/ui/SellerManagementPanel.fxml"); }
    @FXML private void handleOrderManagement(ActionEvent event) { loadContent("/ui/OrderManagementPanel.fxml"); }
    @FXML private void handleReviewManagement(ActionEvent event) { loadContent("/ui/ReviewManagementPanel.fxml"); }
    
    // SỬA CÁC HÀM CHO "MY STORE"
    @FXML private void handleMyProducts(ActionEvent event) { 
        // loadContent("/ui/MyProductsPanel.fxml"); 
        showAlert(Alert.AlertType.INFORMATION, "Future Update", "The 'My Products' feature is currently under development and will be available soon!");
    }
    @FXML private void handleMyOrders(ActionEvent event) { 
        // loadContent("/ui/MyOrdersPanel.fxml"); 
        showAlert(Alert.AlertType.INFORMATION, "Future Update", "The 'My Orders' feature for sellers is currently under development and will be available soon!");
    }
    
    @FXML private void handleSalesReport(ActionEvent event) { 
        loadContent("/ui/NewSalesReportPanel.fxml"); // Đảm bảo đúng tên panel
    }
    @FXML private void handleUserActivityReport(ActionEvent event) { loadContent("/ui/UserActivityReportPanel.fxml"); }

    @FXML
    private void handleLogout(ActionEvent event) {
        System.out.println("Logout Clicked");
        shutdownCurrentContentController(); 
        SessionManager.getInstance().clearSession(); 
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/Login.fxml"));
            if (loader.getLocation() == null) {
                System.err.println("Cannot find Login.fxml at /ui/Login.fxml");
                showAlert(Alert.AlertType.ERROR, "Critical Error", "Login screen resource not found.");
                return;
            }
            Parent loginRoot = loader.load();
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();
            Scene scene = new Scene(loginRoot);
            currentStage.setScene(scene);
            currentStage.setTitle("Marketplace Login"); 
            currentStage.centerOnScreen(); 
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Logout Error", "Could not switch to login screen: " + e.getMessage());
        }
    }

    private void shutdownCurrentContentController() {
        if (currentContentController != null) {
            if (currentContentController instanceof NewSalesReportController) {
                ((NewSalesReportController) currentContentController).shutdownExecutor();
                System.out.println("Shutdown NewSalesReportController executor.");
            } else if (currentContentController instanceof UserActivityReportController) {
                // Giả sử UserActivityReportController cũng có shutdownExecutor()
                if (currentContentController instanceof UserActivityReportController) {
                     ((UserActivityReportController) currentContentController).shutdownExecutor();
                     System.out.println("Shutdown UserActivityReportController executor.");
                }
            }
            // Thêm các else if khác nếu cần
        }
    }

    private void loadContent(String fxmlPath) {
        shutdownCurrentContentController(); 

        try {
            if (mainContentPane == null) {
                 System.err.println("ERROR MainController: mainContentPane is null. Check FXML @FXML injection for mainContentPane.");
                 showAlert(Alert.AlertType.ERROR, "UI Error", "Main content area not available.");
                 return;
            }
            System.out.println("Attempting to load FXML: " + fxmlPath);
            URL resourceUrl = getClass().getResource(fxmlPath);
            if (resourceUrl == null) {
                String errorMessage = "Cannot find FXML resource: " + fxmlPath +
                                      "\nPlease ensure the path is correct, starts with '/', " +
                                      "and the FXML file is in the 'ui' package within your resources folder (e.g., src/main/resources/ui/YourPanel.fxml).";
                System.err.println(errorMessage);
                Text errorText = new Text("Error: Could not load " + fxmlPath.substring(fxmlPath.lastIndexOf('/') + 1) + ".\nResource not found.");
                mainContentPane.getChildren().setAll(errorText);
                showAlert(Alert.AlertType.ERROR, "Navigation Error", errorMessage);
                currentContentController = null; 
                return;
            }

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Node contentNode = loader.load();
            
            currentContentController = loader.getController(); 

            mainContentPane.getChildren().setAll(contentNode);

            AnchorPane.setTopAnchor(contentNode, 0.0);
            AnchorPane.setBottomAnchor(contentNode, 0.0);
            AnchorPane.setLeftAnchor(contentNode, 0.0);
            AnchorPane.setRightAnchor(contentNode, 0.0);
            System.out.println("Successfully loaded content from: " + fxmlPath);

        } catch (IOException e) {
            System.err.println("Failed to load FXML content from: " + fxmlPath);
            e.printStackTrace();
            Text errorText = new Text("Error loading view: " + fxmlPath.substring(fxmlPath.lastIndexOf('/') + 1) + "\n" + e.getClass().getSimpleName() + (e.getMessage() != null ? ": " + e.getMessage() : ""));
            if (mainContentPane != null) mainContentPane.getChildren().setAll(errorText);
            showAlert(Alert.AlertType.ERROR, "Load Error", "Could not load page: " + fxmlPath.substring(fxmlPath.lastIndexOf('/') + 1) + "\nDetails: " + e.getClass().getSimpleName() + (e.getMessage() != null ? ": " + e.getMessage() : ""));
            currentContentController = null; 
        }  catch (Exception e) {
            System.err.println("Unexpected error loading FXML content from: " + fxmlPath);
            e.printStackTrace();
            Text errorText = new Text("Unexpected error loading view: " + fxmlPath.substring(fxmlPath.lastIndexOf('/') + 1) + "\n" + e.getClass().getSimpleName() + (e.getMessage() != null ? ": " + e.getMessage() : ""));
            if (mainContentPane != null) mainContentPane.getChildren().setAll(errorText);
            showAlert(Alert.AlertType.ERROR, "Load Error", "Unexpected error loading page: " + fxmlPath.substring(fxmlPath.lastIndexOf('/') + 1) + "\nDetails: " + e.getClass().getSimpleName());
            currentContentController = null;
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (root != null && root.getScene() != null && root.getScene().getWindow() != null && root.getScene().getWindow().isShowing()) {
            alert.initOwner(root.getScene().getWindow());
        }
        alert.showAndWait();
    }

    public void shutdownApp() {
        System.out.println("MainController shutting down app-wide resources.");
        shutdownCurrentContentController(); 
    }
}