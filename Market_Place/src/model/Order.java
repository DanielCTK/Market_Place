package model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private String orderIdValue; // Đổi tên biến để tránh nhầm lẫn
    private Timestamp orderTimestampValue;
    private String customerContactValue;
    private List<OrderItem> orderItemsList;

    public Order() {
        this.orderItemsList = new ArrayList<>();
    }

    // Constructor nhận tất cả các trường (ngoại trừ list items)
    public Order(String id, Timestamp ts, String contact) {
        this.orderIdValue = id;
        this.orderTimestampValue = ts;
        this.customerContactValue = contact;
        this.orderItemsList = new ArrayList<>();
    }

    // Getters
    public String getOrderIdValue() {
        return orderIdValue;
    }

    public Timestamp getOrderTimestampValue() {
        return orderTimestampValue;
    }

    public String getCustomerContactValue() {
        return customerContactValue;
    }

    public List<OrderItem> getOrderItemsList() {
        return orderItemsList;
    }

    // Setters
    public void setOrderIdValue(String orderIdValue) {
        this.orderIdValue = orderIdValue;
    }

    public void setOrderTimestampValue(Timestamp orderTimestampValue) {
        this.orderTimestampValue = orderTimestampValue;
    }

    public void setCustomerContactValue(String customerContactValue) {
        this.customerContactValue = customerContactValue;
    }

    public void setOrderItemsList(List<OrderItem> orderItemsList) {
        this.orderItemsList = orderItemsList;
    }

    public void addOrderItemToOrder(OrderItem item) {
        if (this.orderItemsList == null) {
            this.orderItemsList = new ArrayList<>();
        }
        this.orderItemsList.add(item);
        if (this.orderIdValue != null) {
             item.setReferencedOrderId(this.orderIdValue); // Đảm bảo item có order_id đúng
        }
    }
}