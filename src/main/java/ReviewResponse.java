import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReviewResponse {
Map<Review,List<String>> m;
int sentiment;

    public ReviewResponse(Map<Review, List<String>> m, int sentiment) {
        this.m = m;
        this.sentiment=sentiment;
    }

    public Map<Review, List<String>> getM() {
        return m;
    }

    public void setM(Map<Review, List<String>> m) {
        this.m = m;
    }

    public int getSentiment() {
        return sentiment;
    }

    public void setSentiment(int sentiment) {
        this.sentiment = sentiment;
    }
}
