package edu.ucsal.fiadopay.service.payment;

import edu.ucsal.fiadopay.annotations.idempontent.Idempotent;
import edu.ucsal.fiadopay.annotations.logged.Logged;

import edu.ucsal.fiadopay.annotations.validTransactionWindow.ValidTransactionWindow;
import edu.ucsal.fiadopay.domain.paymant.PaymentMapper;

import edu.ucsal.fiadopay.domain.paymant.Status;
import edu.ucsal.fiadopay.domain.paymant.dto.PaymentRequest;
import edu.ucsal.fiadopay.domain.paymant.dto.PaymentResponse;
import edu.ucsal.fiadopay.domain.merchant.Merchant;
import edu.ucsal.fiadopay.domain.paymant.Payment;
import edu.ucsal.fiadopay.domain.WebhookDelivery.WebhookDelivery;
import edu.ucsal.fiadopay.domain.paymant.factory.PaymentFactoryImpl;
import edu.ucsal.fiadopay.domain.paymant.strategy.PaymentStrategy;
import edu.ucsal.fiadopay.repo.PaymentRepository;
import edu.ucsal.fiadopay.service.merchantService.MerchantService;
import edu.ucsal.fiadopay.service.webhook.WebhookDeliveryService;
import edu.ucsal.fiadopay.service.webhook.WebhookEventFactory;
import edu.ucsal.fiadopay.service.webhook.WebhookSigner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import java.util.concurrent.ExecutorService;

    @Service
    @Slf4j
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
    @Logged("metricas")
    @Idempotent
    @ValidTransactionWindow
    @Transactional
    public PaymentResponse createPayment(Merchant merchant, String idemKey, PaymentRequest req) {

        log.info("Iniciando criação de pagamento merchantId={} idemKey={} method={} amount={}",
                merchant.getId(),
                idemKey,
                req.method(),
                req.amount()
        );

        PaymentStrategy strategy = paymentFactory.getStrategy(req.method().getValue());
        log.debug("Usando estratégia de pagamento: {}", req.method().getValue());

        Payment payment = strategy.process(req, merchant, idemKey);
        payments.save(payment);

        log.info("Pagamento criado com sucesso paymentId={} status={}",
                payment.getId(), payment.getStatus());

        paymentExecutor.submit(() -> handleWebhook(payment.getId()));

        return PaymentMapper.toResponse(payment);
    }


        public PaymentResponse getPayment(String id, Merchant merchant) {
            if (merchant == null || merchant.getId() == null) {
                log.error("Merchant inválido na consulta de pagamento id={}", id);
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Merchant information is missing"
                );
            }

            var payment = payments.findById(id)
                    .filter(p -> p.belongsToMerchant(merchant.getId()))
                    .orElseThrow(() -> {
                        log.warn("Tentativa de acesso não autorizado paymentId={} merchantId={}", id, merchant.getId());
                        return new ResponseStatusException(
                                HttpStatus.FORBIDDEN,
                                "Payment does not exist or does not belong to this merchant."
                        );
                    });

            log.debug("Pagamento {} consultado para merchant {}", id, merchant.getId());

            return PaymentMapper.toResponse(payment);
        }


        public Map<String,Object> refund(Merchant merchant, String paymentId){

            var p = payments.findById(paymentId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
            if (!merchant.getId().equals(p.getMerchant().getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
            p.setStatus(Status.REFUNDED);
            p.setUpdatedAt(Instant.now());
            payments.save(p);
            handleWebhook(p.getId());
            return Map.of("id","ref_"+UUID.randomUUID(),"status","PENDING");
        }
        private void handleWebhook(String paymentId) {

        log.info("Processando webhook para paymentId={}", paymentId);

        var payment = processor.process(paymentId);
        if (payment == null) {
            log.warn("Pagamento não encontrado para entrega de webhook paymentId={}", paymentId);
            return;
        }

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

        log.info("Agendando entrega de webhook eventId={} paymentId={} url={}",
                delivery.getEventId(),
                delivery.getPaymentId(),
                delivery.getTargetUrl()
        );

        deliveryService.scheduleDelivery(delivery);
    }
    }
