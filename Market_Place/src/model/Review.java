package model; 
// Hoặc package model của bạn

public class Review {
    private String reviewId;         // review_id
    private String orderId;          // order_id (tham chiếu đến order nào)
    private Integer reviewScore;      // review_score (Integer để cho phép null)
    private String reviewComment;    // review_comment_message

    public Review() {
    }

    // Constructor đầy đủ
    public Review(String reviewId, String orderId, Integer reviewScore, String reviewComment) {
        this.reviewId = reviewId;
        this.orderId = orderId;
        this.reviewScore = reviewScore;
        this.reviewComment = reviewComment;
    }
    
    // Constructor khi thêm mới (review_id có thể người dùng nhập)
    public Review(String orderId, Integer reviewScore, String reviewComment) {
        this.orderId = orderId;
        this.reviewScore = reviewScore;
        this.reviewComment = reviewComment;
    }


    // Getters
    public String getReviewId() {
        return reviewId;
    }

    public String getOrderId() {
        return orderId;
    }

    public Integer getReviewScore() {
        return reviewScore;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    // Setters
    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setReviewScore(Integer reviewScore) {
        this.reviewScore = reviewScore;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }

    @Override
    public String toString() {
        return "Review{" +
               "reviewId='" + reviewId + '\'' +
               ", orderId='" + orderId + '\'' +
               ", reviewScore=" + reviewScore +
               ", reviewComment='" + (reviewComment != null && reviewComment.length() > 30 ? reviewComment.substring(0, 30) + "..." : reviewComment) + '\'' +
               '}';
    }
}