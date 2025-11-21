package edu.ucsal.fiadopay.controller.annotations.idempontent;

import edu.ucsal.fiadopay.domain.merchant.Merchant;
import edu.ucsal.fiadopay.domain.paymant.PaymentMapper;
import edu.ucsal.fiadopay.repo.PaymentRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class IdempotencyAspect {

    @Autowired
    private PaymentRepository payments;

    @Around("@annotation(Idempotent)")
    public Object handleIdempotency(ProceedingJoinPoint pjp) throws Throwable {


        var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        System.out.println("Attrs: " + attrs);
        if (attrs == null)
            return pjp.proceed();


        String idemKey = attrs.getRequest().getHeader("Idempotency-Key");


        if (idemKey == null)
            return pjp.proceed();

        System.out.println("IdemKEy: " + idemKey);
        var auth = SecurityContextHolder.getContext().getAuthentication();
        Merchant merchant = (Merchant) auth.getPrincipal();


        var existing = payments.findByIdempotencyKeyAndMerchantId(idemKey, merchant.getId());

        // se existir, retorna como está (o Service converte, ok)
        if (existing.isPresent()) {

            return PaymentMapper.toResponse(existing.get());
        }

        // segue o fluxo normal e deixa o service resolver tudo
        return pjp.proceed();
    }
}
