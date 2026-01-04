package server;

import handler.FavoriteHandler;
import handler.RatingHandler;
import handler.UserHandler;
import handler.MediaHandler;
import http.ContentType;
import http.Request;
import http.Response;
import http.Status;

public class Application {
    private final UserHandler userHandler = new UserHandler();
    private final MediaHandler mediaHandler = new MediaHandler();
    private final RatingHandler ratingHandler = new RatingHandler();
    private final FavoriteHandler favoriteHandler = new FavoriteHandler();

    public Response handle(Request request) {
        String path = request.getPath();

        // ✅ 1. /api/reset zuerst!
        if ("/api/reset".equals(path)) {
            return userHandler.handle(request);
        }

        if (path.startsWith("/api/users")) {
            return userHandler.handle(request);
        }
        if (path.startsWith("/api/media")) {
            return mediaHandler.handle(request);
        }
        if (path.startsWith("/api/ratings") || path.matches("/api/media/\\d+/ratings")) {
            return ratingHandler.handle(request);
        }
        // ✅ 2. /api/favorites/ → mit "/" am Ende!
        if (path.startsWith("/api/favorites/")) {
            return favoriteHandler.handle(request);
        }

        return new Response(Status.NOT_FOUND, ContentType.APPLICATION_JSON,
                "{\"error\":\"Endpoint not found\"}");
    }
}