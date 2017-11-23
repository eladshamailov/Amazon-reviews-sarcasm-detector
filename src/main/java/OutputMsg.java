public class OutputMsg {
    ReviewResponse reviewResponse;
    boolean lastMsg;

    public OutputMsg(ReviewResponse reviewResponse, int sentiment) {
        this.reviewResponse = reviewResponse;
        this.lastMsg = false;
    }

    public ReviewResponse getReviewResponse() {
        return reviewResponse;
    }

    public void setReviewResponse(ReviewResponse reviewResponse) {
        this.reviewResponse = reviewResponse;
    }

    public boolean isLastMsg() {
        return lastMsg;
    }

    public void setLastMsg(boolean lastMsg) {
        this.lastMsg = lastMsg;
    }

}
