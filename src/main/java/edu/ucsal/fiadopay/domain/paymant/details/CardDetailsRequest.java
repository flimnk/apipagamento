package edu.ucsal.fiadopay.domain.paymant.details;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record CardDetailsRequest(
        @Min(1) @Max(12) Integer installments
) {}
