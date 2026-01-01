package test;

import dao.UserDao;
import model.User;
import org.junit.jupiter.api.Test;
import service.UserService;

import static org.junit.jupiter.api.Assertions.*;

public class AllTests {

    // ==== 1. UserService Tests (5) ====
    @Test
    void test_register_success() {
        UserService service = new UserService();
        assertDoesNotThrow(() -> service.register("user1", "pass1"));
    }

    @Test
    void test_register_duplicate() {
        UserService service = new UserService();
        service.register("user2", "pass2");
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.register("user2", "pass2"));
        assertEquals("Username already exists", e.getMessage());
    }

    @Test
    void test_login_success() {
        UserService service = new UserService();
        service.register("user3", "pass3");
        String token = service.login("user3", "pass3");
        assertNotNull(token);
        assertTrue(token.startsWith("user3-"));
    }

    @Test
    void test_login_invalid() {
        UserService service = new UserService();
        service.register("user4", "pass4");
        assertNull(service.login("user4", "wrong"));
    }

    @Test
    void test_getUsernameByToken() {
        UserService service = new UserService();
        service.register("user5", "pass5");
        String token = service.login("user5", "pass5");
        assertEquals("user5", UserService.getUsernameByToken(token));
    }

    // ==== 2. UserDao Tests (5) – echte DB-Tests ====
    @Test
    void test_userDao_insert_and_find() {
        UserDao dao = new UserDao();
        dao.insert(new User("dbuser1", "dbpass1"));
        User found = dao.findByUsername("dbuser1");
        assertNotNull(found);
        assertEquals("dbuser1", found.getUsername());
    }

    @Test
    void test_userDao_exists_true() {
        UserDao dao = new UserDao();
        dao.insert(new User("dbuser2", "dbpass2"));
        assertTrue(dao.exists("dbuser2"));
    }

    @Test
    void test_userDao_exists_false() {
        UserDao dao = new UserDao();
        assertFalse(dao.exists("nonexistent"));
    }

    @Test
    void test_userDao_insert_duplicate_throws() {
        UserDao dao = new UserDao();
        dao.insert(new User("dbuser3", "dbpass3"));
        assertThrows(Exception.class, () ->
                dao.insert(new User("dbuser3", "dbpass3"))
        );
    }

    @Test
    void test_userDao_roundtrip() {
        UserDao dao = new UserDao();
        User u = new User("roundtrip", "test");
        dao.insert(u);
        User found = dao.findByUsername("roundtrip");
        assertEquals("roundtrip", found.getUsername());
        assertEquals("test", found.getPasswordHash());
    }

    // ==== 3. Business-Logik Tests (10) ====
    @Test
    void test_rating_stars_min() {
        model.Rating r = new model.Rating();
        r.setStars(1);
        assertEquals(1, r.getStars());
    }

    @Test
    void test_rating_stars_max() {
        model.Rating r = new model.Rating();
        r.setStars(5);
        assertEquals(5, r.getStars());
    }

    @Test
    void test_media_title_not_null() {
        model.MediaEntry m = new model.MediaEntry();
        m.setTitle("Test");
        assertEquals("Test", m.getTitle());
    }

    @Test
    void test_media_creator_not_null() {
        model.MediaEntry m = new model.MediaEntry();
        m.setCreatorUsername("alice");
        assertEquals("alice", m.getCreatorUsername());
    }

    @Test
    void test_token_format() {
        UserService service = new UserService();
        service.register("check", "pass");
        String token = service.login("check", "pass");
        assertTrue(token.matches("check-mrpToken"));
    }

    @Test
    void test_profile_has_username() {
        UserService service = new UserService();
        service.register("profiletest", "pass");
        var profile = service.getProfile("profiletest");
        assertEquals("profiletest", profile.get("username"));
    }

    @Test
    void test_favorite_structure() {
        model.Favorite f = new model.Favorite("user", 123);
        assertEquals("user", f.getUsername());
        assertEquals(123, f.getMediaId());
    }

    @Test
    void test_rating_unconfirmed_by_default() {
        model.Rating r = new model.Rating();
        assertFalse(r.isConfirmed());
    }

    @Test
    void test_media_age_restriction_valid() {
        model.MediaEntry m = new model.MediaEntry();
        m.setAgeRestriction(16);
        assertEquals(16, m.getAgeRestriction());
    }

    @Test
    void test_media_genres_array() {
        model.MediaEntry m = new model.MediaEntry();
        m.setGenres(new String[]{"sci-fi", "action"});
        assertArrayEquals(new String[]{"sci-fi", "action"}, m.getGenres());
    }
}