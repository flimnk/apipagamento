package edu.ucsal.fiadopay.domain.paymant.factory;

import edu.ucsal.fiadopay.domain.paymant.MethodPayment;
import edu.ucsal.fiadopay.domain.paymant.strategy.PaymentStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PaymentFactoryImpl implements PaymentFactory {

    private final List<PaymentStrategy> strategies;

    @Override
    public PaymentStrategy getStrategy(MethodPayment type) {
        return strategies.stream()
                .filter(s -> s.getType() == type)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("No strategy for type: " + type)
                );
    }
}
