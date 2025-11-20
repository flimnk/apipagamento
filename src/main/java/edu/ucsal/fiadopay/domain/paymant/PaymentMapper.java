package edu.ucsal.fiadopay.domain.paymant;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.ucsal.fiadopay.domain.paymant.dto.PaymentResponse;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
public class PaymentMapper {
   static public PaymentResponse toResponse(Payment payment) {

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
