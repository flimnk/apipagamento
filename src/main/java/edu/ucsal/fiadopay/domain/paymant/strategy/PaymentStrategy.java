package edu.ucsal.fiadopay.domain.paymant.strategy;

import edu.ucsal.fiadopay.domain.merchant.Merchant;
import edu.ucsal.fiadopay.domain.paymant.MethodPayment;
import edu.ucsal.fiadopay.domain.paymant.Payment;
import edu.ucsal.fiadopay.domain.paymant.dto.PaymentRequest;
import org.springframework.stereotype.Component;


public interface PaymentStrategy {
    Payment process(PaymentRequest request, Merchant merchant, String IdemKey);
}
