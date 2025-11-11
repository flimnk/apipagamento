package edu.ucsal.fiadopay.domain.paymant.dto;
import edu.ucsal.fiadopay.domain.paymant.MethodPayment;
import edu.ucsal.fiadopay.domain.paymant.Status;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(String id, BigDecimal amount, String currency,
                              MethodPayment method, Status status,
                                Instant CreatedAt,
                                Object details,
                                String  metadataOrderId) {}
