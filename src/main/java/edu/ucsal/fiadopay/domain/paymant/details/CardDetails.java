package edu.ucsal.fiadopay.domain.paymant.details;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class CardDetails {

    private int installments;
   private BigDecimal baseAmount;
    private BigDecimal interestAmount;
    private BigDecimal installmentAmount;

}
