package handler;

import http.Request;
import http.Response;
import http.Status;
import http.ContentType;
import service.MediaService;
import service.UserService;
import model.MediaEntry;
import util.JsonUtil;

import java.util.List;
import java.util.Map;

public class MediaHandler {
    private final MediaService mediaService = new MediaService();

    public Response handle(Request request) {
        String path = request.getPath();
        String method = request.getMethod();

        if ("POST".equals(method) && "/api/media".equals(path)) {
            return handleCreate(request);
        } else if ("GET".equals(method) && path.matches("/api/media/\\d+")) {
            return handleGet(request);
        } else if ("PUT".equals(method) && path.matches("/api/media/\\d+")) {
            return handleUpdate(request);
        } else if ("DELETE".equals(method) && path.matches("/api/media/\\d+")) {
            return handleDelete(request);
        } else if ("GET".equals(method) && "/api/media".equals(path)) {
            return handleList(request);
        }
        return new Response(Status.BAD_REQUEST, ContentType.APPLICATION_JSON,
                "{\"error\":\"Invalid request\"}");
    }

    private Response handleCreate(Request request) {
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
            Map<String, Object> body = JsonUtil.fromJson(request.getBody(), Map.class);
            MediaEntry entry = new MediaEntry();
            entry.setTitle((String) body.get("title"));
            entry.setMediaType((String) body.get("mediaType"));
            entry.setCreatorUsername(username);
            // Optional: description, releaseYear, etc.

            MediaEntry saved = mediaService.create(entry);
            return new Response(Status.CREATED, ContentType.APPLICATION_JSON,
                    JsonUtil.toJson(saved));
        } catch (Exception e) {
            return new Response(Status.BAD_REQUEST, ContentType.APPLICATION_JSON,
                    "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private Response handleGet(Request request) {
        try {
            String idStr = request.getPath().split("/")[3];
            int id = Integer.parseInt(idStr);
            MediaEntry entry = mediaService.getById(id);
            if (entry == null) {
                return new Response(Status.BAD_REQUEST, ContentType.APPLICATION_JSON,
                        "{\"error\":\"Media not found\"}");
            }
            return new Response(Status.OK, ContentType.APPLICATION_JSON,
                    JsonUtil.toJson(entry));
        } catch (NumberFormatException e) {
            return new Response(Status.BAD_REQUEST, ContentType.APPLICATION_JSON,
                    "{\"error\":\"Invalid ID\"}");
        }
    }

    private Response handleUpdate(Request request) {
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
            String idStr = request.getPath().split("/")[3];
            int id = Integer.parseInt(idStr);
            MediaEntry existing = mediaService.getById(id);
            if (existing == null || !existing.getCreatorUsername().equals(username)) {
                return new Response(Status.BAD_REQUEST, ContentType.APPLICATION_JSON,
                        "{\"error\":\"Only creator can edit\"}");
            }

            Map<String, Object> body = JsonUtil.fromJson(request.getBody(), Map.class);
            MediaEntry update = new MediaEntry();
            update.setId(id);
            update.setTitle((String) body.get("title"));
            update.setMediaType((String) body.get("mediaType"));
            update.setCreatorUsername(username);

            mediaService.update(update);
            return new Response(Status.OK, ContentType.APPLICATION_JSON,
                    "{\"message\":\"Updated\"}");
        } catch (Exception e) {
            return new Response(Status.BAD_REQUEST, ContentType.APPLICATION_JSON,
                    "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private Response handleDelete(Request request) {
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
            String idStr = request.getPath().split("/")[3];
            int id = Integer.parseInt(idStr);
            MediaEntry existing = mediaService.getById(id);
            if (existing == null || !existing.getCreatorUsername().equals(username)) {
                return new Response(Status.BAD_REQUEST, ContentType.APPLICATION_JSON,
                        "{\"error\":\"Only creator can delete\"}");
            }

            mediaService.delete(id);
            return new Response(Status.OK, ContentType.APPLICATION_JSON,
                    "{\"message\":\"Deleted\"}");
        } catch (Exception e) {
            return new Response(Status.BAD_REQUEST, ContentType.APPLICATION_JSON,
                    "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private Response handleList(Request request) {
        String query = request.getPath(); // oder erweitere Request um getQuery()
        String titleFilter = null;
        List<MediaEntry> entries = mediaService.list(titleFilter);
        return new Response(Status.OK, ContentType.APPLICATION_JSON,
                JsonUtil.toJson(entries));
    }
}