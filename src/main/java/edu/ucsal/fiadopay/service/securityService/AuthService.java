package edu.ucsal.fiadopay.service.securityService;


import edu.ucsal.fiadopay.domain.user.Role;
import edu.ucsal.fiadopay.domain.user.User;
import edu.ucsal.fiadopay.domain.user.dto.LoginRequest;
import edu.ucsal.fiadopay.domain.user.dto.LoginResponse;
import edu.ucsal.fiadopay.domain.user.dto.UserResponse;
import edu.ucsal.fiadopay.repo.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class AuthService {

    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (AuthenticationException e) {
            throw new RuntimeException("Credenciais inválidas", e);
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + request.email()));

        String token = tokenService.generateToken(user);

        return new LoginResponse(token, tokenService.getExpirationTime(), new UserResponse(user));
    }
    public LoginResponse register(LoginRequest request) {
        // 1. Verificar se já existe usuário com o e-mail informado
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("E-mail já está em uso: " + request.email());
        }


        User user = new User();

        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.SHOPKEEPER);


        userRepository.save(user);


        String token = tokenService.generateToken(user);
        return new LoginResponse(token, tokenService.getExpirationTime(), new UserResponse(user));
    }

}

