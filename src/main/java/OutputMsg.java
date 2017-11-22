import java.util.ArrayList;

public class OutputMsg {
    ReviewRespons reviewRespons;
    boolean lastMsg;

    public OutputMsg(ReviewRespons reviewRespons, int sentiment) {
        this.reviewRespons = reviewRespons;
        this.lastMsg = false;
    }

    public ReviewRespons getReviewRespons() {
        return reviewRespons;
    }

    public void setReviewRespons(ReviewRespons reviewRespons) {
        this.reviewRespons = reviewRespons;
    }

    public boolean isLastMsg() {
        return lastMsg;
    }

    public void setLastMsg(boolean lastMsg) {
        this.lastMsg = lastMsg;
    }

}
