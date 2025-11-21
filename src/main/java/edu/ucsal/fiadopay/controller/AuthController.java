package edu.ucsal.fiadopay.controller;

import edu.ucsal.fiadopay.domain.user.dto.LoginRequest;
import edu.ucsal.fiadopay.domain.user.dto.LoginResponse;
import edu.ucsal.fiadopay.service.securityService.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fiadopay/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@RequestBody @Valid LoginRequest request) {
        LoginResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }
}
