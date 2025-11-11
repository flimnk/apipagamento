package edu.ucsal.fiadopay.domain.merchant.dto;

import edu.ucsal.fiadopay.domain.merchant.Merchant;

public record MerchantRensponse(
        Long id,
        String name,
        String clientId,
        String clientSecret,
        String webhookUrl,
        Status status,
        Double interest,
        boolean enable

) {
    public MerchantRensponse (Merchant merchant){
        this(merchant.getId(), merchant.getName(), merchant.getClientId(),
                merchant.getClientSecret(), merchant.getWebhookUrl(), merchant.getStatus(),
                    merchant.getInterest(),merchant.isEnable());
    }
}
