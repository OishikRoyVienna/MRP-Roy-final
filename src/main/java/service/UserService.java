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

    private static final ConcurrentHashMap<String, String> tokenToUsername = new ConcurrentHashMap<>();

    public String login(String username, String password) {
        User user = userDao.findByUsername(username);
        if (user != null && password.equals(user.getPasswordHash())) {
            String token = username + "-mrpToken";
            tokenToUsername.put(token, username);
            return token;
        }
        return null; // → führt zu 401 Unauthorized
    }

    public void register(String username, String password) {
        if (username == null || password == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username and password must not be empty");
        }
        if (userDao.exists(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        userDao.insert(new User(username, password));
    }

    public static String getUsernameByToken(String token) {
        return tokenToUsername.get(token);
    }

    }

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

    public boolean isOwner(String token, String resourceOwner) {
        String username = getUsernameByToken(token);
        return username != null && username.equals(resourceOwner);
    }
}