# Market Place Application
## Link Youtube: https://youtu.be/rJLmK0TC458?si=LEE3wcuot4OjywYE
## Link Github: https://github.com/DanielCTK/Market_Place
## 1. Introduction

**Market_Place** is a desktop application built with JavaFX, simulating a transactional marketplace environment. It allows users with different roles (Customers, Sellers, Administrators) to interact with products, orders, reviews, and manage their accounts. The application features a role-based access system, various management panels, and reporting functionalities.

## 2. Key Features

The application comprises the following main modules:

*   **User Authentication & Account Management:**
    *   Login (`Login.fxml`)
    *   Registration (`Registration.fxml`)
    *   View Profile (`ViewProfilePanel.fxml`)
    *   Change Password (`ChangePasswordDialog.fxml`)
*   **Admin-Specific Management Panels:**
    *   User Management (`UserManagementPanel.fxml`, `UserFormDialog.fxml`)
    *   Product Management (`ProductManagementPanel.fxml`, `ProductFormDialog.fxml`)
    *   Seller Management (`SellerManagementPanel.fxml`, `SellerFormDialog.fxml`)
    *   Order Management (view all orders) (`OrderManagementPanel.fxml`, `OrderNewFormDialog.fxml`, `OrderItemFormDialog.fxml`)
    *   Review Management (`ReviewManagementPanel.fxml`, `OrderReviewFormDialog.fxml`)
*   **Reporting (Admin/Seller):**
    *   New Sales Report (`NewSalesReportPanel.fxml`)
    *   User Activity Report (`UserActivityReportPanel.fxml`)
*   **"My Store" (for Sellers - based on menu items in `Main.fxml`):**
    *   Manage "My Products"
    *   Manage "My Orders"
*   **General UI:**
    *   Main application window (`Main.fxml`) with a dynamic navigation menu.
    *   Interface customization via CSS (`styles.css`, `styles2.css`).

## 3. Technologies Used

*   **Language:** Java (most likely JDK 17+ for JavaFX 21 compatibility)
*   **UI Framework:** JavaFX
*   **UI Components Library:** JFoenix (for enhanced JavaFX controls)
*   **Database:** MySQL (based on `DBConnection.java` and `DatabaseManager.java`)
*   **Password Hashing:** jBCrypt (from `PasswordHasher.java`)
*   **IDE (Assumed):** Eclipse or IntelliJ IDEA

## 4. Prerequisites

