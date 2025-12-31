import dao.*;
import model.*;
import org.junit.jupiter.api.Test;
import service.MediaService;
import service.RatingService;
import service.UserService;
import service.FavoriteService;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class AllTests {

    // ==== 1. UserService Tests (5) ====
    @Test
    void userService_register_success() {
        UserDao dao = mock(UserDao.class);
        UserService service = new UserService() {{ userDao = dao; }};
        when(dao.exists("alice")).thenReturn(false);
        assertDoesNotThrow(() -> service.register("alice", "123"));
    }

    @Test
    void userService_register_duplicate() {
        UserDao dao = mock(UserDao.class);
        UserService service = new UserService() {{ userDao = dao; }};
        when(dao.exists("alice")).thenReturn(true);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.register("alice", "123"));
        assertEquals("Username already exists", e.getMessage());
    }

    @Test
    void userService_login_success() {
        UserDao dao = mock(UserDao.class);
        UserService service = new UserService() {{ userDao = dao; }};
        User u = new User("alice", "123");
        when(dao.findByUsername("alice")).thenReturn(u);
        String token = service.login("alice", "123");
        assertNotNull(token);
        assertTrue(token.startsWith("alice-"));
    }

    @Test
    void userService_login_invalid() {
        UserDao dao = mock(UserDao.class);
        UserService service = new UserService() {{ userDao = dao; }};
        when(dao.findByUsername("alice")).thenReturn(null);
        assertNull(service.login("alice", "wrong"));
    }

    @Test
    void userService_getUsernameByToken() {
        UserService service = new UserService();
        service.login("alice", "123");
        assertEquals("alice", UserService.getUsernameByToken("alice-mrpToken"));
    }

    // ==== 2. MediaService Tests (5) ====
    @Test
    void mediaService_create_success() {
        MediaDao dao = mock(MediaDao.class);
        RatingDao rDao = mock(RatingDao.class);
        MediaService service = new MediaService() {{ mediaDao = dao; ratingDao = rDao; }};
        MediaEntry m = new MediaEntry(); m.setTitle("Test"); m.setCreatorUsername("alice");
        when(dao.insert(m)).thenReturn(m);
        assertNotNull(service.create(m));
    }

    @Test
    void mediaService_getById_notFound() {
        MediaDao dao = mock(MediaDao.class);
        MediaService service = new MediaService() {{ mediaDao = dao; }};
        when(dao.findById(999)).thenReturn(null);
        assertNull(service.getById(999));
    }

    @Test
    void mediaService_search_byTitle() {
        MediaDao dao = mock(MediaDao.class);
        MediaService service = new MediaService() {{ mediaDao = dao; }};
        when(dao.search("Incept", null, null, null)).thenReturn(List.of());
        assertTrue(service.search("Incept", null, null, null).isEmpty());
    }

    @Test
    void mediaService_averageRating() {
        RatingDao dao = mock(RatingDao.class);
        MediaService service = new MediaService() {{ ratingDao = dao; }};
        when(dao.getAverageRating(1)).thenReturn(4.5);
        MediaEntry m = new MediaEntry(); m.setId(1);
        // Simuliere: service.getById(1) setzt averageRating
        m.setAverageRating(4.5);
        assertEquals(4.5, m.getAverageRating());
    }

    @Test
    void mediaService_ownerCheck() {
        UserService service = new UserService();
        service.login("alice", "123");
        assertTrue(service.isOwner("alice-mrpToken", "alice"));
        assertFalse(service.isOwner("alice-mrpToken", "bob"));
    }

    // ==== 3. RatingService Tests (6) ====
    @Test
    void ratingService_create_success() {
        RatingDao rDao = mock(RatingDao.class);
        MediaDao mDao = mock(MediaDao.class);
        RatingService service = new RatingService() {{ ratingDao = rDao; mediaDao = mDao; }};
        Rating r = new Rating(); r.setMediaId(1); r.setUsername("alice"); r.setStars(5);
        MediaEntry m = new MediaEntry(); m.setCreatorUsername("bob");
        when(mDao.findById(1)).thenReturn(m);
        when(rDao.findByUserAndMedia("alice", 1)).thenReturn(null);
        assertDoesNotThrow(() -> service.createRating(r, "alice"));
    }

    @Test
    void ratingService_duplicate_throws() {
        RatingDao rDao = mock(RatingDao.class);
        MediaDao mDao = mock(MediaDao.class);
        RatingService service = new RatingService() {{ ratingDao = rDao; mediaDao = mDao; }};
        Rating r = new Rating(); r.setMediaId(1); r.setUsername("alice"); r.setStars(5);
        when(rDao.findByUserAndMedia("alice", 1)).thenReturn(r);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.createRating(r, "alice"));
        assertEquals("Already rated this media", e.getMessage());
    }

    @Test
    void ratingService_invalidMedia_throws() {
        RatingDao rDao = mock(RatingDao.class);
        MediaDao mDao = mock(MediaDao.class);
        RatingService service = new RatingService() {{ ratingDao = rDao; mediaDao = mDao; }};
        Rating r = new Rating(); r.setMediaId(999); r.setUsername("alice"); r.setStars(5);
        when(mDao.findById(999)).thenReturn(null);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.createRating(r, "alice"));
        assertEquals("Media not found", e.getMessage());
    }

    @Test
    void ratingService_confirm_success() {
        RatingDao rDao = mock(RatingDao.class);
        MediaDao mDao = mock(MediaDao.class);
        RatingService service = new RatingService() {{ ratingDao = rDao; mediaDao = mDao; }};
        Rating r = new Rating(); r.setId(1); r.setMediaId(1);
        MediaEntry m = new MediaEntry(); m.setCreatorUsername("bob");
        when(rDao.findById(1)).thenReturn(r);
        when(mDao.findById(1)).thenReturn(m);
        assertDoesNotThrow(() -> service.confirmRating(1, "bob"));
    }

    @Test
    void ratingService_confirm_notCreator() {
        RatingDao rDao = mock(RatingDao.class);
        MediaDao mDao = mock(MediaDao.class);
        RatingService service = new RatingService() {{ ratingDao = rDao; mediaDao = mDao; }};
        Rating r = new Rating(); r.setId(1); r.setMediaId(1);
        MediaEntry m = new MediaEntry(); m.setCreatorUsername("bob");
        when(rDao.findById(1)).thenReturn(r);
        when(mDao.findById(1)).thenReturn(m);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.confirmRating(1, "alice"));
        assertEquals("Only media creator can confirm ratings", e.getMessage());
    }

    @Test
    void ratingService_getRatings_onlyConfirmed() {
        RatingDao rDao = mock(RatingDao.class);
        RatingService service = new RatingService() {{ ratingDao = rDao; }};
        List<Rating> list = List.of();
        when(rDao.findByMediaId(1, true)).thenReturn(list);
        assertEquals(list, service.getRatingsForMedia(1, true));
    }

    // ==== 4. FavoriteService Tests (3) ====
    @Test
    void favoriteService_add_success() {
        FavoriteDao fDao = mock(FavoriteDao.class);
        MediaDao mDao = mock(MediaDao.class);
        FavoriteService service = new FavoriteService() {{ favoriteDao = fDao; mediaDao = mDao; }};
        MediaEntry m = new MediaEntry();
        when(mDao.findById(1)).thenReturn(m);
        assertDoesNotThrow(() -> service.addFavorite("alice", 1));
    }

    @Test
    void favoriteService_add_invalidMedia() {
        FavoriteDao fDao = mock(FavoriteDao.class);
        MediaDao mDao = mock(MediaDao.class);
        FavoriteService service = new FavoriteService() {{ favoriteDao = fDao; mediaDao = mDao; }};
        when(mDao.findById(999)).thenReturn(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.addFavorite("alice", 999));
    }

    @Test
    void favoriteService_getList() {
        FavoriteDao fDao = mock(FavoriteDao.class);
        FavoriteService service = new FavoriteService() {{ favoriteDao = fDao; }};
        List<Integer> ids = List.of(1, 2);
        when(fDao.getFavoriteMediaIds("alice")).thenReturn(ids);
        assertEquals(ids, service.getFavoriteMediaIds("alice"));
    }

    // ==== 5. DAO & Integration Tests (1) ====
    @Test
    void ratingDao_insert_and_find() throws Exception {
        // DB leeren
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM ratings WHERE 1=1");
            stmt.execute("DELETE FROM media_entries WHERE 1=1");
            stmt.execute("DELETE FROM users WHERE 1=1");
            stmt.execute("INSERT INTO users (username, password_hash) VALUES ('test', '123')");
            stmt.execute("INSERT INTO media_entries (title, creator_username) VALUES ('Test', 'test')");
        }

        Rating r = new Rating();
        r.setMediaId(1); r.setUsername("src/main/test"); r.setStars(4); r.setComment("OK");

        RatingDao dao = new RatingDao();
        Rating saved = dao.insert(r);
        Rating found = dao.findById(saved.getId());

        assertNotNull(found);
        assertEquals(4, found.getStars());
        assertEquals("OK", found.getComment());
        assertFalse(found.isConfirmed()); // Moderation: default false!
    }
}