package edu.ucsal.fiadopay.service.merchantService;

import edu.ucsal.fiadopay.domain.merchant.Merchant;
import edu.ucsal.fiadopay.domain.paymant.MethodPayment;
import edu.ucsal.fiadopay.domain.paymant.Payment;
import edu.ucsal.fiadopay.domain.paymant.Status;
import edu.ucsal.fiadopay.domain.paymant.dto.PaymentRequest;
import edu.ucsal.fiadopay.domain.paymant.dto.PaymentResponse;
import edu.ucsal.fiadopay.domain.paymant.factory.PaymentFactoryImpl;
import edu.ucsal.fiadopay.domain.paymant.strategy.PaymentStrategy;
import edu.ucsal.fiadopay.repo.PaymentRepository;

import edu.ucsal.fiadopay.service.payment.PaymentProcessor;
import edu.ucsal.fiadopay.service.payment.PaymentService;
import edu.ucsal.fiadopay.service.webhook.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock ExecutorService executor;
    @Mock PaymentRepository payments;
    @Mock
    PaymentFactoryImpl factory;
    @Mock
    PaymentProcessor processor;
    @Mock WebhookEventFactory eventFactory;
    @Mock WebhookSigner signer;
    @Mock WebhookDeliveryService deliveryService;

    @InjectMocks
    PaymentService service;

    Merchant merchant;

    @BeforeEach
    void setup() {
        merchant = new Merchant();
        merchant.setId(100L);
    }

    // ---------------------------------------------------------
    // TESTE DO GETPAYMENT
    // ---------------------------------------------------------

    @Test
    void deveFalharQuandoMerchantForNulo() {
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.getPayment("abc", null)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void deveFalharQuandoMerchantNaoTemId() {
        Merchant m = new Merchant(); // sem ID

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.getPayment("abc", m)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void deveFalharQuandoPagamentoNaoExiste() {
        when(payments.findById("abc")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.getPayment("abc", merchant)
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void deveFalharQuandoPagamentoNaoPertenceAoMerchant() {
        Payment p = mock(Payment.class);

        when(payments.findById("abc")).thenReturn(Optional.of(p));
        when(p.belongsToMerchant(100L)).thenReturn(false);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.getPayment("abc", merchant)
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void deveRetornarPagamentoComSucesso() {
        Payment p = mock(Payment.class);

        when(payments.findById("abc")).thenReturn(Optional.of(p));
        when(p.belongsToMerchant(100L)).thenReturn(true);
        when(p.getId()).thenReturn("abc");
        when(p.getStatus()).thenReturn(Status.PENDING);

        PaymentResponse resp = service.getPayment("abc", merchant);

        assertNotNull(resp);
        assertEquals("abc", resp.id());
        assertEquals(Status.PENDING, resp.status());
    }

    // ---------------------------------------------------------
    // TESTE DO CREATEPAYMENT
    // ---------------------------------------------------------

    @Test
    void deveCriarPagamentoComSucesso() {
        PaymentRequest req = new PaymentRequest(
                MethodPayment.CARD,
                "BRL",
                new BigDecimal("99.90"),
                null,
                null
        );

        PaymentStrategy strategy = mock(PaymentStrategy.class);
        Payment payment = mock(Payment.class);

        when(factory.getStrategy("CARD")).thenReturn(strategy);
        when(strategy.process(req, merchant, "idem123")).thenReturn(payment);

        when(payment.getId()).thenReturn("P123");
        when(payment.getStatus()).thenReturn(Status.PENDING);

        PaymentResponse resp = service.createPayment(merchant, "idem123", req);

        assertNotNull(resp);
        assertEquals("P123", resp.id());
        verify(payments, times(1)).save(payment);
        verify(executor, times(1)).submit(any(Runnable.class));
    }
}
