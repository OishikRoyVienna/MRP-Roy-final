package handler;

import http.Request;
import http.Response;
import http.Status;
import http.ContentType;
import service.FavoriteService;
import util.JsonUtil;
import service.UserService;

public class FavoriteHandler {
    private final FavoriteService favoriteService = new FavoriteService();

    public Response handle(Request request) {
        String path = request.getPath();
        String method = request.getMethod();

        // POST /api/favorites/{mediaId}
        if ("POST".equals(method) && path.matches("/api/favorites/\\d+")) {
            return handleAddFavorite(request);
        }
        // DELETE /api/favorites/{mediaId}
        if ("DELETE".equals(method) && path.matches("/api/favorites/\\d+")) {
            return handleRemoveFavorite(request);
        }
        // GET /api/favorites
        if ("GET".equals(method) && "/api/favorites".equals(path)) {
            return handleGetFavorites(request);
        }
        return new Response(Status.BAD_REQUEST, ContentType.APPLICATION_JSON,
                "{\"error\":\"Unsupported favorite operation\"}");
    }

    private Response handleAddFavorite(Request request) {
        String auth = request.getAuthorization();
        if (auth == null || !auth.startsWith("Bearer ")) {
            return new Response(Status.BAD_REQUEST, ContentType.APPLICATION_JSON,
                    "{\"error\":\"Missing or invalid token\"}");
        }
        String token = auth.substring(7);
        String username = UserService.getUsernameByToken(token);
        if (username == null) {
            return new Response(Status.BAD_REQUEST, ContentType.APPLICATION_JSON,
                    "{\"error\":\"Invalid token\"}");
        }

        try {
            int mediaId = Integer.parseInt(request.getPath().split("/")[3]);
            favoriteService.addFavorite(username, mediaId);
            return new Response(Status.OK, ContentType.APPLICATION_JSON,
                    "{\"message\":\"Added to favorites\"}");
        } catch (Exception e) {
            return new Response(Status.BAD_REQUEST, ContentType.APPLICATION_JSON,
                    "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private Response handleRemoveFavorite(Request request) {
        String auth = request.getAuthorization();
        if (auth == null || !auth.startsWith("Bearer ")) {
            return new Response(Status.BAD_REQUEST, ContentType.APPLICATION_JSON,
                    "{\"error\":\"Missing or invalid token\"}");
        }
        String token = auth.substring(7);
        String username = UserService.getUsernameByToken(token);
        if (username == null) {
            return new Response(Status.BAD_REQUEST, ContentType.APPLICATION_JSON,
                    "{\"error\":\"Invalid token\"}");
        }

        try {
            int mediaId = Integer.parseInt(request.getPath().split("/")[3]);
            favoriteService.removeFavorite(username, mediaId);
            return new Response(Status.OK, ContentType.APPLICATION_JSON,
                    "{\"message\":\"Removed from favorites\"}");
        } catch (Exception e) {
            return new Response(Status.BAD_REQUEST, ContentType.APPLICATION_JSON,
                    "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private Response handleGetFavorites(Request request) {
        String auth = request.getAuthorization();
        if (auth == null || !auth.startsWith("Bearer ")) {
            return new Response(Status.BAD_REQUEST, ContentType.APPLICATION_JSON,
                    "{\"error\":\"Missing or invalid token\"}");
        }
        String token = auth.substring(7);
        String username = UserService.getUsernameByToken(token);
        if (username == null) {
            return new Response(Status.BAD_REQUEST, ContentType.APPLICATION_JSON,
                    "{\"error\":\"Invalid token\"}");
        }

        try {
            var mediaIds = favoriteService.getFavoriteMediaIds(username);
            return new Response(Status.OK, ContentType.APPLICATION_JSON,
                    JsonUtil.toJson(mediaIds));
        } catch (Exception e) {
            return new Response(Status.BAD_REQUEST, ContentType.APPLICATION_JSON,
                    "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}