package dao;

import model.Order;
import model.OrderItem;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface OrderNewDAO { // Tên Interface mới

    boolean addNewOrder(Order order) throws SQLException;
    boolean updateExistingOrderInfo(Order order) throws SQLException;
    boolean removeOrder(String orderId) throws SQLException;
    Optional<Order> fetchOrderById(String orderId) throws SQLException;
    List<Order> fetchPaginatedOrders(int offset, int limit, String searchTerm) throws SQLException;
    int countTotalOrders(String searchTerm) throws SQLException;
    boolean checkOrderIdExists(String orderId) throws SQLException;

    boolean addNewOrderItem(OrderItem item) throws SQLException;
    boolean updateExistingOrderItem(OrderItem item) throws SQLException;
    boolean removeOrderItem(int itemPkValue) throws SQLException;
    Optional<OrderItem> fetchOrderItemByPk(int itemPkValue) throws SQLException;
    List<OrderItem> fetchOrderItemsListByOrderId(String orderId) throws SQLException;
}