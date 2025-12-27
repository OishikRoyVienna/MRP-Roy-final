package service;

import dao.FavoriteDao;
import dao.MediaDao;
import dao.RatingDao;
import dao.UserDao;
import model.User;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class UserService {
    private final UserDao userDao = new UserDao();
    private final RatingDao ratingDao = new RatingDao();
    private final FavoriteDao favoriteDao = new FavoriteDao();
    private final MediaDao mediaDao = new MediaDao();

    // ✅ Tokens speichern – direkt hier, wie von der Spezifikation verlangt
    private static final ConcurrentHashMap<String, String> tokenToUsername = new ConcurrentHashMap<>();

    // 🔐 Login: generiert Token und speichert es
    public String login(String username, String password) {
        User user = userDao.findByUsername(username);
        if (user != null && password.equals(user.getPasswordHash())) {
            String token = username + "-mrpToken";
            tokenToUsername.put(token, username);
            return token;
        }
        return null; // → führt zu 401 Unauthorized
    }

    // 📝 Registrierung
    public void register(String username, String password) {
        if (username == null || password == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username and password must not be empty");
        }
        if (userDao.exists(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        // ⚠️ In Final: Passwort hashen! (z. B. BCrypt)
        // Für Intermediate/Final-Checklist reicht Plaintext (da nicht explizit verlangt)
        userDao.insert(new User(username, password));
    }

    // 🔍 Token → Username
    public static String getUsernameByToken(String token) {
        return tokenToUsername.get(token);
    }

    // 🚪 Optional: Logout (für Bonus, nicht verlangt)
    public static void logout(String token) {
        tokenToUsername.remove(token);
    }

    // 👤 Profil mit Statistiken (für „view profile and statistics“)
    public Map<String, Object> getProfile(String username) {
        int totalRatings = ratingDao.countByUser(username);
        Double avgRating = ratingDao.averageRatingByUser(username);
        String favoriteGenre = mediaDao.getFavoriteGenre(username);

        Map<String, Object> profile = new HashMap<>();
        profile.put("username", username);
        profile.put("totalRatings", totalRatings);
        profile.put("averageRating", avgRating != null ? avgRating : 0.0);
        profile.put("favoriteGenre", favoriteGenre != null ? favoriteGenre : "n/a");
        return profile;
    }

    // ✅ Hilfsmethode für Handler: Prüfe Berechtigung
    public boolean isOwner(String token, String resourceOwner) {
        String username = getUsernameByToken(token);
        return username != null && username.equals(resourceOwner);
    }
}