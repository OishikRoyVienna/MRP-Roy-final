package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Handler;

public class Server {
    public static void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", port), 0);
        server.createContext("/", new Handler()); // ← nutzt deinen zentralen Handler
        server.setExecutor(null);
        server.start();
        System.out.println("✅ MRP Final Server running on http://localhost:" + port);
    }
}