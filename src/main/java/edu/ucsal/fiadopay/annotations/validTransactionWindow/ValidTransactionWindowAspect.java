package edu.ucsal.fiadopay.annotations.validTransactionWindow;

import edu.ucsal.fiadopay.domain.paymant.dto.PaymentRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalTime;

@Slf4j
@Component
@Aspect
@Order(3)
public class ValidTransactionWindowAspect {

    @Around("@annotation(ValidTransactionWindow)")
    public Object valid(ProceedingJoinPoint pj) throws Throwable {

        var args = pj.getArgs();
        var payment = (PaymentRequest) args[2];

        boolean greaterThan10k = payment.amount().compareTo(new BigDecimal(10000)) > 0;

        LocalTime now = LocalTime.now();
        LocalTime limit = LocalTime.of(22, 0);
        boolean isAfter10 = now.isAfter(limit);

        log.info("⏱️ Validando janela de transação amount={} now={} greaterThan10k={} after22h={}",
                payment.amount(), now, greaterThan10k, isAfter10);

        if (greaterThan10k && isAfter10) {
            log.warn("⛔ Transação bloqueada — valor acima de 10k após as 22h. amount={} time={}",
                    payment.amount(), now);

            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Value above the limit allowed at this time.");
        }

        return pj.proceed();
    }
}

