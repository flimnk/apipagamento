package edu.ucsal.fiadopay.controller.annotations.validTransactionWindow;

import edu.ucsal.fiadopay.domain.paymant.dto.PaymentRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Date;

@Component
@Aspect
public class ValidTransactionWindowAspect {
    @Around("@annotation(ValidTransactionWindow)")
    public  Object valid (ProceedingJoinPoint pj) throws Throwable {

        var args = pj.getArgs();
        var payment = (PaymentRequest)args[2];

        boolean valueGretherThanTenThousand = payment.amount().compareTo(new BigDecimal(10000)) > 0;

        LocalTime now = LocalTime.now();
        LocalTime limit = LocalTime.of(22, 0);
        boolean isAfter10 =  now.isAfter(limit);

        if(valueGretherThanTenThousand &&  isAfter10){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Value above the limit allowed at this time.");

        }
        return  pj.proceed();
    }
}
