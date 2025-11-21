package edu.ucsal.fiadopay.service.merchantService;

import edu.ucsal.fiadopay.domain.merchant.dto.MerchantCreate;
import edu.ucsal.fiadopay.domain.merchant.Merchant;
import edu.ucsal.fiadopay.domain.merchant.dto.MerchantRensponse;
import edu.ucsal.fiadopay.domain.merchant.dto.Status;
import edu.ucsal.fiadopay.repo.MerchantRepository;
import edu.ucsal.fiadopay.service.securityService.SecurityService;
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
            if(user.getMerchant() != null) throw new   ResponseStatusException(HttpStatus.CONFLICT, "User already has merchant");

            var m = Merchant.builder()
                    .interest(dto.interest())
                    .name(dto.name())
                    .webhookUrl(dto.webhookUrl())
                    .user(user)
                    .clientId("cli_"+UUID.randomUUID().toString())
                    .clientSecret("sec_"+UUID.randomUUID().toString().replace("-", ""))
                    .status(Status.ACTIVE)
                    .build();
            var response = new MerchantRensponse(m);
            m.setClientSecret(passwordEncoder.encode(m.getClientSecret()));
            user.setMerchant(m);
            merchantRepository.save(m);
            return  response;
        }

    public  Merchant findById(Long id){
        return merchantRepository.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.BAD_REQUEST,"Merchant not found"));
    }


    public Merchant findAndVerifyByClientId(String clientId, String clientSecret) {
        var merchant = merchantRepository.findByClientId(clientId)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.BAD_REQUEST,"Merchant not found"));
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


