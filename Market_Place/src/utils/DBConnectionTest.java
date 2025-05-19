package utils;

import java.sql.Connection;
import java.sql.SQLException;

// import utils.DBConnection; // XÓA DÒNG NÀY NẾU LỚP CỦA BẠN TÊN LÀ DatabaseConnection
import utils.DBConnection;// SỬA THÀNH TÊN ĐÚNG CỦA LỚP KẾT NỐI

public class DBConnectionTest {
    public static void main(String[] args) { // Nên xử lý SQLException ở đây hoặc khai báo throws
        Connection conn = null; // Khởi tạo là null
        try {
            // Gọi phương thức getConnection() từ lớp đúng
            conn = DBConnection.getConnection(); // SỬA TÊN LỚP Ở ĐÂY

            if (conn != null) {
                System.out.println("✅ Kết nối database thành công!");
                // Quan trọng: Đóng kết nối sau khi kiểm tra
                conn.close();
                System.out.println("ℹ️ Kết nối đã được đóng.");
            } else {
                // Trường hợp này ít khi xảy ra nếu getConnection() ném SQLException khi thất bại
                System.out.println("❌ Kết nối database thất bại! (getConnection() trả về null)");
            }
        } catch (SQLException e) {
            System.err.println("❌ LỖI KẾT NỐI DATABASE:");
            e.printStackTrace(); // In ra chi tiết lỗi SQLException
        } finally {
            // Đảm bảo kết nối được đóng ngay cả khi có lỗi không mong muốn khác
            if (conn != null) {
                try {
                    if (!conn.isClosed()) {
                        conn.close();
                        System.out.println("ℹ️ Kết nối đã được đóng trong khối finally.");
                    }
                } catch (SQLException ex) {
                    System.err.println("Lỗi khi đóng kết nối trong khối finally:");
                    ex.printStackTrace();
                }
            }
        }
    }
}