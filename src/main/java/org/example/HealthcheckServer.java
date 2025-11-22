package org.example;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public final class HealthcheckServer {

    private HealthcheckServer() {
        // Utility class
    }

    public static HttpServer start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/health", new HealthcheckHandler());
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();
        return server;
    }

    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = start(port);
        System.out.println("Healthcheck server started on http://localhost:" + port + "/health");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop(0)));
    }

    static class HealthcheckHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                exchange.close();
                return;
            }

            byte[] response = "OK".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        }
    }
}

