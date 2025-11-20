package edu.ucsal.fiadopay.domain.paymant.factory;

import edu.ucsal.fiadopay.domain.paymant.MethodPayment;
import edu.ucsal.fiadopay.domain.paymant.strategy.PaymentStrategy;
import org.springframework.stereotype.Component;


public interface PaymentFactory  {

    public PaymentStrategy  getStrategy(String type);
}
