package handler;

import http.Request;
import http.Response;
import http.Status;
import http.ContentType;
import util.JsonUtil;
import service.UserService;

import java.util.Map;

public class UserHandler {
    private final UserService userService = new UserService();

    public Response handle(Request request) {
        String path = request.getPath();
        String method = request.getMethod();

        if ("POST".equals(method) && "/api/users/register".equals(path)) {
            return handleRegister(request);
        } else if ("POST".equals(method) && "/api/users/login".equals(path)) {
            return handleLogin(request);
        } else if ("GET".equals(method) && path.matches("/api/users/[^/]+/profile")) {
            return handleProfile(request);
        }
        return new Response(Status.BAD_REQUEST, ContentType.APPLICATION_JSON,
                "{\"error\":\"Invalid request\"}");
    }

    private Response handleRegister(Request request) {
        Map<String, Object> body = JsonUtil.fromJson(request.getBody(), Map.class);
        String username = (String) body.get("username");
        String password = (String) body.get("password");

        if (username == null || password == null) {
            return new Response(Status.BAD_REQUEST, ContentType.APPLICATION_JSON,
                    "{\"error\":\"Username and password required\"}");
        }

        try {
            userService.register(username, password);
            return new Response(Status.CREATED, ContentType.APPLICATION_JSON,
                    "{\"message\":\"User registered\"}");
        } catch (IllegalArgumentException e) {
            return new Response(Status.BAD_REQUEST, ContentType.APPLICATION_JSON,
                    "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private Response handleLogin(Request request) {
        Map<String, Object> body = JsonUtil.fromJson(request.getBody(), Map.class);
        String username = (String) body.get("username");
        String password = (String) body.get("password");

        if (username == null || password == null) {
            return new Response(Status.BAD_REQUEST, ContentType.APPLICATION_JSON,
                    "{\"error\":\"Username and password required\"}");
        }

        String token = userService.login(username, password);
        if (token == null) {
            return new Response(Status.BAD_REQUEST, ContentType.APPLICATION_JSON,
                    "{\"error\":\"Invalid credentials\"}");
        }
        return new Response(Status.OK, ContentType.APPLICATION_JSON,
                "{\"token\":\"" + token + "\"}");
    }

    private Response handleProfile(Request request) {
        String auth = request.getAuthorization();        if (auth == null || !auth.startsWith("Bearer ")) {
            return new Response(Status.BAD_REQUEST, ContentType.APPLICATION_JSON,
                    "{\"error\":\"Missing or invalid token\"}");
        }
        String token = auth.substring(7);
        String requester = UserService.getUsernameByToken(token);
        if (requester == null) {
            return new Response(Status.BAD_REQUEST, ContentType.APPLICATION_JSON,
                    "{\"error\":\"Invalid token\"}");
        }

        String targetUser = request.getPath().split("/")[3];
        if (!requester.equals(targetUser)) {
            return new Response(Status.BAD_REQUEST, ContentType.APPLICATION_JSON,
                    "{\"error\":\"Access denied\"}");
        }

        var profile = userService.getProfile(targetUser);
        return new Response(Status.OK, ContentType.APPLICATION_JSON,
                JsonUtil.toJson(profile));
    }
}