package edu.ucsal.fiadopay.domain.paymant.strategy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.ucsal.fiadopay.annotations.paymentMethod.PaymentMethod;
import edu.ucsal.fiadopay.domain.merchant.Merchant;
import edu.ucsal.fiadopay.domain.paymant.MethodPayment;
import edu.ucsal.fiadopay.domain.paymant.Payment;
import edu.ucsal.fiadopay.domain.paymant.Status;
import edu.ucsal.fiadopay.domain.paymant.details.CardDetails;
import edu.ucsal.fiadopay.domain.paymant.dto.PaymentRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@Component
@PaymentMethod(type = "CARD")
public class CardPaymentStrategy implements PaymentStrategy {

    private final ObjectMapper mapper;



    @Override
    public Payment process(PaymentRequest req, Merchant merchant, String idemKey) {

        // 1. Extrair detalhes enviados pelo cliente
        CardDetails cardReq = mapper.convertValue(req.details(), CardDetails.class);
        int installments = cardReq.getInstallments();
        BigDecimal baseAmount = req.amount();

        // 2. Calcular o valor final usando regras do merchant
        BigDecimal finalAmount = calculateFinalAmount(
                merchant.getInterest(),
                installments,
                baseAmount
        );

        BigDecimal interestAmount = finalAmount.subtract(baseAmount);
        BigDecimal installmentAmount = finalAmount
                .divide(BigDecimal.valueOf(installments), 2, RoundingMode.HALF_UP);


        CardDetails detailsToSave = new CardDetails(
                installments,
                baseAmount,
                interestAmount,
                installmentAmount,
                finalAmount
        );

        // 4. Criar o pagamento
        Payment payment = new Payment();
        payment.setId("pay_" + UUID.randomUUID());
        payment.setMethod(MethodPayment.CARD);
        payment.setAmount(baseAmount);
        payment.setCurrency(req.currency());
        payment.setMerchant(merchant);
        payment.setIdempotencyKey(idemKey);
        payment.setStatus(Status.PENDING);
        payment.setCreatedAt(Instant.now());
        payment.setUpdatedAt(Instant.now());
        payment.setMetadataOrderId(req.metadataOrderId());
        try {
            payment.setDetailsJson(mapper.writeValueAsString(detailsToSave));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erro ao salvar detalhes", e);
        }

        return payment;
    }


    private BigDecimal calculateFinalAmount(
            Double interest,
            int installments,
            BigDecimal baseAmount
    ) {


        double rate = interest / 100.0;
        BigDecimal factor = BigDecimal.valueOf(Math.pow(1 + rate, installments));

        return baseAmount.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }
}
