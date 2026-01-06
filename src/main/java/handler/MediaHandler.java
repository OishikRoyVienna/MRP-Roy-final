package handler;

import http.ContentType;
import http.Request;
import http.Response;
import http.Status;
import model.MediaEntry;
import service.MediaService;
import service.UserService;
import util.JsonUtil;

import java.util.HashMap;
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
            if (body.containsKey("description")) entry.setDescription((String) body.get("description"));
            if (body.containsKey("releaseYear")) entry.setReleaseYear(((Number) body.get("releaseYear")).intValue());
            if (body.containsKey("genres")) {
                Object genresObj = body.get("genres");
                if (genresObj instanceof List<?>) {
                    @SuppressWarnings("unchecked")
                    List<String> genresList = (List<String>) genresObj;
                    entry.setGenres(genresList.toArray(new String[0]));
                }
            }
            if (body.containsKey("ageRestriction")) {
                entry.setAgeRestriction(((Number) body.get("ageRestriction")).intValue());
            }

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
                return new Response(Status.NOT_FOUND, ContentType.APPLICATION_JSON,
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
            if (body.containsKey("description")) update.setDescription((String) body.get("description"));
            if (body.containsKey("releaseYear")) update.setReleaseYear(((Number) body.get("releaseYear")).intValue());
            if (body.containsKey("genres")) {
                Object genresObj = body.get("genres");
                if (genresObj instanceof List<?>) {
                    @SuppressWarnings("unchecked")
                    List<String> genresList = (List<String>) genresObj;
                    update.setGenres(genresList.toArray(new String[0]));
                }
            }
            if (body.containsKey("ageRestriction")) {
                update.setAgeRestriction(((Number) body.get("ageRestriction")).intValue());
            }

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
        String title = null;
        String genre = null;
        String mediaType = null;
        Integer minAge = null;
        Double minRating = null;

        if (request.getBody() != null && !request.getBody().trim().isEmpty()) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> filters = JsonUtil.fromJson(request.getBody(), Map.class);
                title = (String) filters.get("title");
                genre = (String) filters.get("genre");
                mediaType = (String) filters.get("mediaType");
                if (filters.get("minAge") instanceof Number) {
                    minAge = ((Number) filters.get("minAge")).intValue();
                }
                if (filters.get("minRating") instanceof Number) {
                    minRating = ((Number) filters.get("minRating")).doubleValue();
                }
            } catch (Exception e) {
                return new Response(Status.BAD_REQUEST, ContentType.APPLICATION_JSON,
                        "{\"error\":\"Invalid filter JSON\"}");
            }
        }

        List<MediaEntry> entries;
        try {
            entries = mediaService.list(title, genre, mediaType, minAge, minRating);
        } catch (Exception e) {
            return new Response(Status.INTERNAL_SERVER_ERROR, ContentType.APPLICATION_JSON,
                    "{\"error\":\"Database query failed\"}");
        }

        return new Response(Status.OK, ContentType.APPLICATION_JSON, JsonUtil.toJson(entries));
    }
}