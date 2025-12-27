package http;

public enum Method {
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE");

    private final String verb;

    Method(String verb) {
        this.verb = verb;
    }

    public String getVerb() {
        return verb;
    }
}