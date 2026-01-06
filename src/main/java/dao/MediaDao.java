package dao;

import model.MediaEntry;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MediaDao {
    public MediaEntry insert(MediaEntry m) {
        String sql = "INSERT INTO media_entries (title, description, media_type, release_year, genres, age_restriction, creator_username) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getTitle());
            ps.setString(2, m.getDescription());
            ps.setString(3, m.getMediaType());
            ps.setObject(4, m.getReleaseYear());
            ps.setArray(5, conn.createArrayOf("text", m.getGenres()));
            ps.setObject(6, m.getAgeRestriction());
            ps.setString(7, m.getCreatorUsername());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) m.setId(rs.getInt("id"));
            return m;
        } catch (Exception e) {
            throw new RuntimeException("Insert media failed", e);
        }
    }

    public MediaEntry findById(int id) {
        String sql = "SELECT * FROM media_entries WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<MediaEntry> findAll(String titleFilter, String genre, String mediaType,
                                    Integer minAge, Double minRating) {
        StringBuilder sql = new StringBuilder(
                "SELECT m.*, COALESCE(AVG(r.stars), 0) AS average_rating " +
                        "FROM media_entries m " +
                        "LEFT JOIN ratings r ON m.id = r.media_id AND r.is_confirmed = true " +
                        "WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (titleFilter != null && !titleFilter.trim().isEmpty()) {
            sql.append(" AND m.title ILIKE ?");
            params.add("%" + titleFilter.trim() + "%");
        }
        if (genre != null && !genre.trim().isEmpty()) {
            sql.append(" AND ? = ANY(m.genres)");
            params.add(genre.trim());
        }
        if (mediaType != null && !mediaType.trim().isEmpty()) {
            sql.append(" AND m.media_type = ?");
            params.add(mediaType.trim());
        }
        if (minAge != null) {
            sql.append(" AND m.age_restriction >= ?");
            params.add(minAge);
        }

        sql.append(" GROUP BY m.id");

        if (minRating != null) {
            sql.append(" HAVING COALESCE(AVG(r.stars), 0) >= ?");
            params.add(minRating);
        }

        sql.append(" ORDER BY m.title ASC");

        List<MediaEntry> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                MediaEntry m = mapRow(rs);
                m.setAverageRating(rs.getDouble("average_rating"));
                list.add(m);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // 👇 Kompatibilität: alte Signatur weiterhin nutzbar (delegiert)
    public List<MediaEntry> findAll(String titleFilter) {
        return findAll(titleFilter, null, null, null, null);
    }

    public void update(MediaEntry m) {
        String sql = "UPDATE media_entries SET title=?,description=?,media_type=?,release_year=?,genres=?,age_restriction=? WHERE id=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getTitle());
            ps.setString(2, m.getDescription());
            ps.setString(3, m.getMediaType());
            ps.setObject(4, m.getReleaseYear());
            ps.setArray(5, conn.createArrayOf("text", m.getGenres()));
            ps.setObject(6, m.getAgeRestriction());
            ps.setInt(7, m.getId());
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Update failed", e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM media_entries WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Delete failed", e);
        }
    }

    private MediaEntry mapRow(ResultSet rs) throws SQLException {
        MediaEntry m = new MediaEntry();
        m.setId(rs.getInt("id"));
        m.setTitle(rs.getString("title"));
        m.setDescription(rs.getString("description"));
        m.setMediaType(rs.getString("media_type"));
        m.setReleaseYear((Integer) rs.getObject("release_year"));
        m.setAgeRestriction((Integer) rs.getObject("age_restriction"));
        m.setCreatorUsername(rs.getString("creator_username"));
        Array genresArray = rs.getArray("genres");
        if (genresArray != null) {
            m.setGenres((String[]) genresArray.getArray());
        }
        return m;
    }

    private MediaEntry mapRowWithAvg(ResultSet rs) throws SQLException {
        MediaEntry m = mapRow(rs);
        m.setAverageRating(rs.getDouble("average_rating"));
        return m;
    }

    public String getFavoriteGenre(String username) {
        String sql = """
        SELECT unnest(genres) AS genre, COUNT(*) 
        FROM media_entries m 
        JOIN ratings r ON m.id = r.media_id 
        WHERE r.username = ? AND r.is_confirmed = true
        GROUP BY genre 
        ORDER BY COUNT(*) DESC 
        LIMIT 1
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("genre");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}