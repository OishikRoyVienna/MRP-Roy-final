package handler;

import dao.DatabaseManager;
import http.Request;
import http.Response;
import http.Status;
import http.ContentType;
import service.UserService;
import util.JsonUtil;
import java.util.Map;

public class UserHandler {
    private final UserService userService = new UserService();

    public Response handle(Request request) {
        String path = request.getPath();
        String method = request.getMethod();

        //1. /api/reset (MUST-HAVE für Tests)
        if ("POST".equals(method) && "/api/reset".equals(path)) {
            return handleReset();
        }

        //2. /api/users/register
        if ("POST".equals(method) && "/api/users/register".equals(path)) {
            return handleRegister(request);
        }

        //3. /api/users/login
        if ("POST".equals(method) && "/api/users/login".equals(path)) {
            return handleLogin(request);
        }

        //4. /api/users/{username}/profile
        if ("GET".equals(method) && path.matches("/api/users/[^/]+/profile")) {
            return handleProfile(request);
        }

        //Fallback
        return new Response(Status.BAD_REQUEST, ContentType.APPLICATION_JSON,
                "{\"error\":\"Invalid request\"}");
    }

    private Response handleReset() {
        try {
            try (var conn = DatabaseManager.getConnection();
                 var stmt = conn.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS favorites");
                stmt.execute("DROP TABLE IF EXISTS ratings");
                stmt.execute("DROP TABLE IF EXISTS media_entries");
                stmt.execute("DROP TABLE IF EXISTS users");
            }
            DatabaseManager.initializeDatabase();
            return new Response(Status.OK, ContentType.APPLICATION_JSON,
                    "{\"message\":\"DB reset\"}");
        } catch (Exception e) {
            return new Response(Status.INTERNAL_SERVER_ERROR, ContentType.APPLICATION_JSON,
                    "{\"error\":\"Reset failed\"}");
        }
    }

    private Response handleRegister(Request request) {
        try {
            Map<String, Object> body = JsonUtil.fromJson(request.getBody(), Map.class);
            String username = (String) body.get("username");
            String password = (String) body.get("password");
            if (username == null || password == null) {
                return new Response(Status.BAD_REQUEST, ContentType.APPLICATION_JSON,
                        "{\"error\":\"Username and password required\"}");
            }
            userService.register(username, password);
            return new Response(Status.CREATED, ContentType.APPLICATION_JSON,
                    "{\"message\":\"User registered\"}");
        } catch (IllegalArgumentException e) {
            return new Response(Status.BAD_REQUEST, ContentType.APPLICATION_JSON,
                    "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            return new Response(Status.INTERNAL_SERVER_ERROR, ContentType.APPLICATION_JSON,
                    "{\"error\":\"Registration failed\"}");
        }
    }

    private Response handleLogin(Request request) {
        try {
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
        } catch (Exception e) {
            return new Response(Status.INTERNAL_SERVER_ERROR, ContentType.APPLICATION_JSON,
                    "{\"error\":\"Login failed\"}");
        }
    }

    private Response handleProfile(Request request) {
        try {
            String auth = request.getAuthorization();
            if (auth == null || !auth.startsWith("Bearer ")) {
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
        } catch (Exception e) {
            return new Response(Status.INTERNAL_SERVER_ERROR, ContentType.APPLICATION_JSON,
                    "{\"error\":\"Profile fetch failed\"}");
        }
    }
}