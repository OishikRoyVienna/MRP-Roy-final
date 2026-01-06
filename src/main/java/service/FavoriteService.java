package service;

import dao.FavoriteDao;
import dao.MediaDao;
import model.Favorite;

public class FavoriteService {
    protected FavoriteDao favoriteDao = new FavoriteDao();
    protected MediaDao mediaDao = new MediaDao();

    /**
     * Fügt ein Medium zu den Favoriten eines Benutzers hinzu.
     *
     * @param username  Der Benutzername
     * @param mediaId   Die ID des Media
     * @throws IllegalArgumentException wenn das Media nicht existiert
     */
    public void addFavorite(String username, int mediaId) {
        // Prüfe, Medium existiert?
        if (mediaDao.findById(mediaId) == null) {
            throw new IllegalArgumentException("Media not found");
        }
        favoriteDao.addFavorite(new Favorite(username, mediaId));
    }

    /**
     * Entfernt ein Media aus den Favoriten
     */
    public void removeFavorite(String username, int mediaId) {
        favoriteDao.removeFavorite(username, mediaId);
    }

    public java.util.List<Integer> getFavoriteMediaIds(String username) {
        return favoriteDao.getFavoriteMediaIds(username);
    }

    /**
     * Prüft ob ein Media zu den Favoriten von Benutzers gehört
     */
    public boolean isFavorite(String username, int mediaId) {
        return favoriteDao.isFavorite(username, mediaId);
    }
}