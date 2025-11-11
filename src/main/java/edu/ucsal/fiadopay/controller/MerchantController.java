package edu.ucsal.fiadopay.controller;

import edu.ucsal.fiadopay.domain.merchant.Merchant;
import edu.ucsal.fiadopay.domain.merchant.dto.MerchantCreate;
import edu.ucsal.fiadopay.domain.merchant.dto.MerchantRensponse;
import edu.ucsal.fiadopay.service.MerchantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;

import java.util.Map;

@RestController
@RequestMapping("/fiadopay/merchants")
@RequiredArgsConstructor
public class MerchantController {
  private final MerchantService merchantsService;

  @PostMapping
  public ResponseEntity<MerchantRensponse> create(@Valid @RequestBody MerchantCreate dto) {
        return  ResponseEntity.status(HttpStatus.CREATED).body(merchantsService.create(dto));
  }
    @PostMapping("/basic-token")
    public ResponseEntity<?> generateBasicToken(@RequestParam String clientId,
                                                @RequestParam String clientSecret) {


        merchantsService.findAndVerifyByClientId(clientId, clientSecret);

        String token = merchantsService.generateBasicToken(clientId, clientSecret);

        return ResponseEntity.ok().body(Map.of(
                "authorization_header", token,
                "example_usage", "Send this header in Authorization field",
                "format", "Authorization: " + token
        ));
    }

}
