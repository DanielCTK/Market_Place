# Market Place Application

## 1. Giới thiệu

**Market_Place** là một ứng dụng desktop được xây dựng bằng JavaFX, mô phỏng một môi trường chợ/sàn giao dịch. Nó cho phép người dùng với các vai trò khác nhau (Khách hàng, Người bán, Quản trị viên) tương tác với sản phẩm, đơn hàng, đánh giá và quản lý tài khoản của họ. Ứng dụng có hệ thống phân quyền dựa trên vai trò, các bảng điều khiển quản lý khác nhau và chức năng báo cáo.

## 2. Tính năng chính

Ứng dụng bao gồm các module chính sau:

*   **Xác thực & Quản lý tài khoản người dùng:**
    *   Đăng nhập (`Login.fxml`)
    *   Đăng ký (`Registration.fxml`)
    *   Xem thông tin cá nhân (`ViewProfilePanel.fxml`)
    *   Thay đổi mật khẩu (`ChangePasswordDialog.fxml`)
*   **Bảng điều khiển quản lý (dành cho Admin):**
    *   Quản lý người dùng (`UserManagementPanel.fxml`, `UserFormDialog.fxml`)
    *   Quản lý sản phẩm (`ProductManagementPanel.fxml`, `ProductFormDialog.fxml`)
    *   Quản lý người bán (`SellerManagementPanel.fxml`, `SellerFormDialog.fxml`)
    *   Quản lý đơn hàng (xem tất cả đơn hàng) (`OrderManagementPanel.fxml`, `OrderNewFormDialog.fxml`, `OrderItemFormDialog.fxml`)
    *   Quản lý đánh giá (`ReviewManagementPanel.fxml`, `OrderReviewFormDialog.fxml`)
*   **Báo cáo (Admin/Người bán):**
    *   Báo cáo doanh thu mới (`NewSalesReportPanel.fxml`)
    *   Báo cáo hoạt động người dùng (`UserActivityReportPanel.fxml`)
*   **"Cửa hàng của tôi" (dành cho Người bán - dựa trên menu trong `Main.fxml`):**
    *   Quản lý "Sản phẩm của tôi"
    *   Quản lý "Đơn hàng của tôi"
*   **Giao diện chung:**
    *   Cửa sổ ứng dụng chính (`Main.fxml`) với menu điều hướng động.
    *   Tùy chỉnh giao diện qua CSS (`styles.css`, `styles2.css`).

## 3. Công nghệ sử dụng

*   **Ngôn ngữ:** Java (khả năng cao là JDK 17+ để tương thích với JavaFX 21)
*   **UI Framework:** JavaFX
*   **Thư viện UI Components:** JFoenix (cho các control JavaFX nâng cao)
*   **Cơ sở dữ liệu:** MySQL (dựa trên `DBConnection.java` và `DatabaseManager.java`)
*   **Băm mật khẩu:** jBCrypt (từ `PasswordHasher.java`)
*   **IDE (Giả định):** Eclipse hoặc IntelliJ IDEA

## 4. Yêu cầu môi trường

