package model;

public class MediaEntry {
    private Integer id;
    private String title;
    private String description;
    private String mediaType; // "movie", "series", "game"
    private Integer releaseYear;
    private String[] genres;
    private Integer ageRestriction;
    private String creatorUsername;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

    public Integer getReleaseYear() { return releaseYear; }
    public void setReleaseYear(Integer releaseYear) { this.releaseYear = releaseYear; }

    public String[] getGenres() { return genres; }
    public void setGenres(String[] genres) { this.genres = genres; }

    public Integer getAgeRestriction() { return ageRestriction; }
    public void setAgeRestriction(Integer ageRestriction) { this.ageRestriction = ageRestriction; }

    public String getCreatorUsername() { return creatorUsername; }
    public void setCreatorUsername(String creatorUsername) { this.creatorUsername = creatorUsername; }

    private Double averageRating;

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }
}