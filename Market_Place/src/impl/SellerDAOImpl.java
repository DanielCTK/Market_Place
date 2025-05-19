package impl;

import dao.SellerDAO;
import model.Seller;
import utils.DBConnection; // Đảm bảo bạn có lớp này

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SellerDAOImpl implements SellerDAO {

    private Seller mapRowToSeller(ResultSet rs) throws SQLException {
        Seller seller = new Seller();
        seller.setSellerId(rs.getString("seller_id"));
        seller.setSellerState(rs.getString("seller_state")); // seller_state có thể null
        return seller;
    }

    @Override
    public Optional<Seller> findSellerById(String sellerId) throws SQLException {
        String sql = "SELECT seller_id, seller_state FROM sellers WHERE seller_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToSeller(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in findSellerById for ID " + sellerId + ": " + e.getMessage());
            throw e;
        }
        return Optional.empty();
    }

    @Override
    public boolean sellerIdExists(String sellerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM sellers WHERE seller_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in sellerIdExists for ID " + sellerId + ": " + e.getMessage());
            throw e;
        }
        return false;
    }

    @Override
    public boolean addSeller(Seller seller) throws SQLException {
        if (sellerIdExists(seller.getSellerId())) {
            System.err.println("DAO Error: Seller ID '" + seller.getSellerId() + "' already exists.");
            return false;
        }
        String sql = "INSERT INTO sellers (seller_id, seller_state) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, seller.getSellerId());
            if (seller.getSellerState() != null && !seller.getSellerState().trim().isEmpty()) {
                ps.setString(2, seller.getSellerState());
            } else {
                ps.setNull(2, Types.VARCHAR); // seller_state có thể null
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error in addSeller for ID " + seller.getSellerId() + ": " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean updateSeller(Seller seller) throws SQLException {
        // Chỉ cập nhật seller_state
        String sql = "UPDATE sellers SET seller_state = ? WHERE seller_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (seller.getSellerState() != null && !seller.getSellerState().trim().isEmpty()) {
                ps.setString(1, seller.getSellerState());
            } else {
                ps.setNull(1, Types.VARCHAR);
            }
            ps.setString(2, seller.getSellerId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error in updateSeller for ID " + seller.getSellerId() + ": " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean deleteSeller(String sellerId) throws SQLException {
        String sql = "DELETE FROM sellers WHERE seller_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sellerId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error in deleteSeller for ID " + sellerId + ": " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Seller> getSellers(int offset, int limit, String searchTerm, String filterState) throws SQLException {
        List<Seller> sellers = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT seller_id, seller_state FROM sellers ");
        List<String> whereConditions = new ArrayList<>();

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            // Tìm kiếm trong seller_id hoặc seller_state
            whereConditions.add("(LOWER(seller_id) LIKE LOWER(?) OR LOWER(seller_state) LIKE LOWER(?))");
            String searchPattern = "%" + searchTerm.trim() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
        }

        if (filterState != null && !filterState.trim().isEmpty()) {
            whereConditions.add("LOWER(seller_state) = LOWER(?)"); // So sánh chính xác, không phân biệt hoa thường
            params.add(filterState.trim());
        }

        if (!whereConditions.isEmpty()) {
            sqlBuilder.append("WHERE ").append(String.join(" AND ", whereConditions)).append(" ");
        }

        sqlBuilder.append("ORDER BY seller_id ASC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        System.out.println("DAO getSellers SQL: " + sqlBuilder.toString());
        if (!params.isEmpty()) System.out.println("DAO getSellers Params: " + params);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlBuilder.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    sellers.add(mapRowToSeller(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("DAO getSellers - SQLException: " + e.getMessage());
            throw e;
        }
        System.out.println("DAO getSellers - Returning " + sellers.size() + " sellers.");
        return sellers;
    }

    @Override
    public int getTotalSellerCount(String searchTerm, String filterState) throws SQLException {
        List<Object> params = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT COUNT(*) FROM sellers ");
        List<String> whereConditions = new ArrayList<>();

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            whereConditions.add("(LOWER(seller_id) LIKE LOWER(?) OR LOWER(seller_state) LIKE LOWER(?))");
            String searchPattern = "%" + searchTerm.trim() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
        }

        if (filterState != null && !filterState.trim().isEmpty()) {
            whereConditions.add("LOWER(seller_state) = LOWER(?)");
            params.add(filterState.trim());
        }

        if (!whereConditions.isEmpty()) {
            sqlBuilder.append("WHERE ").append(String.join(" AND ", whereConditions));
        }
        
        System.out.println("DAO getTotalSellerCount SQL: " + sqlBuilder.toString());
        if (!params.isEmpty()) System.out.println("DAO getTotalSellerCount Params: " + params);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlBuilder.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("DAO getTotalSellerCount - Count from DB: " + count);
                    return count;
                }
            }
        } catch (SQLException e) {
             System.err.println("DAO getTotalSellerCount - SQLException: " + e.getMessage());
            throw e;
        }
        return 0;
    }

    @Override
    public List<String> getAllDistinctSellerStates() throws SQLException {
        List<String> states = new ArrayList<>();
        String sql = "SELECT DISTINCT seller_state FROM sellers WHERE seller_state IS NOT NULL AND seller_state != '' ORDER BY seller_state ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                states.add(rs.getString("seller_state"));
            }
        } catch (SQLException e) {
            System.err.println("Error in getAllDistinctSellerStates: " + e.getMessage());
            throw e;
        }
        return states;
    }
}