package utils; // Hoặc package bạn dùng cho các lớp tiện ích

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // THAY ĐỔI CÁC THÔNG TIN NÀY CHO PHÙ HỢP VỚI CSDL MYSQL CỦA BẠN
    private static final String DB_URL = "jdbc:mysql://localhost:3306/marketplace"; // Thay 'ten_database_cua_ban'
    private static final String DB_USER = "appuser"; // Thay bằng user MySQL
    private static final String DB_PASSWORD = "app_pass123"; // Thay bằng password MySQL

    static {
        try {
            // Đảm bảo MySQL JDBC driver đã được tải
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi: Không tìm thấy MySQL JDBC Driver! Hãy thêm vào classpath.");
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // Bạn có thể thêm các phương thức đóng kết nối, statement, resultset ở đây nếu muốn
    public static void closeQuietly(AutoCloseable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception e) {
                // Log hoặc bỏ qua
            }
        }
    }
}