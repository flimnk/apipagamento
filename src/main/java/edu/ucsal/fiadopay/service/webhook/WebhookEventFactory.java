package edu.ucsal.fiadopay.service.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.ucsal.fiadopay.domain.paymant.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WebhookEventFactory {

    private final ObjectMapper mapper;

    public String buildPaymentUpdatedEvent(Payment p) {
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
            return mapper.writeValueAsString(event);

        } catch (Exception e) {
            return null;
        }
    }
}
