import java.util.ArrayList;

public class ReviewMsg {
    int action;
    ArrayList<Review> reviews= new ArrayList<Review>();
    String title;

    public ReviewMsg(ArrayList<Review> reviews, String title) {
        action=Actions.REVIEW.ordinal();
        this.reviews = reviews;
        this.title = title;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public ArrayList<Review> getReviews() {
        return reviews;
    }

    public void setReviews(ArrayList<Review> reviews) {
        this.reviews = reviews;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
