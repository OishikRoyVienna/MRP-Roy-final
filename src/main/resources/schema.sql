
-- 1. Users
CREATE TABLE IF NOT EXISTS users (
                                     id SERIAL PRIMARY KEY,
                                     username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL
    );

-- 2. Media Entries
CREATE TABLE IF NOT EXISTS media_entries (
                                             id SERIAL PRIMARY KEY,
                                             title VARCHAR(255) NOT NULL,
    description TEXT,
    media_type VARCHAR(20) NOT NULL CHECK (media_type IN ('movie', 'series', 'game', 'book')),
    release_year INTEGER CHECK (release_year >= 1800 AND release_year <= EXTRACT(YEAR FROM CURRENT_DATE) + 10),
    genres TEXT[],  -- z. B. ARRAY['sci-fi','action']
    age_restriction INTEGER CHECK (age_restriction IN (0, 6, 12, 16, 18)),
    creator_username VARCHAR(50) NOT NULL REFERENCES users(username) ON DELETE CASCADE
    );

-- 3. Ratings (mit Moderation!)
CREATE TABLE IF NOT EXISTS ratings (
                                       id SERIAL PRIMARY KEY,
                                       media_id INTEGER NOT NULL REFERENCES media_entries(id) ON DELETE CASCADE,
    username VARCHAR(50) NOT NULL REFERENCES users(username) ON DELETE CASCADE,
    stars INTEGER NOT NULL CHECK (stars BETWEEN 1 AND 5),
    comment TEXT,
    is_confirmed BOOLEAN DEFAULT false,  -- 🔑 Moderation: false = unsichtbar
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- 4. Favorites
CREATE TABLE IF NOT EXISTS favorites (
                                         username VARCHAR(50) NOT NULL REFERENCES users(username) ON DELETE CASCADE,
    media_id INTEGER NOT NULL REFERENCES media_entries(id) ON DELETE CASCADE,
    PRIMARY KEY (username, media_id)
    );

-- Indizes für Performance
CREATE INDEX IF NOT EXISTS idx_media_title ON media_entries(title);
CREATE INDEX IF NOT EXISTS idx_media_genres ON media_entries USING GIN(genres);
CREATE INDEX IF NOT EXISTS idx_ratings_media ON ratings(media_id);
CREATE INDEX IF NOT EXISTS idx_ratings_user ON ratings(username);
CREATE INDEX IF NOT EXISTS idx_ratings_confirmed ON ratings(is_confirmed);
CREATE INDEX IF NOT EXISTS idx_favorites_user ON favorites(username);