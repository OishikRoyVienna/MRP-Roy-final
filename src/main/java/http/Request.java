package http;

public class Request {

    private Method method;

    private String path;

    private String body;

    private String authorization;

    public Request() {
    }

    public String getAuthorization() { return authorization; }
    public void setAuthorization(String authorization) { this.authorization = authorization; }

    public String getMethod() {
        return method.getVerb();
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}