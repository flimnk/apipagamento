package edu.ucsal.fiadopay.service.webhook;

import edu.ucsal.fiadopay.domain.WebhookDelivery.WebhookDelivery;
import edu.ucsal.fiadopay.repo.WebhookDeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Service

public class WebhookDeliveryService {
    private final ExecutorService webhookExecutor;

    private final WebhookDeliveryRepository deliveries;
    private final WebhookSender sender;

    public WebhookDeliveryService(@Qualifier("webhookExecutor") ExecutorService webhookExecutor, WebhookDeliveryRepository deliveries, WebhookSender sender) {
        this.webhookExecutor = webhookExecutor;
        this.deliveries = deliveries;
        this.sender = sender;
    }

    public void scheduleDelivery(WebhookDelivery delivery) {
        deliveries.save(delivery);

        webhookExecutor.submit(() -> attempt(delivery.getId()));
    }

    private void attempt(Long id) {
        var d = deliveries.findById(id).orElse(null);
        if (d == null) return;

        try {
            boolean ok = sender.send(d);

            d.setAttempts(d.getAttempts() + 1);
            d.setLastAttemptAt(Instant.now());
            d.setDelivered(ok);
            deliveries.save(d);

            if (!ok && d.getAttempts() < 5) {
                Thread.sleep(1000L * d.getAttempts());
                attempt(id);
            }

        } catch (Exception e) {
            d.setAttempts(d.getAttempts() + 1);
            d.setLastAttemptAt(Instant.now());
            deliveries.save(d);

            if (d.getAttempts() < 5) {
                try { Thread.sleep(1000L * d.getAttempts()); } catch (Exception ignored) {}
                attempt(id);
            }
        }
    }
}
