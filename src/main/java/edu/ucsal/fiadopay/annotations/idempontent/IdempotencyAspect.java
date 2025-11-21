package edu.ucsal.fiadopay.annotations.idempontent;

import edu.ucsal.fiadopay.domain.merchant.Merchant;
import edu.ucsal.fiadopay.domain.paymant.PaymentMapper;
import edu.ucsal.fiadopay.repo.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@Order(2)
public class IdempotencyAspect {

    @Autowired
    private PaymentRepository payments;

    @Around("@annotation(Idempotent)")
    public Object handleIdempotency(ProceedingJoinPoint pjp) throws Throwable {

        var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null)
            return pjp.proceed();

        String idemKey = attrs.getRequest().getHeader("Idempotency-Key");

        if (idemKey == null)
            return pjp.proceed();

        var auth = SecurityContextHolder.getContext().getAuthentication();
        Merchant merchant = (Merchant) auth.getPrincipal();

        log.info("🔁 Verificando idempotência merchantId={} idemKey={}", merchant.getId(), idemKey);

        var existing = payments.findByIdempotencyKeyAndMerchantId(idemKey, merchant.getId());

        if (existing.isPresent()) {
            log.info("🔁 Request repetido — retornando pagamento existente paymentId={}", existing.get().getId());
            return PaymentMapper.toResponse(existing.get());
        }

        log.info("🔁 Nenhuma operação anterior encontrada — prosseguindo execução.");
        return pjp.proceed();
    }
}
