package edu.ucsal.fiadopay.domain.paymant;

import edu.ucsal.fiadopay.domain.merchant.Merchant;
import jakarta.persistence.*;
import lombok.*;


import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Table(
    indexes = { @Index(columnList="merchantId"), @Index(columnList="status") },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_payment_merchant_idempotency", columnNames = {"merchantId", "idempotencyKey"})
    }
)
public class Payment {
    @Id
    private String id; // pay_xxx

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(nullable = false, length = 20)
    private MethodPayment method;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 10)
    private String currency;

    @Column(length = 64)
    private String idempotencyKey;
    @Column(length = 255)
    private String metadataOrderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(nullable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant updatedAt;

    @Column(columnDefinition = "jsonb")
    private String detailsJson;






}
