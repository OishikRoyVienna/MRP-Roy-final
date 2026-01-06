package model;

import java.time.Instant;

public class Rating {
    private Integer id;
    private Integer mediaId;
    private String username;
    private Integer stars;
    private String comment;
    private boolean confirmed;

    private String timestamp;

    public Rating() {
        this.timestamp = Instant.now().toString();  // z. B. "2026-01-06T10:30:45.123Z"
        this.confirmed = false;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getMediaId() { return mediaId; }
    public void setMediaId(Integer mediaId) { this.mediaId = mediaId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Integer getStars() { return stars; }
    public void setStars(Integer stars) {
        if (stars != null && (stars < 1 || stars > 5))
            throw new IllegalArgumentException("Stars must be between 1 and 5");
        this.stars = stars;
    }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public boolean isConfirmed() { return confirmed; }
    public void setConfirmed(boolean confirmed) { this.confirmed = confirmed; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public Instant getTimestampAsInstant() {
        return timestamp != null ? Instant.parse(timestamp) : null;
    }
}