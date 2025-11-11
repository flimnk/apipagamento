package edu.ucsal.fiadopay.domain.paymant.dto;

import edu.ucsal.fiadopay.domain.paymant.MethodPayment;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record PaymentRequest(
        @NotNull MethodPayment method,
        @NotBlank String currency,
        @NotNull BigDecimal amount,
        String metadataOrderId,
        Object details
) {}
