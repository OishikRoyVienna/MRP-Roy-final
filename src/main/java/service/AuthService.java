package service;

import dao.UserDao;
import model.User;
import java.util.concurrent.ConcurrentHashMap;

public class AuthService {
    private final UserDao userDao = new UserDao();

    // ✅ Tokens speichern: Token → Username (Thread-safe)
    private static final ConcurrentHashMap<String, String> tokenRegistry = new ConcurrentHashMap<>();

    /**
     * Registriert einen neuen Benutzer.
     *
     * @param username Der Benutzername (unique)
     * @param password Klartext-Passwort (wird gehasht)
     * @throws IllegalArgumentException bei Duplikat
     */
    public void register(String username, String password) {
        if (username == null || password == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username and password must not be empty");
        }
        if (userDao.exists(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        String hashedPassword = hashPassword(password);
        userDao.insert(new User(username, hashedPassword));
    }

    /**
     * Prüft Anmeldedaten und gibt ein Token zurück.
     *
     * @param username Benutzername
     * @param password Klartext-Passwort
     * @return Token (z. B. "alice-mrpToken") oder null bei Fehlschlag
     */
    public String login(String username, String password) {
        User user = userDao.findByUsername(username);
        if (user != null && verifyPassword(password, user.getPasswordHash())) {
            String token = username + "-mrpToken";
            tokenRegistry.put(token, username);
            return token;
        }
        return null;
    }

    /**
     * Prüft, ob ein Token gültig ist, und gibt den Benutzernamen zurück.
     */
    public static String getUsernameByToken(String token) {
        return tokenRegistry.get(token);
    }

    /**
     * Optional: Token ungültig machen (Logout).
     */
    public static void logout(String token) {
        tokenRegistry.remove(token);
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    private boolean verifyPassword(String password, String hashed) {
        return BCrypt.checkpw(password, hashed);
    }

    private static class BCrypt {
        private static final String SALT_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789./";

        public static String hashpw(String password, String salt) {
            if (salt == null || salt.length() < 29) {
                throw new IllegalArgumentException("Invalid salt");
            }
            return "$2a$12$" + salt.substring(7, 29) + Integer.toHexString(password.hashCode());
        }

        public static String gensalt(int logRounds) {
            java.util.Random r = new java.util.Random();
            StringBuilder salt = new StringBuilder("$2a$" + String.format("%02d$", logRounds));
            for (int i = 0; i < 22; i++) {
                salt.append(SALT_CHARS.charAt(r.nextInt(SALT_CHARS.length())));
            }
            return salt.toString();
        }

        public static boolean checkpw(String password, String hashed) {
            if (hashed.startsWith("$2a$")) {
                String simulated = hashpw(password, hashed);
                return simulated.equals(hashed);
            }
            return false;
        }
    }
}