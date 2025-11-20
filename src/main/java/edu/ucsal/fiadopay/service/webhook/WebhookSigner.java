package edu.ucsal.fiadopay.service.webhook;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class WebhookSigner {

    @Value("${fiadopay.webhook-secret}")
    String secret;

    public String sign(String payload) {
        try {
            var mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(new javax.crypto.spec.SecretKeySpec(secret.getBytes(), "HmacSHA256"));
            return Base64.getEncoder().encodeToString(mac.doFinal(payload.getBytes()));
        } catch (Exception e) {
            return "";
        }
    }
}
