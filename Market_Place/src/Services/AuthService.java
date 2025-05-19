package Services;

import model.User;
import utils.DBConnection;
import utils.PasswordHasher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class AuthService {

    public enum RegistrationResult {
        SUCCESS,
        USERNAME_EXISTS,
        EMAIL_EXISTS,
        WEAK_PASSWORD,
        DB_ERROR
    }

    public Optional<User> login(String username, String password) {
        // SỬA Ở ĐÂY: fullname -> full_name
        String sql = "SELECT id, username, password, email, full_name, role FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String hashedPasswordFromDB = rs.getString("password");
                if (PasswordHasher.checkPassword(password, hashedPasswordFromDB)) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    // SỬA Ở ĐÂY: lấy từ cột "full_name"
                    user.setFullName(rs.getString("full_name")); // Giả sử model User vẫn dùng setFullname()
                    user.setRole(rs.getString("role"));
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi đăng nhập: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public RegistrationResult registerUser(String username, String email, String plainPassword) {
        // ... (phần kiểm tra username, email tồn tại giữ nguyên) ...

        String hashedPassword = PasswordHasher.hashPassword(plainPassword);

        // SỬA Ở ĐÂY: fullname -> full_name trong danh sách cột
        String insertUserSql = "INSERT INTO users (username, email, password, full_name, role) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement insertStmt = conn.prepareStatement(insertUserSql)) {

            insertStmt.setString(1, username);
            insertStmt.setString(2, email);
            insertStmt.setString(3, hashedPassword);
            // Giả sử cột full_name trong DB cho phép NULL, vì form đăng ký không có trường này
            insertStmt.setNull(4, java.sql.Types.VARCHAR); // Đặt full_name là NULL
            insertStmt.setString(5, "CUSTOMER");           // Vai trò mặc định

            int rowsAffected = insertStmt.executeUpdate();
            if (rowsAffected > 0) {
                return RegistrationResult.SUCCESS;
            } else {
                System.err.println("Không có dòng nào được thêm vào CSDL khi đăng ký.");
                return RegistrationResult.DB_ERROR;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi chèn người dùng mới: " + e.getMessage());
            e.printStackTrace();
            return RegistrationResult.DB_ERROR;
        }
    }
}