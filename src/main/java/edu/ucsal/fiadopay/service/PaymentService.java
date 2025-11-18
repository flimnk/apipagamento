package edu.ucsal.fiadopay.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.ucsal.fiadopay.domain.paymant.Status;
import edu.ucsal.fiadopay.domain.paymant.dto.PaymentRequest;
import edu.ucsal.fiadopay.domain.paymant.dto.PaymentResponse;
import edu.ucsal.fiadopay.domain.merchant.Merchant;
import edu.ucsal.fiadopay.domain.paymant.Payment;
import edu.ucsal.fiadopay.domain.WebhookDelivery.WebhookDelivery;
import edu.ucsal.fiadopay.domain.paymant.factory.PaymentFactoryImpl;
import edu.ucsal.fiadopay.domain.paymant.strategy.PaymentStrategy;
import edu.ucsal.fiadopay.repo.MerchantRepository;
import edu.ucsal.fiadopay.repo.PaymentRepository;
import edu.ucsal.fiadopay.repo.WebhookDeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final MerchantRepository merchants;
    private final PaymentRepository payments;
    private final WebhookDeliveryRepository deliveries;
    private final ObjectMapper objectMapper;
    private final PaymentFactoryImpl paymentFactory;


    @Value("${fiadopay.webhook-secret}")
    String secret;
    @Value("${fiadopay.processing-delay-ms}")
    long delay;
    @Value("${fiadopay.failure-rate}")
    double failRate;


    @Transactional
    public PaymentResponse createPayment(Merchant merchant, String idemKey, PaymentRequest req) {
        var mid = merchant.getId();
        if (idemKey != null) {
            var existing = payments.findByIdempotencyKeyAndMerchantId(idemKey, mid);

            if (existing.isPresent()) {
                return toResponse(existing.get());
            }
        }
        PaymentStrategy strategy = paymentFactory.getStrategy(req.method());
        Payment payment = strategy.process(req, merchant, idemKey);
        CompletableFuture.runAsync(() -> processAndWebhook(payment.getId()));
        return toResponse(payment);
    }

    public PaymentResponse getPayment(String id, Merchant merchant) {
        if (merchant == null || merchant.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Merchant information is missing");
        }

        var payment = payments.findById(id)
                .filter(p -> p.belongsToMerchant(merchant.getId()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Payment does not exist or does not belong to this merchant."
                ));

        return toResponse(payment);
    }

    @Transactional
    public Map<String, Object> refund(Merchant merchant, String paymentId) {
        var p = payments.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!p.belongsToMerchant(merchant.getId())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (p.getStatus() == Status.REFUNDED) {
            return Map.of("id", "ref_" + UUID.randomUUID(), "status", "ALREADY_REFUNDED");
        }

        return Map.of("id", "ref_" + UUID.randomUUID(), "status", "PENDING");
    }

    private void processAndWebhook(String paymentId) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ignored) {
        }
        var p = payments.findById(paymentId).orElse(null);
        if (p == null) return;

        var approved = Math.random() > failRate;
        p.setStatus(approved ? Status.APPROVED : Status.DECLINED);
        p.setUpdatedAt(Instant.now());
        payments.save(p);

        sendWebhook(p);
    }

    private void sendWebhook(Payment p) {
        var merchant = merchants.findById(p.getMerchant().getId()).orElse(null);
        if (merchant == null || merchant.getWebhookUrl() == null || merchant.getWebhookUrl().isBlank()) return;

        String payload;
        try {
            var data = Map.of(
                    "paymentId", p.getId(),
                    "status", p.getStatus().name(),
                    "occurredAt", Instant.now().toString()
            );
            var event = Map.of(
                    "id", "evt_" + UUID.randomUUID().toString().substring(0, 8),
                    "type", "payment.updated",
                    "data", data
            );
            payload = objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            // fallback mínimo: não envia webhook se falhar a serialização
            return;
        }

        var signature = hmac(payload, secret);

        var delivery = deliveries.save(WebhookDelivery.builder()
                .eventId("evt_" + UUID.randomUUID().toString().substring(0, 8))
                .eventType("payment.updated")
                .paymentId(p.getId())
                .targetUrl(merchant.getWebhookUrl())
                .signature(signature)
                .payload(payload)
                .attempts(0)
                .delivered(false)
                .lastAttemptAt(null)
                .build());

        CompletableFuture.runAsync(() -> tryDeliver(delivery.getId()));
    }

    private void tryDeliver(Long deliveryId) {
        var d = deliveries.findById(deliveryId).orElse(null);
        if (d == null) return;
        try {
            var client = HttpClient.newHttpClient();
            var req = HttpRequest.newBuilder(URI.create(d.getTargetUrl()))
                    .header("Content-Type", "application/json")
                    .header("X-Event-Type", d.getEventType())
                    .header("X-Signature", d.getSignature())
                    .POST(HttpRequest.BodyPublishers.ofString(d.getPayload()))
                    .build();
            var res = client.send(req, HttpResponse.BodyHandlers.ofString());
            d.setAttempts(d.getAttempts() + 1);
            d.setLastAttemptAt(Instant.now());
            d.setDelivered(res.statusCode() >= 200 && res.statusCode() < 300);
            deliveries.save(d);
            if (!d.isDelivered() && d.getAttempts() < 5) {
                Thread.sleep(1000L * d.getAttempts());
                tryDeliver(deliveryId);
            }
        } catch (Exception e) {
            d.setAttempts(d.getAttempts() + 1);
            d.setLastAttemptAt(Instant.now());
            d.setDelivered(false);
            deliveries.save(d);
            if (d.getAttempts() < 5) {
                try {
                    Thread.sleep(1000L * d.getAttempts());
                } catch (InterruptedException ignored) {
                }
                tryDeliver(deliveryId);
            }
        }
    }

    private static String hmac(String payload, String secret) {
        try {
            var mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(new javax.crypto.spec.SecretKeySpec(secret.getBytes(), "HmacSHA256"));
            return Base64.getEncoder().encodeToString(mac.doFinal(payload.getBytes()));
        } catch (Exception e) {
            return "";
        }
    }



    private PaymentResponse toResponse(Payment payment) {

        ObjectMapper mapper = new ObjectMapper();

        Object details = null;

        if (payment.getDetailsJson() != null) {
            try {
                details = mapper.readValue(payment.getDetailsJson(), Object.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        return new PaymentResponse(
                payment.getId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getMethod(),
                payment.getStatus(),
                payment.getCreatedAt(),
                details,
                payment.getMetadataOrderId()
        );
    }
}