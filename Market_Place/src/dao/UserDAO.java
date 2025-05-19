package dao;

import model.User;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface UserDAO {

    Optional<User> findByUsername(String username) throws SQLException; // Changed to throw SQLException for consistency

    Optional<User> findById(int userId) throws SQLException; // Changed to throw SQLException

    @Deprecated
    List<User> getAllUsers() throws SQLException; // Changed to throw SQLException

    boolean addUser(User user) throws SQLException; // Changed to throw SQLException

    boolean updateUser(User user) throws SQLException; // Changed to throw SQLException (for non-password updates)

    boolean updateUserWithPassword(User user) throws SQLException; // For updates including password

    boolean deleteUser(int userId) throws SQLException; // Changed to throw SQLException

    @Deprecated
    List<User> searchUsers(String keyword) throws SQLException; // Changed to throw SQLException

    boolean emailExists(String email, int currentUserId) throws SQLException; // Changed to throw SQLException

    // This method seems redundant if you have updatePassword(int, String) and PasswordHasher util
    // boolean changePassword(int userId, String newHashedPassword) throws SQLException; // Assumes newHashedPassword

    List<User> getUsers(int offset, int limit, String searchTerm) throws SQLException; // Changed to throw SQLException

    int getTotalUserCount(String searchTerm) throws SQLException; // Changed to throw SQLException
    
    // This is effectively the same as findByUsername, can be removed if findByUsername throws SQLException
    // User getUserByUsername(String username) throws SQLException; 

    // Use this for changing password, ensures password is hashed correctly
    boolean updatePassword(int userId, String newHashedPassword) throws SQLException;
}