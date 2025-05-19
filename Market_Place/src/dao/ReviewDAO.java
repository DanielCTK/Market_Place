package dao; 
// Hoặc package dao của bạn

import model.Review; // Sử dụng model Review mới
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ReviewDAO {

    boolean createReview(Review review) throws SQLException;

    boolean updateExistingReview(Review review) throws SQLException;

    boolean deleteExistingReview(String reviewId) throws SQLException;

    Optional<Review> findReviewByReviewId(String reviewId) throws SQLException;

    /**
     * Lấy danh sách reviews theo trang, có thể tìm kiếm theo review_id, order_id hoặc lọc theo score.
     */
    List<Review> getPaginatedReviews(int offset, int limit, String searchTerm, Integer filterScore) throws SQLException;

    /**
     * Lấy tổng số reviews, có thể tìm kiếm và lọc.
     */
    int countTotalReviews(String searchTerm, Integer filterScore) throws SQLException;
    
    /**
     * Lấy tất cả các điểm số review duy nhất (1, 2, 3, 4, 5) để làm filter.
     */
    List<Integer> getDistinctReviewScores() throws SQLException;

    /**
     * Kiểm tra xem review_id đã tồn tại chưa.
     */
    boolean checkIfReviewIdExists(String reviewId) throws SQLException;
}