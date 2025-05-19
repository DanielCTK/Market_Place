//package test;
//
//import model.Product;
//
//import java.util.List;
//
//import impl.ProductDAOImpl;
//
//public class ProductTest {
//    public static void main(String[] args) throws Exception {
//        ProductDAOImpl dao = new ProductDAOImpl();
//
//        // 1) Thêm mới
//        Product newP = new Product(0, "Sản phẩm A", "Loại A", 100.0, 50);
//        System.out.println("Add: " + dao.addProduct(newP));
//
//        // 2) Lấy danh sách
//        List<Product> list = dao.getAllProducts();
//        list.forEach(p -> System.out.println(p.getId() + " - " + p.getName()));
//
//        // 3) Cập nhật (ví dụ id = 1)
//        Product up = new Product(1, "SP A cập nhật", "Loại A", 120.0, 40);
//        System.out.println("Update: " + dao.updateProduct(up));
//
//        // 4) Xóa (ví dụ id = 2)
//        System.out.println("Delete: " + dao.deleteProduct(2));
//    }
//}
