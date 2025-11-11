package edu.ucsal.fiadopay.infra.config;


import edu.ucsal.fiadopay.infra.filters.JwtAuthentucationFIlter;
import edu.ucsal.fiadopay.infra.filters.MerchantAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthentucationFIlter jwtAuthFilter;
    private final MerchantAuthenticationFilter merchantAuthenticationFilter;

    @Bean
    @Order(1)
    public SecurityFilterChain dashboardSecurity(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/fiadopay/auth/**", "/fiadopay/user/**", "/fiadopay/merchants/**")
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/fiadopay/auth/**").permitAll()
                        .requestMatchers("/fiadopay/merchants/basic-token").permitAll() //
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/fiadopay/gateway/**")
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .addFilterBefore(merchantAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
