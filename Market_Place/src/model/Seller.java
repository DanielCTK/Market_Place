package model;

public class Seller {
    private String sellerId;
    private String sellerState;

    public Seller() {
    }

    public Seller(String sellerId, String sellerState) {
        this.sellerId = sellerId;
        this.sellerState = sellerState;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerState() {
        return sellerState;
    }

    public void setSellerState(String sellerState) {
        this.sellerState = sellerState;
    }

    @Override
    public String toString() {
        return "Seller{" +
               "sellerId='" + sellerId + '\'' +
               ", sellerState='" + sellerState + '\'' +
               '}';
    }
}