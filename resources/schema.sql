CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS media_entries (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    media_type VARCHAR(20) NOT NULL,
    release_year INTEGER,
    genres TEXT[],
    age_restriction INTEGER,
    creator_username VARCHAR(50) REFERENCES users(username) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS ratings (
    id SERIAL PRIMARY KEY,
    media_id INTEGER NOT NULL REFERENCES media_entries(id) ON DELETE CASCADE,
    username VARCHAR(50) NOT NULL REFERENCES users(username) ON DELETE CASCADE,
    stars INTEGER CHECK (stars BETWEEN 1 AND 5),
    comment TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_confirmed BOOLEAN DEFAULT false
);
