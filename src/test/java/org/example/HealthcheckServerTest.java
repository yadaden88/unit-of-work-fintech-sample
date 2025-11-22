package org.example;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HealthcheckServerTest {

    @Test
    void healthEndpointReturns200Ok() throws Exception {
        HttpServer server = HealthcheckServer.start(0); // 0 = ephemeral port
        int port = server.getAddress().getPort();

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + "/health"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode());
            assertEquals("OK", response.body());
        } finally {
            server.stop(0);
        }
    }
}

