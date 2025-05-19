package utils; // Hoặc package của bạn
import utils.PasswordHasher; // Hoặc đường dẫn đầy đủ đến PasswordHasher của bạn

public class GenerateHashes {
    public static void main(String[] args) {
        String adminPass = "123456";
        String hameiPass = "thaouyn"; // Hoặc mật khẩu bạn muốn cho hamei
        String danhctkPass = "danh_pass"; // Mật khẩu bạn muốn cho danhctk

        System.out.println("Admin ('" + adminPass + "') Hash: " + PasswordHasher.hashPassword(adminPass));
        System.out.println("Hamei ('" + hameiPass + "') Hash: " + PasswordHasher.hashPassword(hameiPass));
        System.out.println("DanhCTK ('" + danhctkPass + "') Hash: " + PasswordHasher.hashPassword(danhctkPass));
    }
}