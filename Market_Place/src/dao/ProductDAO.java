package dao;

import model.Product;
import java.sql.SQLException; // <<<< THAY ĐỔI QUAN TRỌNG
import java.util.List;
import java.util.Optional;

public interface ProductDAO {

    boolean addProduct(Product product) throws SQLException; // Sửa thành SQLException

    boolean updateProduct(Product product) throws SQLException; // Sửa thành SQLException

    boolean deleteProduct(String productId) throws SQLException; // Sửa thành SQLException

    Optional<Product> findById(String productId) throws SQLException; // Sửa thành SQLException

    /**
     * Lấy danh sách sản phẩm theo trang, có tìm kiếm theo từ khóa và lọc theo danh mục.
     * Tìm kiếm dựa trên product_id hoặc product_category_name.
     * @param offset Vị trí bắt đầu.
     * @param limit Số lượng sản phẩm mỗi trang.
     * @param searchTerm Từ khóa tìm kiếm (có thể rỗng hoặc null).
     * @param categoryFilter Tên danh mục để lọc (có thể rỗng hoặc null nếu không lọc).
     * @return Danh sách sản phẩm.
     */
    List<Product> getProducts(int offset, int limit, String searchTerm, String categoryFilter) throws SQLException; // Sửa thành SQLException

    /**
     * Lấy tổng số sản phẩm, có tìm kiếm theo từ khóa và lọc theo danh mục.
     * @param searchTerm Từ khóa tìm kiếm (có thể rỗng hoặc null).
     * @param categoryFilter Tên danh mục để lọc (có thể rỗng hoặc null nếu không lọc).
     * @return Tổng số sản phẩm.
     */
    int getTotalProductCount(String searchTerm, String categoryFilter) throws SQLException; // Sửa thành SQLException

    /**
     * Lấy tất cả các tên danh mục sản phẩm duy nhất.
     * @return Danh sách các tên danh mục.
     */
    List<String> getAllDistinctCategories() throws SQLException; // Sửa thành SQLException

    boolean productIdExists(String productId) throws SQLException; // Sửa thành SQLException
}