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

    // Ersetze die gesamte handle()-Methode durch:
    public Response handle(Request request) {
        String path = request.getPath();

        if (path.startsWith("/api/users")) {
            return userHandler.handle(request);
        }
        if (path.startsWith("/api/media")) {
            return mediaHandler.handle(request);
        }
        if (path.startsWith("/api/ratings") || path.contains("/ratings")) { // ✅ Korrektur
            return ratingHandler.handle(request);
        }
        if (path.startsWith("/api/favorites") || path.contains("/favorites")) { // ✅ Korrektur
            return favoriteHandler.handle(request);
        }

        return new Response(Status.NOT_FOUND, ContentType.APPLICATION_JSON,
                "{\"error\":\"Endpoint not found\"}");
    }
}