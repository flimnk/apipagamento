package edu.ucsal.fiadopay.domain.paymant.strategy;

import edu.ucsal.fiadopay.domain.paymant.MethodPayment;
import edu.ucsal.fiadopay.domain.paymant.Payment;

import java.math.BigDecimal;

public interface RefundStrategy {
    Payment refund(Payment payment, BigDecimal amount);
    MethodPayment getType(); // ex: PIX, CARD, BOLETO
}
