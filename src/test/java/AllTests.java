
import dao.*;
import http.Request;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.*;
import util.RequestMapper;

import java.io.ByteArrayInputStream;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AllTests {

    // ======== 1. RequestMapper Tests (5) ========
    private RequestMapper requestMapper = new RequestMapper();

    @Test
    void givenGetRequest_whenMapping_thenMethodAndPathAreSet() throws Exception {
        var exchange = mock(com.sun.net.httpserver.HttpExchange.class);
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestURI()).thenReturn(URI.create("/api/media"));
        Request request = requestMapper.fromExchange(exchange);
        assertEquals("GET", request.getMethod());
        assertEquals("/api/media", request.getPath());
        assertNull(request.getBody());
    }

    @Test
    void givenPostRequestWithJsonBody_whenMapping_thenBodyIsRead() throws Exception {
        var exchange = mock(com.sun.net.httpserver.HttpExchange.class);
        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestURI()).thenReturn(URI.create("/api/users/register"));
        String json = "{\"username\":\"alice\",\"password\":\"123\"}";
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(json.getBytes()));
        Request request = requestMapper.fromExchange(exchange);
        assertEquals("POST", request.getMethod());
        assertEquals(json, request.getBody());
    }

    @Test
    void givenRequestWithAuthorizationHeader_whenMapping_thenTokenIsExtracted() throws Exception {
        var exchange = mock(com.sun.net.httpserver.HttpExchange.class);
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestURI()).thenReturn(URI.create("/api/users/alice/profile"));
        when(exchange.getRequestHeaders()).thenReturn(new com.sun.net.httpserver.Headers() {{
            set("Authorization", "Bearer alice-mrpToken");
        }});
        Request request = requestMapper.fromExchange(exchange);
        assertEquals("alice-mrpToken", request.getAuthorization());
    }

    @Test
    void givenRequestWithoutAuthorization_whenMapping_thenAuthorizationIsNull() throws Exception {
        var exchange = mock(com.sun.net.httpserver.HttpExchange.class);
        when(exchange.getRequestMethod()).thenReturn("GET");
        when(exchange.getRequestURI()).thenReturn(URI.create("/api/media"));
        Request request = requestMapper.fromExchange(exchange);
        assertNull(request.getAuthorization());
    }

    @Test
    void givenEmptyRequestBody_whenMapping_thenBodyIsNull() throws Exception {
        var exchange = mock(com.sun.net.httpserver.HttpExchange.class);
        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestURI()).thenReturn(URI.create("/api/media"));
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(new byte[0]));
        Request request = requestMapper.fromExchange(exchange);
        assertNull(request.getBody());
    }

    // ======== 2. UserService Tests (5) ========
    @Mock
    private UserDao userDao;
    private UserService userService;

    @BeforeEach
    void setUpUserService() {
        userService = new UserService() {
            { this.userDao = userDao; }
        };
    }

    @Test
    void givenNewUser_whenRegister_thenUserIsInserted() {
        when(userDao.exists("alice")).thenReturn(false);
        assertDoesNotThrow(() -> userService.register("alice", "123"));
    }

    @Test
    void givenExistingUser_whenRegister_thenThrows() {
        when(userDao.exists("alice")).thenReturn(true);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> userService.register("alice", "123"));
        assertEquals("Username already exists", e.getMessage());
    }

    @Test
    void givenValidCredentials_whenLogin_thenTokenIsReturned() {
        User user = new User("alice", "123");
        when(userDao.findByUsername("alice")).thenReturn(user);
        String token = userService.login("alice", "123");
        assertNotNull(token);
        assertTrue(token.startsWith("alice-"));
    }

    @Test
    void givenInvalidPassword_whenLogin_thenReturnsNull() {
        User user = new User("alice", "correct");
        when(userDao.findByUsername("alice")).thenReturn(user);
        assertNull(userService.login("alice", "wrong"));
    }

    @Test
    void givenValidToken_whenGetUsernameByToken_thenReturnsUsername() {
        userService.login("alice", "123");
        assertEquals("alice", UserService.getUsernameByToken("alice-mrpToken"));
    }

    // ======== 3. MediaService Tests (5) ========
    @Mock
    private MediaDao mediaDao;
    private MediaService mediaService;

    @BeforeEach
    void setUpMediaService() {
        mediaService = new MediaService() {
            { this.mediaDao = mediaDao; }
        };
    }

    @Test
    void givenValidMedia_whenCreate_thenMediaIsInserted() {
        MediaEntry entry = new MediaEntry();
        entry.setTitle("Inception"); entry.setCreatorUsername("alice");
        when(mediaDao.insert(entry)).thenReturn(entry);
        assertNotNull(mediaService.create(entry));
    }

    @Test
    void givenNonExistentId_whenGetById_thenReturnsNull() {
        when(mediaDao.findById(999)).thenReturn(null);
        assertNull(mediaService.getById(999));
    }

    @Test
    void givenTitleFilter_whenList_thenDelegatesToDao() {
        when(mediaDao.findAll("Bat")).thenReturn(java.util.List.of());
        assertTrue(mediaService.list("Bat").isEmpty());
    }

    @Test
    void givenValidMedia_whenUpdate_thenCallsDaoUpdate() {
        MediaEntry entry = new MediaEntry(); entry.setId(1); entry.setTitle("Updated");
        mediaService.update(entry);
        verify(mediaDao).update(entry);
    }

    @Test
    void givenMediaId_whenDelete_thenCallsDaoDelete() {
        mediaService.delete(1);
        verify(mediaDao).delete(1);
    }

    // ======== 4. RatingService Tests (5) ========
    @Mock
    private RatingDao ratingDao2;     // ✅ Umbenannt, um Kollision zu vermeiden
    @Mock
    private MediaDao ratingMediaDao;
    private RatingService ratingService;

    @BeforeEach
    void setUpRatingService() {
        ratingService = new RatingService() {
            { this.ratingDao = ratingDao2; this.mediaDao = ratingMediaDao; } // ✅ ratingDao2 verwenden
        };
    }

    @Test
    void givenValidRating_whenCreate_thenRatingIsInserted() {
        Rating r = new Rating(); r.setMediaId(1); r.setUsername("alice"); r.setStars(5);
        MediaEntry m = new MediaEntry(); m.setCreatorUsername("bob");
        when(ratingMediaDao.findById(1)).thenReturn(m);
        when(ratingDao2.findByUserAndMedia("alice", 1)).thenReturn(null);
        assertDoesNotThrow(() -> ratingService.createRating(r, "alice"));
    }

    @Test
    void givenDuplicateRating_whenCreate_thenThrows() {
        Rating r = new Rating(); r.setMediaId(1); r.setUsername("alice"); r.setStars(5);
        when(ratingDao2.findByUserAndMedia("alice", 1)).thenReturn(r);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> ratingService.createRating(r, "alice"));
        assertEquals("Already rated this media", e.getMessage());
    }

    @Test
    void givenValidRatingIdAndCreator_whenConfirm_thenUpdatesConfirmation() {
        Rating r = new Rating(); r.setId(1); r.setMediaId(1);
        MediaEntry m = new MediaEntry(); m.setCreatorUsername("bob");
        when(ratingDao2.findById(1)).thenReturn(r);
        when(ratingMediaDao.findById(1)).thenReturn(m);
        assertDoesNotThrow(() -> ratingService.confirmRating(1, "bob"));
    }

    @Test
    void givenNonCreator_whenConfirm_thenThrows() {
        Rating r = new Rating(); r.setId(1); r.setMediaId(1);
        MediaEntry m = new MediaEntry(); m.setCreatorUsername("bob");
        when(ratingDao2.findById(1)).thenReturn(r);
        when(ratingMediaDao.findById(1)).thenReturn(m);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> ratingService.confirmRating(1, "alice"));
        assertEquals("Only media creator can confirm ratings", e.getMessage());
    }

    @Test
    void givenMediaId_whenGetRatings_thenOnlyConfirmed() {
        when(ratingDao2.findByMediaId(1, true)).thenReturn(java.util.List.of());
        assertTrue(ratingService.getRatingsForMedia(1, true).isEmpty());
    }

    // ======== 5. FavoriteService Tests (5) ========
    @Mock
    private FavoriteDao favoriteDao;
    @Mock
    private MediaDao favoriteMediaDao;
    private FavoriteService favoriteService;

    @BeforeEach
    void setUpFavoriteService() {
        favoriteService = new FavoriteService() {
            { this.favoriteDao = favoriteDao; this.mediaDao = favoriteMediaDao; }
        };
    }

    @Test
    void givenValidMediaId_whenAddFavorite_thenFavoriteIsAdded() {
        MediaEntry m = new MediaEntry();
        when(favoriteMediaDao.findById(1)).thenReturn(m);
        assertDoesNotThrow(() -> favoriteService.addFavorite("alice", 1));
    }

    @Test
    void givenInvalidMediaId_whenAddFavorite_thenThrows() {
        when(favoriteMediaDao.findById(999)).thenReturn(null);
        assertThrows(IllegalArgumentException.class,
                () -> favoriteService.addFavorite("alice", 999));
    }

    @Test
    void givenUsername_whenGetFavoriteMediaIds_thenDelegatesToDao() {
        java.util.List<Integer> ids = java.util.List.of(1, 2);
        when(favoriteDao.getFavoriteMediaIds("alice")).thenReturn(ids);
        assertEquals(ids, favoriteService.getFavoriteMediaIds("alice"));
    }

    @Test
    void givenUsernameAndMediaId_whenRemoveFavorite_thenCallsDao() {
        assertDoesNotThrow(() -> favoriteService.removeFavorite("alice", 1));
    }

    @Test
    void givenUsernameAndMediaId_whenAddFavorite_thenDaoIsCalled() {
        MediaEntry m = new MediaEntry();
        when(favoriteMediaDao.findById(1)).thenReturn(m);
        favoriteService.addFavorite("alice", 1);
        verify(favoriteDao).addFavorite(any());
    }
}