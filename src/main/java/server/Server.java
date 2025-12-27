package server;

import com.sun.net.httpserver.HttpServer;
import util.RequestMapper;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Server {
    public static void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", port), 0);
        // ✅ Korrekt: Übergib Application und RequestMapper
        server.createContext("/", new Handler(new Application(), new RequestMapper()));
        server.setExecutor(null);
        server.start();
        System.out.println("Server running on http://localhost:" + port);
    }
}