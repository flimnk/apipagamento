package edu.ucsal.fiadopay.infra.filters;

import edu.ucsal.fiadopay.domain.merchant.Merchant;

import edu.ucsal.fiadopay.service.merchantService.MerchantService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
@Component
@AllArgsConstructor
public class MerchantAuthenticationFilter extends OncePerRequestFilter {

    private final MerchantService merchantService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (!path.startsWith("/fiadopay/gateway")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Basic ")) {

                String base64Credentials = authHeader.substring("Basic ".length());
                byte[] decodedBytes = Base64.getDecoder().decode(base64Credentials);
                String decoded = new String(decodedBytes, StandardCharsets.UTF_8);

                String[] parts = decoded.split(":", 2);
                if (parts.length != 2) {
                    throw new RuntimeException("Invalid Basic Auth credentials");
                }
                String clientId = parts[0];
                String clientSecret = parts[1];
                Merchant merchant = merchantService.findAndVerifyByClientId(clientId, clientSecret);
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(merchant, null, List.of());

                SecurityContextHolder.getContext().setAuthentication(auth);
            }

        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"" + ex.getMessage() + "\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
