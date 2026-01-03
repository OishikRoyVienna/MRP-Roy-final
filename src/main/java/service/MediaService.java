package service;

import dao.MediaDao;
import dao.RatingDao;
import model.MediaEntry;
import java.util.List;

public class MediaService {
    protected MediaDao mediaDao = new MediaDao();
    protected RatingDao ratingDao = new RatingDao(); // ✅ Feld hinzufügen (optional, aber sauberer)

    public MediaEntry create(MediaEntry entry) {
        return mediaDao.insert(entry);
    }

    //Nur EINE getById-Methode – mit averageRating
    public MediaEntry getById(int id) {
        MediaEntry entry = mediaDao.findById(id);
        if (entry != null) {
            //durchschnitt aus RatingDao holen
            Double avg = ratingDao.getAverageRating(id);  // nutzt das Feld oben
            entry.setAverageRating(avg);
        }
        return entry;
    }

    public List<MediaEntry> list(String titleFilter) {
        return mediaDao.findAll(titleFilter);
    }

    public void update(MediaEntry entry) {
        mediaDao.update(entry);
    }

    public void delete(int id) {
        mediaDao.delete(id);
    }
}