package service;

import dao.RatingDao;
import dao.MediaDao;
import model.Rating;

public class RatingService {
    protected RatingDao ratingDao = new RatingDao();
    protected MediaDao mediaDao = new MediaDao();

    /**
     * Erstellt eine neue Bewertung.
     */
    public Rating createRating(Rating rating, String requester) {
        if (!rating.getUsername().equals(requester)) {
            throw new IllegalArgumentException("Cannot rate for another user");
        }

        if (rating.getMediaId() == null) {
            throw new IllegalArgumentException("mediaId is required");
        }
        if (mediaDao.findById(rating.getMediaId()) == null) {
            throw new IllegalArgumentException("Media not found");
        }
        Rating existing = ratingDao.findByUserAndMedia(rating.getUsername(), rating.getMediaId());
        if (existing != null) {
            throw new IllegalArgumentException("Already rated this media");
        }
        return ratingDao.insert(rating);
    }

    /**
     * Bestätigt eine Bewertung (Moderation).
     */
    public Rating confirmRating(int ratingId, String requester) {
        Rating rating = ratingDao.findById(ratingId);
        if (rating == null) {
            throw new IllegalArgumentException("Rating not found");
        }
        var media = mediaDao.findById(rating.getMediaId());
        if (media == null || !media.getCreatorUsername().equals(requester)) {
            throw new IllegalArgumentException("Only media creator can confirm ratings");
        }
        ratingDao.updateConfirmation(ratingId, true);
        return ratingDao.findById(ratingId);
    }

    /**
     * Gibt alle Bewertungen für ein Medium zurück.
     */
    public java.util.List<Rating> getRatingsForMedia(int mediaId, boolean onlyConfirmed) {
        return ratingDao.findByMediaId(mediaId, onlyConfirmed);
    }

    // 🔥 NEU: Edit eigene Bewertung
    public void updateRating(Rating rating, String requester) {
        Rating existing = ratingDao.findById(rating.getId());
        if (existing == null) throw new IllegalArgumentException("Rating not found");
        if (!existing.getUsername().equals(requester)) {
            throw new IllegalArgumentException("Only owner can edit rating");
        }
        String sql = "UPDATE ratings SET stars = ?, comment = ? WHERE id = ?";
        try (var conn = dao.DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, rating.getStars());
            ps.setString(2, rating.getComment());
            ps.setInt(3, rating.getId());
            if (ps.executeUpdate() == 0) throw new RuntimeException("Update failed");
        } catch (Exception e) {
            throw new RuntimeException("Update rating failed", e);
        }
    }

    // 🔥 NEU: Delete eigene Bewertung
    public void deleteRating(int id, String requester) {
        Rating existing = ratingDao.findById(id);
        if (existing == null) throw new IllegalArgumentException("Rating not found");
        if (!existing.getUsername().equals(requester)) {
            throw new IllegalArgumentException("Only owner can delete rating");
        }
        String sql = "DELETE FROM ratings WHERE id = ?";
        try (var conn = dao.DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Delete rating failed", e);
        }
    }

    // 🔥 NEU: Like toggle
    public void toggleLike(String liker, int ratingId) {
        if (ratingDao.findById(ratingId) == null) {
            throw new IllegalArgumentException("Rating not found");
        }
        String checkSql = "SELECT 1 FROM likes WHERE username = ? AND rating_id = ?";
        String insertSql = "INSERT INTO likes (username, rating_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        String deleteSql = "DELETE FROM likes WHERE username = ? AND rating_id = ?";

        try (var conn = dao.DatabaseManager.getConnection()) {
            var check = conn.prepareStatement(checkSql);
            check.setString(1, liker);
            check.setInt(2, ratingId);
            boolean alreadyLiked = check.executeQuery().next();

            if (alreadyLiked) {
                var del = conn.prepareStatement(deleteSql);
                del.setString(1, liker);
                del.setInt(2, ratingId);
                del.executeUpdate();
            } else {
                var ins = conn.prepareStatement(insertSql);
                ins.setString(1, liker);
                ins.setInt(2, ratingId);
                ins.executeUpdate();
            }
        } catch (Exception e) {
            throw new RuntimeException("Toggle like failed", e);
        }
    }

    // 🔥 NEU: Hilfsmethode für Handler
    public Rating findById(int id) {
        return ratingDao.findById(id);
    }
}