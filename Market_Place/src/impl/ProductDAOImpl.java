package impl;

import dao.ProductDAO;
import model.Product;
import utils.DBConnection; // Đảm bảo bạn có lớp này

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductDAOImpl implements ProductDAO {

    private Product mapRowToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setProductId(rs.getString("product_id"));
        product.setProductCategoryName(rs.getString("product_category_name"));
        
        double weight = rs.getDouble("product_weight_g");
        if (rs.wasNull()) {
            product.setProductWeightG(null);
        } else {
            product.setProductWeightG(weight);
        }
        return product;
    }

    @Override
    public Optional<Product> findById(String productId) throws SQLException {
        String sql = "SELECT product_id, product_category_name, product_weight_g FROM products WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToProduct(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in findById for ID " + productId + ": " + e.getMessage());
            throw e;
        }
        return Optional.empty();
    }

    @Override
    public boolean productIdExists(String productId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM products WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in productIdExists for ID " + productId + ": " + e.getMessage());
            throw e;
        }
        return false;
    }

    @Override
    public boolean addProduct(Product product) throws SQLException {
        if (productIdExists(product.getProductId())) {
            System.err.println("DAO Error: Product ID '" + product.getProductId() + "' already exists. Add operation aborted.");
            return false; 
        }
        String sql = "INSERT INTO products (product_id, product_category_name, product_weight_g) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getProductId());
            ps.setString(2, product.getProductCategoryName());
            if (product.getProductWeightG() != null) {
                ps.setDouble(3, product.getProductWeightG());
            } else {
                ps.setNull(3, Types.DOUBLE);
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error in addProduct for ID " + product.getProductId() + ": " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean updateProduct(Product product) throws SQLException {
        String sql = "UPDATE products SET product_category_name = ?, product_weight_g = ? WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getProductCategoryName());
            if (product.getProductWeightG() != null) {
                ps.setDouble(2, product.getProductWeightG());
            } else {
                ps.setNull(2, Types.DOUBLE);
            }
            ps.setString(3, product.getProductId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error in updateProduct for ID " + product.getProductId() + ": " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean deleteProduct(String productId) throws SQLException {
        String sql = "DELETE FROM products WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error in deleteProduct for ID " + productId + ": " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Product> getProducts(int offset, int limit, String searchTerm, String categoryFilter) throws SQLException {
        List<Product> products = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT product_id, product_category_name, product_weight_g FROM products ");
        List<String> whereConditions = new ArrayList<>();

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            whereConditions.add("(LOWER(product_id) LIKE LOWER(?) OR LOWER(product_category_name) LIKE LOWER(?))");
            String searchPattern = "%" + searchTerm.trim() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
        }

        if (categoryFilter != null && !categoryFilter.trim().isEmpty()) {
            whereConditions.add("product_category_name = ?");
            params.add(categoryFilter.trim());
        }

        if (!whereConditions.isEmpty()) {
            sqlBuilder.append("WHERE ").append(String.join(" AND ", whereConditions)).append(" ");
        }

        sqlBuilder.append("ORDER BY product_id ASC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        System.out.println("DAO getProducts SQL: " + sqlBuilder.toString());
        if (!params.isEmpty()) System.out.println("DAO getProducts Params: " + params);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlBuilder.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRowToProduct(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("DAO getProducts - SQLException: " + e.getMessage());
            throw e;
        }
        System.out.println("DAO getProducts - Returning " + products.size() + " products.");
        return products;
    }

    @Override
    public int getTotalProductCount(String searchTerm, String categoryFilter) throws SQLException {
        List<Object> params = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT COUNT(*) FROM products ");
        List<String> whereConditions = new ArrayList<>();

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            whereConditions.add("(LOWER(product_id) LIKE LOWER(?) OR LOWER(product_category_name) LIKE LOWER(?))");
            String searchPattern = "%" + searchTerm.trim() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
        }

        if (categoryFilter != null && !categoryFilter.trim().isEmpty()) {
            whereConditions.add("product_category_name = ?");
            params.add(categoryFilter.trim());
        }

        if (!whereConditions.isEmpty()) {
            sqlBuilder.append("WHERE ").append(String.join(" AND ", whereConditions));
        }
        
        System.out.println("DAO getTotalProductCount SQL: " + sqlBuilder.toString());
        if (!params.isEmpty()) System.out.println("DAO getTotalProductCount Params: " + params);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlBuilder.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("DAO getTotalProductCount - Count from DB: " + count);
                    return count;
                }
            }
        } catch (SQLException e) {
             System.err.println("DAO getTotalProductCount - SQLException: " + e.getMessage());
            throw e;
        }
        return 0;
    }

    @Override
    public List<String> getAllDistinctCategories() throws SQLException {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT DISTINCT product_category_name FROM products WHERE product_category_name IS NOT NULL AND product_category_name != '' ORDER BY product_category_name ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                categories.add(rs.getString("product_category_name"));
            }
        } catch (SQLException e) {
            System.err.println("Error in getAllDistinctCategories: " + e.getMessage());
            throw e;
        }
        return categories;
    }
}