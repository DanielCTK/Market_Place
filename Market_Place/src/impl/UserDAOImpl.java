package impl;

import dao.UserDAO;
import model.User;
import utils.DBConnection;      // Your DB connection class
import utils.PasswordHasher;  // Your password hashing utility

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAOImpl implements UserDAO {

    @Override
    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password, full_name, email, role, created_at, updated_at FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs, true));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findById(int userId) throws SQLException {
        String sql = "SELECT id, username, password, full_name, email, role, created_at, updated_at FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs, true));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    @Deprecated
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, full_name, email, role, created_at, updated_at FROM users ORDER BY username ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapRowToUser(rs, false));
            }
        }
        return users;
    }

    @Override
    public boolean addUser(User user) throws SQLException {
        if (findByUsername(user.getUsername()).isPresent()) {
            System.err.println("DAO Error: Username '" + user.getUsername() + "' already exists.");
            // Consider throwing a custom exception here for better error handling in UI
            return false;
        }
        if (user.getEmail() != null && !user.getEmail().isEmpty() && emailExists(user.getEmail(), 0)) {
            System.err.println("DAO Error: Email '" + user.getEmail() + "' already exists.");
            return false;
        }

        String sql = "INSERT INTO users (username, password, full_name, email, role, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, PasswordHasher.hashPassword(user.getPassword())); // Hash raw password from model
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getRole());
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean updateUser(User user) throws SQLException { // For non-password updates
        Optional<User> existingUserOpt = findById(user.getId());
        if (existingUserOpt.isEmpty()) { return false; }
        User existingUser = existingUserOpt.get();

        if (user.getEmail() != null && !user.getEmail().equalsIgnoreCase(existingUser.getEmail()) && emailExists(user.getEmail(), user.getId())) {
            System.err.println("DAO Error: Email '" + user.getEmail() + "' already exists for another user.");
            return false;
        }
        String sql = "UPDATE users SET full_name = ?, email = ?, role = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getRole());
            ps.setInt(4, user.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateUserWithPassword(User user) throws SQLException {
        Optional<User> existingUserOpt = findById(user.getId());
        if (existingUserOpt.isEmpty()) { return false; }
        User existingUser = existingUserOpt.get();
        if (user.getEmail() != null && !user.getEmail().equalsIgnoreCase(existingUser.getEmail()) && emailExists(user.getEmail(), user.getId())) {
            System.err.println("DAO Error: Email '" + user.getEmail() + "' already exists for another user.");
            return false;
        }
        String sql = "UPDATE users SET full_name = ?, email = ?, role = ?, password = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getRole());
            ps.setString(4, PasswordHasher.hashPassword(user.getPassword())); // Hash raw password from model
            ps.setInt(5, user.getId());
            return ps.executeUpdate() > 0;
        }
    }
    
    @Override
    public boolean updatePassword(int userId, String newHashedPassword) throws SQLException {
        // This method expects an already hashed password
        if (newHashedPassword == null || newHashedPassword.isEmpty()) return false;
        String sql = "UPDATE users SET password = ?, updated_at = NOW() WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newHashedPassword);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }


    @Override
    public boolean deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    @Deprecated
    public List<User> searchUsers(String keyword) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, full_name, email, role, created_at, updated_at FROM users " +
                     "WHERE LOWER(username) LIKE LOWER(?) OR LOWER(full_name) LIKE LOWER(?) OR LOWER(email) LIKE LOWER(?) " +
                     "ORDER BY username ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String searchTermSql = "%" + keyword.trim().toLowerCase() + "%";
            ps.setString(1, searchTermSql);
            ps.setString(2, searchTermSql);
            ps.setString(3, searchTermSql);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapRowToUser(rs, false));
                }
            }
        }
        return users;
    }

    @Override
    public boolean emailExists(String email, int currentUserId) throws SQLException {
        if (email == null || email.trim().isEmpty()) return false;
        String sql = "SELECT COUNT(*) FROM users WHERE LOWER(email) = LOWER(?) AND id != ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            ps.setInt(2, currentUserId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    @Override
    public List<User> getUsers(int offset, int limit, String searchTerm) throws SQLException {
        List<User> users = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT id, username, full_name, email, role, created_at, updated_at FROM users ");
        List<Object> params = new ArrayList<>();
        boolean hasSearchTerm = searchTerm != null && !searchTerm.trim().isEmpty();

        if (hasSearchTerm) {
            sqlBuilder.append("WHERE (LOWER(username) LIKE LOWER(?) OR LOWER(full_name) LIKE LOWER(?) OR LOWER(email) LIKE LOWER(?)) ");
            String searchPattern = "%" + searchTerm.trim() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
        }
        sqlBuilder.append("ORDER BY username ASC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlBuilder.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapRowToUser(rs, false));
                }
            }
        }
        return users;
    }

    @Override
    public int getTotalUserCount(String searchTerm) throws SQLException {
        StringBuilder sqlBuilder = new StringBuilder("SELECT COUNT(*) FROM users ");
        List<Object> params = new ArrayList<>();
        boolean hasSearchTerm = searchTerm != null && !searchTerm.trim().isEmpty();

        if (hasSearchTerm) {
            sqlBuilder.append("WHERE (LOWER(username) LIKE LOWER(?) OR LOWER(full_name) LIKE LOWER(?) OR LOWER(email) LIKE LOWER(?))");
            String searchPattern = "%" + searchTerm.trim() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
        }
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlBuilder.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
    
    // getUserByUsername (already implemented as findByUsername)
    // The interface had a duplicate. We'll rely on findByUsername.
    // If you specifically need one that returns User directly and throws SQLException (not Optional):
    public User getUserByUsername(String username) throws SQLException {
        Optional<User> userOpt = findByUsername(username);
        if (userOpt.isPresent()) {
            return userOpt.get();
        }
        // Or throw a more specific "UserNotFoundException"
        throw new SQLException("User not found with username: " + username); 
    }


    private User mapRowToUser(ResultSet rs, boolean includePasswordHash) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        if (includePasswordHash) {
            user.setPassword(rs.getString("password")); // This is the HASHED password from DB
        }
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setRole(rs.getString("role"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setUpdatedAt(rs.getTimestamp("updated_at"));
        return user;
    }
}