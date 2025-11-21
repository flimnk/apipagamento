package edu.ucsal.fiadopay.controller;

import edu.ucsal.fiadopay.domain.paymant.dto.PaymentRequest;
import edu.ucsal.fiadopay.domain.paymant.dto.PaymentResponse;
import edu.ucsal.fiadopay.domain.merchant.Merchant;
import edu.ucsal.fiadopay.service.payment.PaymentService;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/fiadopay/gateway")
@RequiredArgsConstructor
public class PaymentController {
  private final PaymentService service;

  @PostMapping("/payments")
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<PaymentResponse> create(
      @RequestHeader(value="Idempotency-Key", required=false) String idemKey,
      @RequestBody @Valid PaymentRequest req,
      @AuthenticationPrincipal Merchant merchant
  ) {
    var resp = service.createPayment(merchant,idemKey, req);
    return ResponseEntity.status(HttpStatus.CREATED).body(resp);
  }

  @GetMapping("/payments/{id}")
  public PaymentResponse get( @AuthenticationPrincipal Merchant merchant, @PathVariable String id) {
    return service.getPayment(id, merchant);
  }

  @PostMapping("/refunds")
  @SecurityRequirement(name = "bearerAuth")
  public java.util.Map<String,Object> refund(@AuthenticationPrincipal Merchant merchant,
                                   @RequestBody @Valid RefundRequest body) {
    return service.refund(merchant, body.paymentId());
  }
}
