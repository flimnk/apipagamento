package edu.ucsal.fiadopay.domain.paymant;

public enum MethodPayment {
    CARD("CARD"),
    BOLETO("BOLETO"),
    TICKET("TICKET");

    private final String value;

    MethodPayment(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
