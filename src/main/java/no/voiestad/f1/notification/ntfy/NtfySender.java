package no.voiestad.f1.notification.ntfy;

import java.net.URI;
import java.net.URISyntaxException;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import no.voiestad.f1.notification.ntfy.message.NtfyMessage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NtfySender {

    @Value("${ntfy.user}")
    private String user;

    @Value("${ntfy.password}")
    private String password;

    public boolean send(NtfyMessage message) {
        try {
            String credentials = user + ":" + password;
            String basicAuth = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

            HttpClient client = HttpClient.newHttpClient();
            String json = new ObjectMapper().writeValueAsString(message);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://ntfy.f1gjettespill.no"))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Authorization", basicAuth)
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return true;
            }
        } catch (IOException | InterruptedException | URISyntaxException ignored) {
        }
            return false;
    }
}
