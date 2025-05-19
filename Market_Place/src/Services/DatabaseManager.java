package Services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    // THAY ĐỔI CÁC THÔNG SỐ NÀY CHO PHÙ HỢP VỚI MYSQL CỦA BẠN
    private static final String DB_HOST = "localhost"; // Hoặc IP server của bạn
    private static final String DB_PORT = "3306";      // Cổng MySQL mặc định
    private static final String DB_NAME = "marketplace"; // Tên database của bạn
    private static final String DB_USER = "appuser"; // Thay bằng user MySQL của bạn
    private static final String DB_PASS = "app_pass123"; // Thay bằng password của bạn
    private static final String DB_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + "?useSSL=false&serverTimezone=UTC";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
            throw new RuntimeException("Failed to load MySQL JDBC Driver", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        try { if (rs != null) rs.close(); } catch (SQLException e) { /* ignored */ }
        try { if (stmt != null) stmt.close(); } catch (SQLException e) { /* ignored */ }
        try { if (conn != null) conn.close(); } catch (SQLException e) { /* ignored */ }
    }
     public static void close(Connection conn, PreparedStatement pstmt) {
        close(conn, pstmt, null);
    }

    public static void close(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        close(conn, (Statement)pstmt, rs);
    }
}