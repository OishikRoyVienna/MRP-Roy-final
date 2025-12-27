package service;

import dao.MediaDao;
import model.MediaEntry;
import java.util.List;

public class MediaService {
    private final MediaDao mediaDao = new MediaDao();

    public MediaEntry create(MediaEntry entry) {
        return mediaDao.insert(entry);
    }

    public MediaEntry getById(int id) {
        return mediaDao.findById(id);
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