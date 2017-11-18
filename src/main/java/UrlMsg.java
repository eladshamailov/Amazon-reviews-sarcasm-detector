public class UrlMsg {
    int action;
    String bucketName;
    String key;
    String URL;

    public UrlMsg(String bucketName, String key, String URL) {
        this.action = Actions.URL.ordinal();
        this.bucketName = bucketName;
        this.key = key;
        this.URL = URL;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }
}

