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

        // ✅ POST /api/ratings (dein curl nutzt das!)
        if ("POST".equals(method) && "/api/ratings".equals(path)) {
            return handleCreateRatingFromBody(request);
        }
        // ✅ POST /api/media/123/ratings
        if ("POST".equals(method) && path.matches("/api/media/\\d+/ratings")) {
            return handleCreateRatingFromPath(request);
        }
        // ✅ PUT /api/ratings/123
        if ("PUT".equals(method) && path.matches("/api/ratings/\\d+")) {
            return handleUpdateRating(request);
        }
        // ✅ DELETE /api/ratings/123
        if ("DELETE".equals(method) && path.matches("/api/ratings/\\d+")) {
            return handleDeleteRating(request);
        }
        // ✅ PUT /api/ratings/123/confirm
        if ("PUT".equals(method) && path.matches("/api/ratings/\\d+/confirm")) {
            return handleConfirmRating(request);
        }
        // ✅ POST /api/ratings/123/like
        if ("POST".equals(method) && path.matches("/api/ratings/\\d+/like")) {
            return handleLikeRating(request);
        }
        // ✅ GET /api/media/123/ratings
        if ("GET".equals(method) && path.matches("/api/media/\\d+/ratings")) {
            return handleGetRatings(request);
        }

        return new Response(Status.BAD_REQUEST, ContentType.APPLICATION_JSON,
                "{\"error\":\"Unsupported rating operation\"}");
    }

    private Response handleCreateRatingFromBody(Request request) {
        return handleCreateRating(request, () -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = JsonUtil.fromJson(request.getBody(), Map.class);
            Integer mediaId = (Integer) body.get("mediaId");
            if (mediaId == null) throw new IllegalArgumentException("mediaId is required");
            return mediaId;
        });
    }

    private Response handleCreateRatingFromPath(Request request) {
        return handleCreateRating(request, () -> Integer.parseInt(request.getPath().split("/")[3]));
    }

    private Response handleCreateRating(Request request, java.util.concurrent.Callable<Integer> mediaIdProvider) {
        String auth = request.getAuthorization();
        if (auth == null || !auth.startsWith("Bearer ")) {
            return error("Missing or invalid token", Status.BAD_REQUEST);
        }
        String token = auth.substring(7);
        String username = UserService.getUsernameByToken(token);
        if (username == null) {
            return error("Invalid token", Status.BAD_REQUEST);
        }

        try {
            Integer mediaId = mediaIdProvider.call();
            @SuppressWarnings("unchecked")
            Map<String, Object> body = JsonUtil.fromJson(request.getBody(), Map.class);
            Integer stars = (Integer) body.get("stars");
            String comment = (String) body.get("comment");

            if (stars == null || stars < 1 || stars > 5) {
                return error("stars must be 1–5", Status.BAD_REQUEST);
            }
            if (comment == null) {
                return error("comment is required", Status.BAD_REQUEST);
            }

            Rating rating = new Rating();
            rating.setMediaId(mediaId);
            rating.setUsername(username);
            rating.setStars(stars);
            rating.setComment(comment);

            Rating saved = ratingService.createRating(rating, username);
            return new Response(Status.CREATED, ContentType.APPLICATION_JSON,
                    JsonUtil.toJson(saved));
        } catch (Exception e) {
            return error("Create rating failed: " + e.getMessage(), Status.BAD_REQUEST);
        }
    }

    private Response handleUpdateRating(Request request) {
        String auth = request.getAuthorization();
        if (auth == null || !auth.startsWith("Bearer ")) {
            return error("Missing or invalid token", Status.BAD_REQUEST);
        }
        String token = auth.substring(7);
        String username = UserService.getUsernameByToken(token);
        if (username == null) {
            return error("Invalid token", Status.BAD_REQUEST);
        }

        try {
            int ratingId = Integer.parseInt(request.getPath().split("/")[3]);
            @SuppressWarnings("unchecked")
            Map<String, Object> body = JsonUtil.fromJson(request.getBody(), Map.class);
            Integer stars = (Integer) body.get("stars");
            String comment = (String) body.get("comment");

            if (stars == null || stars < 1 || stars > 5 || comment == null) {
                return error("stars (1-5) and comment required", Status.BAD_REQUEST);
            }

            Rating existing = ratingService.findById(ratingId);
            if (existing == null) {
                return error("Rating not found", Status.NOT_FOUND);
            }
            if (!existing.getUsername().equals(username)) {
                return error("Only owner can edit rating", Status.BAD_REQUEST);
            }

            Rating update = new Rating();
            update.setId(ratingId);
            update.setMediaId(existing.getMediaId());
            update.setUsername(existing.getUsername());
            update.setStars(stars);
            update.setComment(comment);

            ratingService.updateRating(update, username);
            Rating updated = ratingService.findById(ratingId);
            return new Response(Status.OK, ContentType.APPLICATION_JSON,
                    JsonUtil.toJson(updated));
        } catch (Exception e) {
            return error("Update failed: " + e.getMessage(), Status.BAD_REQUEST);
        }
    }

    private Response handleDeleteRating(Request request) {
        String auth = request.getAuthorization();
        if (auth == null || !auth.startsWith("Bearer ")) {
            return error("Missing or invalid token", Status.BAD_REQUEST);
        }
        String token = auth.substring(7);
        String username = UserService.getUsernameByToken(token);
        if (username == null) {
            return error("Invalid token", Status.BAD_REQUEST);
        }

        try {
            int ratingId = Integer.parseInt(request.getPath().split("/")[3]);
            Rating existing = ratingService.findById(ratingId);
            if (existing == null) {
                return error("Rating not found", Status.NOT_FOUND);
            }
            if (!existing.getUsername().equals(username)) {
                return error("Only owner can delete rating", Status.BAD_REQUEST);
            }

            ratingService.deleteRating(ratingId, username);
            return new Response(Status.OK, ContentType.APPLICATION_JSON,
                    "{\"message\":\"Rating deleted\"}");
        } catch (Exception e) {
            return error("Delete failed: " + e.getMessage(), Status.BAD_REQUEST);
        }
    }

    private Response handleConfirmRating(Request request) {
        String auth = request.getAuthorization();
        if (auth == null || !auth.startsWith("Bearer ")) {
            return error("Missing or invalid token", Status.BAD_REQUEST);
        }
        String token = auth.substring(7);
        String username = UserService.getUsernameByToken(token);
        if (username == null) {
            return error("Invalid token", Status.BAD_REQUEST);
        }

        try {
            int ratingId = Integer.parseInt(request.getPath().split("/")[3]);
            Rating confirmed = ratingService.confirmRating(ratingId, username);
            return new Response(Status.OK, ContentType.APPLICATION_JSON,
                    JsonUtil.toJson(confirmed));
        } catch (Exception e) {
            return error("Confirm failed: " + e.getMessage(), Status.BAD_REQUEST);
        }
    }

    private Response handleLikeRating(Request request) {
        String auth = request.getAuthorization();
        if (auth == null || !auth.startsWith("Bearer ")) {
            return error("Missing or invalid token", Status.BAD_REQUEST);
        }
        String token = auth.substring(7);
        String liker = UserService.getUsernameByToken(token);
        if (liker == null) {
            return error("Invalid token", Status.BAD_REQUEST);
        }

        try {
            int ratingId = Integer.parseInt(request.getPath().split("/")[3]);
            ratingService.toggleLike(liker, ratingId);
            return new Response(Status.OK, ContentType.APPLICATION_JSON,
                    "{\"message\":\"Like toggled\"}");
        } catch (Exception e) {
            return error("Like failed: " + e.getMessage(), Status.BAD_REQUEST);
        }
    }

    private Response handleGetRatings(Request request) {
        try {
            int mediaId = Integer.parseInt(request.getPath().split("/")[3]);
            var ratings = ratingService.getRatingsForMedia(mediaId, true);
            return new Response(Status.OK, ContentType.APPLICATION_JSON,
                    JsonUtil.toJson(ratings));
        } catch (Exception e) {
            return error("Get ratings failed: " + e.getMessage(), Status.BAD_REQUEST);
        }
    }

    private Response error(String msg, Status status) {
        return new Response(status, ContentType.APPLICATION_JSON,
                "{\"error\":\"" + msg + "\"}");
    }
}