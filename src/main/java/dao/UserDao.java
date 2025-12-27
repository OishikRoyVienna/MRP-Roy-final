package dao;

import model.User;

import java.sql.*;

public class UserDao {
    public boolean exists(String username) {
        return findByUsername(username) != null;
    }

    public User findByUsername(String username) {
        String sql = "SELECT username, password_hash FROM users WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new User(rs.getString("username"), rs.getString("password_hash"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void insert(User user) {
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("DB insert failed", e);
        }
    }
}