*   **JDK (Java Development Kit):** Khuyến nghị phiên bản 17 trở lên. Đảm bảo `JAVA_HOME` đã được thiết lập.
*   **JavaFX SDK:** Phiên bản 17 trở lên. Tải về từ [GluonHQ](https://gluonhq.com/products/javafx/).
*   **MySQL Server:** Một instance MySQL đang chạy.
*   **MySQL Connector/J:** JDBC driver cho MySQL. Tải về từ [MySQL Website](https://dev.mysql.com/downloads/connector/j/).
*   **JFoenix JAR:** Tải file JAR JFoenix (ví dụ: `jfoenix-9.0.10.jar` hoặc tương tự).
*   **jBCrypt JAR:** `jbcrypt-0.4.jar` (hoặc mới nhất). Có thể tìm thấy trên Maven Central.
*   **IDE:** Eclipse, IntelliJ IDEA, hoặc NetBeans có hỗ trợ JavaFX.

## 5. Cài đặt và Khởi chạy

### 5.1. Cài đặt Cơ sở dữ liệu

1.  **Tạo Cơ sở dữ liệu:**
    *   Đảm bảo MySQL server của bạn đang chạy.
    *   Tạo một database tên là `marketplace` (như trong `DatabaseManager.java`). Bạn có thể dùng công cụ như MySQL Workbench hoặc dòng lệnh:
        ```sql
        CREATE DATABASE marketplace;
        USE marketplace;
        ```
2.  **Tạo Bảng:**
    Dựa trên các file DAO và Model, bạn cần tạo các bảng sau:
    *   **`users`**: `id` (PK, INT, AI), `username` (VARCHAR, UNIQUE), `password` (VARCHAR, HASHED), `full_name` (VARCHAR), `email` (VARCHAR, UNIQUE), `role` (VARCHAR - vd: 'ADMIN', 'SELLER', 'CUSTOMER'), `created_at` (TIMESTAMP), `updated_at` (TIMESTAMP).
    *   **`products`**: `product_id` (PK, VARCHAR), `product_category_name` (VARCHAR), `product_weight_g` (DOUBLE, NULLABLE).
    *   **`sellers`**: `seller_id` (PK, VARCHAR), `seller_state` (VARCHAR, NULLABLE).
    *   **`orders`**: `order_id` (PK, VARCHAR), `timestamp` (TIMESTAMP), `customer_contact` (VARCHAR - user_id của khách hàng).
    *   **`order_items`**: `order_items_pk` (PK, INT, AI), `order_id` (FK tới orders), `product_id` (FK tới products), `seller_id` (FK tới sellers), `price` (FLOAT).
    *   **`order_reviews`**: `review_id` (PK, VARCHAR), `order_id` (FK tới orders, NULLABLE), `review_score` (INT, NULLABLE), `review_comment_message` (TEXT, NULLABLE).
3.  **Tạo User & Phân quyền:**
    Tạo một user MySQL (ví dụ: `appuser` với mật khẩu `app_pass123`) và cấp quyền cần thiết cho database `marketplace`.
    ```sql
    CREATE USER 'appuser'@'localhost' IDENTIFIED BY 'app_pass123';
    GRANT ALL PRIVILEGES ON marketplace.* TO 'appuser'@'localhost';
    FLUSH PRIVILEGES;
    ```
4.  **Cập nhật thông tin kết nối CSDL (nếu cần):**
    Mở file `src/Services/DatabaseManager.java` và đảm bảo các hằng số `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASS` khớp với cài đặt MySQL của bạn.

### 5.2. Cài đặt trên IDE (Ví dụ: Eclipse/IntelliJ)

1.  **Clone/Tải Project:** Lấy mã nguồn dự án về máy của bạn.
2.  **Tạo Java Project:**
    *   Trong IDE, tạo một Java Project mới (hoặc JavaFX project nếu IDE có template).
    *   Import các file mã nguồn vào cấu trúc thư mục `src` tương ứng.
3.  **Thêm Thư viện/JARs:**
    *   **JavaFX SDK:**
        *   Thêm các thư viện của JavaFX SDK vào build path/module path của project.
        *   (Đối với project không dùng module) Bạn có thể cần thêm VM arguments khi chạy:
            `--module-path /path/to/javafx-sdk-XX/lib --add-modules javafx.controls,javafx.fxml,com.jfoenix` (Thay thế `XX` và đường dẫn).
            Lưu ý: `module-info.java` của bạn đã có `requires com.jfoenix;`, `requires javafx.controls;`, `requires javafx.fxml;` v.v.
    *   **MySQL Connector/J JAR:** Thêm file `mysql-connector-java-X.X.XX.jar` vào build path.
    *   **JFoenix JAR:** Thêm file `jfoenix-X.X.X.jar` vào build path.
    *   **jBCrypt JAR:** Thêm file `jbcrypt-X.X.jar` vào build path.
4.  **Cấu hình Thư mục Mã nguồn:**
    *   Đảm bảo `src` được đánh dấu là source folder.
    *   Thư mục `Resources` (chứa `ui/` và `Image For Market/`) nên được đặt trong một source folder (ví dụ `src/main/resources`) và được thêm vào classpath.
    *   Thư mục `bin` là thư mục output của IDE, không cần cấu hình làm source folder.

### 5.3. Dữ liệu ban đầu (Tùy chọn)

*   Bạn có thể cần tạo sẵn một tài khoản admin trong bảng `users`.
*   Sử dụng `src/utils/GenerateHashes.java` để tạo mật khẩu đã băm nếu cần, sau đó chèn thủ công vào CSDL.

## 6. Chạy Ứng dụng

1.  Đảm bảo MySQL server đang chạy và CSDL đã được cài đặt.
2.  File khởi chạy chính của ứng dụng là `src/ui/Launcher.java`.
3.  Chạy `Launcher.java` như một Java Application từ IDE của bạn.
4.  Màn hình Đăng nhập (`Login.fxml`) sẽ xuất hiện.
    *   Sử dụng thông tin đăng nhập của user bạn đã thêm vào CSDL.
    *   Bạn cũng có thể đăng ký người dùng mới.

## 7. Cấu trúc Project

Market_Place/
├── bin/ # Thư mục output của IDE (chứa file .class, FXML, CSS đã copy)
│ └── ui/
│ ├── *.fxml
│ └── *.css
├── Resources/ # Chứa FXML nguồn, hình ảnh
│ ├── ui/
│ │ └── *.fxml
│ └── Image For Market/ # Hình ảnh của ứng dụng
│ └── *.png, *.gif
├── src/
│ ├── Config/
│ ├── dao/ # Interfaces cho Data Access Objects
│ ├── impl/ # Implementations của DAOs
│ ├── model/ # Các lớp Data Model (POJOs)
│ ├── Services/ # Logic nghiệp vụ và các services
│ ├── test/ # Các lớp Test (ví dụ ProductTest.java)
│ ├── ui/ # Controllers JavaFX và Launcher
│ └── utils/ # Các lớp tiện ích
├── module-info.java # File mô tả module Java
└── README.md # File này


**Lưu ý về vị trí file FXML và hình ảnh:**
*   `Resources/ui/` chứa các file FXML gốc bạn chỉnh sửa.
*   `Resources/Image For Market/` chứa các file hình ảnh.
*   Khi ứng dụng được build, các file này thường được copy vào thư mục output (thường là `bin` hoặc `target/classes`).
*   Các đường dẫn đến hình ảnh trong file FXML (ví dụ `url="@../../src/Image%20For%20Market/..."`) giả định rằng tại thời điểm chạy, file FXML nằm trong một thư mục con (như `ui`) của thư mục output, và thư mục `src` (hoặc cấu trúc tương tự chứa `Image For Market`) cũng được copy vào thư mục output ở cùng cấp với thư mục chứa file FXML. **Cách tốt nhất là đặt thư mục `Image For Market` bên trong `Resources` và tham chiếu trực tiếp từ classpath, ví dụ `/Image For Market/image.png`.** Hoặc đảm bảo cấu trúc thư mục `bin` của bạn phản ánh đúng đường dẫn tương đối này.

## 8. Luồng hoạt động chính (Tổng quan)

1.  **Khởi chạy:** `ui.Launcher` bắt đầu ứng dụng, hiển thị `Login.fxml`.
2.  **Xác thực:** `LoginController` sử dụng `AuthService` để kiểm tra thông tin đăng nhập với CSDL (mật khẩu được kiểm tra qua `PasswordHasher`). `SessionManager` lưu trữ thông tin người dùng đã đăng nhập.
3.  **Đăng ký:** `RegistrationController` sử dụng `AuthService` để tạo người dùng mới.
4.  **Cửa sổ chính:** `MainController` được tải sau khi đăng nhập thành công. Nó quản lý layout chính, menu điều hướng và tải nội dung động vào `mainContentPane`.
5.  **Phân quyền:** Dựa trên vai trò của người dùng (từ `SessionManager`), `MainController` kích hoạt/vô hiệu hóa các mục menu.
6.  **Thao tác dữ liệu:** Controller của mỗi panel (ví dụ: `UserManagementController`) tương tác với DAO tương ứng (ví dụ: `UserDAO`) để thực hiện các thao tác CRUD. DAOs sử dụng `DBConnection` (thông qua `DatabaseManager`) để kết nối CSDL MySQL.
7.  **Dialogs:** Các form thêm/sửa (ví dụ: `UserFormDialog.fxml`) thường được hiển thị dưới dạng dialog modal.

## 9. Những cải tiến tiềm năng

*   Sử dụng hệ thống build (Maven hoặc Gradle) để quản lý thư viện và build project.
*   Cung cấp script SQL để tự động tạo schema CSDL.
*   Xử lý lỗi và phản hồi người dùng tốt hơn.
*   Hoàn thiện chức năng "Cửa hàng của tôi" cho Người bán.
*   Thêm các báo cáo và phân tích chi tiết hơn.
*   Viết Unit test và Integration test.

## 10. Lưu ý quan trọng

*   Đảm bảo đường dẫn đến các file FXML trong các lời gọi `FXMLLoader.load()` là chính xác so với classpath (thường bắt đầu bằng `/` cho đường dẫn tuyệt đối từ gốc resources/class files).
*   Ứng dụng sử dụng JFoenix, đảm bảo file JAR JFoenix đã được cấu hình đúng trong module path hoặc classpath của project. File `module-info.java` cần có `requires com.jfoenix;`.
