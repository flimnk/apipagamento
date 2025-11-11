package edu.ucsal.fiadopay.infra.Exceptions;

public class MerchantNotFoundException extends RuntimeException {
    public MerchantNotFoundException(Long id) {
        super("Merchant with ID " + id + " not found or is inactive.");
    }
}