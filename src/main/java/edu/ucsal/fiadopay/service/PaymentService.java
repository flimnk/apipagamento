package edu.ucsal.fiadopay.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.ucsal.fiadopay.controller.annotations.idempontent.Idempotent;
import edu.ucsal.fiadopay.controller.annotations.validTransactionWindow.ValidTransactionWindow;
import edu.ucsal.fiadopay.domain.paymant.PaymentMapper;

import edu.ucsal.fiadopay.domain.paymant.dto.PaymentRequest;
import edu.ucsal.fiadopay.domain.paymant.dto.PaymentResponse;
import edu.ucsal.fiadopay.domain.merchant.Merchant;
import edu.ucsal.fiadopay.domain.paymant.Payment;
import edu.ucsal.fiadopay.domain.WebhookDelivery.WebhookDelivery;
import edu.ucsal.fiadopay.domain.paymant.factory.PaymentFactoryImpl;
import edu.ucsal.fiadopay.domain.paymant.strategy.PaymentStrategy;
import edu.ucsal.fiadopay.repo.MerchantRepository;
import edu.ucsal.fiadopay.repo.PaymentRepository;
import edu.ucsal.fiadopay.repo.WebhookDeliveryRepository;
import edu.ucsal.fiadopay.service.webhook.WebhookDeliveryService;
import edu.ucsal.fiadopay.service.webhook.WebhookEventFactory;
import edu.ucsal.fiadopay.service.webhook.WebhookSigner;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Service

    public class PaymentService {

        private final ExecutorService paymentExecutor;
        private final MerchantService merchantService;
        private final PaymentRepository payments;
        private final PaymentFactoryImpl paymentFactory;
        private final PaymentProcessor processor;
        private final WebhookEventFactory eventFactory;
        private final WebhookSigner signer;
        private final WebhookDeliveryService deliveryService;


    public PaymentService(
            @Qualifier("paymentExecutor") ExecutorService paymentExecutor,
            PaymentProcessor processor,
            WebhookEventFactory eventFactory,
            WebhookSigner signer,
            WebhookDeliveryService deliveryService,
            MerchantService merchantService,
            PaymentRepository paymentRepository,
            PaymentFactoryImpl paymentFactory
    ) {
        this.paymentExecutor = paymentExecutor;
        this.processor = processor;
        this.eventFactory = eventFactory;
        this.signer = signer;
        this.deliveryService = deliveryService;
        this.paymentFactory = paymentFactory;
        this.merchantService = merchantService;
        this.payments = paymentRepository;
    }

        @Idempotent
        @ValidTransactionWindow
        @Transactional
        public PaymentResponse createPayment(Merchant merchant, String idemKey, PaymentRequest req) {

            PaymentStrategy strategy = paymentFactory.getStrategy(req.method().getValue());
            Payment payment = strategy.process(req, merchant, idemKey);
            payments.save(payment);

            paymentExecutor.submit(() -> handleWebhook(payment.getId()));

            return PaymentMapper.toResponse(payment);
        }
    public PaymentResponse getPayment(String id, Merchant merchant) {
        if (merchant == null || merchant.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Merchant information is missing");
        }

        var payment = payments.findById(id)
                .filter(p -> p.belongsToMerchant(merchant.getId()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Payment does not exist or does not belong to this merchant."
                ));

        return PaymentMapper.toResponse(payment);
    }

        private void handleWebhook(String paymentId) {
            var payment = processor.process(paymentId);
            if (payment == null) return;

            var merchant = merchantService.findById(payment.getMerchant().getId());

            String payload = eventFactory.buildPaymentUpdatedEvent(payment);
            String signature = signer.sign(payload);

            var delivery = WebhookDelivery.builder()
                    .eventId("evt_" + UUID.randomUUID().toString().substring(0, 8))
                    .eventType("payment.updated")
                    .paymentId(payment.getId())
                    .targetUrl(merchant.getWebhookUrl())
                    .payload(payload)
                    .signature(signature)
                    .attempts(0)
                    .delivered(false)
                    .build();

            deliveryService.scheduleDelivery(delivery);
        }
    }
