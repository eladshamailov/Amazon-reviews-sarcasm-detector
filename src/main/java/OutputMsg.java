import java.util.ArrayList;

public class OutputMsg {
    Review review;
    int sentiment;
    ArrayList<String> extractedEntities;

    public OutputMsg(Review review, int sentiment, ArrayList<String> extractedEntities) {
        this.review = review;
        this.sentiment = sentiment;
        this.extractedEntities = extractedEntities;
    }

    public Review getReview() {
        return review;
    }

    public void setReview(Review review) {
        this.review = review;
    }

    public int getSentiment() {
        return sentiment;
    }

    public void setSentiment(int sentiment) {
        this.sentiment = sentiment;
    }

    public ArrayList<String> getExtractedEntities() {
        return extractedEntities;
    }

    public void setExtractedEntities(ArrayList<String> extractedEntities) {
        this.extractedEntities = extractedEntities;
    }
}
