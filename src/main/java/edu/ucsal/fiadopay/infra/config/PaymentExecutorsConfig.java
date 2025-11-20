package edu.ucsal.fiadopay.infra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class PaymentExecutorsConfig {

    @Bean(name = "paymentExecutor")
    public ExecutorService paymentExecutor(
            @Value("${fiadopay.payment-threads:4}") int threads) {
        return Executors.newFixedThreadPool(threads);
    }

    @Bean(name = "webhookExecutor")
    public ExecutorService webhookExecutor(
            @Value("${fiadopay.webhook-threads:8}") int threads) {
        return Executors.newFixedThreadPool(threads);
    }
}
