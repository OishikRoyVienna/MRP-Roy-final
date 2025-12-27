package util;

import com.sun.net.httpserver.HttpExchange;
import http.Method;
import http.Request;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class RequestMapper {

    public Request fromExchange(HttpExchange exchange) throws IOException {
        Request request = new Request();
        request.setMethod(Method.valueOf(exchange.getRequestMethod()));
        request.setPath(exchange.getRequestURI().getPath());

        //Authorization-Header direkt speichern
        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        request.setAuthorization(auth);  //← direkt ins Feld

        InputStream is = exchange.getRequestBody();
        if (is != null) {
            byte[] buf = is.readAllBytes();
            request.setBody(new String(buf, StandardCharsets.UTF_8));
        }

        return request;
    }
}