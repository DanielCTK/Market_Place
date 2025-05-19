package impl;

import dao.OrderNewDAO; // Sử dụng Interface mới
import model.Order;
import model.OrderItem;
import utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderNewDAOImpl implements OrderNewDAO { // Implement Interface mới

    private Order mapRowDataToOrderObject(ResultSet rs) throws SQLException {
        Order order = new Order();
        // Sử dụng tên getter/setter mới từ model.Order
        order.setOrderIdValue(rs.getString("order_id"));
        order.setOrderTimestampValue(rs.getTimestamp("timestamp"));
        order.setCustomerContactValue(rs.getString("customer_contact"));
        return order;
    }

    private OrderItem mapRowDataToOrderItemObject(ResultSet rs) throws SQLException {
        OrderItem item = new OrderItem();
        // Sử dụng tên getter/setter mới từ model.OrderItem
        item.setItemPkValue(rs.getInt("order_items_pk"));
        item.setReferencedOrderId(rs.getString("order_id"));
        item.setItemProductId(rs.getString("product_id"));
        item.setItemSellerId(rs.getString("seller_id"));
        
        float priceVal = rs.getFloat("price");
        if (rs.wasNull()) {
            item.setItemPriceValue(null);
        } else {
            item.setItemPriceValue(priceVal);
        }
        return item;
    }

    @Override
    public boolean checkOrderIdExists(String orderId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM orders WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
    
    @Override
    public List<OrderItem> fetchOrderItemsListByOrderId(String orderId) throws SQLException {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT order_items_pk, order_id, product_id, seller_id, price FROM order_items WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRowDataToOrderItemObject(rs));
                }
            }
        }
        return items;
    }

    @Override
    public Optional<Order> fetchOrderById(String orderId) throws SQLException {
        String sql = "SELECT order_id, timestamp, customer_contact FROM orders WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order order = mapRowDataToOrderObject(rs);
                    order.setOrderItemsList(fetchOrderItemsListByOrderId(orderId)); 
                    return Optional.of(order);
                }
            }
        }
        return Optional.empty();
    }
    
    @Override
    public Optional<OrderItem> fetchOrderItemByPk(int itemPkValue) throws SQLException {
        String sql = "SELECT order_items_pk, order_id, product_id, seller_id, price FROM order_items WHERE order_items_pk = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemPkValue);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowDataToOrderItemObject(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean addNewOrder(Order order) throws SQLException {
        if (checkOrderIdExists(order.getOrderIdValue())) {
            System.err.println("DAO Error: Order ID '" + order.getOrderIdValue() + "' already exists.");
            return false; 
        }
        String orderSql = "INSERT INTO orders (order_id, timestamp, customer_contact) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement psOrder = conn.prepareStatement(orderSql)) {
            
            psOrder.setString(1, order.getOrderIdValue());
            psOrder.setTimestamp(2, order.getOrderTimestampValue() != null ? order.getOrderTimestampValue() : new Timestamp(System.currentTimeMillis()));
            psOrder.setString(3, order.getCustomerContactValue());
            
            return psOrder.executeUpdate() > 0;
        }
    }
    
    @Override
    public boolean updateExistingOrderInfo(Order order) throws SQLException {
        String sql = "UPDATE orders SET timestamp = ?, customer_contact = ? WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, order.getOrderTimestampValue());
            ps.setString(2, order.getCustomerContactValue());
            ps.setString(3, order.getOrderIdValue());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean removeOrder(String orderId) throws SQLException {
        String deleteItemsSql = "DELETE FROM order_items WHERE order_id = ?";
        String deleteOrderSql = "DELETE FROM orders WHERE order_id = ?";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement psItems = conn.prepareStatement(deleteItemsSql)) {
                psItems.setString(1, orderId);
                psItems.executeUpdate(); 
            }
            
            try (PreparedStatement psOrder = conn.prepareStatement(deleteOrderSql)) {
                psOrder.setString(1, orderId);
                if (psOrder.executeUpdate() == 0) { 
                    conn.rollback(); 
                    return false; 
                }
            }
            conn.commit(); 
            return true;
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true); 
                conn.close();
            }
        }
    }

    @Override
    public List<Order> fetchPaginatedOrders(int offset, int limit, String searchTerm) throws SQLException {
        List<Order> orders = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT order_id, timestamp, customer_contact FROM orders ");
        
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            sqlBuilder.append("WHERE (LOWER(order_id) LIKE LOWER(?) OR LOWER(customer_contact) LIKE LOWER(?)) ");
            String searchPattern = "%" + searchTerm.trim() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
        }
        sqlBuilder.append("ORDER BY timestamp DESC, order_id ASC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlBuilder.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapRowDataToOrderObject(rs));
                }
            }
        }
        return orders;
    }

    @Override
    public int countTotalOrders(String searchTerm) throws SQLException {
        List<Object> params = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT COUNT(*) FROM orders ");
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            sqlBuilder.append("WHERE (LOWER(order_id) LIKE LOWER(?) OR LOWER(customer_contact) LIKE LOWER(?))");
            String searchPattern = "%" + searchTerm.trim() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
        }
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlBuilder.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    @Override
    public boolean addNewOrderItem(OrderItem item) throws SQLException {
        String itemSql = "INSERT INTO order_items (order_id, product_id, seller_id, price) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection(); 
             PreparedStatement psItem = conn.prepareStatement(itemSql, Statement.RETURN_GENERATED_KEYS)) {
            psItem.setString(1, item.getReferencedOrderId());
            psItem.setString(2, item.getItemProductId());
            psItem.setString(3, item.getItemSellerId());
            if (item.getItemPriceValue() != null) {
                psItem.setFloat(4, item.getItemPriceValue());
            } else {
                psItem.setNull(4, Types.FLOAT);
            }
            if (psItem.executeUpdate() > 0) {
                 try (ResultSet generatedKeys = psItem.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        item.setItemPkValue(generatedKeys.getInt(1)); 
                    }
                }
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean updateExistingOrderItem(OrderItem item) throws SQLException {
        String sql = "UPDATE order_items SET product_id = ?, seller_id = ?, price = ? WHERE order_items_pk = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getItemProductId());
            ps.setString(2, item.getItemSellerId());
             if (item.getItemPriceValue() != null) {
                ps.setFloat(3, item.getItemPriceValue());
            } else {
                ps.setNull(3, Types.FLOAT);
            }
            ps.setInt(4, item.getItemPkValue());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean removeOrderItem(int itemPkValue) throws SQLException {
        String sql = "DELETE FROM order_items WHERE order_items_pk = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemPkValue);
            return ps.executeUpdate() > 0;
        }
    }
}