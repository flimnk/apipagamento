package edu.ucsal.fiadopay.service.webhook;

import edu.ucsal.fiadopay.domain.WebhookDelivery.WebhookDelivery;
import edu.ucsal.fiadopay.repo.WebhookDeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ExecutorService;

@Slf4j
@Service
public class WebhookDeliveryService {

    private final ExecutorService webhookExecutor;
    private final WebhookDeliveryRepository deliveries;
    private final WebhookSender sender;

    public WebhookDeliveryService(
            @Qualifier("webhookExecutor") ExecutorService webhookExecutor,
            WebhookDeliveryRepository deliveries,
            WebhookSender sender
    ) {
        this.webhookExecutor = webhookExecutor;
        this.deliveries = deliveries;
        this.sender = sender;
    }

    public void scheduleDelivery(WebhookDelivery delivery) {
        deliveries.save(delivery);
        log.info("Webhook agendado: id={}, url={}, payloadSize={}",
                delivery.getId(),
                delivery.getTargetUrl(),
                delivery.getPayload() != null ? delivery.getPayload().length() : 0
        );

        webhookExecutor.submit(() -> attempt(delivery.getId()));
    }

    private void attempt(Long id) {
        var d = deliveries.findById(id).orElse(null);
        if (d == null) {
            log.error("Tentativa abortada: webhook id={} não encontrado", id);
            return;
        }

        log.info("Iniciando tentativa {} para webhook id={} (url={})",
                d.getAttempts() + 1,
                d.getId(),
                d.getTargetUrl()
        );

        try {
            boolean ok = sender.send(d);

            d.setAttempts(d.getAttempts() + 1);
            d.setLastAttemptAt(Instant.now());
            d.setDelivered(ok);
            deliveries.save(d);

            if (ok) {
                log.info("Webhook entregue com sucesso: id={}, attempts={}", d.getId(), d.getAttempts());
                return;
            }

            log.warn("Falha ao entregar webhook id={} tentativa={}", d.getId(), d.getAttempts());

            if (d.getAttempts() < 5) {
                long wait = 1000L * d.getAttempts();
                log.info("Reagendando id={} para daqui {}ms", d.getId(), wait);
                Thread.sleep(wait);
                attempt(id);
            } else {
                log.error("Webhook id={} atingiu o limite máximo de tentativas (5)", d.getId());
            }

        } catch (Exception e) {
            log.error("Erro inesperado no webhook id={}: {}", d.getId(), e.getMessage(), e);

            d.setAttempts(d.getAttempts() + 1);
            d.setLastAttemptAt(Instant.now());
            deliveries.save(d);

            if (d.getAttempts() < 5) {
                long wait = 1000L * d.getAttempts();
                log.warn("Erro – reagendando id={} para daqui {}ms", d.getId(), wait);
                try { Thread.sleep(wait); } catch (Exception ignored) {}
                attempt(id);
            } else {
                log.error("Webhook id={} falhou definitivamente após 5 tentativas.", d.getId());
            }
        }
    }
}
