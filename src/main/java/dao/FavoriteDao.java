package dao;

import model.Favorite;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FavoriteDao {
    public void addFavorite(Favorite fav) {
        String sql = "INSERT INTO favorites (username, media_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fav.getUsername());
            ps.setInt(2, fav.getMediaId());
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to add favorite", e);
        }
    }

    public void removeFavorite(String username, int mediaId) {
        String sql = "DELETE FROM favorites WHERE username = ? AND media_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setInt(2, mediaId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to remove favorite", e);
        }
    }

    public List<Integer> getFavoriteMediaIds(String username) {
        String sql = "SELECT media_id FROM favorites WHERE username = ?";
        List<Integer> ids = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) ids.add(rs.getInt("media_id"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ids;
    }

    // In deiner FavoriteDao.java:
    public boolean isFavorite(String username, int mediaId) {
        String sql = "SELECT 1 FROM favorites WHERE username = ? AND media_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setInt(2, mediaId);
            return ps.executeQuery().next();
        } catch (Exception e) {
            return false;
        }
    }
}