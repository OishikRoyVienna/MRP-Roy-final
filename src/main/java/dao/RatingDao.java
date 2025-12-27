package dao;

import model.Rating;
import java.sql.*;

public class RatingDao {

    public Rating insert(Rating rating) {
        String sql = """
            INSERT INTO ratings (media_id, username, stars, comment, is_confirmed)
            VALUES (?, ?, ?, ?, ?)
            RETURNING id, timestamp
            """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, rating.getMediaId());
            ps.setString(2, rating.getUsername());
            ps.setInt(3, rating.getStars());
            ps.setString(4, rating.getComment());
            ps.setBoolean(5, rating.isConfirmed());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                rating.setId(rs.getInt("id"));
                rating.setTimestamp(rs.getTimestamp("timestamp").toInstant());
            }
            return rating;
        } catch (SQLException e) {  // ✅ SQLException statt Exception
            throw new RuntimeException("Failed to insert rating", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Rating findByUserAndMedia(String username, int mediaId) {
        String sql = "SELECT * FROM ratings WHERE username = ? AND media_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setInt(2, mediaId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapRow(rs) : null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void updateConfirmation(int id, boolean confirmed) {
        String sql = "UPDATE ratings SET is_confirmed = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, confirmed);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update confirmation", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Double getAverageRating(int mediaId) {
        String sql = "SELECT AVG(stars) FROM ratings WHERE media_id = ? AND is_confirmed = true";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, mediaId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getDouble(1) : null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Rating mapRow(ResultSet rs) throws SQLException {  // ✅ throws SQLException
        Rating r = new Rating();
        r.setId(rs.getInt("id"));
        r.setMediaId(rs.getInt("media_id"));
        r.setUsername(rs.getString("username"));
        r.setStars(rs.getInt("stars"));
        r.setComment(rs.getString("comment"));
        r.setConfirmed(rs.getBoolean("is_confirmed"));
        r.setTimestamp(rs.getTimestamp("timestamp").toInstant());
        return r;
    }

    /**
     * Findet eine Bewertung anhand ihrer ID.
     */
    public Rating findById(int id) throws RuntimeException {
        String sql = "SELECT * FROM ratings WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapRow(rs) : null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Findet alle Bewertungen für ein Medium.
     *
     * @param mediaId       Die Medien-ID
     * @param onlyConfirmed Nur bestätigte Bewertungen zurückgeben
     */
    public java.util.List<Rating> findByMediaId(int mediaId, boolean onlyConfirmed) {
        StringBuilder sql = new StringBuilder("SELECT * FROM ratings WHERE media_id = ?");
        if (onlyConfirmed) {
            sql.append(" AND is_confirmed = true");
        }
        sql.append(" ORDER BY timestamp DESC");

        java.util.List<Rating> list = new java.util.ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, mediaId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public int countByUser(String username) {
        String sql = "SELECT COUNT(*) FROM ratings WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Double averageRatingByUser(String username) {
        String sql = "SELECT AVG(stars) FROM ratings WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double avg = rs.getDouble(1);
                return rs.wasNull() ? null : avg;
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}