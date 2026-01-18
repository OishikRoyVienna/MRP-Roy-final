package dao;

import java.sql.*;

public class DatabaseManager {
    protected static final String URL = "jdbc:postgresql://localhost:5432/mrp";
    protected static final String USER = "mrp";
    protected static final String PASSWORD = "mrp";

    public static void initializeDatabase() {
        try (Connection conn = getConnection()) {
            // 1. Stelle sicher, dass Tabellen existieren
            ensureTablesExist(conn);

            // 2. LEERE IMMER alle Tabellen (unabhängig vom Vorhandensein)
            try (Statement stmt = conn.createStatement()) {
                // Reihenfolge wegen Fremdschlüssel: zuerst abhängige Tabellen
                stmt.execute("TRUNCATE TABLE likes RESTART IDENTITY CASCADE");
                stmt.execute("TRUNCATE TABLE ratings RESTART IDENTITY CASCADE");
                stmt.execute("TRUNCATE TABLE favorites RESTART IDENTITY CASCADE");
                stmt.execute("TRUNCATE TABLE media_entries RESTART IDENTITY CASCADE");
                stmt.execute("TRUNCATE TABLE users RESTART IDENTITY CASCADE");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to reset database", e);
        }
    }

    private static void ensureTablesExist(Connection conn) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();

        // Prüfe und erstelle users
        try (ResultSet rs = meta.getTables(null, null, "users", new String[]{"TABLE"})) {
            if (!rs.next()) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("""
                        CREATE TABLE users (
                            id SERIAL PRIMARY KEY,
                            username VARCHAR(50) UNIQUE NOT NULL,
                            password_hash VARCHAR(255) NOT NULL
                        );
                        """);
                }
            }
        }

        // Prüfe und erstelle media_entries
        try (ResultSet rs = meta.getTables(null, null, "media_entries", new String[]{"TABLE"})) {
            if (!rs.next()) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("""
                        CREATE TABLE media_entries (
                            id SERIAL PRIMARY KEY,
                            title VARCHAR(255) NOT NULL,
                            description TEXT,
                            media_type VARCHAR(20) NOT NULL,
                            release_year INTEGER,
                            genres TEXT[],
                            age_restriction INTEGER,
                            creator_username VARCHAR(50) REFERENCES users(username) ON DELETE CASCADE
                        );
                        """);
                }
            }
        }

        // Prüfe und erstelle ratings
        try (ResultSet rs = meta.getTables(null, null, "ratings", new String[]{"TABLE"})) {
            if (!rs.next()) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("""
                        CREATE TABLE ratings (
                            id SERIAL PRIMARY KEY,
                            media_id INTEGER NOT NULL REFERENCES media_entries(id) ON DELETE CASCADE,
                            username VARCHAR(50) NOT NULL REFERENCES users(username) ON DELETE CASCADE,
                            stars INTEGER CHECK (stars BETWEEN 1 AND 5),
                            comment TEXT,
                            timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            is_confirmed BOOLEAN DEFAULT false
                        );
                        """);
                }
            }
        }

        // Prüfe und erstelle favorites
        try (ResultSet rs = meta.getTables(null, null, "favorites", new String[]{"TABLE"})) {
            if (!rs.next()) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("""
                        CREATE TABLE favorites (
                            username VARCHAR(50) NOT NULL REFERENCES users(username) ON DELETE CASCADE,
                            media_id INTEGER NOT NULL REFERENCES media_entries(id) ON DELETE CASCADE,
                            PRIMARY KEY (username, media_id)
                        );
                        """);
                }
            }
        }

        // Prüfe und erstelle likes
        try (ResultSet rs = meta.getTables(null, null, "likes", new String[]{"TABLE"})) {
            if (!rs.next()) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("""
                        CREATE TABLE likes (
                            username VARCHAR(50) NOT NULL REFERENCES users(username) ON DELETE CASCADE,
                            rating_id INTEGER NOT NULL REFERENCES ratings(id) ON DELETE CASCADE,
                            PRIMARY KEY (username, rating_id)
                        );
                        """);
                }
            }
        }
    }

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL JDBC Driver not found", e);
        }
    }

    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}