package edu.ucsal.fiadopay.controller;

import edu.ucsal.fiadopay.domain.merchant.dto.BasicTokenRequest;
import edu.ucsal.fiadopay.domain.merchant.dto.MerchantCreate;
import edu.ucsal.fiadopay.domain.merchant.dto.MerchantRensponse;
import edu.ucsal.fiadopay.service.merchantService.MerchantService;
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
    public ResponseEntity<?> generateBasicToken(@Valid @RequestBody  BasicTokenRequest dto) {
        merchantsService.findAndVerifyByClientId(dto.clientId(), dto.clientSecret());

        String token = merchantsService.generateBasicToken(dto.clientId(), dto.clientSecret());

        return ResponseEntity.ok().body(Map.of(
                "authorization_header", token,
                "example_usage", "Send this header in Authorization field",
                "format", "Authorization: " + token
        ));
    }

}
