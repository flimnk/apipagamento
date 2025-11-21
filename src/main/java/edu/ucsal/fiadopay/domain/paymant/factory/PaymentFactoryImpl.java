package edu.ucsal.fiadopay.domain.paymant.factory;

import edu.ucsal.fiadopay.annotations.paymentMethod.PaymentMethod;
import edu.ucsal.fiadopay.domain.paymant.strategy.PaymentStrategy;
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
    public PaymentStrategy getStrategy(String type) {

        return strategies.stream()
                .filter(s -> {

                    var annotation = s.getClass().getAnnotation(PaymentMethod.class);

                    return annotation != null && annotation.type().equalsIgnoreCase(type);
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
