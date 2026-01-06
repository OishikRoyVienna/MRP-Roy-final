package service;

import dao.UserDao;
import model.User;
import java.util.concurrent.ConcurrentHashMap;

public class AuthService {
    protected UserDao userDao = new UserDao();

    protected static final ConcurrentHashMap<String, String> tokenRegistry = new ConcurrentHashMap<>();

    public void register(String username, String password) {
        if (username == null || password == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username and password must not be empty");
        }
        if (userDao.exists(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        userDao.insert(new User(username, password));
    }

    /**
     * Prüft Passwort und gibt Token zurück.
     */
    public String login(String username, String password) {
        User user = userDao.findByUsername(username);
        if (user != null && password.equals(user.getPasswordHash())) {
            String token = username + "-mrpToken";
            tokenRegistry.put(token, username);
            return token;
        }
        return null;
    }

    /**
     * Gibt den Benutzernamen zu einem Token zurück
     */
    public static String getUsernameByToken(String token) {
        return tokenRegistry.get(token);
    }

    public static void logout(String token) {
        tokenRegistry.remove(token);
    }
}