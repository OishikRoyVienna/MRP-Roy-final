package handler;

import http.Request;
import http.Response;
import http.Status;
import http.ContentType;
import util.JsonUtil;
import model.Rating;
import java.util.Map;
import service.RatingService;
import service.UserService;


public class RatingHandler {
    private final RatingService ratingService = new RatingService();

    public Response handle(Request request) {
        String path = request.getPath();
        String method = request.getMethod();

        // POST /api/media/{id}/ratings
        if ("POST".equals(method) && path.matches("/api/media/\\d+/ratings")) {
            return handleCreateRating(request);
        }
        // PUT /api/ratings/{id}/confirm
        if ("PUT".equals(method) && path.matches("/api/ratings/\\d+/confirm")) {
            return handleConfirmRating(request);
        }
        // GET /api/media/{id}/ratings
        if ("GET".equals(method) && path.matches("/api/media/\\d+/ratings")) {
            return handleGetRatings(request);
        }
        return new Response(Status.BAD_REQUEST, ContentType.APPLICATION_JSON,
                "{\"error\":\"Unsupported rating operation\"}");
    }

    private Response handleCreateRating(Request request) {
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
            Map<String, Object> body = JsonUtil.fromJson(request.getBody(), Map.class);
            Rating rating = new Rating();
            rating.setMediaId(mediaId);
            rating.setUsername(username);
            rating.setStars((Integer) body.get("stars"));
            rating.setComment((String) body.get("comment"));

            Rating saved = ratingService.createRating(rating, username);
            return new Response(Status.CREATED, ContentType.APPLICATION_JSON,
                    JsonUtil.toJson(saved));
        } catch (Exception e) {
            return new Response(Status.BAD_REQUEST, ContentType.APPLICATION_JSON,
                    "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private Response handleConfirmRating(Request request) {
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
            int ratingId = Integer.parseInt(request.getPath().split("/")[3]);
            Rating confirmed = ratingService.confirmRating(ratingId, username);
            return new Response(Status.OK, ContentType.APPLICATION_JSON,
                    JsonUtil.toJson(confirmed));
        } catch (Exception e) {
            return new Response(Status.BAD_REQUEST, ContentType.APPLICATION_JSON,
                    "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private Response handleGetRatings(Request request) {
        try {
            int mediaId = Integer.parseInt(request.getPath().split("/")[3]);
            var ratings = ratingService.getRatingsForMedia(mediaId, true); // nur bestätigte
            return new Response(Status.OK, ContentType.APPLICATION_JSON,
                    JsonUtil.toJson(ratings));
        } catch (Exception e) {
            return new Response(Status.BAD_REQUEST, ContentType.APPLICATION_JSON,
                    "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
}