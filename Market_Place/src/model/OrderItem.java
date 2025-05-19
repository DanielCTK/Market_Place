package model;

public class OrderItem {
    private int itemPkValue; // order_items_pk
    private String referencedOrderId; // order_id (FK)
    private String itemProductId;
    private String itemSellerId;
    private Float itemPriceValue;

    public OrderItem() {
    }

    // Constructor có thể cần cho mapRowToOrderItem
    public OrderItem(int pk, String orderId, String productId, String sellerId, Float price) {
        this.itemPkValue = pk;
        this.referencedOrderId = orderId;
        this.itemProductId = productId;
        this.itemSellerId = sellerId;
        this.itemPriceValue = price;
    }
    
    // Constructor khi tạo mới item mà chưa có PK (PK là auto-increment)
    public OrderItem(String orderId, String productId, String sellerId, Float price) {
        this.referencedOrderId = orderId;
        this.itemProductId = productId;
        this.itemSellerId = sellerId;
        this.itemPriceValue = price;
    }


    // Getters
    public int getItemPkValue() {
        return itemPkValue;
    }

    public String getReferencedOrderId() {
        return referencedOrderId;
    }

    public String getItemProductId() {
        return itemProductId;
    }

    public String getItemSellerId() {
        return itemSellerId;
    }

    public Float getItemPriceValue() {
        return itemPriceValue;
    }

    // Setters
    public void setItemPkValue(int itemPkValue) {
        this.itemPkValue = itemPkValue;
    }

    public void setReferencedOrderId(String referencedOrderId) {
        this.referencedOrderId = referencedOrderId;
    }

    public void setItemProductId(String itemProductId) {
        this.itemProductId = itemProductId;
    }

    public void setItemSellerId(String itemSellerId) {
        this.itemSellerId = itemSellerId;
    }

    public void setItemPriceValue(Float itemPriceValue) {
        this.itemPriceValue = itemPriceValue;
    }
}