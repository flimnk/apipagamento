package edu.ucsal.fiadopay.domain.paymant.strategy;

import edu.ucsal.fiadopay.domain.paymant.MethodPayment;
import edu.ucsal.fiadopay.domain.paymant.Payment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PixRefundStrategy implements RefundStrategy {
    @Override
    public Payment refund(Payment payment, BigDecimal amount) {

        return payment;
    }



    @Override
    public MethodPayment getType() {
        return MethodPayment.CARD;
    }
}
