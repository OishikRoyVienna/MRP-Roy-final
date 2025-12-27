package main;

import dao.DatabaseManager;
import server.Server;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("🚀 Starting MRP Final Server...");

        // 1. Datenbank initialisieren (Tabellen erstellen, falls nötig)
        DatabaseManager.initializeDatabase();

        // 2. Server starten
        Server.start(8080);

        System.out.println("Server ready at http://localhost:8080");
    }
}