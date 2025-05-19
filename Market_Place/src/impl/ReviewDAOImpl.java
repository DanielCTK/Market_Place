package impl; 
// Hoặc package impl của bạn

import dao.ReviewDAO; // Sử dụng interface ReviewDAO mới
import model.Review;  // Sử dụng model Review mới
import utils.DBConnection; // Đảm bảo bạn có lớp này

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReviewDAOImpl implements ReviewDAO {

    private Review mapResultSetToReview(ResultSet rs) throws SQLException {
        Review review = new Review();
        review.setReviewId(rs.getString("review_id"));
        review.setOrderId(rs.getString("order_id")); // order_id có thể null

        int score = rs.getInt("review_score");
        if (rs.wasNull()) {
            review.setReviewScore(null);
        } else {
            review.setReviewScore(score);
        }
        
        review.setReviewComment(rs.getString("review_comment_message")); // comment có thể null
        return review;
    }

    @Override
    public boolean checkIfReviewIdExists(String reviewId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM order_reviews WHERE review_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reviewId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error in checkIfReviewIdExists for ID " + reviewId + ": " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Optional<Review> findReviewByReviewId(String reviewId) throws SQLException {
        String sql = "SELECT review_id, order_id, review_score, review_comment_message FROM order_reviews WHERE review_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reviewId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToReview(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in findReviewByReviewId for ID " + reviewId + ": " + e.getMessage());
            throw e;
        }
        return Optional.empty();
    }

    @Override
    public boolean createReview(Review review) throws SQLException {
        if (review.getReviewId() == null || review.getReviewId().trim().isEmpty()) {
            System.err.println("DAO Error: Review ID cannot be null or empty for creation.");
            return false;
        }
        if (checkIfReviewIdExists(review.getReviewId())) {
            System.err.println("DAO Error: Review ID '" + review.getReviewId() + "' already exists.");
            return false;
        }
        // order_id, review_score, review_comment_message có thể NULL dựa trên schema
        String sql = "INSERT INTO order_reviews (review_id, order_id, review_score, review_comment_message) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, review.getReviewId());

            if (review.getOrderId() != null && !review.getOrderId().trim().isEmpty()) {
                ps.setString(2, review.getOrderId());
            } else {
                ps.setNull(2, Types.VARCHAR);
            }

            if (review.getReviewScore() != null) {
                ps.setInt(3, review.getReviewScore());
            } else {
                ps.setNull(3, Types.INTEGER);
            }

            if (review.getReviewComment() != null) {
                ps.setString(4, review.getReviewComment());
            } else {
                ps.setNull(4, Types.VARCHAR); // Hoặc Types.LONGVARCHAR nếu TEXT map sang đó
            }
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error in createReview for ID " + review.getReviewId() + ": " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean updateExistingReview(Review review) throws SQLException {
        if (review.getReviewId() == null || review.getReviewId().trim().isEmpty()) {
            System.err.println("DAO Error: Review ID cannot be null or empty for update.");
            return false;
        }
        // Cập nhật tất cả các trường có thể thay đổi, dựa trên review_id
        String sql = "UPDATE order_reviews SET order_id = ?, review_score = ?, review_comment_message = ? WHERE review_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (review.getOrderId() != null && !review.getOrderId().trim().isEmpty()) {
                ps.setString(1, review.getOrderId());
            } else {
                ps.setNull(1, Types.VARCHAR);
            }

            if (review.getReviewScore() != null) {
                ps.setInt(2, review.getReviewScore());
            } else {
                ps.setNull(2, Types.INTEGER);
            }

            if (review.getReviewComment() != null) {
                ps.setString(3, review.getReviewComment());
            } else {
                ps.setNull(3, Types.VARCHAR);
            }
            
            ps.setString(4, review.getReviewId()); // Điều kiện WHERE
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error in updateExistingReview for ID " + review.getReviewId() + ": " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean deleteExistingReview(String reviewId) throws SQLException {
        String sql = "DELETE FROM order_reviews WHERE review_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reviewId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error in deleteExistingReview for ID " + reviewId + ": " + e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Review> getPaginatedReviews(int offset, int limit, String searchTerm, Integer filterScore) throws SQLException {
        List<Review> reviews = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT review_id, order_id, review_score, review_comment_message FROM order_reviews ");
        List<String> whereConditions = new ArrayList<>();

        // Tìm kiếm theo review_id hoặc order_id
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            whereConditions.add("(LOWER(review_id) LIKE LOWER(?) OR LOWER(order_id) LIKE LOWER(?))");
            String searchPattern = "%" + searchTerm.trim() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
        }

        // Lọc theo review_score
        if (filterScore != null && filterScore >= 1 && filterScore <= 5) { // Giả sử score từ 1-5
            whereConditions.add("review_score = ?");
            params.add(filterScore);
        }

        if (!whereConditions.isEmpty()) {
            sqlBuilder.append("WHERE ").append(String.join(" AND ", whereConditions)).append(" ");
        }

        sqlBuilder.append("ORDER BY review_id ASC LIMIT ? OFFSET ?"); // Hoặc sắp xếp theo tiêu chí khác
        params.add(limit);
        params.add(offset);

        System.out.println("DAO getPaginatedReviews SQL: " + sqlBuilder.toString());
        if (!params.isEmpty()) System.out.println("DAO getPaginatedReviews Params: " + params);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlBuilder.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reviews.add(mapResultSetToReview(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("DAO getPaginatedReviews - SQLException: " + e.getMessage());
            throw e;
        }
        System.out.println("DAO getPaginatedReviews - Returning " + reviews.size() + " reviews.");
        return reviews;
    }

    @Override
    public int countTotalReviews(String searchTerm, Integer filterScore) throws SQLException {
        List<Object> params = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT COUNT(*) FROM order_reviews ");
        List<String> whereConditions = new ArrayList<>();

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            whereConditions.add("(LOWER(review_id) LIKE LOWER(?) OR LOWER(order_id) LIKE LOWER(?))");
            String searchPattern = "%" + searchTerm.trim() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
        }

        if (filterScore != null && filterScore >= 1 && filterScore <= 5) {
            whereConditions.add("review_score = ?");
            params.add(filterScore);
        }

        if (!whereConditions.isEmpty()) {
            sqlBuilder.append("WHERE ").append(String.join(" AND ", whereConditions));
        }
        
        System.out.println("DAO countTotalReviews SQL: " + sqlBuilder.toString());
        if (!params.isEmpty()) System.out.println("DAO countTotalReviews Params: " + params);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlBuilder.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("DAO countTotalReviews - Count from DB: " + count);
                    return count;
                }
            }
        } catch (SQLException e) {
             System.err.println("DAO countTotalReviews - SQLException: " + e.getMessage());
            throw e;
        }
        return 0;
    }
    
    @Override
    public List<Integer> getDistinctReviewScores() throws SQLException {
        List<Integer> scores = new ArrayList<>();
        String sql = "SELECT DISTINCT review_score FROM order_reviews WHERE review_score IS NOT NULL ORDER BY review_score ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                scores.add(rs.getInt("review_score"));
            }
        } catch (SQLException e) {
            System.err.println("Error in getDistinctReviewScores: " + e.getMessage());
            throw e;
        }
        return scores;
    }
}