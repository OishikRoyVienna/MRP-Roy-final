package model;


public class Favorite {
    private String username;
    private int mediaId;

    public Favorite() {}
    public Favorite(String username, int mediaId) {
        this.username = username;
        this.mediaId = mediaId;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getMediaId() { return mediaId; }
    public void setMediaId(int mediaId) { this.mediaId = mediaId; }
}