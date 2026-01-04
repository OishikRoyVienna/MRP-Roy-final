package test;

import dao.*;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.*;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AllTests {

    @BeforeEach
    void cleanDB() {
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.createStatement()) {
            // Tabellen in korrekter Reihenfolge löschen (Foreign Keys!)
            stmt.execute("DROP TABLE IF EXISTS favorites");
            stmt.execute("DROP TABLE IF EXISTS ratings");
            stmt.execute("DROP TABLE IF EXISTS media_entries");
            stmt.execute("DROP TABLE IF EXISTS users");
            DatabaseManager.initializeDatabase();
        } catch (Exception e) {
            throw new RuntimeException("DB setup failed", e);
        }
    }

    // ==== 1. User Management (5 Tests) ====
    @Test
    void testUserRegisterSuccess() {
        UserService service = new UserService();
        assertDoesNotThrow(() -> service.register("alice", "123"));
    }

    @Test
    void testUserRegisterDuplicate() {
        UserService service = new UserService();
        service.register("bob", "123");
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.register("bob", "123"));
        assertEquals("Username already exists", e.getMessage());
    }

    @Test
    void testUserLoginSuccess() {
        UserService service = new UserService();
        service.register("charlie", "123");
        String token = service.login("charlie", "123");
        assertNotNull(token);
        assertTrue(token.startsWith("charlie-"));
    }

    @Test
    void testUserLoginInvalid() {
        UserService service = new UserService();
        service.register("dave", "123");
        assertNull(service.login("dave", "wrong"));
    }

    @Test
    void testGetUsernameByToken() {
        UserService service = new UserService();
        service.register("eve", "123");
        String token = service.login("eve", "123");
        assertEquals("eve", UserService.getUsernameByToken(token));
    }

    // ==== 2. Media Management (5 Tests) ====
    @Test
    void testMediaCreate() {
        UserService userService = new UserService();
        userService.register("alice", "123");
        MediaService service = new MediaService();
        MediaEntry m = new MediaEntry();
        m.setTitle("Inception");
        m.setMediaType("movie");
        m.setCreatorUsername("alice");
        MediaEntry saved = service.create(m);
        assertNotNull(saved.getId());
        assertEquals("Inception", saved.getTitle());
    }

    @Test
    void testMediaGetById() {
        UserService userService = new UserService();
        userService.register("alice", "123");
        MediaService service = new MediaService();
        MediaEntry m = new MediaEntry(); m.setTitle("Test"); m.setMediaType("movie"); m.setCreatorUsername("alice");
        int id = service.create(m).getId();
        MediaEntry found = service.getById(id);
        assertEquals("Test", found.getTitle());
    }

    @Test
    void testMediaList() {
        UserService userService = new UserService();
        userService.register("alice", "123");
        MediaService service = new MediaService();
        MediaEntry m1 = new MediaEntry(); m1.setTitle("A Movie"); m1.setMediaType("movie"); m1.setCreatorUsername("alice");
        MediaEntry m2 = new MediaEntry(); m2.setTitle("B Series"); m2.setMediaType("series"); m2.setCreatorUsername("alice");
        service.create(m1);
        service.create(m2);
        List<MediaEntry> list = service.list("A");
        assertEquals(1, list.size());
        assertEquals("A Movie", list.get(0).getTitle());
    }

    @Test
    void testMediaUpdate() {
        UserService userService = new UserService();
        userService.register("alice", "123");
        MediaService service = new MediaService();
        MediaEntry m = new MediaEntry(); m.setTitle("Old"); m.setMediaType("movie"); m.setCreatorUsername("alice");
        int id = service.create(m).getId();
        MediaEntry update = new MediaEntry();
        update.setId(id);
        update.setTitle("New");
        update.setMediaType("movie");
        service.update(update);
        assertEquals("New", service.getById(id).getTitle());
    }

    @Test
    void testMediaDelete() {
        UserService userService = new UserService();
        userService.register("alice", "123");
        MediaService service = new MediaService();
        MediaEntry m = new MediaEntry(); m.setTitle("To Delete"); m.setMediaType("movie"); m.setCreatorUsername("alice");
        int id = service.create(m).getId();
        service.delete(id);
        assertNull(service.getById(id));
    }

    // ==== 3. Rating & Moderation (5 Tests) ====
    @Test
    void testRatingCreate() {
        UserService userService = new UserService();
        userService.register("alice", "123");
        userService.register("bob", "123");
        MediaService mediaService = new MediaService();
        RatingService ratingService = new RatingService();
        MediaEntry m = new MediaEntry(); m.setTitle("Test"); m.setMediaType("movie"); m.setCreatorUsername("alice");
        int mediaId = mediaService.create(m).getId();
        Rating r = new Rating();
        r.setMediaId(mediaId);
        r.setUsername("bob");
        r.setStars(5);
        Rating saved = ratingService.createRating(r, "bob");
        assertNotNull(saved.getId());
        assertEquals(5, saved.getStars());
    }

    @Test
    void testRatingDuplicateNotAllowed() {
        UserService userService = new UserService();
        userService.register("alice", "123");
        userService.register("bob", "123");
        MediaService mediaService = new MediaService();
        RatingService ratingService = new RatingService();
        MediaEntry m = new MediaEntry(); m.setTitle("Test"); m.setMediaType("movie"); m.setCreatorUsername("alice");
        int mediaId = mediaService.create(m).getId();
        Rating r1 = new Rating(); r1.setMediaId(mediaId); r1.setUsername("bob"); r1.setStars(4);
        ratingService.createRating(r1, "bob");
        Rating r2 = new Rating(); r2.setMediaId(mediaId); r2.setUsername("bob"); r2.setStars(5);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> ratingService.createRating(r2, "bob"));
        assertEquals("Already rated this media", e.getMessage());
    }

    @Test
    void testConfirmRatingByCreator() {
        UserService userService = new UserService();
        userService.register("alice", "123");
        userService.register("bob", "123");
        MediaService mediaService = new MediaService();
        RatingService ratingService = new RatingService();
        MediaEntry m = new MediaEntry(); m.setTitle("Test"); m.setMediaType("movie"); m.setCreatorUsername("alice");
        int mediaId = mediaService.create(m).getId();
        Rating r = new Rating(); r.setMediaId(mediaId); r.setUsername("bob"); r.setStars(5);
        int ratingId = ratingService.createRating(r, "bob").getId();
        ratingService.confirmRating(ratingId, "alice");
        assertTrue(ratingService.getRatingsForMedia(mediaId, true).size() > 0);
    }

    @Test
    void testGetOnlyConfirmedRatings() {
        UserService userService = new UserService();
        userService.register("alice", "123");
        userService.register("bob", "123");
        MediaService mediaService = new MediaService();
        RatingService ratingService = new RatingService();
        MediaEntry m = new MediaEntry(); m.setTitle("Test"); m.setMediaType("movie"); m.setCreatorUsername("alice");
        int mediaId = mediaService.create(m).getId();
        Rating r = new Rating(); r.setMediaId(mediaId); r.setUsername("bob"); r.setStars(5);
        ratingService.createRating(r, "bob");
        assertEquals(0, ratingService.getRatingsForMedia(mediaId, true).size());
        assertEquals(1, ratingService.getRatingsForMedia(mediaId, false).size());
    }

    @Test
    void testAverageRatingCalculation() {
        UserService userService = new UserService();
        userService.register("alice", "123");
        userService.register("bob", "123");
        MediaService mediaService = new MediaService();
        RatingService ratingService = new RatingService();
        MediaEntry m = new MediaEntry(); m.setTitle("Test"); m.setMediaType("movie"); m.setCreatorUsername("alice");
        int mediaId = mediaService.create(m).getId();
        Rating r1 = new Rating(); r1.setMediaId(mediaId); r1.setUsername("bob"); r1.setStars(4);
        Rating r2 = new Rating(); r2.setMediaId(mediaId); r2.setUsername("charlie"); r2.setStars(2);
        ratingService.createRating(r1, "bob");
        ratingService.createRating(r2, "charlie");
        // Unbestätigt → avg = null
        assertNull(mediaService.getById(mediaId).getAverageRating());
        // Bestätige eine
        ratingService.confirmRating(r1.getId(), "alice");
        assertEquals(4.0, mediaService.getById(mediaId).getAverageRating());
    }

    // ==== 4. Favorites & Profile (5 Tests) ====
    @Test
    void testAddFavorite() {
        UserService userService = new UserService();
        userService.register("alice", "123");
        MediaService mediaService = new MediaService();
        FavoriteService favoriteService = new FavoriteService();
        MediaEntry m = new MediaEntry(); m.setTitle("Test"); m.setMediaType("movie"); m.setCreatorUsername("alice");
        int mediaId = mediaService.create(m).getId();
        favoriteService.addFavorite("alice", mediaId);
        assertTrue(favoriteService.getFavoriteMediaIds("alice").contains(mediaId));
    }

    @Test
    void testRemoveFavorite() {
        UserService userService = new UserService();
        userService.register("alice", "123");
        MediaService mediaService = new MediaService();
        FavoriteService favoriteService = new FavoriteService();
        MediaEntry m = new MediaEntry(); m.setTitle("Test"); m.setMediaType("movie"); m.setCreatorUsername("alice");
        int mediaId = mediaService.create(m).getId();
        favoriteService.addFavorite("alice", mediaId);
        favoriteService.removeFavorite("alice", mediaId);
        assertFalse(favoriteService.getFavoriteMediaIds("alice").contains(mediaId));
    }

    @Test
    void testIsFavorite() {
        UserService userService = new UserService();
        userService.register("alice", "123");
        MediaService mediaService = new MediaService();
        FavoriteService favoriteService = new FavoriteService();
        MediaEntry m = new MediaEntry(); m.setTitle("Test"); m.setMediaType("movie"); m.setCreatorUsername("alice");
        int mediaId = mediaService.create(m).getId();
        assertFalse(favoriteService.isFavorite("alice", mediaId));
        favoriteService.addFavorite("alice", mediaId);
        assertTrue(favoriteService.isFavorite("alice", mediaId));
    }

    @Test
    void testUserProfileStats() {
        UserService userService = new UserService();
        userService.register("alice", "123");
        userService.register("bob", "123");
        MediaService mediaService = new MediaService();
        RatingService ratingService = new RatingService();
        MediaEntry m = new MediaEntry(); m.setTitle("Test"); m.setMediaType("movie"); m.setCreatorUsername("alice");
        int mediaId = mediaService.create(m).getId();
        Rating r = new Rating(); r.setMediaId(mediaId); r.setUsername("bob"); r.setStars(5);
        ratingService.createRating(r, "bob");
        ratingService.confirmRating(r.getId(), "alice");
        var profile = userService.getProfile("bob");
        assertEquals(1, profile.get("totalRatings"));
        assertEquals(5.0, profile.get("averageRating"));
    }

    @Test
    void testFavoriteGenreCalculation() {
        UserService userService = new UserService();
        userService.register("alice", "123");
        MediaService mediaService = new MediaService();
        RatingService ratingService = new RatingService();
        // Medien mit Genres erstellen
        MediaEntry m1 = new MediaEntry(); m1.setTitle("A"); m1.setMediaType("movie"); m1.setCreatorUsername("alice"); m1.setGenres(new String[]{"sci-fi"});
        MediaEntry m2 = new MediaEntry(); m2.setTitle("B"); m2.setMediaType("movie"); m2.setCreatorUsername("alice"); m2.setGenres(new String[]{"sci-fi", "action"});
        int id1 = mediaService.create(m1).getId();
        int id2 = mediaService.create(m2).getId();
        // Bewertungen
        Rating r1 = new Rating(); r1.setMediaId(id1); r1.setUsername("alice"); r1.setStars(5);
        Rating r2 = new Rating(); r2.setMediaId(id2); r2.setUsername("alice"); r2.setStars(4);
        ratingService.createRating(r1, "alice");
        ratingService.createRating(r2, "alice");
        ratingService.confirmRating(r1.getId(), "alice");
        ratingService.confirmRating(r2.getId(), "alice");
        String favGenre = mediaService.getById(id1).getCreatorUsername(); // Trick: nutze DAO direkt
        String genre = new MediaDao().getFavoriteGenre("alice");
        assertEquals("sci-fi", genre);
    }
}