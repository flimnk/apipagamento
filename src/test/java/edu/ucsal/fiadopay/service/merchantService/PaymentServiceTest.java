package edu.ucsal.fiadopay.service.merchantService;

import edu.ucsal.fiadopay.domain.merchant.Merchant;
import edu.ucsal.fiadopay.domain.paymant.Payment;
import edu.ucsal.fiadopay.domain.paymant.Status;
import edu.ucsal.fiadopay.domain.paymant.dto.PaymentRequest;
import edu.ucsal.fiadopay.domain.paymant.factory.PaymentFactoryImpl;
import edu.ucsal.fiadopay.domain.paymant.strategy.PaymentStrategy;
import edu.ucsal.fiadopay.repo.PaymentRepository;
import edu.ucsal.fiadopay.service.PaymentProcessor;
import edu.ucsal.fiadopay.service.PaymentService;
import edu.ucsal.fiadopay.service.webhook.WebhookDeliveryService;
import edu.ucsal.fiadopay.service.webhook.WebhookEventFactory;
import edu.ucsal.fiadopay.service.webhook.WebhookSigner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private ExecutorService executor;
    @Mock private PaymentRepository paymentRepo;
    @Mock private PaymentFactoryImpl factory;
    @Mock private PaymentProcessor processor;
    @Mock private WebhookEventFactory eventFactory;
    @Mock private WebhookSigner signer;
    @Mock private WebhookDeliveryService deliveryService;

    @Mock private PaymentStrategy strategy;

    @InjectMocks
    private PaymentService service;

    @Test
    void testCreatePayment_Success() {

        Merchant merchant = Merchant.builder().id(1L).build();
        PaymentRequest req = new PaymentRequest("CARD", new BigDecimal("100.00"));
        Payment payment = Payment.builder().id("pay_123").status(Status.PENDING).merchant(merchant).build();

        when(factory.getStrategy("CARD")).thenReturn(strategy);
        when(strategy.process(req, merchant, "idempKey")).thenReturn(payment);

        PaymentResponse resp = service.createPayment(merchant, "idempKey", req);

        // verificações
        verify(factory).getStrategy("CARD");
        verify(strategy).process(req, merchant, "idempKey");
        verify(paymentRepo).save(payment);
        verify(executor).submit(any(Runnable.class)); // webhook chamado

        assertEquals("pay_123", resp.id());
    }
}
