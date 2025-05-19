package utils; // Hoặc package util của bạn

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {

    // Số vòng lặp (work factor) cho BCrypt. Giá trị càng cao càng an toàn nhưng càng tốn thời gian.
    // 10-12 thường là một sự cân bằng tốt.
    private static final int WORK_FACTOR = 12;

    /**
     * Mã hóa mật khẩu dạng thuần (plain text) bằng BCrypt.
     *
     * @param plainPassword Mật khẩu cần mã hóa.
     * @return Chuỗi mật khẩu đã được mã hóa.
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty.");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(WORK_FACTOR));
    }

    /**
     * Kiểm tra mật khẩu dạng thuần (plain text) với mật khẩu đã được mã hóa (lưu trong CSDL).
     *
     * @param plainPassword Mật khẩu người dùng nhập vào khi đăng nhập.
     * @param hashedPassword Mật khẩu đã được mã hóa lưu trong CSDL.
     * @return true nếu mật khẩu khớp, false nếu không.
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || plainPassword.isEmpty() ||
            hashedPassword == null || hashedPassword.isEmpty()) {
            return false; // Hoặc ném IllegalArgumentException tùy theo logic bạn muốn
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // BCrypt.checkpw có thể ném IllegalArgumentException nếu hashedPassword không phải là định dạng bcrypt hợp lệ
            System.err.println("Error checking password: " + e.getMessage());
            return false;
        }
    }
}