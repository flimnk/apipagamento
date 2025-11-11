package edu.ucsal.fiadopay.service;

import edu.ucsal.fiadopay.domain.merchant.dto.MerchantCreate;
import edu.ucsal.fiadopay.domain.merchant.Merchant;
import edu.ucsal.fiadopay.domain.merchant.dto.MerchantRensponse;
import edu.ucsal.fiadopay.domain.merchant.dto.Status;
import edu.ucsal.fiadopay.infra.Exceptions.MerchantNotFoundException;
import edu.ucsal.fiadopay.repo.MerchantRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Service
@AllArgsConstructor
public class MerchantService {

    private MerchantRepository merchantRepository;
    private SecurityService securityService;
    private final PasswordEncoder passwordEncoder;

    public MerchantRensponse create(MerchantCreate dto) {
       var user = securityService.getAuthenticatedUserId();

        if (merchantRepository.existsByName(dto.name()) || merchantRepository.existsByWebhookUrl(dto.webhookUrl())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Merchant name or  webHook   already exists");
        }

        var m = Merchant.builder()
                .interest(dto.interest())
                .name(dto.name())
                .webhookUrl(dto.webhookUrl())
                .user(user)
                .clientId("cli_"+UUID.randomUUID().toString())
                .clientSecret("sec_"+UUID.randomUUID().toString().replace("-", ""))
                .status(Status.ACTIVE)
                .build();
        user.setMerchant(m);
        merchantRepository.save(m);
        return  new MerchantRensponse(m);
    }



    public Merchant findAndVerifyByClientId(String clientId, String clientSecret) {
        var merchant = merchantRepository.findByClientId(clientId)
                .orElseThrow(() -> new RuntimeException("Merchant not found"));
        if (!passwordEncoder.matches(clientSecret, merchant.getClientSecret())) {
            throw new RuntimeException("Invalid secret key");
        }
        return merchant;
    }

    public String generateBasicToken(String clientId, String clientSecret) {
        String authString = clientId + ":" + clientSecret;
        return "Basic " + Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8));
    }



}


