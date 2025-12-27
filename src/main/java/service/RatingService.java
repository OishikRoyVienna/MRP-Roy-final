package service;

import dao.RatingDao;
import dao.MediaDao;
import model.Rating;

public class RatingService {
    private final RatingDao ratingDao = new RatingDao();
    private final MediaDao mediaDao = new MediaDao();

    /**
     * Erstellt eine neue Bewertung.
     *
     * @param rating    Die Bewertung (ohne ID)
     * @param requester Der Benutzername des Erstellers
     * @return Die gespeicherte Bewertung mit ID
     * @throws IllegalArgumentException bei doppelter Bewertung oder ungültigem Medium
     */
    public Rating createRating(Rating rating, String requester) {
        // 1. Nur eigene Bewertungen erlaubt
        if (!rating.getUsername().equals(requester)) {
            throw new IllegalArgumentException("Cannot rate for another user");
        }

        // 2. Medium muss existieren
        if (mediaDao.findById(rating.getMediaId()) == null) {
            throw new IllegalArgumentException("Media not found");
        }

        // 3. Nur eine Bewertung pro User/Media
        Rating existing = ratingDao.findByUserAndMedia(rating.getUsername(), rating.getMediaId());
        if (existing != null) {
            throw new IllegalArgumentException("Already rated this media");
        }

        return ratingDao.insert(rating);
    }

    /**
     * Bestätigt eine Bewertung (Moderation).
     * Nur der Ersteller des Mediums darf bestätigen.
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
     *
     * @param mediaId       Die Medien-ID
     * @param onlyConfirmed Nur bestätigte Bewertungen
     */
    public java.util.List<Rating> getRatingsForMedia(int mediaId, boolean onlyConfirmed) {
        return ratingDao.findByMediaId(mediaId, onlyConfirmed);
    }

}