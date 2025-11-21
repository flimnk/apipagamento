package edu.ucsal.fiadopay.service.payment;
import edu.ucsal.fiadopay.domain.paymant.Status;
import edu.ucsal.fiadopay.domain.paymant.Payment;
import edu.ucsal.fiadopay.repo.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PaymentProcessor {

    @Value("${fiadopay.processing-delay-ms}")
    long delay;

    @Value("${fiadopay.failure-rate}")
    double failRate;

    private final PaymentRepository payments;

    public Payment process(String paymentId) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ignored) {}

        var payment = payments.findById(paymentId).orElse(null);
        if (payment == null) return null;

        boolean approved = Math.random() > failRate;

        payment.setStatus(approved ? Status.APPROVED : Status.DECLINED);
        payment.setUpdatedAt(Instant.now());

        return payments.save(payment);
    }
}