*   **JDK (Java Development Kit):** Version 17 or newer recommended. Ensure `JAVA_HOME` is set.
*   **JavaFX SDK:** Version 17 or newer. Download from [GluonHQ](https://gluonhq.com/products/javafx/).
*   **MySQL Server:** A running MySQL instance.
*   **MySQL Connector/J:** JDBC driver for MySQL. Download from [MySQL Website](https://dev.mysql.com/downloads/connector/j/).
*   **JFoenix JAR:** Download the JFoenix JAR file (e.g., `jfoenix-9.0.10.jar` or similar).
*   **jBCrypt JAR:** `jbcrypt-0.4.jar` (or latest). Can be found on Maven Central.
*   **IDE:** Eclipse, IntelliJ IDEA, or NetBeans with JavaFX support.

## 5. Setup and Launch

### 5.1. Database Setup

1.  **Create Database:**
    *   Ensure your MySQL server is running.
    *   Create a database named `marketplace` (as in `DatabaseManager.java`). You can use a tool like MySQL Workbench or the command line:
        ```sql
        CREATE DATABASE marketplace;
        USE marketplace;
        ```
2.  **Create Tables:**
    Based on the DAO and Model files, you need to create the following tables:
    *   **`users`**: `id` (PK, INT, AI), `username` (VARCHAR, UNIQUE), `password` (VARCHAR, HASHED), `full_name` (VARCHAR), `email` (VARCHAR, UNIQUE), `role` (VARCHAR - e.g., 'ADMIN', 'SELLER', 'CUSTOMER'), `created_at` (TIMESTAMP), `updated_at` (TIMESTAMP).
    *   **`products`**: `product_id` (PK, VARCHAR), `product_category_name` (VARCHAR), `product_weight_g` (DOUBLE, NULLABLE).
    *   **`sellers`**: `seller_id` (PK, VARCHAR), `seller_state` (VARCHAR, NULLABLE).
    *   **`orders`**: `order_id` (PK, VARCHAR), `timestamp` (TIMESTAMP), `customer_contact` (VARCHAR - customer's user_id).
    *   **`order_items`**: `order_items_pk` (PK, INT, AI), `order_id` (FK to orders), `product_id` (FK to products), `seller_id` (FK to sellers), `price` (FLOAT).
    *   **`order_reviews`**: `review_id` (PK, VARCHAR), `order_id` (FK to orders, NULLABLE), `review_score` (INT, NULLABLE), `review_comment_message` (TEXT, NULLABLE).
3.  **Create User & Grant Privileges:**
    Create a MySQL user (e.g., `appuser` with password `app_pass123`) and grant necessary privileges for the `marketplace` database.
    ```sql
    CREATE USER 'appuser'@'localhost' IDENTIFIED BY 'app_pass123';
    GRANT ALL PRIVILEGES ON marketplace.* TO 'appuser'@'localhost';
    FLUSH PRIVILEGES;
    ```
4.  **Update Database Connection Info (if necessary):**
    Open `src/Services/DatabaseManager.java` and ensure the `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASS` constants match your MySQL setup.

### 5.2. IDE Setup (Example: Eclipse/IntelliJ)

1.  **Clone/Download Project:** Get the project source code onto your machine.
2.  **Create Java Project:**
    *   In your IDE, create a new Java Project (or JavaFX project if your IDE has a template).
    *   Import the source files into the appropriate `src` directory structure.
3.  **Add Libraries/JARs:**
    *   **JavaFX SDK:**
        *   Add the JavaFX SDK libraries to your project's build path/module path.
        *   (For non-modular projects) You might need to add VM arguments when running:
            `--module-path /path/to/javafx-sdk-XX/lib --add-modules javafx.controls,javafx.fxml,com.jfoenix` (Replace `XX` and the path).
            Note: Your `module-info.java` already includes `requires com.jfoenix;`, `requires javafx.controls;`, `requires javafx.fxml;`, etc.
    *   **MySQL Connector/J JAR:** Add the `mysql-connector-java-X.X.XX.jar` file to the build path.
    *   **JFoenix JAR:** Add the `jfoenix-X.X.X.jar` file to the build path.
    *   **jBCrypt JAR:** Add the `jbcrypt-X.X.jar` file to the build path.
4.  **Configure Source Folders:**
    *   Ensure `src` is marked as a source folder.
    *   The `Resources` folder (containing `ui/` and `Image For Market/`) should be placed in a source folder (e.g., `src/main/resources`) and added to the classpath.
    *   The `bin` folder is the IDE's output directory and does not need to be configured as a source folder.

### 5.3. Initial Data (Optional)

*   You may need to create an admin account in the `users` table.
*   Use `src/utils/GenerateHashes.java` to create hashed passwords if needed, then manually insert them into the database.

## 6. Running the Application

1.  Ensure the MySQL server is running and the database is set up.
2.  The main entry point for the application is `src/ui/Launcher.java`.
3.  Run `Launcher.java` as a Java Application from your IDE.
4.  The Login screen (`Login.fxml`) will appear.
    *   Use the login credentials of users you've added to the database.
    *   You can also register new users.

## 7. Project Structure
```
Market_Place/
├── bin/ # IDE output directory (contains .class files, copied FXML, CSS)
│ └── ui/
│ ├── *.fxml
│ └── *.css
├── Resources/ # Contains source FXML, images
│ ├── ui/
│ │ └── *.fxml
│ └── Image For Market/ # Application images
│ └── *.png, *.gif
├── src/
│ ├── Config/
│ ├── dao/ # Data Access Object Interfaces
│ ├── impl/ # DAO Implementations
│ ├── model/ # Data Model classes (POJOs)
│ ├── Services/ # Business logic and services
│ ├── test/ # Test classes (e.g., ProductTest.java)
│ ├── ui/ # JavaFX Controllers and Launcher
│ └── utils/ # Utility classes
├── module-info.java # Java module descriptor file
└── README.md # This file
```

**Note on FXML and Image File Locations:**
*   `Resources/ui/` contains the original FXML files you edit.
*   `Resources/Image For Market/` contains the image files.
*   When the application is built, these files are typically copied to the output directory (usually `bin` or `target/classes`).
*   Image paths in FXML files (e.g., `url="@../../src/Image%20For%20Market/..."`) assume that at runtime, the FXML file is in a subdirectory (like `ui`) of the output directory, and the `src` directory (or a similar structure containing `Image For Market`) is also copied to the output directory at the same level as the FXML file's directory. **The best practice is to place the `Image For Market` directory inside `Resources` and reference images directly from the classpath, e.g., `/Image For Market/image.png`.** Alternatively, ensure your `bin` directory structure correctly reflects this relative path.

## 8. Main Workflow (Overview)

1.  **Launch:** `ui.Launcher` starts the application, displaying `Login.fxml`.
2.  **Authentication:** `LoginController` uses `AuthService` to verify login credentials against the database (passwords are checked via `PasswordHasher`). `SessionManager` stores the logged-in user's information.
3.  **Registration:** `RegistrationController` uses `AuthService` to create new users.
4.  **Main Window:** `MainController` is loaded after a successful login. It manages the main layout, navigation menu, and dynamically loads content into `mainContentPane`.
5.  **Access Control:** Based on the user's role (from `SessionManager`), `MainController` enables/disables menu items.
6.  **Data Operations:** Each panel's controller (e.g., `UserManagementController`) interacts with the corresponding DAO (e.g., `UserDAO`) to perform CRUD operations. DAOs use `DBConnection` (via `DatabaseManager`) to connect to the MySQL database.
7.  **Dialogs:** Add/edit forms (e.g., `UserFormDialog.fxml`) are typically displayed as modal dialogs.

## 9. Potential Enhancements

*   Use a build system (Maven or Gradle) to manage libraries and build the project.
*   Provide an SQL script to automatically create the database schema.
*   Improve error handling and user feedback.
*   Complete the "My Store" functionality for Sellers.
*   Add more detailed reports and analytics.
*   Write Unit and Integration tests.

## 10. Important Notes

*   Ensure that paths to FXML files in `FXMLLoader.load()` calls are correct relative to the classpath (usually starting with `/` for absolute paths from the root of resources/class files).
*   The application uses JFoenix; ensure the JFoenix JAR file is correctly configured in your project's module path or classpath. The `module-info.java` file should include `requires com.jfoenix;`.

## 11. Team & Members
* **Nguyen Thanh Danh - 97482403175**: FullStack
