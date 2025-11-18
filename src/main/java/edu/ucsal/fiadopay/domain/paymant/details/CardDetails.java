package edu.ucsal.fiadopay.domain.paymant.details;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class CardDetails {

    private int installments;            // parcelas
    private BigDecimal baseAmount;       // valor original
    private BigDecimal interestAmount;   // total de juros
    private BigDecimal installmentAmount; // valor de cada parcela
    private BigDecimal totalWithInterest; // baseAmount + interestAmount
}
