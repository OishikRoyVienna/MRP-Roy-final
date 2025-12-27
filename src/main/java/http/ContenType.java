package http;

public enum ContentType {
    TEXT_PLAIN("text/plain"),
    TEXT_HTML("text/html"),
    APPLICATION_JSON("application/json");

    private final String mimeType;

    ContentType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }
}