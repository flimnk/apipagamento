package edu.ucsal.fiadopay.service.webhook;

import edu.ucsal.fiadopay.domain.WebhookDelivery.WebhookDelivery;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class WebhookSender {

    public boolean send(WebhookDelivery d) throws Exception {
        var client = HttpClient.newHttpClient();
        var req = HttpRequest.newBuilder(URI.create(d.getTargetUrl()))
                .header("Content-Type", "application/json")
                .header("X-Event-Type", d.getEventType())
                .header("X-Signature", d.getSignature())
                .POST(HttpRequest.BodyPublishers.ofString(d.getPayload()))
                .build();

        var res = client.send(req, HttpResponse.BodyHandlers.ofString());
        return res.statusCode() >= 200 && res.statusCode() < 300;
    }
}
