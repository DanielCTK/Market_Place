package dao;

import model.Seller;
import java.util.List;
import java.util.Optional;

public interface SellerDAO {

    boolean addSeller(Seller seller) throws Exception;

    boolean updateSeller(Seller seller) throws Exception; // Sẽ chỉ cập nhật seller_state dựa trên seller_id

    boolean deleteSeller(String sellerId) throws Exception;

    Optional<Seller> findSellerById(String sellerId) throws Exception;

    List<Seller> getSellers(int offset, int limit, String searchTerm, String filterState) throws Exception;

    int getTotalSellerCount(String searchTerm, String filterState) throws Exception;

    List<String> getAllDistinctSellerStates() throws Exception;

    boolean sellerIdExists(String sellerId) throws Exception;
}