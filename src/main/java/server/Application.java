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

        // 1. /api/users/...
        if (path.startsWith("/api/users")) {
            return userHandler.handle(request);
        }

        // 2. /api/media/...
        if (path.startsWith("/api/media")) {
            return mediaHandler.handle(request);
        }

        // 3. /api/ratings/...  ODER  /api/media/.../ratings → beide an RatingHandler
        if (path.startsWith("/api/ratings") || path.contains("/ratings")) {
            return ratingHandler.handle(request);
        }

        // 4. /api/favorites/...
        if (path.startsWith("/api/favorites")) {
            return favoriteHandler.handle(request);
        }

        // 5. Fallback
        return new Response(
                Status.NOT_FOUND,
                ContentType.APPLICATION_JSON,
                "{\"error\":\"Endpoint not found\"}"
        );
    }
}