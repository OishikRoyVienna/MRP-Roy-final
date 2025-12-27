package dao;

import java.sql.*;

public class DatabaseManager {
    private static final String URL = "jdbc:postgresql://localhost:5432/mrp";
    private static final String USER = "mrp";
    private static final String PASSWORD = "mrp";

    public static void initializeDatabase() {
        try (Connection conn = getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getTables(null, null, "users", new String[]{"TABLE"})) {
                if (!rs.next()) {
                    //erstelung von tabellen
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute("""
                            CREATE TABLE users (
                                id SERIAL PRIMARY KEY,
                                username VARCHAR(50) UNIQUE NOT NULL,
                                password_hash VARCHAR(255) NOT NULL
                            );
                            """);

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
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database", e);
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