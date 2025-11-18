package edu.ucsal.fiadopay.domain.paymant.factory;

import edu.ucsal.fiadopay.controller.annotations.PaymentMethod;
import edu.ucsal.fiadopay.domain.paymant.MethodPayment;
import edu.ucsal.fiadopay.domain.paymant.strategy.PaymentStrategy;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
@Component
@RequiredArgsConstructor
public class PaymentFactoryImpl implements PaymentFactory {

    private final List<PaymentStrategy> strategies;

    @Override
    public PaymentStrategy getStrategy(MethodPayment type) {

        return strategies.stream()
                .filter(s -> {
                    var annotation = s.getClass().getAnnotation(PaymentMethod.class);
                    return annotation != null && annotation.type() == type;
                })
                .findFirst()
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "type not supported"
                        )
                );
    }
}
