package service;

import dao.MediaDao;
import dao.RatingDao;
import model.MediaEntry;
import java.util.List;

public class MediaService {
    protected MediaDao mediaDao = new MediaDao();
    protected RatingDao ratingDao = new RatingDao(); // optional, aber sauber

    //5 Parameter-Version → benötigt für Filter
    public List<MediaEntry> list(String titleFilter, String genre, String mediaType,
                                 Integer minAge, Double minRating) {
        return mediaDao.findAll(titleFilter, genre, mediaType, minAge, minRating);
    }

    public List<MediaEntry> list(String titleFilter) {
        return list(titleFilter, null, null, null, null);
    }

    public MediaEntry create(MediaEntry entry) {
        return mediaDao.insert(entry);
    }

    public MediaEntry getById(int id) {
        MediaEntry entry = mediaDao.findById(id);
        if (entry != null) {
            Double avg = ratingDao.getAverageRating(id);
            entry.setAverageRating(avg);
        }
        return entry;
    }

    public void update(MediaEntry entry) {
        mediaDao.update(entry);
    }

    public void delete(int id) {
        mediaDao.delete(id);
    }
}