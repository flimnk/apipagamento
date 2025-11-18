package edu.ucsal.fiadopay.domain.merchant.dto;

import jakarta.validation.constraints.NotBlank;

public record  BasicTokenRequest(
     @NotBlank String clientId,
     @NotBlank String clientSecret
) {
}
