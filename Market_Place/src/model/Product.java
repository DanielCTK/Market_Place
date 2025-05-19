package model;

// Không cần import Timestamp nữa nếu không có createdAt/updatedAt
// import java.sql.Timestamp;

public class Product {
    private String productId;
    private String productCategoryName;
    private Double productWeightG; // Giữ là Double vì nó có thể NULL trong CSDL

    // Constructors
    public Product() {
    }

    public Product(String productId, String productCategoryName, Double productWeightG) {
        this.productId = productId;
        this.productCategoryName = productCategoryName;
        this.productWeightG = productWeightG;
    }

    // Getters and Setters
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductCategoryName() {
        return productCategoryName;
    }

    public void setProductCategoryName(String productCategoryName) {
        this.productCategoryName = productCategoryName;
    }

    public Double getProductWeightG() {
        return productWeightG;
    }

    public void setProductWeightG(Double productWeightG) {
        this.productWeightG = productWeightG;
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId='" + productId + '\'' +
                ", productCategoryName='" + productCategoryName + '\'' +
                ", productWeightG=" + productWeightG +
                '}';
    }
}