package edu.ucsal.fiadopay.annotations.logged;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
@Order(1)
public class LogAspect {

    @Around("@annotation(logged)")
    public Object around(ProceedingJoinPoint pjp, Logged logged) throws Throwable {


        long start = System.currentTimeMillis();

        try {
            Object result = pjp.proceed();

            long time = System.currentTimeMillis() - start;

            log.info("[{}] Executado em {}ms | args={}",
                    logged.value(), time, Arrays.toString(pjp.getArgs()));

            return result;

        } catch (Exception e) {

            long time = System.currentTimeMillis() - start;

            log.error("[{}] Falhou em {}ms | erro={} | args={}",
                    logged.value(), time, e.getMessage(), Arrays.toString(pjp.getArgs()));

            throw e;
        }
    }
}
